package i18n;

import jlibs.core.util.i18n.Message;
import jlibs.core.util.i18n.ResourceBundle;

/**
 * @author Santhosh Kumar T
 */
@ResourceBundle
public interface DuplicateKey2Bundle{
    @Message(key="JLIBS015", value="Encountered an exception while executing the following statement:\n{0}")
    public String executionException(String query);
}
