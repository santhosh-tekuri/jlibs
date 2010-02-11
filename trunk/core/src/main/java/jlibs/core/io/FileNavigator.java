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

package jlibs.core.io;

import jlibs.core.graph.Navigator2;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.Convertor;
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
        return getRelativePath(fromFile, toFile, this, FileUtil.SEPARATOR, false);
    }
}
