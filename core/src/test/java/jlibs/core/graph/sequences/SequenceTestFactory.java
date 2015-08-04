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