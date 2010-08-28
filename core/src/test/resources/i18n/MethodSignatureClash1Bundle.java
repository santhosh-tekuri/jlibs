package i18n;

import jlibs.core.util.i18n.Message;
import jlibs.core.util.i18n.ResourceBundle;

/**
 * @author Santhosh Kumar T
 */
@ResourceBundle
public interface MethodSignatureClash1Bundle{
    @Message(key="EXECUTING", value="executing {0}")
    public String executing(String query);
}
