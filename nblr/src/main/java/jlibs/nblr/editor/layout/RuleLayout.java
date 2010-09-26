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

package jlibs.nblr.editor.layout;

/**
 * @author Santhosh Kumar T
 */

import jlibs.core.lang.NotImplementedException;
import jlibs.nblr.editor.RuleScene;
import jlibs.nblr.rules.Edge;
import jlibs.nblr.rules.Node;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.UniversalGraph;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class RuleLayout extends GraphLayout<Node, Edge>{
    private boolean animated;
    public static final int originX = 100;
    public static final int originY = 50;
    public static final int horizontalGap = 100;
    public static final int verticalGap = 25;

    public RuleLayout(boolean animated){
        this.animated = animated;
    }

    private List<List<Node>> coordinates;

    @Override
    protected void performGraphLayout(UniversalGraph<Node, Edge> graph){
        RuleScene scene = (RuleScene)graph.getScene();
        if(scene.getRule()==null)
            return;

        coordinates = scene.getRule().node.coordinates();

        List<Integer> columns = new ArrayList<Integer>();
        columns.add(originX);
        for(List<Node> nodes: coordinates){
            for(int col=1; col<nodes.size(); col++){
                while(columns.size()<=col)
                    columns.add(horizontalGap);

                Node minNode = nodes.get(col-1);
                Node maxNode = nodes.get(col);
                int width = horizontalGap;
                if(maxNode!=null && minNode!=null){
                    int middleWidth = 0;
                    for(Edge edge: minNode.outgoing){
                        if(edge.target==maxNode){
                            LabelWidget label = (LabelWidget)scene.findWidget(edge).getChildren().get(0);
                            if(label.getLabel()!=null)
                                middleWidth = Math.max(middleWidth, 30+label.getPreferredBounds().width);
                        }
                    }
                    for(Edge edge: maxNode.outgoing){
                        if(edge.target==minNode){
                            LabelWidget label = (LabelWidget)scene.findWidget(edge).getChildren().get(0);
                            if(label.getLabel()!=null)
                                middleWidth = Math.max(middleWidth, 30+label.getPreferredBounds().width);
                        }
                    }
                    middleWidth = Math.max(horizontalGap, middleWidth);

                    int minWidth = scene.findWidget(minNode).getPreferredBounds().width/2;
                    int minLoopWidth = 0;
                    for(Edge edge: minNode.outgoing){
                        if(edge.loop()){
                            LabelWidget label = (LabelWidget)scene.findWidget(edge).getChildren().get(0);
                            if(label.getLabel()!=null)
                                minLoopWidth = Math.max(minLoopWidth, label.getPreferredBounds().width/2);
                        }
                    }

                    int maxWidth = scene.findWidget(maxNode).getPreferredBounds().width/2;
                    int maxLoopWidth = 0;
                    for(Edge edge: maxNode.outgoing){
                        if(edge.loop()){
                            LabelWidget label = (LabelWidget)scene.findWidget(edge).getChildren().get(0);
                            if(label.getLabel()!=null)
                                maxLoopWidth = Math.max(maxLoopWidth, label.getPreferredBounds().width/2);
                        }
                    }

                    width = Math.max(horizontalGap, Math.max(minWidth, minLoopWidth) + middleWidth + Math.max(maxWidth, maxLoopWidth));
                }
                columns.set(col, Math.max(columns.get(col), width));
            }
        }

        for(int i=1; i<columns.size(); i++)
            columns.set(i, columns.get(i-1)+columns.get(i));

        analyzeEdges(graph, columns.size());

        int rowCount = coordinates.size();
        int rows[] = new int[rowCount];
        for(int row=0; row<rowCount; row++){
            int top = 0;
            for(Node node: coordinates.get(row)){
                if(node!=null){
                    top = Math.max(top, node.conLeftTop);
                    top = Math.max(top, node.conRightTop);

                    int loop = 0;
                    for(Edge edge: node.outgoing){
                        if(edge.loop())
                            loop++;
                    }
                    top = Math.max(top, loop);
                }
            }
            
            int bottom = 0;
            if(row>0){
                for(Node node: coordinates.get(row-1)){
                    if(node!=null){
                        bottom = Math.max(bottom, node.conLeftBottom);
                        bottom = Math.max(bottom, node.conRightBottom);
                    }
                }
            }

            int height = top+bottom;
            rows[row] = 50*height;
            if(row>0)
                rows[row] += rows[row-1];
            if(height==0)
                rows[row] += originY;
            else
                rows[row] += verticalGap;
        }

        for(Node node: graph.getNodes()){
            Widget widget = scene.findWidget(node);
            Point location = new Point(columns.get(node.col), rows[node.row]/*originY + node.row * verticalGap*/);
            if(animated)
                scene.getSceneAnimator().animatePreferredLocation(widget, location);
            else
                widget.setPreferredLocation(location);
        }
    }

    private void analyzeEdges(UniversalGraph<Node, Edge> graph, int colCount){
        for(Node node: graph.getNodes()){
            node.conLeft        = false;
            node.conRight       = false;
            node.conTop         = false;
            node.conBottom      = false;
            node.conLeftTop     = 0;
            node.conLeftBottom  = 0;
            node.conRightTop    = 0;
            node.conRightBottom = 0;
        }

        int rowCount = coordinates.size();
        ArrayList<Edge> edgesList = new ArrayList<Edge>(graph.getEdges());
        for(int row=0; row<rowCount; row++){
            for(int jump=0; jump<colCount-1; jump++){
                Iterator<Edge> edges = edgesList.iterator();
                while(edges.hasNext()){
                    Edge edge = edges.next();
                    if(edge.forward() && edge.sameRow(row) && edge.jump()==jump){
                        analyzeEdge(row, jump, edge);
                        edges.remove();
                    }
                }
                edges = edgesList.iterator();
                while(edges.hasNext()){
                    Edge edge = edges.next();
                    if(edge.backward() && edge.sameRow(row) && edge.jump()==jump){
                        analyzeEdge(row, jump, edge);
                        edges.remove();
                    }
                }
            }
        }
    }

    private void analyzeEdge(int row, int jump, Edge edge){
        Node minNode = edge.min();
        Node maxNode = edge.max();
        if(jump==0 && !minNode.conRight && !maxNode.conLeft){
            edge.con = 0;
            minNode.conRight = maxNode.conLeft = true;
        }else{
            boolean topPossible = !minNode.conTop && !maxNode.conTop;
            if(topPossible){
                int topHeight = Math.max(minNode.conRightTop, maxNode.conLeftTop);
                List<Node> nodes = coordinates.get(row);
                for(int i=minNode.col+1; i<maxNode.col; i++){
                    Node node = nodes.get(i);
                    if(node!=null){
                        topHeight = Math.max(topHeight, node.conRightTop);
                        topHeight = Math.max(topHeight, node.conLeftTop);
                    }
                }
                topHeight++;

                edge.con = -topHeight;
                minNode.conRightTop = maxNode.conLeftTop = topHeight;
                for(int i=minNode.col+1; i<maxNode.col; i++){
                    Node node = nodes.get(i);
                    if(node!=null)
                        node.conTop = true;
                }
            }else{
                //boolean bottomPossible = minNode.conBottom==0 && maxNode.conBottom==0;

                int bottomHeight = Math.max(minNode.conRightBottom, maxNode.conLeftBottom);
                List<Node> nodes = coordinates.get(row);
                for(int i=minNode.col+1; i<maxNode.col; i++){
                    Node node = nodes.get(i);
                    if(node!=null){
                        bottomHeight = Math.max(bottomHeight, node.conRightBottom);
                        bottomHeight = Math.max(bottomHeight, node.conLeftBottom);
                    }
                }
                bottomHeight++;

                edge.con = +bottomHeight;
                minNode.conRightBottom = maxNode.conLeftBottom = bottomHeight;
                for(int i=minNode.col+1; i<maxNode.col; i++){
                    Node node = nodes.get(i);
                    if(node!=null)
                        node.conBottom = true;
                }
            }
        }
    }

    @Override
    protected void performNodesLayout(UniversalGraph<Node, Edge> neUniversalGraph, Collection<Node> ns){
        throw new NotImplementedException();
    }
}
