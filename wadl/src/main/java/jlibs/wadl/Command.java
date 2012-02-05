package jlibs.wadl;

import jlibs.core.net.URLUtil;
import jlibs.wadl.model.Application;
import jlibs.wadl.model.Resource;
import jlibs.wadl.model.Resources;
import jlibs.wadl.runtime.Path;

import javax.xml.bind.JAXBContext;
import java.net.URI;
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
        }
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
}
