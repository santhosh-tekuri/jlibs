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