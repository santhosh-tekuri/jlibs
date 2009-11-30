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

package jlibs.xml.sax.dog.tests;

import jlibs.core.io.FileUtil;
import jlibs.core.lang.JavaProcessBuilder;
import jlibs.xml.sax.dog.TestSuite;
import jlibs.xml.sax.dog.TestCase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * @author Santhosh Kumar T
 */
public class XPathPerformanceTest{
    private static final int runCount = 20;
    private TestSuite testSuite;

    public XPathPerformanceTest(TestSuite testSuite){
        this.testSuite = testSuite;
    }

    public void run(boolean cumulative, boolean xmlDog) throws Exception{
        long stats[] = new long[testSuite.testCases.size()];

        if(cumulative){
            for(int i=0; i<testSuite.testCases.size(); i++){
                TestCase testCase = testSuite.testCases.get(i);

                for(int count=0; count<runCount; count++){
                    long time = System.nanoTime();
                    if(xmlDog)
                        testCase.usingXMLDog();
                    else
                        testCase.usingDOM();
                    time = System.nanoTime() - time;
                    stats[i] += time;
                    if(xmlDog)
                        testCase.dogResult = null;
                    else
                        testCase.jdkResult = null;
                }
            }
        }else{
            for(int count=0; count<runCount; count++){
                for(int i=0; i<testSuite.testCases.size(); i++){
                    TestCase testCase = testSuite.testCases.get(i);
                    long time = System.nanoTime();
                    if(xmlDog)
                        testCase.usingXMLDog();
                    else
                        testCase.usingDOM();
                    time = System.nanoTime() - time;
                    stats[i] += time;
                    if(xmlDog)
                        testCase.dogResult = null;
                    else
                        testCase.jdkResult = null;
                }
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(FileUtil.TMP_DIR+FileUtil.SEPARATOR+xmlDog+".txt"));
        for(long stat: stats){
            writer.write(String.valueOf(stat/runCount));
            writer.newLine();
        }
        writer.close();
    }

    private static void printLine(){
        for(int i=0; i<70; i++)
            System.out.print("-");
        System.out.println();
    }

    private static void printStat(String file, int xpaths, long dogTime, long domTime){
        long diff = dogTime - domTime;
        double faster = (1.0*Math.max(dogTime, domTime)/Math.min(dogTime, domTime)) * (dogTime<=domTime ? -1 : +1);
        System.out.format("%30s %6d %6d %6d %6d   %+2.2f%n", file, xpaths, (long)(dogTime*1E-06), (long)(domTime*1E-06), (long)(diff*1E-06), faster);
    }

    public static void main(String[] args) throws Exception{
        if(args.length==1){
            JavaProcessBuilder jvm = new JavaProcessBuilder()
                    .jvmArg("-server")
                    .jvmArg("-classpath")
                    .jvmArg(System.getProperty("java.class.path"))
                    .mainClass(XPathPerformanceTest.class.getName())
                    .arg(args[0]);

            jvm.arg("true");
            if(jvm.launch(System.out, System.err).waitFor()!=0)
                return;
            System.out.println("Done");

            jvm.args().set(jvm.args().size()-1, "false");
            if(jvm.launch(System.out, System.err).waitFor()!=0)
                return;
            System.out.println("Done");

            TestSuite testSuite = new TestSuite(args[0]);
            BufferedReader dogReader = new BufferedReader(new FileReader(FileUtil.TMP_DIR+FileUtil.SEPARATOR+"true.txt"));
            BufferedReader domReader = new BufferedReader(new FileReader(FileUtil.TMP_DIR+FileUtil.SEPARATOR+"false.txt"));

            System.out.format("%nAverage Execution Time over %d runs:%n", runCount);
            printLine();
            System.out.format("%30s %6s %6s %6s %6s %6s%n", "File", "XPaths", "XMLDog", TestCase.domEngine.getName(), "Diff", "Percentage");
            printLine();

            long dogTotal = 0;
            long domTotal = 0;
            for(int i=0; i<testSuite.testCases.size(); i++){
                TestCase testCase = testSuite.testCases.get(i);
                long dogTime = Long.parseLong(dogReader.readLine());
                long domTime = Long.parseLong(domReader.readLine());
                printStat(testCase.file, testCase.xpaths.size(), dogTime, domTime);
                dogTotal += dogTime;
                domTotal += domTime;
            }
            printLine();
            printStat("Total", testSuite.total, dogTotal, domTotal);

            dogReader.close();
            domReader.close();
        }else if(args.length==2){
            TestSuite testSuite = new TestSuite(args[0]);
            new XPathPerformanceTest(testSuite).run(true, args[1].equals("true"));
        }
    }
}