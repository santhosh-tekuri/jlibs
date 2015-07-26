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

import jlibs.core.util.i18n.I18N;
import jlibs.core.util.i18n.Message;
import jlibs.core.util.i18n.ResourceBundle;

import java.io.File;

/**
 * @author Santhosh Kumar T
 */
@ResourceBundle
public interface UIBundle{
    public static final UIBundle UI_BUNDLE = I18N.getImplementation(UIBundle.class);

    @Message("Execute")
    public String executeButton();

    @Message("File {0} already exists.  Do you really want to replace it?")
    public String confirmReplace(File file);

    /**
        * thrown when failed to load application
        * because of network failure
        *
        * @param application   UID of application
        * @param version       version of the application
        */
    @Message(key = "cannotKillApplication", value="failed to kill application {0} with version {1}")
    public String cannotKillApplication(String application, String version);
}