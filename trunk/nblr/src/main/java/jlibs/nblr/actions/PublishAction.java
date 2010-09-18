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

package jlibs.nblr.actions;

import jlibs.core.lang.StringUtil;
import jlibs.nblr.Parser;
import jlibs.xml.sax.XMLDocument;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;

/**
 * @author Santhosh Kumar T
 */
public class PublishAction implements Action{
    public final String name;
    public final int begin;
    public final int end;

    public PublishAction(String name, int begin, int end){
        this.name = name;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public void execute(Parser parser){
        String data = parser.data(begin, end);
        System.out.println(name+"(\""+ StringUtil.toLiteral(data, false)+"\");");
        if(parser.consumer!=null){
            try{
                Method method = parser.consumer.getClass().getMethod(name, String.class);
                method.invoke(parser.consumer, data);
            }catch(Exception ex){
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public String javaCode(){
        return "consumer."+name+"(data("+begin+", "+end+"))";
    }

    @Override
    public String toString(){
        if(begin==0 && end==0)
            return name+"(data)";
        else
            return name+"(data["+begin+", "+-end+"])";
    }

    /*-------------------------------------------------[ SAXProducer ]---------------------------------------------------*/

    @Override
    public void serializeTo(QName rootElement, XMLDocument xml) throws SAXException{
        xml.startElement("publish");
        xml.addAttribute("name", name);
        xml.addAttribute("begin", ""+begin);
        xml.addAttribute("end", ""+end);
        xml.endElement();
    }
}
