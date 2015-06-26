package i18n;

import jlibs.core.util.i18n.Message;
import jlibs.core.util.i18n.ResourceBundle;

/**
 * @author Santhosh Kumar T
 */
@ResourceBundle
public interface ArgumentCountMismatchBundle{
    @Message("SQL Execution completed in {0} seconds with {1} errors")
    public String executionFinished(long seconds);
}
