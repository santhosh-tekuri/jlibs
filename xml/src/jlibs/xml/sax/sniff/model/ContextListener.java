package jlibs.xml.sax.sniff.model;

import jlibs.xml.sax.sniff.engine.context.Context;
import jlibs.xml.sax.sniff.events.Event;

/**
 * @author Santhosh Kumar T
 */
public interface ContextListener{
    public void contextStarted(Context context, Event event);
    public void contextEnded(Context context);
    public int priority();
}
