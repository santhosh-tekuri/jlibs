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

package jlibs.examples.core.util.i18n;

/**
 * @author Santhosh Kumar T
 */
public class UncheckedException extends RuntimeException{
    private String errorCode;

    public UncheckedException(String errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode(){
        return errorCode;
    }

    @Override
    public String toString(){
        String s = getClass().getName()+": "+errorCode;
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
