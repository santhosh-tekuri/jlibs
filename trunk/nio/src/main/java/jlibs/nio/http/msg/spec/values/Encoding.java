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

import jlibs.core.lang.NotImplementedException;
import jlibs.nio.channels.filters.*;
import jlibs.nio.channels.impl.filters.InputFilterChannel;
import jlibs.nio.channels.impl.filters.OutputFilterChannel;
import jlibs.nio.util.IOFunction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Encoding{
    public static final Encoding CHUNKED = new Encoding("chunked", ChunkedInputFilter::new, ChunkedOutputFilter::new, null, null);
    public static final Encoding DEFLATE = new Encoding("deflate", InflaterInputFilter::new, DeflaterOutputFilter::new, InflaterInputStream::new, DeflaterOutputStream::new);
    public static final Encoding GZIP = new Encoding("gzip", GZIPInputFilter::new, GZIPOutputFilter::new, GZIPInputStream::new, GZIPOutputStream::new);

    public final String name;

    public Encoding(String name,
                    Supplier<InputFilterChannel> inFilterSupplier,
                    Supplier<OutputFilterChannel> outFilterSupplier,
                    IOFunction<InputStream, InputStream> inFilterFunction,
                    IOFunction<OutputStream, OutputStream> outFilterFunction
                    ){
        this.name = Objects.requireNonNull(name, "name==null");
        this.inFilterSupplier = inFilterSupplier;
        this.outFilterSupplier = outFilterSupplier;
        this.inFilterFunction = inFilterFunction;
        this.outFilterFunction = outFilterFunction;
    }

    private Supplier<InputFilterChannel> inFilterSupplier;
    public InputFilterChannel createInputFilter(){
        if(inFilterSupplier==null)
            throw new NotImplementedException("unsupported encoding: "+name);
        return inFilterSupplier.get();
    }

    private Supplier<OutputFilterChannel> outFilterSupplier;
    public OutputFilterChannel createOutputFilter(){
        if(outFilterSupplier==null)
            throw new NotImplementedException("unsupported encoding: "+name);
        return outFilterSupplier.get();
    }

    IOFunction<InputStream, InputStream> inFilterFunction;
    public InputStream apply(InputStream is) throws IOException{
        if(inFilterFunction==null)
            throw new NotImplementedException("unsupported encoding: "+name);
        return inFilterFunction.apply(is);
    }

    IOFunction<OutputStream, OutputStream> outFilterFunction;
    public OutputStream apply(OutputStream os) throws IOException{
        if(outFilterFunction==null)
            throw new NotImplementedException("unsupported encoding: "+name);
        return outFilterFunction.apply(os);
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public boolean equals(Object that){
        return this==that || (that instanceof Encoding && this.name.equalsIgnoreCase(((Encoding)that).name));
    }

    @Override
    public String toString(){
        return name;
    }
}
