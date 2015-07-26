/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

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
