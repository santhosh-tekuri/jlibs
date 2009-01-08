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
import org.testng.annotations.Factory;

public class SequenceTestFactory{
    @Factory
    @SuppressWarnings({"unchecked"})
    public static SequenceTest[] createTests(){
        Sequence sequences[] = new Sequence[]{
            new TOCSequence(1, 10),
            new IterableSequence(System.getProperties().entrySet()),
            new ArraySequence(System.getProperties().entrySet().toArray()),
        };

        SequenceTest tests[] = new SequenceTest[sequences.length];
        for(int i=0; i<sequences.length; i++)
            tests[i] = new SequenceTest(sequences[i]);

        return tests;
    }
}