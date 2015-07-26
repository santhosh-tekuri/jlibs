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

package jlibs.wadl.cli.commands;

import jlibs.core.net.URLUtil;
import jlibs.wadl.cli.WADLTerminal;
import jlibs.wadl.cli.model.Path;
import jlibs.wadl.cli.model.WADLReader;
import jlibs.wadl.model.Application;
import jlibs.wadl.model.Include;
import jlibs.wadl.model.Resource;
import jlibs.wadl.model.Resources;
import jlibs.xml.xsd.DOMLSInputList;
import jlibs.xml.dom.DOMUtil;
import jlibs.xml.xsd.XSParser;
import org.apache.xerces.xs.XSModel;
import org.w3c.dom.Element;

import java.net.URI;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Import extends Command{
    public Import(WADLTerminal terminal){
        super(terminal);
    }

    @Override
    public boolean run(String cmd, List<String> args) throws Exception{
        if(args.size()==0){
            System.err.println("nothing to import!!!");
            return false;
        }
        for(String systemID: args)
            importWADL(systemID);
        return true;
    }

    private void importWADL(String systemID) throws Exception{
        Application application = new WADLReader().read(systemID);

        DOMLSInputList inputList = new DOMLSInputList();
        XSModel schema = null;
        if(application.getGrammars()!=null){
            for(Include include: application.getGrammars().getInclude()){
                if(include.getHref()!=null)
                    inputList.addSystemID(URLUtil.resolve(systemID, include.getHref()).toString());
            }
            for(Object any: application.getGrammars().getAny()){
                if(any instanceof Element)
                    inputList.addStringData(DOMUtil.toString((Element)any), systemID);
            }
        }
        if(!inputList.isEmpty())
            schema = new XSParser().parse(inputList);

        Path root = null;
        for(Resources resources: application.getResources()){
            URI base = URI.create(resources.getBase());
            String url = base.getScheme()+"://"+base.getHost();
            if(base.getPort()!=-1)
                url += ":"+base.getPort();
            root = null;
            for(Path path: terminal.getRoots()){
                if(path.name.equals(url)){
                    root = path;
                    break;
                }
            }
            if(root==null){
                root = new Path(null, url);
                terminal.getRoots().add(root);
                if(base.getPath()!=null && !base.getPath().isEmpty())
                    root = root.add(base.getPath());
            }
            root.schema = schema;
            for(Resource resource: resources.getResource())
                importResource(resource, root);
        }
        terminal.setCurrentPath(root);
    }

    private void importResource(Resource resource, Path path){
        path = path.add(resource.getPath());
        if(path.resource==null)
            path.resource = resource;
        else
            path.resource.getMethodOrResource().addAll(resource.getMethodOrResource());

        for(Object obj: resource.getMethodOrResource()){
            if(obj instanceof Resource)
                importResource((Resource)obj, path);
        }
    }
}
