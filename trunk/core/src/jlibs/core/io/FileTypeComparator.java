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

package jlibs.core.io;

import jlibs.core.util.DefaultComparator;

import java.io.File;

import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class FileTypeComparator extends DefaultComparator<File>{
    @Override
    protected int _compare(@NotNull File file1, @NotNull File file2){
        boolean dir1 = file1.isDirectory();
        boolean dir2 = file2.isDirectory();
        if(dir1==dir2)
            return 0;
        else
            return dir1 ? +1 : -1;
    }
}
