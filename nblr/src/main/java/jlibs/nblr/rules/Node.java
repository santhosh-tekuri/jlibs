/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

import jlibs.nblr.actions.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Node{
    public String name;
    public int id;
    public int stateID;
    public Answer buffering;
    
    public static final String DYNAMIC_STRING_MATCH = "@DYNAMIC_STRING_MATCH";
    public Action action;
    
    public List<Edge> outgoing = new ArrayList<Edge>();
    public List<Edge> incoming = new ArrayList<Edge>();

    @Override
    public String toString(){
        StringBuilder buff = new StringBuilder();
        if(name!=null)
            buff.append('[').append(name).append(']');
        if(action!=null){
            if(buff.length()>0)
                buff.append("; ");
            buff.append(action);
        }
        return buff.toString();
    }

    public Edge addEdgeTo(Node target){
        return new Edge(this, target);
    }

    public Edge addEdgeFrom(Node source){
        return new Edge(source, this);
    }

    public Edge[] incoming(){
        return incoming.toArray(new Edge[incoming.size()]);
    }

    public Edge[] outgoing(){
        return outgoing.toArray(new Edge[outgoing.size()]);
    }

    public boolean junction(){
        return incoming.size()>1;
    }
    
    /*-------------------------------------------------[ Layout ]---------------------------------------------------*/

    public int row;
    public int col;

    public boolean conLeft;
    public boolean conRight;
    public boolean conTop;
    public boolean conBottom;
    
    public int conLeftTop;
    public int conLeftBottom;
    public int conRightTop;
    public int conRightBottom;

    public List<List<Node>> coordinates(){
        List<List<Node>> coordinates = new ArrayList<List<Node>>();
        coordinates(new ArrayList<Node>(), coordinates, this, 0, 0);
        return coordinates;
    }

    private boolean coordinates(List<Node> visited, List<List<Node>> coordinates, Node node, int row, int col){
        if(!visited.contains(node)){
            node.row = row;
            node.col = col;
            visited.add(node);

            while(coordinates.size()<=node.row)
                coordinates.add(new ArrayList<Node>());
            List<Node> rowList = coordinates.get(node.row);
            while(rowList.size()<=node.col)
                rowList.add(null);
            rowList.set(node.col, node);

            col++;
            for(Edge edge: node.outgoing){
                if(coordinates(visited, coordinates, edge.target, row, col))
                    row++;
            }
            return true;
        }else
            return false;
    }
}
