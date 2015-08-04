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
