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

package jlibs.examples.xml.sax.dog;

import jlibs.examples.xml.sax.dog.tests.XPathConformanceTest;
import jlibs.examples.xml.sax.dog.tests.XPathPerformanceTest;

/**
 * @author Santhosh Kumar T
 */
public class XPathTest{
    public static void main(String[] args) throws Exception{
        XPathConformanceTest.main(args);
        XPathPerformanceTest.main(args);
    }
}
