/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

package jlibs.examples.xml.sax.dog;

import jlibs.core.lang.OS;
import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;
import jlibs.xml.sax.dog.expr.EvaluationListener;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.InstantEvaluationListener;
import jlibs.xml.sax.dog.sniff.DOMBuilder;
import jlibs.xml.sax.dog.sniff.Event;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class XMLDogTest{
    public static void main(String[] args) throws Exception{
        boolean useSTAX = false;
        boolean createDOM = false;
        boolean instantResults = false;
        final boolean printResults;

        boolean _printResults = true;
        String file = null;
        for(String arg: args){
            if("-dom".equalsIgnoreCase(arg))
                createDOM = true;
            if("-instantResults".equalsIgnoreCase(arg))
                instantResults = true;
            if("-dontPrintResults".equalsIgnoreCase(arg))
                _printResults = false;
            if("-useSTAX".equalsIgnoreCase(arg))
                useSTAX = true;
            else
                file = arg;
        }
        printResults = _printResults;

        if(file==null){
            System.err.println("usage: xmldog."+(OS.get().isWindows()?"bat":"sh")+" [-dom] [-instantResults] [-dontPrintResults] [-useSTAX] <xml-file>");
            System.exit(1);
        }

        System.out.println("Namespaces:");

        final DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
        SAXUtil.newSAXParser(true, false, false).parse(new InputSource(file), new DefaultHandler(){
            @Override
            public void startPrefixMapping(String prefix, String uri) throws SAXException{
                if(uri.length()>0 && prefix.length()==0)
                    prefix = "ns";
                if(nsContext.getPrefix(uri)==null){
                    String _uri = nsContext.getNamespaceURI(prefix);
                    if(_uri!=null && _uri.length()>0){
                        int i = 1;
                        String _prefix;
                        if(prefix.length()==0)
                            prefix = "ns";
                        while(true){
                            _prefix = prefix + i;
                            if(nsContext.getNamespaceURI(_prefix)==null){
                                prefix = _prefix;
                                break;
                            }
                            i++;
                        }
                    }
                    nsContext.declarePrefix(prefix, uri);
                    System.out.println(prefix+"\t= "+uri);
                }
            }
        });
        String prefix = nsContext.declarePrefix("*");
        System.out.println(prefix+"\t= *");

        final XMLDog dog = new XMLDog(nsContext, null, null);
        List<Expression> expressions = new ArrayList<Expression>();

        System.out.println();
        System.out.println("XPaths: (press CTRL+"+(OS.get().isUnix()?'D':'Z')+" after all xpaths are specified)");
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while((line=console.readLine())!=null){
            line = line.trim();
            if(line.startsWith("#for-each ")){
                int i = line.indexOf("#eval ");
                String forEach = line.substring("#for-each ".length(), i);
                String xpath = line.substring(i+"#eval ".length());
                expressions.add(dog.addForEach(forEach, xpath));
            }else if(line.length()>0)
                expressions.add(dog.addXPath(line));
            else
                break;
        }

        System.out.println("+----------------------------------------+");
        System.out.println("|              XPath-Results             |");
        System.out.println("+----------------------------------------+");
        System.out.println();
        long time = System.nanoTime();

        Event event = dog.createEvent();
        if(createDOM)
            event.setXMLBuilder(new DOMBuilder());

        EvaluationListener listener;
        if(instantResults){
            listener = new InstantEvaluationListener(){
                int nodeCounts[] = new int[dog.getDocumentXPathsCount()];
                @Override
                public void onNodeHit(Expression expression, NodeItem nodeItem){
                    if(printResults){
                        System.out.print("XPath: "+expression.getXPath()+" Node["+ ++nodeCounts[expression.id]+"]: ");
                        nodeItem.printTo(System.out);
                        System.out.println();
                    }
                }

                @Override
                public void finishedNodeSet(Expression expression){
                    System.out.println("Finished Nodeset: "+expression.getXPath());
                }

                @Override
                public void onResult(Expression expression, Object result){
                    if(printResults){
                        XPathResults.print(System.out, expression.getXPath(), result);
                        System.out.println();
                    }
                }
            };
        }else
            listener = new XPathResults(event);
        event.setListener(listener);
        dog.sniff(event, new InputSource(file), useSTAX);
        time = System.nanoTime() - time;
        if(printResults && listener instanceof XPathResults)
            ((XPathResults)listener).print(expressions, System.out);
        System.err.println("Evaluated in "+(long)(time*1E-06)+" milliseconds");
        if(printResults && instantResults)
            System.err.println("Note: the above duration include the time to print results.");
    }
}
