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
