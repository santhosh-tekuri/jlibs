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

package jlibs.wadl.cli.commands.auth;

import javax.xml.bind.DatatypeConverter;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

/**
 * @author Santhosh Kumar T
 */
public class BasicAuthenticator implements Authenticator{
    public static final String TYPE = "Basic";
    private String user;
    private String passwd;
    private String headerValue;

    public BasicAuthenticator(String user, String passwd){
        this.user = user;
        this.passwd = passwd;
        String base64UserCredentials = DatatypeConverter.printBase64Binary((user + ":" + passwd).getBytes(Charset.forName("US-ASCII")));
        headerValue = TYPE +" "+ base64UserCredentials;
    }

    @Override
    public void authenticate(HttpURLConnection con){
        con.setRequestProperty(HEADER_AUTHORIZATION, headerValue);
    }
}
