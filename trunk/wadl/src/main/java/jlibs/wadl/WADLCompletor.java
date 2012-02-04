package jlibs.wadl;

import jlibs.wadl.runtime.Path;
import jline.Completor;

import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class WADLCompletor implements Completor{
    private List<Path> roots;
    public WADLCompletor(List<Path> roots){
        this.roots = roots;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int complete(String buffer, int cursor, List candidates){
        Path path = null;
        if(roots.size()==1)
            path = roots.get(0);

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
    
    private void fillPathCandidates(List<String> candidates, String token, List<Path> args){
        for(Path arg: args){
            if(arg.name.startsWith(token)){
                if(arg.children.isEmpty())
                    candidates.add(arg.name+' ');
                else
                    candidates.add(arg.name+'/');
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
            for(Path c: path.children){
                if(c.name.equals(token)){
                    child = c;
                    break;
                }
            }
            assert child!=null;
            return completePath(buffer, cursor, candidates, child, to);
        }
        String arg = buffer.substring(from, Math.min(to, cursor));
        fillPathCandidates(candidates, arg, path.children);
        return from;
    }
}
