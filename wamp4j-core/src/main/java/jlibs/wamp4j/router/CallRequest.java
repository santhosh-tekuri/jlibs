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

import com.fasterxml.jackson.core.JsonParser;
import jlibs.wamp4j.error.ErrorCode;
import jlibs.wamp4j.msg.*;

/**
 * @author Santhosh Kumar Tekuri
 */
class CallRequest{
    public final long callID;
    final Procedure procedure;
    public final Session callSession;

    public CallRequest(long callID, Procedure procedure, Session callSession){
        this.callID = callID;
        this.procedure = procedure;
        this.callSession = callSession;
    }

    public void reply(long requestID, JsonParser yield) throws Throwable{
        assert procedure.requests.get(requestID)==this;
        procedure.requests.remove(requestID);
        callSession.send(callSession.resultMessage(callID, yield));
    }

    public void error(long requestID, JsonParser error) throws Throwable{
        assert procedure.requests.get(requestID)==this;
        procedure.requests.remove(requestID);
        callSession.send(callSession.errorMessage(CallMessage.ID, callID, error));
    }

    public void error(ErrorCode errorCode) throws Throwable{
        callSession.send(callSession.errorMessage(CallMessage.ID, callID, errorCode));
    }

    public void noSuchProcedure() throws Throwable{
        error(ErrorCode.noSuchProcedure(procedure.uri()));
    }
}
