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

package jlibs.wadl;

import jlibs.core.lang.Ansi;
import jlibs.wadl.runtime.Path;
import jline.CandidateListCompletionHandler;
import jline.ConsoleReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static jlibs.core.lang.Ansi.Attribute;
import static jlibs.core.lang.Ansi.Color;

/**
 * @author Santhosh Kumar T
 */
public class WADLTerminal{
    private final Command command = new Command(this);
    private List<Path> roots = new ArrayList<Path>();

    public List<Path> getRoots(){
        return roots;
    }
    
    private Path currentPath;

    public Path getCurrentPath(){
        return currentPath;
    }

    public void setCurrentPath(Path currentPath){
        this.currentPath = currentPath;
    }
    
    private static Ansi PROMPT[][] ={
        {
            new Ansi(Attribute.BRIGHT, Color.WHITE, Color.BLUE),
            new Ansi(Attribute.BRIGHT, Color.GREEN, Color.BLUE)
        },
        {
            new Ansi(Attribute.BRIGHT, Color.WHITE, Color.RED),
            new Ansi(Attribute.BRIGHT, Color.GREEN, Color.RED)
        }
    };
    public String getPrompt(int index){
        if(currentPath==null)
            return "[?]";
        else{
            StringBuilder buff = new StringBuilder();
            buff.append(PROMPT[index][0].colorize("["));

            Deque<Path> stack = currentPath.getStack();
            boolean first = true;
            Path path;
            while(!stack.isEmpty()){
                if(first)
                    first = false;
                else
                    buff.append(PROMPT[index][0].colorize("/"));
                path = stack.pop();

                if(path.value==null)
                    buff.append(PROMPT[index][0].colorize(path.name));
                else
                    buff.append(PROMPT[index][1].colorize(path.value));
            }
            buff.append(PROMPT[index][0].colorize("]"));
            return buff.toString();
        }
    }
    
    public String getURL() throws MalformedURLException{
        Path path = currentPath;
        StringBuilder buff = new StringBuilder();
        Deque<Path> stack = path.getStack();
        boolean first = true;
        while(!stack.isEmpty()){
            if(first)
                first = false;
            else
                buff.append('/');
            path = stack.pop();
            buff.append(path.resolve());
        }
        return buff.toString();
    }

    public void start() throws IOException{
        ConsoleReader console = new ConsoleReader();
        WADLCompletor completor = new WADLCompletor(this);
        console.addCompletor(completor);

        CandidateListCompletionHandler completionHandler = new CandidateListCompletionHandler();
        console.setCompletionHandler(completionHandler);

        String line;
        int promptIndex = 0;
        while((line=console.readLine(getPrompt(promptIndex)+" "))!=null){
            line = line.trim();
            if(line.length()>0){
                if(line.equals("exit") || line.equals("quit"))
                    return;
                try{
                    promptIndex = command.run(line) ? 0 : 1;
                }catch(Exception ex){
                    ex.printStackTrace();
                    promptIndex = 1;
                }
            }
        }
    }

    public static void main(String[] args) throws Exception{
        WADLTerminal terminal = new WADLTerminal();
        for(String arg: args)
            terminal.command.run("import "+arg);
        terminal.start();
    }
    
    private static void print(Path path){
        if(path.resource!=null)
            System.out.println(path);
        for(Path child: path.children)
            print(child);
    }
}
