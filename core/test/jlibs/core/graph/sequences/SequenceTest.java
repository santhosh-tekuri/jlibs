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

package jlibs.core.graph.sequences;

import jlibs.core.graph.Sequence;
import jlibs.core.graph.SequenceUtil;
import org.testng.ITest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class SequenceTest<E> implements ITest{

    private Sequence<E> seq;

    public SequenceTest(Sequence<E> seq){
        this.seq = seq;
    }

    @Override
    public String getTestName(){
        return seq.getClass().getSimpleName();
    }

    @BeforeMethod
    public void reset(){
        seq = seq.copy();
    }

    @Test
    public void testReset(){
        List<E> list = SequenceUtil.addAll(new LinkedList<E>(), seq);
        seq.reset();
        for(E elem; (elem=seq.next())!=null;){
            E prev = list.remove(0);
            assert prev.equals(elem) : prev+"!="+elem;
        }
    }

    @Test
    public void testCopy(){
        List<E> list = SequenceUtil.addAll(new LinkedList<E>(), seq);
        seq = seq.copy();
        for(E elem; (elem=seq.next())!=null;){
            E prev = list.remove(0);
            assert prev.equals(elem) : prev+"!="+elem;
        }
    }

    @Test
    public void testLength(){
        int len = seq.length();
        List<E> list = SequenceUtil.addAll(new LinkedList<E>(), seq);
        assert list.size()==len : list.size()+"!="+len;
    }

    @Test
    public void testHasNext(){
        List<E> list = SequenceUtil.addAll(new LinkedList<E>(), seq);
        assert !seq.hasNext() : "next()==null then hasNext()==false";
        seq.reset();

        E elem;
        while(true){
            boolean hasNext = seq.hasNext();
            assert hasNext==seq.hasNext() : "seq.hasNext()==seq.hasNext()";
            elem = seq.next();
            assert hasNext==(elem!=null) : "hasNext() == next()!=null";
            if(!hasNext)
                return;
            E prev = list.remove(0);
            assert prev.equals(elem) : prev+"!="+elem;
        }
    }
}
