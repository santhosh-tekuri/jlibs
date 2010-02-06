package jlibs.core.util.i18n;

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
}