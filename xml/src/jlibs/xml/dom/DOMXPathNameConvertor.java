package jlibs.xml.dom;

import jlibs.core.graph.Convertor;
import jlibs.core.lang.StringUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * @author Santhosh Kumar T
 */
public class DOMXPathNameConvertor implements Convertor<Node, String>{
    @Override
    public String convert(Node source){
        switch(source.getNodeType()){
            case Node.DOCUMENT_NODE:
                return "/";
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                return "text()";
            case Node.COMMENT_NODE:
                return "comment()";
            case Node.ELEMENT_NODE:
                String prefix = source.lookupPrefix(source.getNamespaceURI());
                String name = source.getNodeName();
                return StringUtil.isEmpty(prefix) ? name : prefix+':'+name;
            case Node.ATTRIBUTE_NODE:
                return '@'+convert(((Attr)source).getOwnerElement());
            default:
                return null;
        }
    }
}
