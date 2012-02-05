package jlibs.wadl;

import jlibs.core.io.IOPump;
import jlibs.core.net.URLUtil;
import jlibs.core.util.RandomUtil;
import jlibs.wadl.model.*;
import jlibs.wadl.runtime.Path;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.xsd.XSInstance;
import jlibs.xml.xsd.XSParser;
import org.apache.xerces.xs.XSModel;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Santhosh Kumar T
 */
public class Command{
    private WADLTerminal terminal;
    private Editor editor = new Editor();

    public Command(WADLTerminal terminal){
        this.terminal = terminal;
    }

    public boolean run(String command) throws Exception{
        List<String> args = getArguments(command);

        String arg1 = args.get(0);
        if(arg1.equals("import"))
            importWADL(args.get(1));
        else if(arg1.equals("cd")){
            Path path = terminal.getCurrentPath();
            if(args.size()==1)
                path = path.getRoot();
            else
                path = path.get(args.get(1));
            terminal.setCurrentPath(path);
        }else if(arg1.equals("set")){
            for(String arg: args){
                int equals = arg.indexOf('=');
                if(equals!=-1){
                    String var = arg.substring(0, equals);
                    String value = arg.substring(equals+1);
                    terminal.getVariables().put(var, value);
                }
            }
        }else if(arg1.equals("target")){
            if(args.size()==1)
                terminal.setTarget(null);
            else
                terminal.setTarget(args.get(1));
        }else if(arg1.equals("server")){
            server(args.get(1));
        }else
            send(args);
        return true;
    }
    
    private List<String> getArguments(String command){
        List<String> args = new ArrayList<String>();
        StringTokenizer stok = new StringTokenizer(command, " ");
        while(stok.hasMoreTokens())
            args.add(stok.nextToken());
        return args;
    }
    
    private void importWADL(String systemID) throws Exception{
        JAXBContext jc = JAXBContext.newInstance(Application.class.getPackage().getName());
        Application application = (Application)jc.createUnmarshaller().unmarshal(URLUtil.toURL(systemID));

        XSModel schema = null;
        if(application.getGrammars()!=null){
            List<String> includes = new ArrayList<String>();
            for(Include include: application.getGrammars().getInclude()){
                if(include.getHref()!=null)
                    includes.add(URLUtil.resolve(systemID, include.getHref()).toString());
            }
            if(!includes.isEmpty())
                schema = new XSParser().parse(includes.toArray(new String[includes.size()]));
        }
        
        Path root = null;
        for(Resources resources: application.getResources()){
            URI base = URI.create(resources.getBase());
            String url = base.getScheme()+"://"+base.getHost();
            if(base.getPort()!=-1)
                url += ":"+base.getPort();
            root = null;
            for(Path path: terminal.getRoots()){
                if(path.name.equals(url)){
                    root = path;
                    break;
                }
            }
            if(root==null){
                root = new Path(null, url, false);
                terminal.getRoots().add(root);
                if(base.getPath()!=null && !base.getPath().isEmpty())
                    root = root.add(base.getPath());
            }
            root.schema = schema;
            for(Resource resource: resources.getResource()){
                Path child = root.add(resource.getPath());
                if(child.resource==null)
                    child.resource = resource;
                else
                    child.resource.getMethodOrResource().addAll(resource.getMethodOrResource());
            }
        }
        terminal.setCurrentPath(root);
    }
    
    private void server(String server){
        for(Path root: terminal.getRoots()){
            if(root.name.equalsIgnoreCase(server)){
                terminal.setCurrentPath(root);
                terminal.setTarget(null);
                return;
            }
        }
    }
    
    private void send(List<String> args) throws Exception{
        Path path = terminal.getCurrentPath();
        if(path.resource==null){
            System.err.println("resource not found");
            return;
        }

        Method method = null;
        for(Object obj: path.resource.getMethodOrResource()){
            if(obj instanceof Method){
                Method m = (Method)obj;
                if(m.getName().equalsIgnoreCase(args.get(0))){
                    method = m;
                    break;
                }
            }
        }
        if(method==null){
            System.err.println("unsupported method: "+args.get(0));
            return;
        }

        StringBuilder buff = new StringBuilder();
        Deque<Path> stack = terminal.getCurrentPath().getStack();
        boolean first = true;
        while(!stack.isEmpty()){
            if(first){
                first = false;
                if(terminal.getTarget()!=null){
                    stack.pop();
                    buff.append(terminal.getTarget());
                    continue;
                }
            }else
                buff.append('/');
            path = stack.pop();
            if(path.variable()==null)
                buff.append(path.name);
            else{
                String value = terminal.getVariables().get(path.variable());
                if(value==null){
                    System.err.println("unresolved variable: "+path.variable());
                    return;
                }
                buff.append(value);
            }
        }
        URL url = new URL(buff.toString());
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod(method.getName());

        String payload = null;
        Request request = method.getRequest();
        if(request!=null){
            if(!request.getRepresentation().isEmpty()){
                Representation rep = request.getRepresentation().get(RandomUtil.random(0, request.getRepresentation().size()-1));
                if(rep.getMediaType()!=null)
                    con.addRequestProperty("Content-Type", rep.getMediaType());
                if(rep.getElement()!=null){
                    XSInstance xsInstance = new XSInstance();
                    StringWriter writer = new StringWriter();
                    XMLDocument xml = new XMLDocument(new StreamResult(writer), true, 4, null);
                    xsInstance.generate(terminal.getCurrentPath().getSchema(), rep.getElement(), xml);
                    payload = writer.toString();
                }
            }
        }

        if(payload!=null)
            editor.show(payload, "text/xml");

        String command = "tty vi "+payload;
        final Process process = Runtime.getRuntime().exec(command);
        new Thread(new IOPump(process.getInputStream(), System.out, false, false).asRunnable()).start();
        OutputStream dummy = new OutputStream(){
            @Override
            public void write(int b) throws IOException{}

            @Override
            public void write(byte[] b) throws IOException{}

            @Override
            public void write(byte[] b, int off, int len) throws IOException{}
        };
        new Thread(new IOPump(process.getErrorStream(), dummy, false, false).asRunnable()).start();
        OutputStream out = new OutputStream(){
            private OutputStream delegate = process.getOutputStream();
            @Override
            public void write(int b) throws IOException{
                delegate.write(b);
                delegate.flush();
            }

            @Override
            public void write(byte[] b) throws IOException{
                delegate.write(b);
                delegate.flush();
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException{
                delegate.write(b, off, len);
                delegate.flush();
            }
        };
        new Thread(new IOPump(System.in, out, false, false).asRunnable()).start();
        process.waitFor();

        if(payload!=null)
            con.setDoOutput(true);
        con.connect();
        con.getOutputStream().write(payload.getBytes());

        System.out.println(con.getResponseCode()+" "+con.getResponseMessage());
        System.out.println();

        InputStream in = con.getErrorStream();
        if(in==null)
            in = con.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while((line=reader.readLine())!=null)
            System.out.println(line);
    }
}
