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

import jlibs.nio.http.msg.spec.values.QualityItem;

import java.util.List;

/**
 * @author Santhosh Kumar Tekuri
 */
public class AcceptCharset extends QualityItems{
    public static final String ISO_8859_1 = "ISO-8859-1";
    public AcceptCharset(){
        super("Accept-Charset");
    }

    public static double getQuality(String charset, List<QualityItem<String>> acceptCharset){
        if(acceptCharset==null || acceptCharset.isEmpty())
            return 1.0;

        double defaultQuality = 0;
        for(QualityItem<String> qualityItem: acceptCharset){
            if("*".equals(qualityItem.item))
                defaultQuality = qualityItem.quality;
            else if(qualityItem.item.equalsIgnoreCase(charset))
                return qualityItem.quality;
        }

        return ISO_8859_1.equalsIgnoreCase(charset) ? 1 : defaultQuality;
    }
}
