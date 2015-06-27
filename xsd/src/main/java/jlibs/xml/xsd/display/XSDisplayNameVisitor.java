/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.xml.xsd.display;

import jlibs.core.graph.visitors.PathReflectionVisitor;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.StringUtil;
import jlibs.xml.XMLUtil;
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
        return StringUtil.toString(elem);
    }

    protected String process(XSNamespaceItem nsItem){
        return StringUtil.toString(nsItem.getSchemaNamespace());
    }

    protected String process(XSObject obj){
        return XMLUtil.getQName(XSUtil.getQName(obj, nsSupport));
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
                str = "*:*";
                break;
            case XSWildcard.NSCONSTRAINT_LIST:
                StringBuilder buff = new StringBuilder();
                StringList list = wildcard.getNsConstraintList();
                for(int i=0; i<list.getLength(); i++){
                    String item = list.item(i);
                    if(item==null)
                        item = "";
                    if(buff.length()>0)
                        buff.append('|');
                    String prefix = nsSupport.findPrefix(item);
                    if(prefix!=null)
                        buff.append(prefix);
                    else
                        buff.append('{').append(item).append('}');
                }
                if(buff.toString().equals("{}"))
                    str = "*";
                else
                    str = buff+":*";
                break;
            case XSWildcard.NSCONSTRAINT_NOT:
                buff = new StringBuilder();
                list = wildcard.getNsConstraintList();
                for(int i=0; i<list.getLength(); i++){
                    String item = list.item(i);
                    if(item==null)
                        item = "";
                    String prefix = nsSupport.findPrefix(item);
                    if(buff.length()>0)
                        buff.append(',');
                    if(!StringUtil.isEmpty(prefix))
                        buff.append(prefix);
                    else{
                        buff.append('{').append(item).append('}');
                    }
                }
                if(!buff.toString().contains(","))
                    str = "!"+buff+":*";
                else
                    str = "!("+buff+"):*";
                break;
            default:
                throw new ImpossibleException("Invalid Constraint: "+wildcard.getConstraintType());
        }

        boolean attribute = false;
        if(path.getParentPath().getElement() instanceof XSElementDeclaration){
            XSElementDeclaration elem = (XSElementDeclaration)path.getParentPath().getElement();
            if(((XSComplexTypeDefinition)elem.getTypeDefinition()).getAttributeWildcard()==wildcard)
                attribute = true;
        }
        if(attribute)
            str = '@'+str;
        else
            str = '<'+str+'>';

        return addCardinal(str);
    }
}
