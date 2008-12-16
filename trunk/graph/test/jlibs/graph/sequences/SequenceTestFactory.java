package jlibs.graph.sequences;

import org.testng.annotations.Factory;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.sequences.CollectionSequence;
import jlibs.core.graph.sequences.ArraySequence;

public class SequenceTestFactory{
    @Factory
    @SuppressWarnings({"unchecked"})
    public static SequenceTest[] createTests(){
        Sequence sequences[] = new Sequence[]{
            new TOCSequence(1, 10),
            new CollectionSequence(System.getProperties().entrySet()),
            new ArraySequence(System.getProperties().entrySet().toArray()),
        };

        SequenceTest tests[] = new SequenceTest[sequences.length];
        for(int i=0; i<sequences.length; i++)
            tests[i] = new SequenceTest(sequences[i]);

        return tests;
    }
}