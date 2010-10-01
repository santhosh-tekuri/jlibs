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

import jlibs.nblr.rules.Edge;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.modules.visual.router.DirectRouter;

import java.awt.*;
import java.util.Collections;

import static jlibs.nblr.editor.Util.*;

/**
 * @author Santhosh Kumar T
 */
public class RuleRouter extends DirectRouter{
    @Override
    public java.util.List<Point> routeConnection(ConnectionWidget connection){
        ObjectScene scene = (ObjectScene)connection.getScene();

        Edge edge = edge(connection);
        if(edge.loop()){
            Point p0 = topCenter(sourceBounds(connection));
            Point p1 = point(p0, -10, -20);
            Point p2 = point(p0, +10, -20);
            return points(p0, p1, p2, p0);
        }else if(edge.sameRow() && edge.con!=0){
            Point p0 = rightCenter(bounds(scene.findWidget(edge.min())));
            Point p3 = leftCenter(bounds(scene.findWidget(edge.max())));
            int y = 30 + sourceBounds(connection).height;
            Point p1 = point(p0, +10, +edge.con*y);
            Point p2 = point(p3, -10, +edge.con*y);
            java.util.List<Point> points = points(p0, p1, p2, p3);
            if(edge.backward())
                Collections.reverse(points);
            return points;
        }else if(edge.source.row<edge.target.row && edge.source.col<edge.target.col){
            //  S
            //  |
            //  |
            //  +----T
            //
            Point p0 = bottomCenter(sourceBounds(connection));
            Point p2 = leftCenter(targetBounds(connection));
            Point p1 = new Point(p0.x, p2.y);
            return route(connection, 1, p0, p1, p2);
        }else if(edge.source.row>edge.target.row && edge.source.col<edge.target.col){
            //       T
            //       |
            //       |
            //  S----+
            //
            Point p0 = rightCenter(sourceBounds(connection));
            Point p2 = bottomCenter(targetBounds(connection));
            Point p1 = new Point(p2.x, p0.y);
            return route(connection, 0, p0, p1, p2);
        }else
            return super.routeConnection(connection);
    }
}
