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

/**
 * @author Santhosh Kumar T
 */
public class ServerCompletion extends Completion{
    public ServerCompletion(WADLTerminal terminal){
        super(terminal);
    }

    @Override
    public void complete(Buffer buffer){
        buffer.next();
        if(!buffer.hasNext()){
            Path currentRoot = terminal.getCurrentPath();
            if(currentRoot!=null)
                currentRoot = currentRoot.getRoot();
            for(Path root: terminal.getRoots()){
                if(root!=currentRoot)
                    buffer.addCandidate(root.name);
            }
        }
    }
}
