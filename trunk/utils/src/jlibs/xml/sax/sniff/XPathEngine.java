package jlibs.xml.sax.sniff;

import jlibs.core.lang.ImpossibleException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class XPathEngine{
    public abstract String getName();
    public abstract List<Object> evaluate(TestCase testCase, Document doc) throws Exception;

    @SuppressWarnings({"unchecked"})
    public List<NodeItem> translate(Object result, NamespaceContext nsContext){
        List<NodeItem> nodeList = new ArrayList<NodeItem>();

        if(result instanceof NodeList){
            NodeList nodeSet = (NodeList)result;
            for(int i=0; i<nodeSet.getLength(); i++){
                Node node = nodeSet.item(i);
                NodeItem item = new NodeItem(node, nsContext);
                nodeList.add(item);
            }
        }else{
            if(result instanceof List){
                for(Object obj: (Collection)result){
                    NodeItem item;
                    if(obj instanceof Node)
                        item = new NodeItem((Node)obj, nsContext);
                    else if(obj instanceof net.sf.saxon.om.NodeInfo){
                        net.sf.saxon.om.NodeInfo info = (net.sf.saxon.om.NodeInfo)obj;
                        Node node = (Node)((net.sf.saxon.dom.NodeWrapper)info.getParent()).getUnderlyingNode();
                        item = new NodeItem(node, info.getLocalPart(), info.getStringValue(), nsContext);
                    }else
                        throw new ImpossibleException();
                    nodeList.add(item);
                }
            }
        }

        return nodeList;
    }
}
