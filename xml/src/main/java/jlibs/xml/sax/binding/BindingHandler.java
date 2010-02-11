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

package jlibs.xml.sax.binding;

import jlibs.xml.sax.binding.impl.Handler;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
public class BindingHandler extends Handler{
    public BindingHandler(Class clazz){
        this(new BindingRegistry(clazz));
    }
    
    public BindingHandler(QName qname, Class clazz){
        this(new BindingRegistry(qname, clazz));
    }

    public BindingHandler(BindingRegistry docRegistry){
        super(docRegistry.registry);
    }

    private BindingListener listener;

    public void setBindingListener(BindingListener listener){
        this.listener = listener;
    }

    @Override
    protected void onUnresolvedElement(SAXContext context) throws SAXException{
        if(listener!=null)
            listener.unresolvedElement(context);
    }
}
