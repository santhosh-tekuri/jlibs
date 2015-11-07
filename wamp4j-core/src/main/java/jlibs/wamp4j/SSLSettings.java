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

import java.io.File;

/**
 * @author Santhosh Kumar Tekuri
 */
public class SSLSettings{
    public File keyFile;
    public String keyPassword;
    public File certificateFile;
    public File trustCertChainFile;
    public ClientAuthentication clientAuthentication = ClientAuthentication.NONE;

    public SSLSettings keyFile(File keyFile){
        this.keyFile = keyFile;
        return this;
    }

    public SSLSettings keyPassword(String keyPassword){
        this.keyPassword = keyPassword;
        return this;
    }

    public SSLSettings certificateFile(File certificateFile){
        this.certificateFile = certificateFile;
        return this;
    }

    public SSLSettings trustCertChainFile(File trustCertChainFile){
        this.trustCertChainFile = trustCertChainFile;
        return this;
    }

    public SSLSettings clientAuthentication(ClientAuthentication clientAuthentication){
        this.clientAuthentication = clientAuthentication;
        return this;
    }
}
