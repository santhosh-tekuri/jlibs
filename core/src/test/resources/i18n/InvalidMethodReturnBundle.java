package i18n;

import jlibs.core.util.i18n.Message;
import jlibs.core.util.i18n.ResourceBundle;

import java.util.Date;

/**
 * @author Santhosh Kumar T
 */
@ResourceBundle
public interface InvalidMethodReturnBundle{
    @Message("your lass successfull login is on {0, timee}")
    public Date lastSucussfullLogin(Date date);
}
