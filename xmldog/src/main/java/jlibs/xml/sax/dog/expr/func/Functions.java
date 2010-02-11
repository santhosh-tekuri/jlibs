/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax.dog.expr.func;

import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.path.LocationPath;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class Functions{
    public static Expression typeCast(Object current, DataType expected){
        if(current instanceof Expression){
            Expression expr = (Expression)current;
            DataType exprResultType = expr.resultType;

            if(exprResultType==expected || expected==DataType.STRINGS)
                return expr;

            if(expected==DataType.NUMBERS){
                if(exprResultType==DataType.NUMBER)
                    return expr;
                else
                    expected = DataType.NUMBER;
            }

            if(expected==DataType.PRIMITIVE){
                switch(exprResultType){
                    case STRING:
                    case BOOLEAN:
                    case NUMBER:
                        return expr;
                    default:
                        expected = DataType.STRING;
                }
            }

            FunctionCall function = new FunctionCall(new Functions.TypeCast(expected));
            function.addValidMember(expr, 0);
            return function.simplify();
        }else
            return ((LocationPath)current).typeCast(expected).simplify();
    }

    public static class TypeCast extends Function{
        public TypeCast(DataType resultType){
            super(resultType.name().toLowerCase(), resultType, false, DataType.PRIMITIVE);
        }

        @Override
        public Object evaluate(Object... args){
            return resultType.convert(args[0]);
        }
    }

    public static class UserFunction extends Function{
        public final XPathFunction xpathFunction;

        public UserFunction(String name, XPathFunction xpathFunction){
            super(name, DataType.PRIMITIVE, true, DataType.PRIMITIVE);
            this.xpathFunction = xpathFunction;
        }

        @Override
        public Object evaluate(Object... args){
            try{
                return xpathFunction.evaluate(Arrays.asList(args));
            }catch(XPathFunctionException ex){
                throw new RuntimeException(ex);
            }
        }
    }

    /*-------------------------------------------------[ Arithmetic ]---------------------------------------------------*/

    private static abstract class ArithmeticFunction extends PeekingFunction{
        protected ArithmeticFunction(String name){
            super(name, DataType.NUMBER, false, DataType.NUMBER, DataType.NUMBER);
        }

        @Override
        protected final Object onMemberResult(int index, Object result){
            Double d = (Double)result;
            return d.isNaN() || d.isInfinite() ? d : null;
        }
    }

    public static final ArithmeticFunction ADD = new ArithmeticFunction("+"){
        @Override
        public Object evaluate(Object... args){
            return (Double)args[0] + (Double)args[1];
        }
    };

    public static final ArithmeticFunction SUBSTRACT = new ArithmeticFunction("-"){
        @Override
        public Object evaluate(Object... args){
            return (Double)args[0] - (Double)args[1];
        }
    };

    public static final ArithmeticFunction MULTIPLY = new ArithmeticFunction("*"){
        @Override
        public Object evaluate(Object... args){
            return (Double)args[0] * (Double)args[1];
        }
    };

    public static final ArithmeticFunction DIV = new ArithmeticFunction("div"){
        @Override
        public Object evaluate(Object... args){
            return (Double)args[0] / (Double)args[1];
        }
    };

    public static final ArithmeticFunction MOD = new ArithmeticFunction("mod"){
        @Override
        public Object evaluate(Object... args){
            return (Double)args[0] % (Double)args[1];
        }
    };

    /*-------------------------------------------------[ Numeric ]---------------------------------------------------*/

    public static final Function CEIL = new Function("ceiling", DataType.NUMBER, false, DataType.NUMBER, DataType.NUMBER){
        @Override
        public Object evaluate(Object... args){
            Double d = (Double)args[0];
            if(Double.isNaN(d) || Double.isInfinite(d))
                return d;

            return Math.ceil(d);
        }
    };

    public static final Function FLOOR = new Function("floor", DataType.NUMBER, false, DataType.NUMBER, DataType.NUMBER){
        @Override
        public Object evaluate(Object... args){
            Double d = (Double)args[0];
            if(Double.isNaN(d) || Double.isInfinite(d))
                return d;

            return Math.floor(d);
        }
    };

    public static final Function ROUND = new Function("round", DataType.NUMBER, false, DataType.NUMBER, DataType.NUMBER){
        @Override
        public Object evaluate(Object... args){
            Double d = (Double)args[0];
            if(Double.isNaN(d) || Double.isInfinite(d))
                return d;

            return (double)Math.round(d);
        }
    };

    /*-------------------------------------------------[ String ]---------------------------------------------------*/

    public static final Function LENGTH = new Function("string-length", DataType.NUMBER, false, DataType.STRING){
        @Override
        public Object evaluate(Object... args){
            String str = (String)args[0];

            int length = 0;
            for(int i=0; i<str.length(); i++){
                char c = str.charAt(i);
                length++;
                // if this is a high surrogate; assume the next character is
                // is a low surrogate and skip it
                if(c>=0xD800){
                    try{
                        char low = str.charAt(i+1);
                        if (low < 0xDC00 || low > 0xDFFF)
                            throw new IllegalArgumentException("Bad surrogate pair in string " + str);
                        i++; // increment past low surrogate
                    }catch(StringIndexOutOfBoundsException ex){
                        throw new IllegalArgumentException("Bad surrogate pair in string " + str);
                    }
                }
            }
            return (double)length;
        }
    };

    public static final Function STARTS_WITH = new Function("starts-with", DataType.BOOLEAN, false, DataType.STRING, DataType.STRING){
        @Override
        public Object evaluate(Object... args){
            return ((String)args[0]).startsWith((String)args[1]);
        }
    };

    public static final Function ENDS_WITH = new Function("ends-with", DataType.BOOLEAN, false, DataType.STRING, DataType.STRING){
        @Override
        public Object evaluate(Object... args){
            return ((String)args[0]).endsWith((String)args[1]);
        }
    };

    public static final Function CONTAINS = new Function("contains", DataType.BOOLEAN, false, DataType.STRING, DataType.STRING){
        @Override
        public Object evaluate(Object... args){
            return ((String)args[0]).contains((String)args[1]);
        }
    };

    public static final Function CONCAT = new Function("concat", DataType.STRING, true, DataType.STRING, DataType.STRING){
        @Override
        public Object evaluate(Object... args){
            StringBuilder buff = new StringBuilder();
            for(Object arg: args)
                buff.append(arg);
            return buff.toString();
        }
    };

    public static final Function LANGUAGE_MATCH = new Function("language-match", DataType.BOOLEAN, false, DataType.STRING, DataType.STRING){
        @Override
        public Object evaluate(Object... args){
            String sublang = (String)args[0];
            String lang = (String)args[1];
            if(sublang.equalsIgnoreCase(lang))
                return true;

            int len = lang.length();
            return
                sublang.length() > len &&
                sublang.charAt(len) == '-' &&
                sublang.substring(0, len).equalsIgnoreCase(lang);
        }
    };

    public static final Function TRANSLATE = new Function("translate", DataType.STRING, false, DataType.STRING, DataType.STRING, DataType.STRING){
        @Override
        public Object evaluate(Object... args){
            return translate((String)args[0], (String)args[1], (String)args[2]);
        }

        public String translate(String input, String from, String to){
            // Initialize the mapping in a HashMap
            Map<String, String> characterMap = new HashMap<String, String>();
            String[] fromCharacters = toUnicodeCharacters(from);
            String[] toCharacters = toUnicodeCharacters(to);
            int fromLen = fromCharacters.length;
            int toLen = toCharacters.length;
            for(int i=0; i<fromLen; i++){
                String cFrom = fromCharacters[i];
                if(characterMap.containsKey(cFrom)) // We've seen the character before, ignore
                    continue;
                if(i<toLen) // Will change
                    characterMap.put(cFrom, toCharacters[i]);
                else // Will delete
                    characterMap.put(cFrom, null);
            }

            // Process the input string thru the map
            StringBuilder output = new StringBuilder(input.length());
            String[] inCharacters = toUnicodeCharacters(input);
            int inLen = inCharacters.length;
            for(int i=0; i<inLen; i++){
                String cIn = inCharacters[i];
                if(characterMap.containsKey(cIn)){
                    String cTo = characterMap.get(cIn);
                    if(cTo!=null)
                        output.append(cTo);
                }else
                    output.append(cIn);
            }

            return output.toString();
        }

        private String[] toUnicodeCharacters(final String s){
            String[] result = new String[s.length()];
            int stringLength = 0;
            int slen = s.length();
            for(int i=0; i<slen; i++){
                char c1 = s.charAt(i);
                if(c1>=0xD800 && c1<=0xDBFF){ // isHighSurrogate(c1)
                    try{
                        char c2 = s.charAt(i+1);
                        if(c2>=0xDC00 && c2<=0xDFFF){ //isLowSurrogate(c2)
                            result[stringLength] = (c1 + "" + c2).intern();
                            i++;
                        }else
                            throw new IllegalArgumentException("Mismatched surrogate pair in translate function");
                    }catch (StringIndexOutOfBoundsException ex){
                        throw new IllegalArgumentException("High surrogate without low surrogate at end of string passed to translate function");
                    }
                }else
                    result[stringLength] = String.valueOf(c1).intern();
                stringLength++;
            }

            if(stringLength==result.length)
                return result;

            // trim array
            String[] trimmed = new String[stringLength];
            System.arraycopy(result, 0, trimmed, 0, stringLength);
            return trimmed;
        }
    };


    public static final Function NORMALIZE_SPACE = new Function("normalize-space", DataType.STRING, false, DataType.STRING){
        @Override
        public Object evaluate(Object... args){
            return normalize((String)args[0]);
        }

        public String normalize(String str){
            char[] buffer = str.toCharArray();
            int write = 0;
            int lastWrite = 0;
            boolean wroteOne = false;
            int read = 0;
            while (read < buffer.length){
                if (isXMLSpace(buffer[read])){
                    if (wroteOne)
                        buffer[write++] = ' ';

                    do{
                        read++;
                    }while(read < buffer.length && isXMLSpace(buffer[read]));
                }else{
                    buffer[write++] = buffer[read++];
                    wroteOne = true;
                    lastWrite = write;
                }
            }

            return new String(buffer, 0, lastWrite);
        }

        private boolean isXMLSpace(char c) {
            return c==' ' || c=='\n' || c=='\r' || c=='\t';
        }
    };

    public static final Function SUBSTRING = new Function("substring", DataType.STRING, false, 2, DataType.STRING, DataType.NUMBER, DataType.NUMBER){
        @Override
        public Object evaluate(Object... args){
            String str = (String)args[0];
            if(str==null)
                return "";

            int stringLength = ((Double)LENGTH.evaluate(str)).intValue();
            if(stringLength==0)
                return "";

            Double d1 = (Double)args[1];
            if(d1.isNaN())
                return "";

            int start = ((Double)ROUND.evaluate(d1)).intValue() - 1; // subtract 1 as Java strings are zero based

            int substringLength = stringLength;
            if(args.length==3){
                Double d2 = (Double)args[2];
                if(!d2.isNaN())
                    substringLength = ((Double)ROUND.evaluate(d2)).intValue();
                else
                    substringLength = 0;
            }

            if (substringLength<0)
                return "";

            int end = start + substringLength;
            if(args.length==2)
                end = stringLength;

            if(start<0) // negative start is treated as 0
                start = 0;
            else if(start>stringLength)
                return "";

            if(end>stringLength)
                end = stringLength;
            else if(end<start)
                return "";

            if(stringLength==str.length()) // // easy case; no surrogate pairs
                return str.substring(start, end);
            else
                return unicodeSubstring(str, start, end);
        }

        private String unicodeSubstring(String s, int start, int end){
            StringBuffer result = new StringBuffer(s.length());
            for(int jChar=0, uChar=0; uChar<end; jChar++, uChar++){
                char c = s.charAt(jChar);
                if(uChar>=start)
                    result.append(c);
                if(c>=0xD800){ // get the low surrogate
                    // ???? we could check here that this is indeed a low surroagte
                    // we could also catch StringIndexOutOfBoundsException
                    jChar++;
                    if(uChar>=start)
                        result.append(s.charAt(jChar));
                }
            }
            return result.toString();
        }
    };

    public static abstract class ChangeCase extends Function{
        protected ChangeCase(String name){
            super(name, DataType.STRING, false, 1, DataType.STRING, DataType.STRING);
        }

        @Override
        public Object evaluate(Object... args){
            Locale locale = Locale.ENGLISH;
            if(args.length>1){
                locale = findLocale((String)args[1]);
                if(locale==null)
                    locale = Locale.ENGLISH;
            }

            return evaluate((String)args[0], locale);
        }

        protected abstract String evaluate(String arg, Locale locale);

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
            if(tokens.hasMoreTokens()){
                String language = tokens.nextToken();
                if(!tokens.hasMoreTokens())
                    return findLocaleForLanguage(language);
                else{
                    String country = tokens.nextToken();
                    if(!tokens.hasMoreTokens())
                        return new Locale(language, country);
                    else{
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
            for(Locale locale: Locale.getAvailableLocales()){
                if(language.equals(locale.getLanguage())){
                    String country = locale.getCountry();
                    if(country==null || country.length()==0){
                        String variant = locale.getVariant();
                        if(variant==null || variant.length()==0)
                            return locale;
                    }
                }
            }
            return null;
        }
    }

    public static final ChangeCase UPPER_CASE = new ChangeCase("upper-case"){
        @Override
        protected String evaluate(String arg, Locale locale){
            return arg.toUpperCase(locale);
        }
    };

    public static final ChangeCase LOWER_CASE = new ChangeCase("lower-case"){
        @Override
        protected String evaluate(String arg, Locale locale){
            return arg.toLowerCase(locale);
        }
    };

    /*-------------------------------------------------[ Boolean ]---------------------------------------------------*/

    public static final Function AND = new PeekingFunction("and", DataType.BOOLEAN, false, DataType.BOOLEAN, DataType.BOOLEAN){
        @Override
        public Object evaluate(Object... args){
            return (Boolean)args[0] && (Boolean)args[1];
        }

        @Override
        protected Object onMemberResult(int index, Object result){
            return result==Boolean.FALSE ? result : null;
        }
    };

    public static final Function OR = new PeekingFunction("or", DataType.BOOLEAN, false, DataType.BOOLEAN, DataType.BOOLEAN){
        @Override
        public Object evaluate(Object... args){
            return (Boolean)args[0] || (Boolean)args[1];
        }

        @Override
        protected Object onMemberResult(int index, Object result){
            return result==Boolean.TRUE ? result : null;
        }
    };

    public static final Function NOT = new Function("not", DataType.BOOLEAN, false, DataType.BOOLEAN){
        @Override
        public Object evaluate(Object... args){
            return !(Boolean)args[0];
        }
    };

    /*-------------------------------------------------[ Equals ]---------------------------------------------------*/

    public static final Function NUMBER_EQUALS_NUMBER = new PeekingFunction("=", DataType.BOOLEAN, false, DataType.NUMBER, DataType.NUMBER){
        @Override
        public Object evaluate(Object... args){
            return args[0].equals(args[1]);
        }

        @Override
        protected Object onMemberResult(int index, Object result){
            return Double.isNaN((Double)result) ? Boolean.FALSE : null;
        }
    };

    public static final Function STRING_EQUALS_STRING = new Function("=", DataType.BOOLEAN, false, DataType.STRING, DataType.STRING){
        @Override
        public Object evaluate(Object... args){
            assert args[0] instanceof String;
            assert args[1] instanceof String;
            return args[0].equals(args[1]);
        }
    };

    public static final Function STRINGS_EQUALS_STRING = new PeekingFunction("=", DataType.BOOLEAN, false, DataType.STRINGS, DataType.STRING){
        @Override
        public Object evaluate(Object... args){
            assert args[1] instanceof String;
            if(args[0] instanceof Collection)
                return ((Collection)args[0]).contains(args[1]);
            else
                return args[0].equals(args[1]);
        }

        @Override
        protected Object onMemberResult(int index, Object result){
            return result instanceof Collection && ((Collection)result).size()==0 ? Boolean.FALSE : null;
        }
    };

    public static final Function NUMBERS_EQUALS_NUMBER = new PeekingFunction("=", DataType.BOOLEAN, false, DataType.NUMBERS, DataType.NUMBER){
        @Override
        public Object evaluate(Object... args){
            double rhs = (Double)args[1];
            if(Double.isNaN(rhs))
                return false;
            if(args[0] instanceof Collection)
                return ((Collection)args[0]).contains(args[1]);
            else
                return (Double)args[0]==rhs;
        }

        @Override
        protected Object onMemberResult(int index, Object result){
            if(result instanceof Double)
                return Double.isNaN((Double)result) ? Boolean.FALSE : null;
            else
                return ((Collection)result).size()==0 ? Boolean.FALSE : null;
        }
    };

    public static final Function STRINGS_EQUALS_STRINGS = new PeekingFunction("=", DataType.BOOLEAN, false, DataType.STRINGS, DataType.STRINGS){
        @Override
        public Object evaluate(Object... args){
            boolean list0 = args[0] instanceof Collection;
            boolean list1 = args[1] instanceof Collection;
            if(list0 && list1){
                Collection rhs = (Collection)args[1];
                for(Object lhs: (Collection)args[0]){
                    if(rhs.contains(lhs))
                        return true;
                }
                return false;
            }else if(list0)
                return ((Collection)args[0]).contains(args[1]);
            else if(list1)
                return ((Collection)args[1]).contains(args[0]);
            else
                return args[0].equals(args[1]);
        }

        @Override
        protected Object onMemberResult(int index, Object result){
            return result instanceof Collection && ((Collection)result).size()==0 ? Boolean.FALSE : null;
        }
    };

    /*-------------------------------------------------[ Comparison ]---------------------------------------------------*/

    private abstract static class Comparison extends PeekingFunction{
        protected Comparison(String name, DataType memberType){
            super(name, DataType.BOOLEAN, false, memberType, memberType);
        }

        @Override
        public final Object evaluate(Object[] args){
            boolean list1 = args[0] instanceof Collection;
            boolean list2 = args[1] instanceof Collection;
            if(list1 && list2){
                Collection rhsCollection = (Collection)args[1];
                for(Object lhs: (Collection)args[0]){
                    for(Object rhs: rhsCollection){
                        if(evaluateObjectObject(lhs, rhs))
                            return true;
                    }
                }
            }else if(list1){
                for(Object lhs: (Collection)args[0]){
                    if(evaluateObjectObject(lhs, args[1]))
                        return true;
                }
            }else if(list2){
                for(Object rhs: (Collection)args[1]){
                    if(evaluateObjectObject(args[0], rhs))
                        return true;
                }
            }else
                return evaluateObjectObject(args[0], args[1]);

            return false;
        }

        @Override
        protected Object onMemberResult(int index, Object result){
            return result instanceof Collection && ((Collection)result).size()==0 ? Boolean.FALSE : null;
        }

        protected abstract boolean evaluateObjectObject(Object lhs, Object rhs);
    }

    private static abstract class Equality extends Comparison{
        public Equality(String name){
            super(name, DataType.STRINGS);
        }

        @Override
        protected final boolean evaluateObjectObject( Object lhs, Object rhs){
            assert lhs!=null && rhs!=null;
            if(lhs instanceof Boolean || rhs instanceof Boolean)
                return evaluateObjects(DataType.asBoolean(lhs), DataType.asBoolean(rhs));
            else if(lhs instanceof Double || rhs instanceof Double)
                return evaluateObjects(DataType.asNumber(lhs), DataType.asNumber(rhs));
            else
                return evaluateObjects(lhs.toString(), rhs.toString());
        }

        protected abstract boolean evaluateObjects(Object lhs, Object rhs);
    }

    public static final Function EQUALS = new Equality("="){
        @Override
        protected boolean evaluateObjects(Object lhs, Object rhs){
            if(lhs instanceof Double){
                if(Double.isNaN((Double)lhs) || Double.isNaN((Double)rhs))
                    return false;
            }
            return lhs.equals(rhs);
        }

        @Override
        protected final Object onMemberResult(int index, Object result){
            if(result instanceof Double)
                return Double.isNaN((Double)result) ? Boolean.FALSE : null;
            return super.onMemberResult(index, result);
        }
    };

    public static final Function NOT_EQUALS = new Equality("!="){
        @Override
        protected boolean evaluateObjects(Object lhs, Object rhs){
            if(lhs instanceof Double){
                if(Double.isNaN((Double)lhs) || Double.isNaN((Double)rhs))
                    return true;
            }
            return !lhs.equals(rhs);
        }

        @Override
        protected final Object onMemberResult(int index, Object result){
            if(result instanceof Double)
                return Double.isNaN((Double)result) ? Boolean.TRUE : null;
            return super.onMemberResult(index, result);
        }
    };

    /*-------------------------------------------------[ Relational ]---------------------------------------------------*/

    private static abstract class Relational extends PeekingFunction{
        public Relational(String name){
            super(name, DataType.BOOLEAN, false, DataType.NUMBERS, DataType.NUMBERS);
        }

        public final Object evaluate(Object[] args){
            boolean list1 = args[0] instanceof Collection;
            boolean list2 = args[1] instanceof Collection;
            if(list1 && list2){
                Collection rhsCollection = (Collection)args[1];
                for(Object lhs: (Collection)args[0]){
                    double lhsNum = (Double)lhs;
                    if(!Double.isNaN(lhsNum)){
                        for(Object rhs: rhsCollection){
                            double rhsNum = (Double)rhs;
                            if(!Double.isNaN(rhsNum) && evaluateDoubles(lhsNum, rhsNum))
                                return true;
                        }
                    }
                }
            }else if(list1){
                double rhsNum = (Double)args[1];
                if(!Double.isNaN(rhsNum)){
                    for(Object lhs: (Collection)args[0]){
                        double lhsNum = (Double)lhs;
                        if(!Double.isNaN(lhsNum) && evaluateDoubles(lhsNum, rhsNum))
                            return true;
                    }
                }
            }else if(list2){
                double lhsNum = (Double)args[0];
                if(!Double.isNaN(lhsNum)){
                    for(Object rhs: (Collection)args[1]){
                        double rhsNum = (Double)rhs;
                        if(!Double.isNaN(rhsNum) && evaluateDoubles(lhsNum, rhsNum))
                            return true;
                    }
                }
            }else{
                double lhsNum = (Double)args[0];
                if(Double.isNaN(lhsNum))
                    return false;

                double rhsNum = (Double)args[1];
                return !Double.isNaN(rhsNum) && evaluateDoubles(lhsNum, rhsNum);

            }

            return false;
        }

        protected abstract boolean evaluateDoubles(double lhs, double rhs);

        @Override
        protected final Object onMemberResult(int index, Object result){
            if(result instanceof Collection)
                return ((Collection)result).size()==0 ? Boolean.FALSE : null;
            else
                return ((Double)result).isNaN() ? Boolean.FALSE : null;
        }
    }

    public static final Function GREATER_THAN = new Relational(">"){
        @Override
        protected boolean evaluateDoubles(double lhs, double rhs){
            return lhs>rhs;
        }
    };

    public static final Function GREATER_THAN_EQUAL = new Relational(">="){
        @Override
        protected boolean evaluateDoubles(double lhs, double rhs){
            return lhs>=rhs;
        }
    };

    public static final Function LESS_THAN = new Relational("<"){
        @Override
        protected boolean evaluateDoubles(double lhs, double rhs){
            return lhs<rhs;
        }
    };

    public static final Function LESS_THAN_EQUAL = new Relational("<="){
        @Override
        protected boolean evaluateDoubles(double lhs, double rhs){
            return lhs<=rhs;
        }
    };

    /*-------------------------------------------------[ Lookup ]---------------------------------------------------*/

    public static final Map<String, Function> library = new HashMap<String, Function>();

    static{
        Function functions[] = {
            ADD, SUBSTRACT, MULTIPLY, DIV, MOD,
            CEIL, FLOOR, ROUND,
            LENGTH, STARTS_WITH, ENDS_WITH, CONTAINS, CONCAT, LANGUAGE_MATCH, TRANSLATE, NORMALIZE_SPACE, SUBSTRING, UPPER_CASE, LOWER_CASE,
            AND, OR, NOT,
            EQUALS, NOT_EQUALS, GREATER_THAN, GREATER_THAN_EQUAL, LESS_THAN, LESS_THAN_EQUAL,
            new TypeCast(DataType.STRING), new TypeCast(DataType.NUMBER), new TypeCast(DataType.BOOLEAN)
        };

        for(Function f: functions)
            library.put(f.name, f);
    }
}
