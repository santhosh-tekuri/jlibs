package jlibs.wadl;

import jlibs.wadl.runtime.Path;
import jline.Completor;

import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class WADLCompletor implements Completor{
    private WADLTerminal terminal;
    public WADLCompletor(WADLTerminal terminal){
        this.terminal = terminal;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int complete(String buffer, int cursor, List candidates){
        Path path = terminal.getCurrentPath();

        int from = 0;
        int to = findArgument(buffer, from, ' ');
        String arg = buffer.substring(from, Math.min(to, cursor));
        if(arg.isEmpty() || cursor<=to){
            fillCandidates(candidates, arg, Collections.singletonList("cd"));
            return from;
        }else{
            return completePath(buffer, cursor, candidates, path, to);
        }
    }
    
    private int findArgument(String buffer, int from, char separator){
        for(; from<buffer.length(); from++){
            if(buffer.charAt(from)==separator)
                return from;
        }
        return from;
    }
    
    private void fillCandidates(List<String> candidates, String token, List<String> args){
        for(String arg: args){
            if(arg.startsWith(token))
                candidates.add(arg+' ');
        }
    }
    
    private void fillPathCandidates(List<String> candidates, String token, Path current){
        for(Path child: current.children){
            if(child.name.startsWith(token)){
                if(child.children.isEmpty())
                    candidates.add(child.name+' ');
                else{
                    String candidate = child.name;
                    while(child.resource!=null && child.children.size()==1){
                        child = child.children.get(0);
                        candidate += "/"+child.name;
                    }
                    candidate += child.children.isEmpty() ? ' ' : '/';
                    candidates.add(candidate);
                }
            }
        }
    }

    private int completePath(String buffer, int cursor, List<String>candidates, Path path, int to){
        if(path.children.isEmpty())
            return -1;

        int from = to+1;
        to = findArgument(buffer, from, '/');
        if(to<buffer.length()){
            assert buffer.charAt(to)=='/';
            String token = buffer.substring(from, to);
            Path child = null;
            if(token.equals(".."))
                child = path.parent;
            else{
                for(Path c: path.children){
                    if(c.name.equals(token)){
                        child = c;
                        break;
                    }
                }
            }
            if(child==null)
                return -1;
            return completePath(buffer, cursor, candidates, child, to);
        }
        String arg = buffer.substring(from, Math.min(to, cursor));
        fillPathCandidates(candidates, arg, path);
        return from;
    }
}
