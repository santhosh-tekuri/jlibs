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

package jlibs.nio.http.msg;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Santhosh Kumar Tekuri
 */
public class JAXBPayload extends EncodablePayload{
    public final JAXBContext context;
    public final Object bean;
    public boolean format;
    public JAXBPayload(String contentType, Object bean, JAXBContext context){
        super(contentType);
        this.bean = bean;
        this.context = context;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException{
        try{
            Marshaller m = context.createMarshaller();
            if(format)
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(bean, out);
        }catch(JAXBException ex){
            throw new IOException(ex);
        }
    }
}
