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

package jlibs.wadl.cli.commands;

import jlibs.core.io.FileUtil;
import jlibs.core.io.IOUtil;
import jlibs.core.lang.Ansi;
import jlibs.core.lang.JavaProcessBuilder;
import jlibs.core.util.CollectionUtil;
import jlibs.core.util.RandomUtil;
import jlibs.wadl.cli.Util;
import jlibs.wadl.cli.WADLTerminal;
import jlibs.wadl.cli.model.Path;
import jlibs.wadl.cli.ui.Editor;
import jlibs.wadl.model.Representation;
import jlibs.wadl.model.Request;
import jlibs.xml.sax.AnsiHandler;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.xsd.XSInstance;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Santhosh Kumar T
 */
public class Method extends Command{
    private static final Ansi SUCCESS = new Ansi(Ansi.Attribute.BRIGHT, Ansi.Color.GREEN, Ansi.Color.BLACK);
    private static final Ansi FAILURE = new Ansi(Ansi.Attribute.BRIGHT, Ansi.Color.RED, Ansi.Color.BLACK);

    public Method(WADLTerminal terminal){
        super(terminal);
    }

    @Override
    public boolean run(String cmd, List<String> args) throws Exception{
        args.add(0, cmd);
        return execute(args);
    }

    private boolean execute(List<String> args) throws Exception{
        File responseFile = getFile(args, ">");

        HttpURLConnection con = prepare(args);
        if(con==null)
            return false;

        if(con.getResponseCode()==401){ // Unauthorized
            if(authenticate(con))
                return execute(args);
            else
                return false;
        }
        Ansi result = con.getResponseCode()/100==2 ? SUCCESS : FAILURE;
        result.outln(con.getResponseCode()+" "+con.getResponseMessage());
        System.out.println();

        boolean success = true;
        InputStream in = con.getErrorStream();
        if(in==null)
            in = con.getInputStream();
        else
            success = false;

        PushbackInputStream pin = new PushbackInputStream(in);
        int data = pin.read();
        if(data==-1){
            if(responseFile!=null)
                responseFile.delete();
            return success;
        }
        pin.unread(data);
        if(success && responseFile!=null){
            IOUtil.pump(pin, new FileOutputStream(responseFile), true, true);
            return true;
        }

        String contentType = con.getContentType();
        if(Util.isXML(contentType)){
            PrintStream sysErr = System.err;
            System.setErr(new PrintStream(new ByteArrayOutputStream()));
            try {
                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer();
                transformer.transform(new StreamSource(pin), new SAXResult(new AnsiHandler()));
                transformer.reset();
                return success;
            } catch (Exception ex) {
                sysErr.println("response is not valid xml: "+ex.getMessage());
                return false;
            } finally {
                System.setErr(sysErr);
            }
        }
        if(Util.isPlain(contentType) || Util.isJSON(contentType) || Util.isHTML(contentType)){
            IOUtil.pump(pin, System.out, true, false);
            System.out.println();
        }else{
            File temp = File.createTempFile("attachment", "."+Util.getExtension(contentType), FileUtil.USER_DIR);
            IOUtil.pump(pin, new FileOutputStream(temp), true, true);
            System.out.println("response saved to "+temp.getAbsolutePath());
        }
        return success;
    }

    private File getFile(List<String>args, String argPrefix){
        File file = null;
        Iterator<String> iter = args.iterator();
        while(iter.hasNext()){
            String arg = iter.next();
            if(arg.startsWith(argPrefix)){
                iter.remove();
                file = Util.toFile(arg.substring(1));
            }
        }
        return file;
    }
    
    private HttpURLConnection prepare(List<String> args) throws Exception{
        String methodName = args.remove(0);

        File payload = getFile(args, "<");
        Path path = terminal.getCurrentPath();
        if(!args.isEmpty()){
            String pathString = args.get(0);
            if(pathString.indexOf('=')==-1 && pathString.indexOf(':')==-1
                    && !pathString.startsWith(">") && !pathString.startsWith("<")){
                path = path.get(args.remove(0));
            }
        }
        if(path==null || path.resource==null){
            System.err.println("resource not found");
            return null;
        }

        jlibs.wadl.model.Method method = path.findMethod(methodName);
        if(method==null){
            System.err.println("unsupported method: "+methodName);
            return null;
        }

        Request request = method.getRequest();

        if(payload==null && request!=null){
            if(!request.getRepresentation().isEmpty()){
                Representation rep = request.getRepresentation().get(RandomUtil.random(0, request.getRepresentation().size() - 1));
                if(rep.getElement()!=null){
                    payload = FILE_PAYLOAD;
                    generatePayload(path, rep.getElement());
                }
            }
            if(payload!=null){
                JavaProcessBuilder processBuilder = new JavaProcessBuilder();
                StringTokenizer stok = new StringTokenizer(System.getProperty("java.class.path"), FileUtil.PATH_SEPARATOR);
                while(stok.hasMoreTokens())
                    processBuilder.classpath(stok.nextToken());
                processBuilder.mainClass(Editor.class.getName());
                processBuilder.arg(payload.getAbsolutePath());
                processBuilder.arg("text/xml");
                processBuilder.launch(DUMMY_OUTPUT, DUMMY_OUTPUT).waitFor();
                if(!payload.exists())
                    return null;
            }
        }

        return path.execute(method, args, payload);
    }

    private static final File FILE_PAYLOAD = new File("temp.xml");
    private void generatePayload(Path path, QName element) throws Exception{
        if(path.variable()!=null){
            for(Object item: path.resource.getMethodOrResource()){
                if(item instanceof jlibs.wadl.model.Method){
                    jlibs.wadl.model.Method method = (jlibs.wadl.model.Method)item;
                    if(method.getName().equalsIgnoreCase("GET")){
                        try{
                            HttpURLConnection con = path.execute(method, Collections.<String>emptyList(), null);
                            if(con.getResponseCode()==200){
                                IOUtil.pump(con.getInputStream(), new FileOutputStream(FILE_PAYLOAD), true, true);
                                return;
                            }
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }

        XSInstance xsInstance = new XSInstance();
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("xsd-instance.properties");
        if(is!=null)
            xsInstance.loadOptions(CollectionUtil.readProperties(is, null));
        XMLDocument xml = new XMLDocument(new StreamResult(FILE_PAYLOAD), true, 4, null);
        xsInstance.generate(path.getSchema(), element, xml);
    }

    public boolean authenticate(HttpURLConnection con) throws IOException{
        String value = con.getHeaderField("WWW-Authenticate");
        if(value==null)
            return false;
        int space = value.indexOf(' ');
        if(space==-1)
            return false;
        if(!Authenticate.authenticate(terminal, value.substring(0, space), Collections.<String>emptyList()))
            return false;
        return true;
    }

    private static final OutputStream DUMMY_OUTPUT = new OutputStream(){
        @Override
        public void write(int b) throws IOException{}
        @Override
        public void write(byte[] b) throws IOException{}
        @Override
        public void write(byte[] b, int off, int len) throws IOException{}
    };
}
