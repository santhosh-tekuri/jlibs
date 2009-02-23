package jlibs.xml.sax.sniff;

import org.w3c.dom.Node;

/**
 * @author Santhosh Kumar T
 */
public interface NodeTypes{
    public static final int ANY = -1;
    public static final int NAMESPACE = 13;
    public static final int DOCUMENT = Node.DOCUMENT_NODE;
    public static final int ELEMENT = Node.ELEMENT_NODE;
    public static final int TEXT = Node.TEXT_NODE;
    public static final int ATTRIBUTE = Node.ATTRIBUTE_NODE;
    public static final int COMMENT = Node.COMMENT_NODE;
    public static final int PI = Node.PROCESSING_INSTRUCTION_NODE;
}
