package jlibs.xml.sax.sniff.model;

import jlibs.xml.sax.sniff.events.Event;

/**
 * @author Santhosh Kumar T
 */
public interface ContextListener{
    public void contextStarted(Event event);
    public void contextEnded();
    public int priority();
}
