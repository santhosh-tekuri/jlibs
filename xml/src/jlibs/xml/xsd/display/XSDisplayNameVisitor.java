package jlibs.xml.xsd.display;

import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.StringUtil;
import jlibs.core.graph.visitors.PathReflectionVisitor;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import jlibs.xml.xsd.XSUtil;
import org.apache.xerces.xs.*;

/**
 * @author Santhosh Kumar T
 */
public class XSDisplayNameVisitor extends PathReflectionVisitor<Object, String>{
    XSPathDiplayFilter filter;
    private MyNamespaceSupport nsSupport;

    public XSDisplayNameVisitor(MyNamespaceSupport nsSupport, XSPathDiplayFilter filter){
        this.nsSupport = nsSupport;
        this.filter = filter;
    }

    @Override
    protected String getDefault(Object elem){
        return elem.toString();
    }

    protected String process(XSNamespaceItem nsItem){
        return nsItem.getSchemaNamespace();
    }

    protected String process(XSObject obj){
        return XSUtil.getQName(obj, nsSupport);
    }

    private String addCardinal(String str){
        if(!filter.select(path.getParentPath())){
            XSParticle particle = (XSParticle)path.getParentPath().getElement();
            return str+process(particle);
        }else
            return str;
    }

    protected String process(XSElementDeclaration elem){
        String str = '<'+process((XSObject)elem)+'>';
        return addCardinal(str);
    }
    
    protected String process(XSAttributeUse attrUse){
        String str = '@' + process((XSObject)attrUse);
        if(!attrUse.getRequired())
            str += '?';
        return str;
    }

    protected String process(XSParticle particle){
        if(particle.getMinOccurs()==0 && particle.getMaxOccursUnbounded())
            return "*";
        else if(particle.getMinOccurs()==1 && particle.getMaxOccursUnbounded())
            return "+";
        else if(particle.getMaxOccursUnbounded())
            return particle.getMinOccurs()+"+";
        else if(particle.getMinOccurs()==0 && particle.getMaxOccurs()==1)
            return "?";
        else
            return "["+particle.getMinOccurs()+","+particle.getMaxOccurs()+"]";
    }

    protected String process(XSModelGroup modelGroup){
        String str;
        switch(modelGroup.getCompositor()){
            case XSModelGroup.COMPOSITOR_ALL :
                str = "[ALL]";
                break;
            case XSModelGroup.COMPOSITOR_CHOICE :
                str =  "[OR]";
                break;
            case XSModelGroup.COMPOSITOR_SEQUENCE :
                str = "[SEQUENCE]";
                break;
            default:
                throw new ImpossibleException("Invalid Compositor: "+modelGroup.getCompositor());
        }
        return addCardinal(str);
    }

    protected String process(XSWildcard wildcard){
        String str;
        switch(wildcard.getConstraintType()){
            case XSWildcard.NSCONSTRAINT_ANY :
                str = "<*:*>";
                break;
            case XSWildcard.NSCONSTRAINT_LIST:
                StringBuilder buff = new StringBuilder();
                StringList list = wildcard.getNsConstraintList();
                for(int i=0; i<list.getLength(); i++){
                    if(buff.length()>0)
                        buff.append('|');
                    buff.append(nsSupport.findPrefix(list.item(i)));
                }
                if(buff.length()==0)
                    str = "<*>";
                else
                    str = "<"+buff+":*>";
                break;
            case XSWildcard.NSCONSTRAINT_NOT:
                buff = new StringBuilder();
                list = wildcard.getNsConstraintList();
                for(int i=0; i<list.getLength(); i++){
                    String prefix = nsSupport.findPrefix(list.item(i));
                    if(!StringUtil.isEmpty(prefix)){
                        if(buff.length()>0)
                            buff.append(',');
                        buff.append(prefix);
                    }
                }
                if(buff.toString().indexOf(",")==-1)
                    str = "<!"+buff+":*>";
                else
                    str = "<!("+buff+"):*>";
                break;
            default:
                throw new ImpossibleException("Invalid Constraint: "+wildcard.getConstraintType());
        }
        return addCardinal(str);
    }
}
