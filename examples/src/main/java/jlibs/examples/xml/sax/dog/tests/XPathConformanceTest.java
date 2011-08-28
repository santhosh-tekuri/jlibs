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

package jlibs.examples.xml.sax.dog.tests;

import jlibs.examples.xml.sax.dog.TestCase;
import jlibs.examples.xml.sax.dog.TestSuite;

/**
 * @author Santhosh Kumar T
 */
public class XPathConformanceTest{
    private boolean printAllResults = false;
    private TestSuite testSuite;

    public XPathConformanceTest(String args[], boolean useSTAX, boolean useXMLBuilder, boolean useInstantResults) throws Exception{
        testSuite = args.length==0 ? new TestSuite() : new TestSuite(args[0]);
        TestCase.useSTAX = useSTAX;
        TestCase.useXMLBuilder = useXMLBuilder;
        TestCase.useInstantResults = useInstantResults;
    }

    public void run() throws Exception{
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("useSTAX: "+TestCase.useSTAX+"\t useXMLBuilder: "+TestCase.useXMLBuilder+"\t useInstantResults: "+TestCase.useInstantResults);
//        System.out.println("Press <ENTER> to run tests.....");
//        System.in.read();

        int failed = 0;
        for(TestCase testCase: testSuite.testCases){
            testCase.usingDOM();
            testCase.usingXMLDog();

            for(int i=0; i<testCase.xpaths.size(); i++){
                boolean passed = testCase.passed(i);
                if(!passed)
                    failed++;
                if(passed && !printAllResults)
                    continue;
                System.out.println(passed ? "SUCCESSFULL:" : "FAILED:");
                testCase.printResults(i);
            }
        }

        System.out.format("testcases are executed: total=%d failed=%d %n", testSuite.total, failed);
        if(failed>0){
            for(int i=0; i<10; i++)
                System.out.println("FAILED FAILED FAILED FAILED FAILED");
        }
    }

    public static void main(String[] args) throws Exception{
        try{
            assert false;
            throw new RuntimeException("assertions are not enabled");
        }catch(AssertionError err){
            // assertions are enabled
        }

        new XPathConformanceTest(args, false, false, false).run();
        new XPathConformanceTest(args, false, false, true).run();

        new XPathConformanceTest(args, false, true, false).run();
        new XPathConformanceTest(args, false, true, true).run();

        new XPathConformanceTest(args, true, false, false).run();
        new XPathConformanceTest(args, true, false, true).run();

        new XPathConformanceTest(args, true, true, false).run();
        new XPathConformanceTest(args, true, true, true).run();
    }
}