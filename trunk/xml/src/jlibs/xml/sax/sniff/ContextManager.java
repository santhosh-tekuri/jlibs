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

package jlibs.xml.sax.sniff;

import jlibs.xml.sax.sniff.events.Attribute;
import jlibs.xml.sax.sniff.events.Document;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.events.DocumentOrder;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Root;
import jlibs.xml.sax.sniff.model.axis.Descendant;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class ContextManager implements Debuggable{
    private Contexts contexts = new Contexts();
    protected XPathResults results;

    public ContextManager(DocumentOrder documentOrder, int minHits){
        results = new XPathResults(documentOrder, minHits);
    }
    
    public void reset(Document document, Root root){
        List<Context> list = new ArrayList<Context>();
        list.add(new Context(root));
        for(Node node: root.constraints()){
            if(node instanceof Descendant)
                list.add(new Context(node));
        }

        contexts.reset(list);

        document.setData();
        for(Context context: list)
            results.hit(context, document, root);

        if(debug){
            contexts.printCurrent("Contexts");
            System.out.println("-----------------------------------------------------------------");
        }
    }

    public void match(Event event){
        for(Context context: contexts)
            context.match(event);
        if(event.type()==Event.ELEMENT)
            contexts.update();
    }

    public void matchAttributes(Attribute attr, Attributes attrs){
        if(contexts.hasAttributeChild){
            for(int i=0; i<attrs.getLength(); i++){
                attr.setData(attrs, i);
                for(Context context: contexts)
                    context.match(attr);
            }
        }
    }

    public void elementEnded(){
        for(Context context: contexts)
            contexts.addUnique(context.endElement());
        contexts.update();
    }

    int maxInstCount;
    int instCount;
    public class Context{
        public Context parent;
        public Node node;
        public int depth;
//        int parentDepths[];

        private Context(){
            instCount++;
            maxInstCount = Math.max(maxInstCount, instCount);
        }

        public Context(Node root){
            this();
//            parentDepths = new int[0];
            node = root;
        }

        public Context childContext(Node child){
            Context context = new Context();
            context.node = child;
            context.parent = this;

//            context.parentDepths = new int[parentDepths.length+1];
//            System.arraycopy(parentDepths, 0, context.parentDepths, 0, parentDepths.length);
//            context.parentDepths[context.parentDepths.length-1] = depth;

            return context;
        }

        public Context parentContext(){
            instCount--;
            return parent;

//            Context parent = new Context();
//            parent.node = node.parent;
//            parent.depth = parentDepths[parentDepths.length-1];
//
//            parent.parentDepths = new int[parentDepths.length-1];
//            System.arraycopy(parentDepths, 0, parent.parentDepths, 0, parent.parentDepths.length);
//            return parent;
        }


        public void match(Event event){
            boolean changeContext = event.hasChildren();

            @SuppressWarnings({"UnusedAssignment"})
            String message = toString();

            contexts.mark();
            
            if(depth>0){
                if(changeContext){
                    depth++;
                    contexts.add(this);
                }
            }else{
                for(Node child: node.children()){
                    if(child.matches(event)){
                        if(results.hit(this, event, child)){
                            if(changeContext)
                                contexts.add(childContext(child));
                            matchConstraints(child, event);
                        }
                    }
                }
                if(node.consumable(event)){
                    if(results.hit(this, event, node)){
                        if(changeContext){
                            depth--;
                            contexts.add(this);
                        }
                        matchConstraints(node, event);
                    }
                }else{
                    if(changeContext && contexts.markSize()==0){
                        depth++;
                        contexts.add(this);
                    }
                }
            }

            //noinspection ConstantConditions
            if(debug && changeContext)
                contexts.printNext(message);
        }

        private void matchConstraints(Node child, Event event){
            boolean changeContext = event.hasChildren();

            for(Node constraint: child.constraints()){
                if(constraint.matches(event)){
                    if(results.hit(this, event, constraint)){
                        if(changeContext)
                            contexts.add(childContext(constraint));
                        matchConstraints(constraint, event);
                    }
                }
            }
        }

        public Context endElement(){
            if(depth==0){
                results.clearHitCounts(this);
                results.clearPredicateCache(depth, node);
                return parentContext();
            }else{
                if(depth>0){
//                    results.clearHitCounts(depth, node);
                    depth--;
                }else{
                    results.clearHitCounts(this);
                    depth++;
                }

                return this;
            }
        }

        @Override
        public int hashCode(){
            return depth + node.hashCode();
        }

        @Override
        public boolean equals(Object obj){
            if(obj instanceof Context){
                Context that = (Context)obj;
                return this.depth==that.depth && this.node==that.node;
            }else
                return false;
        }

        @Override
        public String toString(){
            return "["+depth+"] "+node;
        }
    }
}
