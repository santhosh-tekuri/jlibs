package jlibs.xml.xsd;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XSImplementationImpl;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.XSModelImpl;
import org.apache.xerces.impl.xs.util.StringListImpl;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.dom.DOMXSImplementationSourceImpl;
import org.apache.xerces.dom.DOMInputImpl;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.LSResourceResolver;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import jlibs.core.lang.ImpossibleException;

/**
 * @author Santhosh Kumar T
 */
public class XSParser{
    private XSLoader xsLoader;

    public XSParser(){
        this(null, null);
    }

    public XSParser(@Nullable LSResourceResolver entityResolver, @Nullable DOMErrorHandler errorHandler){
        System.setProperty(DOMImplementationRegistry.PROPERTY, DOMXSImplementationSourceImpl.class.getName());
        DOMImplementationRegistry registry;
        try{
            registry = DOMImplementationRegistry.newInstance();
        }catch(Exception ex){
            throw new ImpossibleException(ex);
        }
        XSImplementationImpl xsImpl = (XSImplementationImpl)registry.getDOMImplementation("XS-Loader");

        xsLoader = xsImpl.createXSLoader(null);
        DOMConfiguration config = xsLoader.getConfig();
        config.setParameter(Constants.DOM_VALIDATE, Boolean.TRUE);

        if(entityResolver!=null)
            config.setParameter(Constants.DOM_RESOURCE_RESOLVER, entityResolver);

        if(errorHandler!=null)
            config.setParameter(Constants.DOM_ERROR_HANDLER, errorHandler);
    }

    @Nullable
    public XSModel parse(@NotNull String uri){
        return xsLoader.loadURI(uri);
    }

    @Nullable
    public XSModel parse(@NotNull String... uris){
        return xsLoader.loadURIList(new StringListImpl(uris, uris.length));
    }

    /**
     * Parse an XML Schema document from String specified
     * 
     * @param schema    String data to parse. If provided, this will always be treated as a
     *                  sequence of 16-bit units (UTF-16 encoded characters). If an XML
     *                  declaration is present, the value of the encoding attribute
     *                  will be ignored.
     * @param baseURI   The base URI to be used for resolving relative
     *                  URIs to absolute URIs.
     */
    @Nullable
    public XSModel parseString(@NotNull String schema, @Nullable String baseURI){
        return xsLoader.load(new DOMInputImpl(null, null, baseURI, schema, null));
    }

    @NotNull
    public static XSModel getBuiltInSchema(){
        return new XSModelImpl(new SchemaGrammar[0]);
    }
}
