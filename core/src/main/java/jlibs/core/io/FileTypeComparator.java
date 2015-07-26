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

package jlibs.core.io;

import jlibs.core.util.DefaultComparator;

import java.io.File;

/**
 * @author Santhosh Kumar T
 */
public class FileTypeComparator extends DefaultComparator<File>{
    @Override
    protected int _compare(File file1, File file2){
        boolean dir1 = file1.isDirectory();
        boolean dir2 = file2.isDirectory();
        if(dir1==dir2)
            return 0;
        else
            return dir1 ? +1 : -1;
    }
}
