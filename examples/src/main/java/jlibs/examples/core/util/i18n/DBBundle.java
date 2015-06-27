/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

/**
 * @author Santhosh Kumar T
 */
@ResourceBundle
public interface DBBundle{
    public static final DBBundle DB_BUNDLE = I18N.getImplementation(DBBundle.class);

    @Message("SQL Execution completed in {0} seconds with {1} errors")
    public String executionFinished(long seconds, int errorCount);

    @Message(key="SQLExecutionException", value="Encountered an exception while executing the following statement:\n{0}")
    public String executionException(String query);

    @Message("executing {0}")
    public String executing(String query);

    @Message("Database connection to host {0} is lost")
    public UncheckedException connectionLost(String host);
}