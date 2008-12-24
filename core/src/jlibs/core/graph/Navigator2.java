package jlibs.core.graph;

import java.util.regex.Pattern;

/**
 * @author Santhosh Kumar T
 */
public abstract class Navigator2<E> extends Ladder<E> implements Navigator<E>{
    public String getRelativePath(E fromElem, E toElem, final Convertor<E, String> convertor, String separator, boolean predicates){
        if(predicates){
            Convertor<E, String> predicateConvertor = new Convertor<E, String>(){
                @Override
                public String convert(E source){
                    String name = convertor.convert(parent(source));
                    Sequence<? extends E> children = children(source);
                    int predicate = 1;
                    while(children.hasNext()){
                        E child = children.next();
                        if(child==source)
                            break;
                        if(name.equals(convertor.convert(child)))
                            predicate++;
                    }
                    if(predicate>1)
                        name += '['+predicate+']';
                    return name;
                }
            };
            return super.getRelativePath(fromElem, toElem, predicateConvertor, separator);
        }else
            return super.getRelativePath(fromElem, toElem, convertor, separator);
    }

    public E resolve(E node, String path, Convertor<E, String> convertor, String separator){
        if(path.equals("."))
            return node;

        String tokens[] = Pattern.compile(separator, Pattern.LITERAL).split(path);
        for(String token: tokens){
            if(token.equals("..")){
                node = parent(node);
                continue;
            }
            int predicate = 1;
            int openBrace = token.lastIndexOf('[');
            if(openBrace!=-1){
                predicate = Integer.parseInt(token.substring(openBrace+1, token.length()-1));
                token = token.substring(0, openBrace);
            }

            Sequence<? extends E> children = children(node);
            while(children.hasNext()){
                E child = children.next();
                if(token.equals(convertor.convert(child))){
                    if(predicate==1){
                        node = child;
                        break;
                    }else
                        predicate--;
                }
            }
        }
        return null;
    }
}
