package jlibs.xml.sax.sniff.position;

import jlibs.xml.sax.sniff.ContextManager;
import jlibs.xml.sax.sniff.model.Position;

/**
 * @author Santhosh Kumar T
 */
public interface PositionTracker{
    public boolean hit(ContextManager.Context context, Position position);
    public void contextEnded(ContextManager.Context context);
}
