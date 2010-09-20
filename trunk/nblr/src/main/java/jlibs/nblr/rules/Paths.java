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

package jlibs.nblr.rules;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * @author Santhosh Kumar T
 */
public class Paths extends ArrayList<Path>{
    public int charIndex;

    public Paths(int charIndex){
        this.charIndex = charIndex;
    }

    @Override
    public String toString(){
        StringBuilder buff = new StringBuilder();
        toString(new ArrayDeque<Path>(), buff);
        return buff.toString();
    }

    void toString(ArrayDeque<Path> pathStack, StringBuilder buff){
        for(Path path: this)
            path.toString(pathStack, buff);
    }
}
