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

package jlibs.nio.http.util;

import jlibs.core.lang.NotImplementedException;
import jlibs.nio.Input;
import jlibs.nio.Output;
import jlibs.nio.filters.DeflaterOutput;
import jlibs.nio.filters.GZIPInput;
import jlibs.nio.filters.GZIPOutput;
import jlibs.nio.filters.InflaterInput;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ContentEncoding{
    public static final ContentEncoding DEFLATE = new ContentEncoding("deflate", InflaterInput::new, DeflaterOutput::new, DeflaterOutputStream::new);
    public static final ContentEncoding GZIP = new ContentEncoding("gzip", GZIPInput::new, GZIPOutput::new, GZIPOutputStream::new);

    public final String name;
    private final Function<Input, Input> inFunction;
    private final Function<Output, Output> outFunction;
    private final IOFunction<OutputStream, OutputStream> osFunction;

    public ContentEncoding(String name,
                           Function<Input, Input> inFunction,
                           Function<Output, Output> outFunction,
                           IOFunction<OutputStream, OutputStream> osFunction){
        this.name = Objects.requireNonNull(name, "name==null").toLowerCase();
        this.inFunction = inFunction;
        this.outFunction = outFunction;
        this.osFunction = osFunction;
    }

    public Input wrap(Input in){
        if(inFunction==null)
            throw new NotImplementedException(name+"-in");
        return inFunction.apply(in);
    }

    public Output wrap(Output out){
        if(outFunction==null)
            throw new NotImplementedException(name+"-out");
        return outFunction.apply(out);
    }

    public OutputStream wrap(OutputStream os) throws IOException{
        if(osFunction==null)
            throw new NotImplementedException(name+"-os");
        return osFunction.apply(os);
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public boolean equals(Object that){
        return this==that || (that instanceof ContentEncoding && this.name.equalsIgnoreCase(((ContentEncoding)that).name));
    }

    @Override
    public String toString(){
        return name;
    }

    public static ContentEncoding valueOf(String encoding){
        if(DEFLATE.name.equalsIgnoreCase(encoding))
            return DEFLATE;
        else if(GZIP.name.equalsIgnoreCase(encoding))
            return GZIP;
        else
            return new ContentEncoding(encoding, null, null, null);
    }
}
