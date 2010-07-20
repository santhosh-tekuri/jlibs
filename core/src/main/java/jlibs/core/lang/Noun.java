/**
 * Copyright (c) 2007, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the Reveille project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package jlibs.core.lang;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>API for performing pluralization, singularization
 * on various strings. These inflections will be useful in code generators that
 * convert things like database table names into Java class names.</p>
 */
public class Noun{
    /*-------------------------------------------------[ Plural ]---------------------------------------------------*/

    /**
     * <p>List of <code>Replacer</code>s for performing replacement operations
     * on matches for plural words.</p>
     */
    private static List<Replacer> plurals = new LinkedList<Replacer>();

    /**
     * <p>Add a match pattern and replacement rule for converting addPlural
     * forms to addSingular forms.  By default, matches will be case
     * insensitive.</p>
     *
     * @param match Match pattern regular expression
     * @param rule Replacement rule
     */
    public static void addPlural(String match, String rule){
        addPlural(match, rule, true);
    }


    /**
     * <p>Add a match pattern and replacement rule for converting addPlural
     * forms to addSingular forms.</p>
     *
     * @param match Match pattern regular expression
     * @param rule Replacement rule
     * @param insensitive Flag indicating this match should be case insensitive
     */
    public static void addPlural(String match, String rule, boolean insensitive){
        plurals.add(0, new Replacer(match, rule, insensitive));
    }

    /*-------------------------------------------------[ Singular ]---------------------------------------------------*/

    /**
     * <p>List of <code>Replacer</code>s for performing replacement operations
     * on matches for addSingular words.</p>
     */
    private static List<Replacer> singulars = new ArrayList<Replacer>();

    /**
     * <p>Add a match pattern and replacement rule for converting addSingular
     * forms to addPlural forms.  By default, matches will be case insensitive.</p>
     *
     * @param match Match pattern regular expression
     * @param rule Replacement rule
     */
    public static void addSingular(String match, String rule){
        addSingular(match, rule, true);
    }

    /**
     * <p>Add a match pattern and replacement rule for converting addSingular
     * forms to addPlural forms.</p>
     *
     * @param match Match pattern regular expression
     * @param rule Replacement rule
     * @param insensitive Flag indicating this match should be case insensitive
     */
    public static void addSingular(String match, String rule, boolean insensitive){
        singulars.add(0, new Replacer(match, rule, insensitive));
    }

    /**
     * <p>Add the addSingular and addPlural forms of words that cannot be
     * converted using the normal rules.</p>
     *
     * @param singular Singular form of the word
     * @param plural Plural form of the word
     */
    public static void addIrregular(String singular, String plural){
        addPlural("(.*)(" + singular.substring(0, 1) + ")" + singular.substring(1) + "$",
                  "\\1\\2" + plural.substring(1));
        addSingular("(.*)(" + plural.substring(0, 1) + ")" + plural.substring(1) + "$",
                    "\\1\\2" + singular.substring(1));
    }

    /*-------------------------------------------------[ Uncountables ]---------------------------------------------------*/

    /**
     * <p>List of words that represent addUncountable concepts that cannot be
     * pluralized or singularized.</p>
     */
    private static List<String> uncountables = new LinkedList<String>();
    
    /**
     * <p>Add a word that cannot be converted between addSingular and addPlural.</p>
     *
     *
     * @param word Word to be added
     */
    public static void addUncountable(String word){
        uncountables.add(0, word.toLowerCase());
    }

    /*-------------------------------------------------[ Translate ]---------------------------------------------------*/

    private static String translate(String word, List<Replacer> replacers){
        // Scan uncountables and leave alone
        for(String uncountable: uncountables){
            if(uncountable.equals(word))
                return word;
        }

        // Scan our patterns for a match and return the correct replacement
        for(Replacer replacer: replacers){
            Matcher matcher = replacer.pattern.matcher(word);

            if(matcher.matches()){
                StringBuffer sb = new StringBuffer();
                boolean group = false;
                for(int i=0; i<replacer.rule.length(); i++){
                    char ch = replacer.rule.charAt(i);
                    if(group){
                        sb.append(matcher.group(Character.digit(ch, 10)));
                        group = false;
                    }else if(ch=='\\')
                        group=true;
                    else
                        sb.append(ch);
                }
                return sb.toString();
            }
        }

        // Return the original string unchanged
        return word;
    }

    /**
     * <p>Return a addPlural version of the specified word.</p>
     *
     * @param word Singular word to be converted
     */
    public static String pluralize(String word){
        return translate(word, plurals);
    }


    /**
     * <p>Return a addSingular version of the specified word.</p>
     *
     * @param word Plural word to be converted
     */
    public static String singularize(String word){
        return translate(word, singulars);
    }

    /*-------------------------------------------------[ Rules ]---------------------------------------------------*/
    
    static{
        addPlural("$", "s", false);
        addPlural("(.*)$", "\\1s");
        addPlural("(.*)(ax|test)is$", "\\1\\2es");
        addPlural("(.*)(octop|vir)us$", "\\1\\2i");
        addPlural("(.*)(alias|status)$", "\\1\\2es");
        addPlural("(.*)(bu)s$", "\\1\\2ses");
        addPlural("(.*)(buffal|tomat)o$", "\\1\\2oes");
        addPlural("(.*)([ti])um$", "\\1\\2a");
        addPlural("(.*)sis$", "\\1ses");
        addPlural("(.*)(?:([^f])fe|([lr])f)$", "\\1\\3ves");
        addPlural("(.*)(hive)$", "\\1\\2s");
        addPlural("(.*)(tive)$", "\\1\\2s"); // Added for consistency with singular rules
        addPlural("(.*)([^aeiouy]|qu)y$", "\\1\\2ies");
        addPlural("(.*)(series)$", "\\1\\2"); // Added for consistency with singular rules
        addPlural("(.*)(movie)$", "\\1\\2s"); // Added for consistency with singular rules
        addPlural("(.*)(x|ch|ss|sh)$", "\\1\\2es");
        addPlural("(.*)(matr|vert|ind)ix|ex$", "\\1\\2ices");
        addPlural("(.*)(o)$", "\\1\\2es"); // Added for consistency with singular rules
        addPlural("(.*)(shoe)$", "\\1\\2s"); // Added for consistency with singular rules
        addPlural("(.*)([m|l])ouse$", "\\1\\2ice");
        addPlural("^(ox)$", "\\1en");
        addPlural("(.*)(vert|ind)ex$", "\\1\\2ices"); // Added for consistency with singular rules
        addPlural("(.*)(matr)ix$", "\\1\\2ices"); // Added for consistency with singular rules
        addPlural("(.*)(quiz)$", "\\1\\2zes");

        addSingular("(.*)s$", "\\1");
        addSingular("(.*)(n)ews$", "\\1\\2ews");
        addSingular("(.*)([ti])a$", "\\1\\2um");
        addSingular("(.*)((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "\\1\\2sis");
        addSingular("(.*)(^analy)ses$", "\\1\\2sis");
        addSingular("(.*)([^f])ves$", "\\1\\2fe");
        addSingular("(.*)(hive)s$", "\\1\\2");
        addSingular("(.*)(tive)s$", "\\1\\2");
        addSingular("(.*)([lr])ves$", "\\1\\2f");
        addSingular("(.*)([^aeiouy]|qu)ies$", "\\1\\2y");
        addSingular("(.*)(s)eries$", "\\1\\2eries");
        addSingular("(.*)(m)ovies$", "\\1\\2ovie");
        addSingular("(.*)(x|ch|ss|sh)es$", "\\1\\2");
        addSingular("(.*)([m|l])ice$", "\\1\\2ouse");
        addSingular("(.*)(bus)es$", "\\1\\2");
        addSingular("(.*)(o)es$", "\\1\\2");
        addSingular("(.*)(shoe)s$", "\\1\\2");
        addSingular("(.*)(cris|ax|test)es$", "\\1\\2is");
        addSingular("(.*)(octop|vir)i$", "\\1\\2us");
        addSingular("(.*)(alias|status)es$", "\\1\\2");
        addSingular("^(ox)en", "\\1");
        addSingular("(.*)(vert|ind)ices$", "\\1\\2ex");
        addSingular("(.*)(matr)ices$", "\\1\\2ix");
        addSingular("(.*)(quiz)zes$", "\\1\\2");

        addIrregular("child", "children");
        addIrregular("man", "men");
        addIrregular("move", "moves");
        addIrregular("person", "people");
        addIrregular("sex", "sexes");

        addUncountable("equipment");
        addUncountable("fish");
        addUncountable("information");
        addUncountable("money");
        addUncountable("rice");
        addUncountable("series");
        addUncountable("sheep");
        addUncountable("species");
    }

    /*-------------------------------------------------[ Replacer ]---------------------------------------------------*/

    /**
     * <p>Internal class that uses a regular expression matcher to both
     * match the specified regular expression to a specified word, and
     * (if successful) perform the appropriate substitutions.</p>
     */
    private static class Replacer{
        Pattern pattern = null;
        String rule = null;

        public Replacer(String match, String rule, boolean insensitive){
            pattern = Pattern.compile(match, insensitive ? Pattern.CASE_INSENSITIVE : 0);
            this.rule = rule;
        }
    }
}
