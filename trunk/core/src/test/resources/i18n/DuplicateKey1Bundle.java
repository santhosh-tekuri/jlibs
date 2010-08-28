package i18n;

import jlibs.core.util.i18n.Message;
import jlibs.core.util.i18n.ResourceBundle;

/**
 * @author Santhosh Kumar T
 */
@ResourceBundle
public interface DuplicateKey1Bundle{
    @Message(key="JLIBS015", value="SQL Execution completed in {0} seconds with {1} errors and {2} warnings")
    public String executionFinished(long seconds, int errorCount, int warningCount);
}
