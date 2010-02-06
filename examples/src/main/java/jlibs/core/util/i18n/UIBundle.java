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