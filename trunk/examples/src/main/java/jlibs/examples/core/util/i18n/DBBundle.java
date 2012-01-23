/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
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