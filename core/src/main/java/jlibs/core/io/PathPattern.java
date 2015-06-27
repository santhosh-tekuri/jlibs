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

package jlibs.core.io;

import jlibs.core.graph.Filter;

import java.util.regex.Pattern;

/**
 * @author Santhosh Kumar T
 */
public class PathPattern{
    private class Include implements Filter{
        private Pattern pattern;
        private Include(String pathPattern){
            this.pattern = Pattern.compile(pathPattern.replace("**", ".+").replace("*", "[^/]+"));
        }
        
        @Override
        public boolean select(Object elem){
            return false;
        }
    }
}
