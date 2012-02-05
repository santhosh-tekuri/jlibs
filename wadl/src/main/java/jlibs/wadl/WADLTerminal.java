package jlibs.wadl;

import jlibs.core.lang.Ansi;
import jlibs.wadl.runtime.Path;
import jline.CandidateListCompletionHandler;
import jline.ConsoleReader;

import java.io.IOException;
import java.util.*;

import static jlibs.core.lang.Ansi.Attribute;
import static jlibs.core.lang.Ansi.Color;

/**
 * @author Santhosh Kumar T
 */
public class WADLTerminal{
    private List<Path> roots = new ArrayList<Path>();

    public List<Path> getRoots(){
        return roots;
    }
    
    private Path currentPath;

    public Path getCurrentPath(){
        return currentPath;
    }

    public void setCurrentPath(Path currentPath){
        this.currentPath = currentPath;
    }
    
    private String target;

    public String getTarget(){
        return target;
    }

    public void setTarget(String target){
        this.target = target;
    }

    private LinkedHashMap<String, String> variables = new LinkedHashMap<String, String>();
    public Map<String, String> getVariables(){
        return variables;
    }

    private static Ansi PLAIN = new Ansi(Attribute.BRIGHT, Color.WHITE, Color.BLUE);
    private static Ansi VARIABLE = new Ansi(Attribute.BRIGHT, Color.GREEN, Color.BLUE);
    public String getPrompt(){
        if(currentPath==null)
            return "[?]";
        else{
            StringBuilder buff = new StringBuilder();
            buff.append(PLAIN.colorize("["));

            Deque<Path> stack = currentPath.getStack();
            boolean first = true;
            Path path;
            while(!stack.isEmpty()){
                if(first){
                    first = false;
                    if(target!=null){
                        stack.pop();
                        buff.append(PLAIN.colorize(target));
                        continue;
                    }
                }else
                    buff.append(PLAIN.colorize("/"));
                path = stack.pop();
                if(path.variable()==null)
                    buff.append(PLAIN.colorize(path.name));
                else{
                    String value = variables.get(path.variable());
                    if(value==null)
                        value = path.name;
                    buff.append(VARIABLE.colorize(value));
                }
            }
            buff.append(PLAIN.colorize("]"));
            return buff.toString();
        }
    }

    public void start() throws IOException{
        ConsoleReader console = new ConsoleReader();
        WADLCompletor completor = new WADLCompletor(this);
        console.addCompletor(completor);
        Command command = new Command(this);

        CandidateListCompletionHandler completionHandler = new CandidateListCompletionHandler();
        console.setCompletionHandler(completionHandler);

        try{
            command.run("import /Users/santhosh/Desktop/enterprise-gateway-wadl.xml");
        }catch(Exception ex){
            ex.printStackTrace();
        }

        String line;
        while((line=console.readLine(getPrompt()+" "))!=null){
            line = line.trim();
            if(line.length()>0){
                if(line.equals("exit") || line.equals("quit"))
                    return;
                try{
                    command.run(line);
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception{
        new WADLTerminal().start();
    }
    
    private static void print(Path path){
        if(path.resource!=null)
            System.out.println(path);
        for(Path child: path.children)
            print(child);
    }
}
