package jlibs.xml.xsd.display;

import jlibs.core.graph.Filter;
import jlibs.core.graph.Navigator;
import jlibs.core.graph.Path;
import jlibs.core.graph.visitors.PathReflectionVisitor;
import org.apache.xerces.xs.XSParticle;

/**
 * @author Santhosh Kumar T
 */
public class XSPathDiplayFilter extends PathReflectionVisitor<Object, Boolean> implements Filter<Path>{
    private Navigator navigator;

    public XSPathDiplayFilter(Navigator navigator){
        this.navigator = navigator;
    }

    @Override
    public boolean select(Path path){
        return visit(path);
    }

    protected Boolean getDefault(Object elem){
        return true;
    }

    @SuppressWarnings({"unchecked"})
    protected boolean process(XSParticle particle){
        return navigator.children(particle).length()!=1;
    }
}
