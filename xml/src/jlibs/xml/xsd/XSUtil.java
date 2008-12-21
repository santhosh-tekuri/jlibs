package jlibs.xml.xsd;

import jlibs.xml.sax.helpers.MyNamespaceSupport;
import jlibs.core.lang.StringUtil;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSAttributeUse;

/**
 * @author Santhosh Kumar T
 */
public class XSUtil{
    public static MyNamespaceSupport createNamespaceSupport(XSModel model){
        MyNamespaceSupport nsSupport = new MyNamespaceSupport();
        StringList list = model.getNamespaces();
        for(int i=0; i<list.getLength(); i++){
            if(list.item(i)!=null) // default namespace is null
                nsSupport.declarePrefix(list.item(i));
        }
        return nsSupport;
    }

    public static String getQName(XSObject obj, MyNamespaceSupport nsSupport){
        if(obj instanceof XSAttributeUse)
            obj = ((XSAttributeUse)obj).getAttrDeclaration();
        
        if(obj.getName()==null)
            return "";
        String ns = obj.getNamespace();
        String prefix = nsSupport.findPrefix(ns==null ? "" : ns);
        return StringUtil.isEmpty(prefix) ? obj.getName() : prefix+':'+obj.getName();
    }
}
