/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
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
import jlibs.xml.xsd.XSParser;
import org.apache.xerces.xs.XSModel;

import java.net.URI;
import java.util.ArrayList;
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

        XSModel schema = null;
        if(application.getGrammars()!=null){
            List<String> includes = new ArrayList<String>();
            for(Include include: application.getGrammars().getInclude()){
                if(include.getHref()!=null)
                    includes.add(URLUtil.resolve(systemID, include.getHref()).toString());
            }
            if(!includes.isEmpty())
                schema = new XSParser().parse(includes.toArray(new String[includes.size()]));
        }

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
