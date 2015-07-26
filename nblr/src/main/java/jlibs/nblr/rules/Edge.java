/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.nblr.rules;

import jlibs.nblr.matchers.Matcher;

/**
 * @author Santhosh Kumar T
 */
public class Edge{
    public Node source;
    public Node target;
    public boolean fallback;

    public Edge(Node source, Node target){
        setSource(source);
        setTarget(target);
    }

    public void setSource(Node source){
        if(this.source!=null)
            this.source.outgoing.remove(this);
        this.source = source;
        if(source!=null)
            source.outgoing.add(this);
    }

    public void setTarget(Node target){
        if(this.target!=null)
            this.target.incoming.remove(this);
        this.target = target;
        if(target!=null)
            target.incoming.add(this);
    }

    public void delete(){
        setSource(null);
        setTarget(null);
    }

    public Matcher matcher;
    public RuleTarget ruleTarget;

    public void inlineRule(){
        Rule rule = ruleTarget.rule.copy();
        source.addEdgeTo(rule.node);
        for(Node node: rule.nodes()){
            boolean sink = true;
            for(Edge outgoing: node.outgoing){
                if(!outgoing.loop()){
                    sink = false;
                    break;
                }
            }
            if(sink)
                node.addEdgeTo(target);
        }
        delete();
    }

    @Override
    public String toString(){
        String prefix = fallback ? "#" : "";
        if(matcher!=null)
            return prefix+(matcher.name==null ? matcher.toString() : '<'+matcher.name+'>');
        else if(ruleTarget!=null)
            return prefix+ruleTarget;
        else
            return "";
    }

    /*-------------------------------------------------[ Layout ]---------------------------------------------------*/

    public int con;

    public boolean loop(){
        return source==target;
    }

    public boolean sameRow(){
        return source.row==target.row;
    }

    public boolean sameRow(int row){
        return sameRow() && source.row==row;
    }

    public Node min(){
        return source.col<target.col ? source : target;
    }

    public Node max(){
        return source.col>target.col ? source : target;
    }

    public boolean forward(){
        return source.col<target.col;
    }

    public boolean backward(){
        return source.col>target.col;
    }

    public int jump(){
        return Math.abs(source.col-target.col)-1;
    }
}
