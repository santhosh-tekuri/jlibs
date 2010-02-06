package jlibs.core.util.i18n;

/**
 * @author Santhosh Kumar T
 */
public enum Hint{
    DISPLAY_NAME("displayName"),
    DESCRIPTION("description"),
    ADVANCED("advanced", "false"),
    NONE(null),
    ;

    private String key;
    private String defaultValue;

    private Hint(String key){
        this(key, null);
    }

    private Hint(String key, String defaultValue){
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String key(){
        return key;
    }

    public String defaultValue(){
        return defaultValue;
    }

    public String stringValue(Class clazz, String member){
        String value = I18N.getHint(clazz, member, key);
        return value==null ? defaultValue : value;
    }

    public String stringValue(Class clazz){
        String value = I18N.getHint(clazz, key);
        return value==null ? defaultValue : value;
    }

    public boolean booleanValue(Class clazz, String member){
        return Boolean.parseBoolean(stringValue(clazz, member));
    }

    public boolean booleanValue(Class clazz){
        return Boolean.parseBoolean(stringValue(clazz));
    }

    public int intValue(Class clazz, String member){
        return Integer.parseInt(stringValue(clazz, member));
    }

    public int intValue(Class clazz){
        return Integer.parseInt(stringValue(clazz));
    }
}
