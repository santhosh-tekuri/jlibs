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

package jlibs.wamp4j.router;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
class Realms{
    private Map<String, Realm> realms = new HashMap<String, Realm>();

    public Realm get(String name){
        Realm realm = realms.get(name);
        if(realm==null)
            realms.put(name, realm=new Realm(name));
        return realm;
    }

    public void close(){
        for(Realm realm : realms.values())
            realm.close();
        realms.clear();
    }
}
