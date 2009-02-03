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

package jlibs.xml.sax.sniff;

/**
 * @author Santhosh Kumar T
 */
public class XPathConformanceTest{
    private TestSuite testSuite;

    public XPathConformanceTest(TestSuite testSuite){
        this.testSuite = testSuite;
    }

    public void run() throws Exception{
        int failed = 0;
        for(TestCase testCase: testSuite.testCases){
            testCase.usingJDK();
            testCase.usingXMLDog();

            for(int i=0; i<testCase.xpaths.size(); i++){
                boolean passed = testCase.passed(i);
                if(!passed)
                    failed++;

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
        TestSuite testSuite = args.length==0 ? new TestSuite() : new TestSuite(args[0]);
        new XPathConformanceTest(testSuite).run();
    }
}