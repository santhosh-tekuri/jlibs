package jlibs.xml.sax.dog.path.tests;

import jlibs.xml.sax.dog.NodeType;
import jlibs.xml.sax.dog.path.Constraint;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public class LocalName extends Constraint{
    public final String localName;

    public LocalName(int id, String localName){
        super(id);
        this.localName = localName;
    }

    @Override
    public boolean matches(Event event){
        switch(event.type()){
            case NodeType.ELEMENT:
            case NodeType.ATTRIBUTE:
                return localName.equals(event.localName());
            default:
                return false;
        }
    }

    @Override
    public String toString(){
        return String.format("{*}%s", localName);
    }
}