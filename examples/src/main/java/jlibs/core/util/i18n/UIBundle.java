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

package jlibs.core.util.i18n;

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