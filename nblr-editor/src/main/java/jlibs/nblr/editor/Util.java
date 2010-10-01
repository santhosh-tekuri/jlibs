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

package jlibs.nblr.editor;

import jlibs.core.lang.NotImplementedException;
import jlibs.core.util.CollectionUtil;
import jlibs.nblr.rules.Edge;
import jlibs.nblr.rules.Node;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Santhosh Kumar T
 */
public class Util{
    public static Font FIXED_WIDTH_FONT = new Font("Monospaced", Font.BOLD, 16);
    public static Stroke STROKE_2 = new BasicStroke(2);

    public static JToolBar toolbar(Action... actions){
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        for(Action action: actions){
            if(action==null)
                toolbar.addSeparator();
            else{
                action.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.NAME));
                toolbar.add(action);
            }
        }
        return toolbar;
    }

    public static java.util.List<Point> points(Point... points){
        ArrayList<Point> list = new ArrayList<Point>();
        CollectionUtil.addAll(list, points);
        return list;
    }

    public static Point point(Rectangle rect, double widthInPercentage, double heightInPercentage){
        return new Point(rect.x+(int)(rect.width*widthInPercentage), rect.y+(int)(rect.height*heightInPercentage));
    }

    public static Point rightCenter(Rectangle rect){
        return point(rect, 1, 0.5);
    }

    public static Point leftCenter(Rectangle rect){
        return point(rect, 0, 0.5);
    }

    public static Point topCenter(Rectangle rect){
        return point(rect, 0.5, 0);
    }

    public static Point bottomCenter(Rectangle rect){
        return point(rect, 0.5, 1);
    }

    public static Point point(Point pt, int dx, int dy){
        return new Point(pt.x+dx, pt.y+dy);
    }

    public static int dist(Point p1, Point p2){
        if(p1.x==p2.x) // horizontal line
            return Math.abs(p1.y-p2.y);
        else if(p1.y==p2.y) // vertical line
            return Math.abs(p1.x-p2.x);
        else
            throw new NotImplementedException();
    }

    public static int dist(java.util.List<Point> points, int labelSegment){
        int dist = 0;
        for(int segment=0; segment<labelSegment; segment++)
            dist += dist(points.get(segment), points.get(segment+1));
        dist += dist(points.get(labelSegment), points.get(labelSegment+1))/2;
        return dist;
    }

    /*-------------------------------------------------[ Scene ]---------------------------------------------------*/

    public static Object model(Widget widget){
        return ((ObjectScene)widget.getScene()).findObject(widget);
    }

    public static Node node(Widget widget){
        return (Node)model(widget);
    }

    public static Edge edge(Widget widget){
        return (Edge)model(widget);
    }

    public static Rectangle bounds(Widget widget){
        return widget.convertLocalToScene(widget.getPreferredBounds());
    }

    public static Rectangle sourceBounds(ConnectionWidget connection){
        return bounds(connection.getSourceAnchor().getRelatedWidget());
    }

    public static Rectangle targetBounds(ConnectionWidget connection){
        return bounds(connection.getTargetAnchor().getRelatedWidget());
    }

    public static java.util.List<Point> route(ConnectionWidget connection, int labelSegment, Point... points){
        java.util.List<Point> pointsList = points(points);
        Widget label = connection.getChildren().get(0);
        connection.removeConstraint(label);
        connection.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_CENTER, dist(pointsList, labelSegment));
        return pointsList;
    }
}
