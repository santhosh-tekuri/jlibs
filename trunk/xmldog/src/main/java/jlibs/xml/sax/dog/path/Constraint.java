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

package jlibs.xml.sax.dog.path;

import jlibs.xml.sax.dog.sniff.Event;

/**
 * The root interface in expression hierarchy.
 *
 * @author Santhosh Kumar T
 */
public abstract class Constraint{
    /**
     * There are few subclasses of Constraint which are sigletons.
     * These subclasses always use same id across xmldog instances.
     * The following constants are used for those subclasses respectively.
     */
    public static final int ID_NODE       = 0; /** used by {@link jlibs.xml.sax.dog.path.tests.Node} **/
    public static final int ID_PARENTNODE = 1; /** used by {@link jlibs.xml.sax.dog.path.tests.ParentNode} **/
    public static final int ID_ELEMENT    = 2; /** used by {@link jlibs.xml.sax.dog.path.tests.Element} **/
    public static final int ID_STAR       = 3; /** used by {@link jlibs.xml.sax.dog.path.tests.Star} **/
    public static final int ID_TEXT       = 4; /** used by {@link jlibs.xml.sax.dog.path.tests.Text} **/
    public static final int ID_COMMENT    = 5; /** used by {@link jlibs.xml.sax.dog.path.tests.Comment} **/
    public static final int ID_PI         = 6; /** used by {@link jlibs.xml.sax.dog.path.tests.PI} **/

    /**
     * the ids of constraints which are non-singletons
     * start from ID_START(inclusive)
     */
    public static final int ID_START      = 7;

    /**
     * each constraint instanceof in a xmldog
     * is assigned a unique id.
     */
    public final int id;

    protected Constraint(int id){
        this.id = id;
    }

    public abstract boolean matches(Event event);

    @Override
    public final int hashCode(){
        return id;
    }
}
