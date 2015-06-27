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
     * each constraint instance in a xmldog is assigned a unique id by
     * {@link jlibs.xml.sax.dog.sniff.XPathParser XPathParser}.
     * this unique id is used as hascode for fast lookup.
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
