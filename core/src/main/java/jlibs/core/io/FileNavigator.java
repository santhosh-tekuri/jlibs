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

import jlibs.core.graph.Convertor;
import jlibs.core.graph.Navigator2;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.sequences.ArraySequence;
import jlibs.core.graph.sequences.EmptySequence;

import java.io.File;
import java.io.FileFilter;

/**
 * @author Santhosh Kumar T
 */
public class FileNavigator extends Navigator2<File> implements Convertor<File, String>{
    public static final FileNavigator INSTANCE = new FileNavigator(null);

    private FileFilter filter;
    public FileNavigator(FileFilter filter){
        this.filter = filter;
    }
    
    @Override
    public File parent(File elem){
        return elem.getParentFile();
    }

    @Override
    public Sequence<? extends File> children(File elem){
        if(elem.isDirectory())
            return new ArraySequence<File>(filter!=null ? elem.listFiles(filter) : elem.listFiles());
        else
            return EmptySequence.getInstance();
    }

    @Override
    public String convert(File source){
        return source.getName();
    }

    public String getRelativePath(File fromFile, File toFile){
        return getRelativePath(fromFile.getAbsoluteFile(), toFile.getAbsoluteFile(), this, FileUtil.SEPARATOR, false);
    }
}
