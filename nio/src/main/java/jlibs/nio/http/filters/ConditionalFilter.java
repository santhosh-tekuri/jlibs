/*
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

package jlibs.nio.http.filters;

import jlibs.nio.http.HTTPTask;

import java.util.function.Predicate;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ConditionalFilter<T extends HTTPTask> implements HTTPTask.ResponseFilter<T>, HTTPTask.RequestFilter<T>{
    private Predicate<T> predicate;
    private HTTPTask.Filter<T> delegate;

    public ConditionalFilter(Predicate<T> predicate, HTTPTask.Filter<T> delegate){
        this.predicate = predicate;
        this.delegate = delegate;
    }

    @Override
    public void filter(T task) throws Exception{
        if(predicate.test(task))
            delegate.filter(task);
        else
            task.resume();
    }
}
