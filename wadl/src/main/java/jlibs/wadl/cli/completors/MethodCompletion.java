/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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

package jlibs.wadl.cli.completors;

import jlibs.wadl.cli.WADLTerminal;
import jlibs.wadl.cli.model.Path;
import jlibs.wadl.model.Method;
import jlibs.wadl.model.Param;
import jlibs.wadl.model.ParamStyle;
import jlibs.xml.Namespaces;
import jlibs.xml.xsd.XSUtil;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Santhosh Kumar T
 */
public class MethodCompletion extends PathCompletion{
    public MethodCompletion(WADLTerminal terminal){
        super(terminal);
    }

    @Override
    protected void completeNext(Buffer buffer, Path path, String arg){
        Method method = path.findMethod(buffer.arg(0));
        if(method==null || method.getRequest()==null)
            return;

        Set<String> params = new HashSet<String>();
        while(buffer.hasNext()){
            int index = arg.indexOf('=');
            if(index!=-1)
                params.add(arg.substring(0, index+1));
            else{
                index = arg.indexOf(':');
                if(index!=-1)
                    params.add(arg.substring(0, index+1));
            }
            arg = buffer.next();
        }
        
        int index = arg.indexOf('=');
        if(index==-1)
            index = arg.indexOf(':');
        if(index==-1){
            for(Param param: method.getRequest().getParam()){
                String candidate = param.getName();
                if(param.getStyle()==ParamStyle.QUERY)
                    candidate += '=';
                else if(param.getStyle()==ParamStyle.HEADER)
                    candidate += ':';
                else
                    continue;
                if(params.contains(candidate) && !param.isRepeating())
                    continue;
                if(param.getFixed()!=null){
                    candidate += param.getFixed();
                    buffer.addCandidate(candidate);
                }else
                    buffer.addCandidate(candidate, (char)0);
            }
        }else{
            for(Param param: method.getRequest().getParam()){
                String paramName = param.getName();
                if(param.getStyle()==ParamStyle.QUERY)
                    paramName += '=';
                else if(param.getStyle()==ParamStyle.HEADER)
                    paramName += ':';
                else
                    continue;
                if(arg.startsWith(paramName)){
                    buffer.eat(paramName.length());
                    QName type = param.getType();
                    if(param.getFixed()!=null)
                        buffer.addCandidate(param.getDefault());
                    else if(type!=null){
                        if(type.equals(new QName(Namespaces.URI_XSD, "boolean"))){
                            buffer.addCandidate("true");
                            buffer.addCandidate("false");
                        }else if(path.getSchema()!=null){
                            XSTypeDefinition typeDef = path.getSchema().getTypeDefinition(type.getLocalPart(), type.getNamespaceURI());
                            if(typeDef instanceof XSSimpleTypeDefinition){
                                List<String> values = XSUtil.getEnumeratedValues((XSSimpleTypeDefinition)typeDef);
                                for(String value: values)
                                    buffer.addCandidate(value);
                            }
                        }
                    }
                    if(!buffer.hasCandidates() && param.getDefault()!=null)
                        buffer.addCandidate(param.getDefault());
                    return;
                }
            }
        }
    }
}
