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