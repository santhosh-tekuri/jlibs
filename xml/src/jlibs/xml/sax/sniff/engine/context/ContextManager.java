/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax.sniff.engine.context;

import jlibs.xml.sax.sniff.Debuggable;
import jlibs.xml.sax.sniff.events.Attribute;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Root;
import org.xml.sax.Attributes;

/**
 * @author Santhosh Kumar T
 */
public class ContextManager implements Debuggable{
    private Contexts contexts = new Contexts();

    public void reset(Root root){
        Context rootContext = new Context(root);
        root.contextStarted(rootContext, null);
        contexts.reset(rootContext);

        if(debug)
            contexts.printCurrent("Contexts");
    }

    public void match(Event event){
        for(Context context: contexts)
            context.match(event, contexts);
        if(event.hasChildren())
            contexts.update();
    }

    public void matchAttributes(Attribute attr, Attributes attrs){
        if(contexts.hasAttributeChild){
            for(int i=0; i<attrs.getLength(); i++){
                attr.setData(attrs, i);
                for(Context context: contexts)
                    context.match(attr, contexts);
            }
        }
    }

    public void elementEnded(){
        for(int i=contexts.current.size()-1; i>=0; i--){
            Context context = contexts.current.get(i);
            contexts.addUnique(context.endElement());
        }
        contexts.update();
    }

    public void documentEnded(){
        while(contexts.current.size()>0)
            elementEnded();
    }
}
