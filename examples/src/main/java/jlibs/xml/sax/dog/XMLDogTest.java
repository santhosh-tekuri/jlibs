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

package jlibs.xml.sax.dog;

import jlibs.core.lang.OS;
import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.dog.expr.Expression;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class XMLDogTest{
    public static void main(String[] args) throws Exception{
        if(args.length!=1){
            System.out.println("usage: xmldog."+(OS.get().isWindows()?"bat":"sh")+" <xml-file>");
            System.exit(1);
        }

        System.out.println("Namespaces:");

        final DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
        SAXUtil.newSAXParser(true, false, false).parse(new InputSource(args[0]), new DefaultHandler(){
            @Override
            public void startPrefixMapping(String prefix, String uri) throws SAXException{
                if(uri.length()>0 && prefix.length()==0)
                    prefix = "ns";
                if(nsContext.getPrefix(uri)==null){
                    String _uri = nsContext.getNamespaceURI(prefix);
                    if(_uri!=null && _uri.length()>0){
                        int i = 1;
                        String _prefix;
                        if(prefix.length()==0)
                            prefix = "ns";
                        while(true){
                            _prefix = prefix + i;
                            if(nsContext.getNamespaceURI(_prefix)==null){
                                prefix = _prefix;
                                break;
                            }
                            i++;
                        }
                    }
                    nsContext.declarePrefix(prefix, uri);
                    System.out.println(prefix+"\t= "+uri);
                }
            }
        });

        XMLDog dog = new XMLDog(nsContext, null, null);
        List<Expression> expressions = new ArrayList<Expression>();

        System.out.println();
        System.out.println("XPaths: ");
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while((line=console.readLine())!=null){
            line = line.trim();
            if(line.length()>0)
                expressions.add(dog.addXPath(line));
            else
                break;
        }

        System.out.println("=========================================");
        System.out.println("|          XPath-Results                |");
        System.out.println("=========================================");
        System.out.println();
        XPathResults results = dog.sniff(new InputSource(args[0]));
        results.print(expressions, System.out);
    }
}
