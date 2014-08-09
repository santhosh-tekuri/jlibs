/*
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

package jlibs.nio.http.msg.spec.values;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Element{
    public String name;
    public String value;
    public List<Param> params;

    public Element(String name, String value){
        this.name = name;
        this.value = value;
    }

    public static List<Element> parse(String line){
        List<Element> elements = new ArrayList<>();

        HeaderValueParser parser = new HeaderValueParser(line, true);
        while(parser.nextElement()){
            elements.add(new Element(parser.name(), parser.value()));
            while(parser.nextParam()){
                Element elem = elements.get(elements.size()-1);
                if(elem.params==null)
                    elem.params = new ArrayList<>();
                elem.params.add(new Param(parser.name(), parser.value()));
            }
        }
        return elements;
    }

    public static void main(String[] args){
        String value = "username=\"Mufasa\",\n"+
                "                     realm=\"testrealm@host.com\",\n"+
                "                     nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\",\n"+
                "                     uri=\"/dir/index.html\",\n"+
                "                     qop=auth,\n"+
                "                     nc=00000001,\n"+
                "                     cnonce=\"0a4f113b\",\n"+
                "                     response=\"6629fae49393a05397450978507c4ef1\",\n"+
                "                     opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";

        List<Element> elements = Element.parse(value);
        System.out.println();
    }
}
