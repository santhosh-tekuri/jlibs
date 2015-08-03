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

package jlibs.wamp4j;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.EnumSet;

import static jlibs.wamp4j.Role.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public enum Peer{
    client(caller, callee, publisher, subscriber),
    router(dealer, broker);

    public final EnumSet<Role> roles;
    public final ObjectNode details;

    Peer(Role... roles){
        this.roles = EnumSet.copyOf(Arrays.asList(roles));
        details = JsonNodeFactory.instance.objectNode();
        details.put("agent", "JLibs-wamp4j-2.2");
        ObjectNode rolesNode = details.putObject("roles");
        for(Role role : roles){
            ObjectNode roleNode = rolesNode.putObject(role.name());
            ObjectNode featuresNode = null;
            for(Feature feature : role.allFeatures()){
                if(feature.implemented){
                    if(featuresNode==null)
                        featuresNode = roleNode.putObject("features");
                    featuresNode.put(feature.name(), true);
                }
            }
        }
    }
}
