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

/**
 * This class contains constants to specify type axis.
 *
 * @author Santhosh Kumar T
 */
public class Axis{
    public static final int MAX                = 7;

    public static final int NAMESPACE          = 0;
    public static final int ATTRIBUTE          = 1;
    public static final int CHILD              = 2;
    public static final int DESCENDANT         = 3;
    public static final int FOLLOWING_SIBLING  = 4;
    public static final int FOLLOWING          = 5;

    public static final int MAX_TRACKED        = MAX - 2;

    /**
     * The following axises are not tracked by EventID.
     * SELF                 - immediatly resolved as it always hits only current eventID
     * DESCENDANT_OR_SELF   - is translated to DESCENDANT for tracking, after SELF part is processed
     *
     * always ensure that the axises that are not tracked by EventID as at last
     */
    public static final int DESCENDANT_OR_SELF = 6;
    public static final int SELF               = 7;

    /*
    public static final int PARENT             = 8;
    public static final int ANCESTOR           = 9;
    public static final int PRECEDING_SIBLING  = 10;
    public static final int PRECEDING          = 11;
    public static final int ANCESTOR_OR_SELF   = 12;
    */

    /**
     * Display names of axises in order.
     * Used only for debugging
     */
    public static final String names[] = {
        "namespace",
        "attribute",
        "child",
        "descendant",
        "following-sibling",
        "following",
        "descendant-or-self",
        "self",
    };
}
