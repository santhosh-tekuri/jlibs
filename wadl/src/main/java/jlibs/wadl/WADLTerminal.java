package jlibs.wadl;

import jlibs.wadl.model.Application;
import jlibs.wadl.model.Resource;
import jlibs.wadl.model.Resources;
import jlibs.wadl.runtime.Path;
import jline.CandidateListCompletionHandler;
import jline.ConsoleReader;

import javax.xml.bind.JAXBContext;
import java.io.FileInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class WADLTerminal{
    public static void main(String[] args) throws Exception{
        String file = "/Users/santhosh/Desktop/enterprise-gateway-wadl.xml";
        JAXBContext jc = JAXBContext.newInstance(Application.class.getPackage().getName());
        Application app = (Application)jc.createUnmarshaller().unmarshal(new FileInputStream(file));
        
        List<Path> roots = new ArrayList<Path>();
        for(Resources resources: app.getResources()){
            URI base = URI.create(resources.getBase());
            String url = base.getScheme()+"://"+base.getHost();
            if(base.getPort()!=-1)
                url += ':'+base.getPort();
            Path root = null;
            for(Path path: roots){
                if(path.name.equals(url)){
                    root = path;
                    break;
                }
            }
            if(root==null){
                root = new Path(null, url, false);
                roots.add(root);
                if(base.getPath()!=null && !base.getPath().isEmpty())
                    root.add(base.getPath());
            }
            for(Resource resource: resources.getResource())
                root.add(resource.getPath()).resource = resource;
        }
        
        for(Path root: roots)
            print(root);

        ConsoleReader console = new ConsoleReader();
        WADLCompletor completor = new WADLCompletor(roots);
        console.addCompletor(completor);

        CandidateListCompletionHandler completionHandler = new CandidateListCompletionHandler();
        console.setCompletionHandler(completionHandler);

        String line;
        while((line=console.readLine("[WADL] "))!=null){
            line = line.trim();
            if(line.length()>0){
                if(line.equals("exit") || line.equals("quit"))
                    return;
            }
        }
    }
    
    private static void print(Path path){
        if(path.resource!=null)
            System.out.println(path);
        for(Path child: path.children)
            print(child);
    }
}
