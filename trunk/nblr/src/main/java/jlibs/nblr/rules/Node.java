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
        int incomingCount = incoming.size();
        for(Edge edge: incoming){
            if(edge.loop())
                incomingCount--;
        }
        return incomingCount>1;
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
