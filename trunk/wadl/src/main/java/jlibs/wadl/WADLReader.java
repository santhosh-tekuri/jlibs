package jlibs.wadl;

import jlibs.core.net.URLUtil;
import jlibs.wadl.model.*;

import javax.xml.bind.JAXBContext;
import java.util.HashSet;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class WADLReader{
    private Application app;
    private HashSet<Object> inlined = new HashSet<Object>();

    public Application read(String systemID) throws Exception{
        JAXBContext jc = JAXBContext.newInstance(Application.class.getPackage().getName());
        app = (Application)jc.createUnmarshaller().unmarshal(URLUtil.toURL(systemID));
        inline();
        return app;
    }

    private void inline(){
        for(Object item: app.getResourceTypeOrMethodOrRepresentation()){
            if(item instanceof Method)
                inline((Method)item);
        }        
        for(Object item: app.getResourceTypeOrMethodOrRepresentation()){
            if(item instanceof ResourceType)
                inline((ResourceType)item);
        }

        for(Resources resources: app.getResources()){
            for(Resource resource: resources.getResource())
                inline(resource);
        }
    }

    public void inline(List<Representation> representations){
        for(int i=0; i<representations.size(); i++){
            Representation representation = representations.get(i);
            if(representation.getHref()!=null)
                representations.set(i, getRepresentation(representation.getHref()));
        }
    }

    public void inline(Method method){
        if(!inlined.add(method))
            return;
        if(method.getRequest()!=null)
            inline(method.getRequest().getRepresentation());
        for(Response response: method.getResponse())
            inline(response.getRepresentation());
    }

    public void inline(ResourceType rt){
        if(!inlined.add(rt))
            return;

        for(int i=0; i<rt.getMethodOrResource().size(); i++){
            Object item = rt.getMethodOrResource().get(i);
            if(item instanceof Method){
                Method method = (Method)item;
                if(method.getHref()!=null)
                    rt.getMethodOrResource().set(i, getMethod(method.getHref()));
            }else if(item instanceof Resource)
                inline((Resource)item);
        }
    }

    public void inline(Resource resource){
        if(!inlined.add(resource))
            return;

        for(String type: resource.getType()){
            ResourceType rt = getResourceType(type);
            if(rt!=null){
                inline(rt);
                resource.getMethodOrResource().addAll(rt.getMethodOrResource());
            }
        }

        for(int i=0; i<resource.getMethodOrResource().size(); i++){
            Object item = resource.getMethodOrResource().get(i);
            if(item instanceof Method){
                Method method = (Method)item;
                if(method.getHref()!=null)
                    resource.getMethodOrResource().set(i, getMethod(method.getHref()));
            }else if(item instanceof Resource)
                inline((Resource)item);
        }
    }

    private ResourceType getResourceType(String ref){
        ref = ref.substring(1);
        for(Object item: app.getResourceTypeOrMethodOrRepresentation()){
            if(item instanceof ResourceType){
                ResourceType rt = (ResourceType)item;
                if(rt.getId().equals(ref))
                    return rt;
            }
        }
        throw new RuntimeException("cannot find resourceType with id: "+ref);
    }

    private Method getMethod(String ref){
        ref = ref.substring(1);
        for(Object item: app.getResourceTypeOrMethodOrRepresentation()){
            if(item instanceof Method){
                Method method = (Method)item;
                if(method.getId().equals(ref))
                    return method;
            }
        }
        throw new RuntimeException("cannot find method with id: "+ref);
    }

    private Representation getRepresentation(String ref){
        ref = ref.substring(1);
        for(Object item: app.getResourceTypeOrMethodOrRepresentation()){
            if(item instanceof Representation){
                Representation rep = (Representation)item;
                if(rep.getId().equals(ref))
                    return rep;
            }
        }
        throw new RuntimeException("cannot find representation with id: "+ref);
    }
}
