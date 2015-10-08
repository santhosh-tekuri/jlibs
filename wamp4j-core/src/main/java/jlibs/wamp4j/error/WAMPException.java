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

package jlibs.wamp4j.error;

import jlibs.wamp4j.msg.AbortMessage;
import jlibs.wamp4j.msg.ErrorMessage;

/**
 * @author Santhosh Kumar Tekuri
 */
public class WAMPException extends Exception{
    private ErrorCode errorCode;

    protected WAMPException(ErrorCode errorCode){
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode(){
        return errorCode;
    }

    public WAMPException initCause(Throwable thr){
        super.initCause(thr);
        return this;
    }

    public static WAMPException newInstance(ErrorCode errorCode){
        String uri = errorCode.uri;
        if(ErrorCode.INVALID_URI.equals(uri))
            return new InvalidURIException(errorCode);
        else if(ErrorCode.NO_SUCH_PROCEDURE.equals(uri))
            return new NoSuchProcedureException(errorCode);
        else if(ErrorCode.PROCEDURE_ALREADY_EXISTS.equals(uri))
            return new ProcedureAlreadyExistsException(errorCode);
        else if(ErrorCode.NO_SUCH_REGISTRATION.equals(uri))
            return new NoSuchRegistrationException(errorCode);
        else if(ErrorCode.NO_SUCH_SUBSCRIPTION.equals(uri))
            return new NoSuchSubscriptionException(errorCode);
        else if(ErrorCode.INVALID_ARGUMENT.equals(uri))
            return new InvalidArgumentException(errorCode);
        else if(ErrorCode.SYSTEM_SHUTDOWN.equals(uri))
            return new SystemShutdownException(errorCode);
        else if(ErrorCode.NOT_AUTHORIZED.equals(uri))
            return new NotAuthorizedException(errorCode);
        else if(ErrorCode.AUTHORIZATION_FAILED.equals(uri))
            return new AuthorizationFailedException(errorCode);
        else if(ErrorCode.NO_SUCH_REALM.equals(uri))
            return new NoSuchRealmException(errorCode);
        else if(ErrorCode.NO_SUCH_ROLE.equals(uri))
            return new NoSuchRoleException(errorCode);
        else if(ErrorCode.INVALID_MESSAGE.equals(uri))
            return new InvalidMessageException(errorCode);
        else if(ErrorCode.SERIALIZATION_FAILED.equals(uri))
            return new SerializationFailedException(errorCode);
        else if(ErrorCode.NOT_CONNECTED.equals(uri))
            return new NotConnectedException(errorCode);
        else if(ErrorCode.UNEXPECTED_ERROR.equals(uri))
            return new UnexpectedException(errorCode);
        else if(ErrorCode.WRONG_THREAD.equals(uri))
            return new WrongThreadException(errorCode);
        else if(ErrorCode.UNSUPPORTED_SERIALIZATION.equals(uri))
            return new UnsupportedSerializationException(errorCode);
        else
            return new WAMPException(errorCode);
    }

    public static WAMPException newInstance(ErrorMessage errorMessage){
        return newInstance(new ErrorCode(errorMessage));
    }

    public static WAMPException newInstance(AbortMessage abortMessage){
        return newInstance(new ErrorCode(abortMessage));
    }
}
