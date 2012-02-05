package jlibs.wadl;

import jlibs.core.io.FileUtil;
import jlibs.core.io.IOUtil;
import jlibs.core.lang.JavaProcessBuilder;
import jlibs.core.net.URLUtil;
import jlibs.core.util.RandomUtil;
import jlibs.wadl.model.*;
import jlibs.wadl.runtime.Path;
import jlibs.xml.sax.AnsiHandler;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.xsd.XSInstance;
import jlibs.xml.xsd.XSParser;
import org.apache.xerces.xs.XSModel;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Santhosh Kumar T
 */
public class Command{
    private WADLTerminal terminal;

    public Command(WADLTerminal terminal){
        this.terminal = terminal;
    }

    public boolean run(String command) throws Exception{
        List<String> args = getArguments(command);

        String arg1 = args.get(0);
        if(arg1.equals("import"))
            importWADL(args.get(1));
        else if(arg1.equals("cd"))
            cd(args.size()==1 ? null : args.get(1));
        else if(arg1.equals("set")){
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
    
    private void cd(String pathString){
        Path path = terminal.getCurrentPath();
        if(pathString==null)
            path = path.getRoot();
        else{
            StringTokenizer stok = new StringTokenizer(pathString, "/");
            while(stok.hasMoreTokens()){
                String token = stok.nextToken();
                if(token.equals(".."))
                    path = path.parent;
                else{
                    for(Path child: path.children){
                        String variable = child.variable();
                        if(variable!=null){
                            terminal.getVariables().put(variable, token);
                            path = child;
                            break;
                        }else if(child.name.equals(token)){
                            path = child;
                            break;
                        }
                    }
                }
                if(path==null)
                    return;
            }
        }
        terminal.setCurrentPath(path);
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
            for(Resource resource: resources.getResource())
                importResource(resource, root);
        }
        terminal.setCurrentPath(root);
    }
    
    private void importResource(Resource resource, Path path){
        path = path.add(resource.getPath());
        if(path.resource==null)
            path.resource = resource;
        else
            path.resource.getMethodOrResource().addAll(resource.getMethodOrResource());
        for(Object obj: resource.getMethodOrResource()){
            if(obj instanceof Resource)
                importResource((Resource)obj, path);
        }
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
        if(args.size()>1)
            path = path.get(args.get(1));
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

        String url = terminal.getURL();
        if(args.size()>1)
            url = url+"/"+args.get(1);

        HttpURLConnection con;

        File payload = null;
        Request request = method.getRequest();
        if(request!=null){
            StringBuilder queryString = new StringBuilder();
            for(Param param: request.getParam()){
                if(param.getStyle()==ParamStyle.QUERY){
                    if(queryString.length()>0)
                        queryString.append('&');
                    queryString.append(param.getName());
                    queryString.append('=');
                    String value = param.getFixed(); //todo
                    queryString.append(value);
                }
            }
            if(queryString.length()>0)
                url += "?"+queryString;
        }
        con = (HttpURLConnection)new URL(url).openConnection();
        if(request!=null){
            if(!request.getRepresentation().isEmpty()){
                Representation rep = request.getRepresentation().get(RandomUtil.random(0, request.getRepresentation().size()-1));
                if(rep.getMediaType()!=null)
                    con.addRequestProperty("Content-Type", rep.getMediaType());
                if(rep.getElement()!=null){
                    XSInstance xsInstance = new XSInstance();
                    payload = new File("temp.xml");
                    XMLDocument xml = new XMLDocument(new StreamResult(payload), true, 4, null);
                    xsInstance.generate(path.getSchema(), rep.getElement(), xml);
                }
            }
        }
        con.setRequestMethod(method.getName());

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
                return;
        }

        if(payload!=null)
            con.setDoOutput(true);
        con.connect();
        if(payload!=null)
            IOUtil.pump(new FileInputStream(payload), con.getOutputStream(), true, false);

        System.out.println(con.getResponseCode()+" "+con.getResponseMessage());
        System.out.println();

        InputStream in = con.getErrorStream();
        if(in==null)
            in = con.getInputStream();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOUtil.pump(in, bout, true, true);
        if (bout.size() == 0)
            return;
        if(isXML(con.getContentType())){
            PrintStream sysErr = System.err;
            System.setErr(new PrintStream(new ByteArrayOutputStream()));
            try {
                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer();
                transformer.transform(new StreamSource(new ByteArrayInputStream(bout.toByteArray())), new SAXResult(new AnsiHandler()));
                transformer.reset();
                return;
            } catch (Exception ex) {
                // ignore
            } finally {
                System.setErr(sysErr);
            }
        }
        System.out.println(bout);
        System.out.println();
    }

    public static boolean isXML(String contentType) {
        if(contentType==null)
            return false;
        int semicolon = contentType.indexOf(';');
        if(semicolon!=-1)
            contentType = contentType.substring(0, semicolon);
        if("text/xml".equalsIgnoreCase(contentType))
            return true;
        else if(contentType.startsWith("application/"))
            return contentType.endsWith("application/xml") || contentType.endsWith("+xml");
        else 
            return false;
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
