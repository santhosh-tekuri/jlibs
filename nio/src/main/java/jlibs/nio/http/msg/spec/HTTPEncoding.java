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
import jlibs.nio.http.msg.spec.values.Encoding;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HTTPEncoding extends ListHeaderSpec<Encoding>{
    public static final Encoding IDENTITY = new Encoding("identity", () -> null, () -> null, is -> is, os -> os);
    public static final Encoding CHUNKED = Encoding.CHUNKED;
    public static final Encoding DEFLATE = Encoding.DEFLATE;
    public static final Encoding GZIP = Encoding.GZIP;
    public static final Encoding COMPRESS = new Encoding("compress", null, null, null, null);
    public static final Encoding X_GZIP = new Encoding("x-gzip", null, null, null, null);
    public static final Encoding X_COMPRESS = new Encoding("x-compress", null, null, null, null);

    public HTTPEncoding(String name){
        super(name);
    }

    @Override
    protected Encoding parseSingle(Parser parser, Version version){
        String value = parser.value();
        if(IDENTITY.name.equalsIgnoreCase(value))
            return IDENTITY;
        else if(CHUNKED.name.equalsIgnoreCase(value))
            return CHUNKED;
        else if(DEFLATE.name.equalsIgnoreCase(value))
            return DEFLATE;
        else if(GZIP.name.equalsIgnoreCase(value))
            return GZIP;
        else if(COMPRESS.name.equalsIgnoreCase(value))
            return COMPRESS;
        else if(X_GZIP.name.equalsIgnoreCase(value))
            return X_GZIP;
        else if(X_COMPRESS.name.equalsIgnoreCase(value))
            return X_COMPRESS;
        else
            return new Encoding(value, null, null, null, null);
    }

    @Override
    public String formatSingle(Encoding value, Version version){
        return value==null ? null : value.name;
    }
}
