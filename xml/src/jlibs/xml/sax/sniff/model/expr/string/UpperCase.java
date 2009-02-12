/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax.sniff.model.expr.string;

import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.expr.Function;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * @author Santhosh Kumar T
 */
public class UpperCase extends Function{
    public UpperCase(Node contextNode){
        super(contextNode, Datatype.STRING, Datatype.STRING, Datatype.STRING);
    }

    @Override
    protected Object evaluate(Object[] args){
        Locale locale = Locale.ENGLISH;
        if(args.length>1){
            locale = findLocale((String)args[1]);
            if(locale==null)
                locale = Locale.ENGLISH;
        }
        
        return ((String)args[0]).toUpperCase(locale);
    }

    /**
     * Tries to find a Locale instance by name using
     * <a href="http://www.ietf.org/rfc/rfc3066.txt" target="_top">RFC 3066</a>
     * language tags such as 'en', 'en-US', 'en-US-Brooklyn'.
     *
     * @param localeText the RFC 3066 language tag
     * @return the locale for the given text or null if one could not
     *      be found
     */
    public static Locale findLocale(String localeText) {
        StringTokenizer tokens = new StringTokenizer( localeText, "-" );
        if (tokens.hasMoreTokens())
        {
            String language = tokens.nextToken();
            if (! tokens.hasMoreTokens())
            {
                return findLocaleForLanguage(language);
            }
            else
            {
                String country = tokens.nextToken();
                if (! tokens.hasMoreTokens())
                {
                    return new Locale(language, country);
                }
                else
                {
                    String variant = tokens.nextToken();
                    return new Locale(language, country, variant);
                }
            }
        }
        return null;
    }

    /**
     * Finds the locale with the given language name with no country
     * or variant, such as Locale.ENGLISH or Locale.FRENCH
     *
     * @param language the language code to look for
     * @return the locale for the given language or null if one could not
     *      be found
     */
    private static Locale findLocaleForLanguage(String language) {
        Locale[] locales = Locale.getAvailableLocales();
        for ( int i = 0, size = locales.length; i < size; i++ )
        {
            Locale locale = locales[i];
            if ( language.equals( locale.getLanguage() ) )
            {
                String country = locale.getCountry();
                if ( country == null || country.length() == 0 )
                {
                    String variant = locale.getVariant();
                    if ( variant == null || variant.length() == 0 )
                    {
                        return locale;
                    }
                }
            }
        }
        return null;
    }
}