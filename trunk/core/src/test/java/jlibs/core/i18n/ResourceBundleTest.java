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

package jlibs.core.i18n;

import jlibs.core.io.IOUtil;
import jlibs.core.lang.ClassUtil;
import jlibs.core.net.URLUtil;
import jlibs.core.util.i18n.I18N;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static jlibs.core.i18n.Bundle1.BUNDLE1;
import static jlibs.core.i18n.Bundle2.BUNDLE2;

/**
 * @author Santhosh Kumar T
 */
public class ResourceBundleTest{
    private String compile(String... files){
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ArrayList<String> args = new ArrayList<String>();
        args.add("-d");
        args.add(ClassUtil.getClassPath(getClass()));
        args.add("-s");
        args.add(ClassUtil.getClassPath(getClass()));
        for(String file: files){
            URL url = getClass().getResource(file);
            args.add(URLUtil.toSystemID(url));
        }
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        if(compiler.run(null, null, err, args.toArray(new String[args.size()]))==0)
            return null;
        return err.toString();
    }

    private void assertErrors(String errors, String... searchFor){
        if(searchFor.length==0)
            Assert.assertNull(errors, "compilation failed with errors:\n"+errors);
        else{
            errors = errors.replace('\\', '/');
            for(String error: searchFor){
                if(errors.indexOf(error)==-1)
                    Assert.fail("[Expected Error] "+error+"\n[Actual Error] "+errors);
            }
        }
    }

    @Test(description="@ResourceBundle can't be applied on class")
    public void classBundle(){
        assertErrors(compile("/i18n/ClassBundle.java"),
            "i18n/ClassBundle.java:12: jlibs.core.util.i18n.ResourceBundle annotation can be applied only for interface\n" +
                "public abstract class ClassBundle{\n" +
                    "                ^"
        );
    }

    @Test(description="methods in interface must have @Message")
    public void missingMessageAnnotation(){
        assertErrors(compile("/i18n/MissingMessageAnnotationBundle.java"),
            "i18n/MissingMessageAnnotationBundle.java:",
            ": jlibs.core.util.i18n.Message annotation is missing on this method\n" +
                    "    public String lastSucussfullLogin(Date date);\n" +
                    "                  ^"
        );
    }

    @Test(description="method with @Message must return String")
    public void invalidMethodReturn(){
        assertErrors(compile("/i18n/InvalidMethodReturnBundle.java"),
            "i18n/InvalidMethodReturnBundle.java:14: method annotated with jlibs.core.util.i18n.Message must return java.lang.String\n" +
                    "    public Date lastSucussfullLogin(Date date);\n" +
                    "                ^"
        );
    }

    @Test(description="invalid formats in message")
    public void invalidMessageFormat(){
        assertErrors(compile("/i18n/InvalidMessageFormatBundle.java"),
            "Invalid Message Format: unknown format type at"
        );
    }

    @Test(description="number of parameter in message and method should match")
    public void argumentCountMismatch(){
        assertErrors(compile("/i18n/ArgumentCountMismatchBundle.java"),
            "no of args in message format doesn't match with the number of parameters this method accepts"
        );
    }

    @Test(description="all method arguments must be used in message")
    public void missingArgument(){
        assertErrors(compile("/i18n/MissingArgumentBundle.java"),
            "/i18n/MissingArgumentBundle.java:",
            ": {1} is missing in message\n" +
                    "    @Message(\"SQL Execution completed in {0} seconds with {2} errors and {2} warnings\")\n" +
                    "    ^"
        );
    }

    @Test(description="two methods in a java file can't have same key")
    public void duplicateKeyInSameFile(){
        assertErrors(compile("/i18n/DuplicateKeyBundle.java"),
            "key 'JLIBS015' is already used by \"public java.lang.String executionFinished(long, int, int)\" in i18n.DuplicateKeyBundle interface"
        );
    }

    @Test(description="two methods in two interfaces can't have same key")
    public void duplicateKeyAcrossFiles(){
        assertErrors(compile("/i18n/DuplicateKey1Bundle.java", "/i18n/DuplicateKey2Bundle.java"),
            "key 'JLIBS015' is already used by \"public java.lang.String executionFinished(long, int, int)\" in i18n.DuplicateKey1Bundle interface"
        );
    }

    @Test(description="two methods in two interfaces can't have same signature")
    public void methodSignatureClash(){
        assertErrors(compile("/i18n/MethodSignatureClash1Bundle.java", "/i18n/MethodSignatureClash2Bundle.java"),
        "i18n/MethodSignatureClash2Bundle.java:12: clashes with similar method in i18n.MethodSignatureClash1Bundle interface\n" +
        "    public String executing(String query);"
        );
    }

    @Test(description="test generated interface implementation methods")
    public void testImplementation(){
        Assert.assertEquals(BUNDLE1.executing("myquery"), "executing myquery");
        Assert.assertEquals(BUNDLE2.executed("myquery"), "executed myquery");
    }

    @Test(description="test whether custom key is used")
    public void testCustomKey(){
        Assert.assertEquals(BUNDLE1.executionTook(10), "execution took 10 seconds");
        Assert.assertEquals(I18N.getValue(Bundle1.class, "timeTaken", 10), "execution took 10 seconds");
    }

    @Test(description="test that documentation is generated in properties file")
    public void testDocumentation() throws IOException{
        String props = IOUtil.pump(Bundle1.class.getResourceAsStream("Bundle.properties"), true).toString();
        if(props.indexOf("# {0} query\nexecuted=executed {0}")==-1)
            Assert.fail("documentation must be generated for method with no javadoc");
        if(props.indexOf("# {0} time ==> time taken in seconds\ntimeTaken=execution took {0, number} seconds")==-1)
            Assert.fail("documentation must be generated for method with javadoc");
        if(props.indexOf("# {0} query\nexecuting=executing {0}")==-1)
            Assert.fail("documentation must be generated for method with javadoc with no param description");
    }
}
