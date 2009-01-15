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

import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Root;
import org.xml.sax.Attributes;

/**
 * @author Santhosh Kumar T
 */
public class ContextManager implements Debuggable{
    private Contexts contexts = new Contexts();
    protected XPathResults results;
    private ElementStack elementStack;

    public ContextManager(int minHits){
        results = new XPathResults(minHits);
    }
    
    public void reset(Root root, ElementStack elementStack){
        contexts.reset(new Context(root));
        this.elementStack = elementStack;

        if(debug){
            contexts.printCurrent("Contexts");
            System.out.println("-----------------------------------------------------------------");
        }
    }

    public void matchText(StringContent contents){
        results.resultWrapper = contents;
        for(Context context: contexts)
            context.matchText(contents);
        contents.reset();
    }

    public void elementStarted(String uri, String name, int pos){
        results.resultWrapper = elementStack;
        for(Context context: contexts)
            context.startElement(uri, name, pos);
        contexts.update();
    }

    public void matchAttributes(Attributes attrs){
        if(contexts.hasAttributeChild){
            for(int i=0; i<attrs.getLength(); i++){
                String attrUri = attrs.getURI(i);
                String attrName = attrs.getLocalName(i);
                String attrValue = attrs.getValue(i);
                results.resultWrapper = attrValue;
                for(Context newContext: contexts)
                    newContext.matchAttribute(attrUri, attrName, attrValue);
            }
        }
    }

    public void elementEnded(StringContent contents){
        results.resultWrapper = contents;
        for(Context context: contexts){
            context.matchText(contents);
            contexts.addUnique(context.endElement());
        }
        contents.reset();
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

        Context(Root root){
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

        public void matchText(StringContent contents){
            if(!contents.isEmpty() && depth<=0){
                if(node.consumable()){
                    results.hit(this, node);
                    checkConstraints(node, contents);
                }
                for(Node child: node.children){
                    if(child.matchesText(contents))
                        results.hit(this, child);
                    checkConstraints(child, contents);
                }
            }
        }

        private void checkConstraints(Node child, String uri, String name, int pos){
            for(Node constraint: child.constraints){
                if(constraint.matchesElement(uri, name, pos)){
                    if(results.hit(this, constraint)){
                        contexts.add(childContext(constraint));
                        checkConstraints(constraint, uri, name, pos);
                    }
                }
            }
        }

        private void checkConstraints(Node child, StringContent contents){
            for(Node constraint: child.constraints){
                if(constraint.matchesText(contents)){
                    results.hit(this, constraint);
                    checkConstraints(constraint, contents);
                }
            }
        }

        public void startElement(String uri, String name, int pos){
            String message = toString();

            contexts.mark();

            if(depth>0){
                depth++;
                contexts.add(this);
            }else{
                for(Node child: node.children){
                    if(child.matchesElement(uri, name, pos)){
                        if(results.hit(this, child)){
                            contexts.add(childContext(child));
                            checkConstraints(child, uri, name, pos);
                        }
                    }
                }
                if(node.consumable()){
                    if(results.hit(this, node)){
                        depth--;
                        contexts.add(this);
                        checkConstraints(node, uri, name, pos);
                    }
                }else{
                    if(contexts.markSize()==0){
                        depth++;
                        contexts.add(this);
                    }
                }
            }

            if(debug)
                contexts.printNext(message);
        }

        public void matchAttribute(String uri, String name, String value){
            if(depth<=0){
                for(Node child: node.children){
                    if(child.matchesAttribute(uri, name, value)){
                        results.hit(this, child);
                        for(Node constraint: child.constraints){
                            if(constraint.matchesAttribute(uri, name, value))
                                results.hit(this, constraint);
                        }
                    }
                }
            }
        }

        public Context endElement(){
            if(depth==0){
                results.clearHitCounts(this, node);
                results.clearPredicateCache(depth, node);
                return parentContext();
            }else{
                if(depth>0){
//                    results.clearHitCounts(depth, node);
                    depth--;
                }else{
                    results.clearHitCounts(this, node);
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
