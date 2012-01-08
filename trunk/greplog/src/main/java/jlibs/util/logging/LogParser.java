/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.util.logging;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Santhosh Kumar T
 */
public class LogParser{
    private LogHeaderDefinition definition;
    private Condition condition;
    private LogHandler handler;

    public LogParser(LogHeaderDefinition definition){
        this.definition = definition;
    }

    public void setCondition(Condition condition){
        this.condition = condition;
    }

    public void setHandler(LogHandler handler){
        this.handler = handler;
    }

    public long[] parse(BufferedReader reader) throws IOException{
        long index = 0;
        long selected = 0;
        String line = reader.readLine();
        Matcher recordMatcher;
        StringBuilder message = new StringBuilder();
        while(true){
            message.setLength(0);
            recordMatcher = definition.pattern.matcher(line);
            if(!recordMatcher.find())
                throw new IllegalArgumentException();
            message.append(line.substring(recordMatcher.end()));

            while(true){
                line = reader.readLine();
                if(line!=null && !definition.pattern.matcher(line).find()){
                    message.append('\n');
                    message.append(line);
                }else{
                    LogRecord record = new LogRecord();
                    record.index = index++;
                    record.fields = new String[definition.groupNames.length];
                    for(int i=0; i<record.fields.length; i++)
                        record.fields[i] = recordMatcher.group(i);
                    record.message = message.toString();
                    if(condition.matches(record)){
                        selected++;
                        handler.consume(record);
                    }
                    if(line==null)
                        return new long[]{ index, selected };
                    else
                        break;
                }
            }
        }
    }

    public static void main(String[] args) throws Exception{
        List<String> files = new ArrayList<String>();
        files.addAll(Arrays.asList(args));
        boolean printIndex = files.remove("-printIndex");
        if(files.size()<3){
            System.err.println("Usage:");
            System.err.println("\tjava -jar jlibs-greplog.jar <header-file> <filter-file> [-printIndex] <log-file> ...");
            System.exit(1);
        }

        LogHeaderDefinition definition = LogHeaderDefinition.parse(new File(files.remove(0)));
        Condition condition = ConditionParser.parse(new File(files.remove(0)), definition);
        LogParser parser = new LogParser(definition);
        parser.setCondition(condition);
        parser.setHandler(new LogPrinter(printIndex));

        List<InputStream> logs = new ArrayList<InputStream>(files.size());
        for(String file: files)
            logs.add(new FileInputStream(file));
        SequenceInputStream stream = new SequenceInputStream(Collections.enumeration(logs));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        long result[] = parser.parse(reader);
        reader.close();
        System.err.println("Selected "+result[1]+" from "+result[0]+" log records");
    }
}
