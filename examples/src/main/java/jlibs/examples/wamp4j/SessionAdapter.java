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

package jlibs.examples.wamp4j;

import jlibs.wamp4j.client.SessionListener;
import jlibs.wamp4j.client.WAMPClient;
import jlibs.wamp4j.error.WAMPException;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class SessionAdapter implements SessionListener{
    @Override
    public void onClose(WAMPClient client){
        System.out.println("SessionAdapter.onClose");
    }

    @Override
    public void onWarning(WAMPClient client, Throwable warning){
        System.out.println("SessionAdapter.onWarning");
        warning.printStackTrace();
    }

    @Override
    public void onError(WAMPClient client, WAMPException error){
        System.out.println("SessionAdapter.onError");
        error.printStackTrace();
    }
}
