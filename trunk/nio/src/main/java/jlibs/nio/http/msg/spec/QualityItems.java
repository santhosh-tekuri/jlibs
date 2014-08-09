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

package jlibs.nio.http.msg.spec;

import jlibs.nio.http.msg.Version;
import jlibs.nio.http.msg.spec.values.QualityItem;

/**
 * @author Santhosh Kumar Tekuri
 */
public class QualityItems extends ListHeaderSpec<QualityItem<String>>{
    public QualityItems(String name){
        super(name);
    }

    @Override
    protected QualityItem<String> parseSingle(Parser parser, Version version){
        String item = parser.lvalue();
        parser.rvalue();
        double quality = 1;
        while(true){
            String paramName = parser.lvalue();
            if(paramName==null)
                break;
            if(QualityItem.QUALITY.equals(paramName))
                quality = Double.parseDouble(parser.rvalue());
        }

        return new QualityItem<>(item, quality);
    }

    @Override
    protected String formatSingle(QualityItem<String> value, Version version){
        return value==null ? null : value.toString();
    }
}
