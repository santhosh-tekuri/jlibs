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

package jlibs.examples.core.util.i18n;

import jlibs.core.util.i18n.Bundle;
import jlibs.core.util.i18n.Entry;
import jlibs.core.util.i18n.Hint;

/**
 * @author Santhosh Kumar T
 */
@Bundle({
    @Entry("this is comment"),
    @Entry(lhs="keyName", rhs="rhsValue")
})
public class Test{
    @Bundle({
        @Entry("this is comment"),
        @Entry(hint= Hint.DISPLAY_NAME, rhs="this is field1")
    })
    private String field1;

    @Bundle({
        @Entry("this is comment"),
        @Entry(hintName="customHint", rhs="this is field2")
    })
    private String field2;

    @Bundle({
        @Entry("{0} - id specified"),
        @Entry(lhs="invalidID", rhs="you have specified invalid id {0}")
    })
    public void test(){

    }
}
