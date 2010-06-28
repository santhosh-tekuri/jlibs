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

package jlibs.jdbc.annotations.processor;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;
import jlibs.core.util.regex.TemplateMatcher;
import jlibs.jdbc.annotations.*;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
// @enhancement allow to ignore nulls in where condition to make dynamic queries
abstract class DMLMethod{
    protected static final Map<String, String> HINTS = new HashMap<String, String>();
    static{
        HINTS.put("where", "=?");
        HINTS.put("eq", "=?");
        HINTS.put("ne", "<>?");
        HINTS.put("lt", "<?");
        HINTS.put("le", "<=?");
        HINTS.put("gt", ">?");
        HINTS.put("ge", ">=?");
        HINTS.put("like", "LIKE ?");
        HINTS.put("nlike", "NOT LIKE ?");
    }

    protected Printer printer;
    protected ExecutableElement method;
    protected AnnotationMirror mirror;
    protected Columns columns;

    protected DMLMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        this.printer = printer;
        this.method = method;
        this.mirror = mirror;
        this.columns = columns;
    }

    public static DMLMethod create(Printer printer, ExecutableElement method, Columns columns){
        AnnotationMirror mirror = ModelUtil.getAnnotationMirror(method, Select.class);
        if(mirror!=null){
            String columnProp = ModelUtil.getAnnotationValue(method, mirror, "column");
            if(columnProp.length()>0)
                return new SelectColumnMethod(printer, method, mirror, columns);
            else
                return new SelectMethod(printer, method, mirror, columns);
        }

        mirror = ModelUtil.getAnnotationMirror(method, Insert.class);
        if(mirror!=null)
            return new InsertMethod(printer, method, mirror, columns);

        mirror = ModelUtil.getAnnotationMirror(method, Update.class);
        if(mirror!=null)
            return new UpdateMethod(printer, method, mirror, columns);

        mirror = ModelUtil.getAnnotationMirror(method, Upsert.class);
        if(mirror!=null)
            return new UpsertMethod(printer, method, mirror, columns);

        mirror = ModelUtil.getAnnotationMirror(method, Delete.class);
        if(mirror!=null)
            return new DeleteMethod(printer, method, mirror, columns);

        return null;
    }

    protected String methodName(){
        return mirror.getAnnotationType().asElement().getSimpleName().toString().toLowerCase(Locale.US);
    }

    protected String userSQL(){
        try{
            return ModelUtil.getAnnotationValue(method, mirror, "value");
        }catch(AnnotationError error){ // doesn't support user sql
            return "";
        }
    }

    public void generate(){
        printer.printlns(
            "",
            "@Override",
            ModelUtil.signature(method, true)+"{",
                PLUS
        );

        printer.printlns(code());
        printer.printlns(
                MINUS,
            "}"
        );
    }

    protected String[] code(){
        String code = queryMethod(sql())+';';
        if(method.getReturnType().getKind()!= TypeKind.VOID)
            code = "return "+code;

        return new String[]{ code };
    }

    protected String queryMethod(CharSequence sequences[]){
        return queryMethod(methodName(), sequences);
    }

    protected static String queryMethod(String methodName, CharSequence... sequences){
        CharSequence query = sequences[0];
        CharSequence params = sequences[1];

        query = '"'+ StringUtil.toLiteral(query, false)+'"';
        String code = methodName+'('+query;
        if(params.length()>0)
            code += ", "+params;
        return code += ')';
    }

    protected final CharSequence[] sql(){
        String userSQL = userSQL();
        if(userSQL.length()==0){
            if(method.getParameters().size()==0)
                throw new AnnotationError(method, "method with "+mirror.getAnnotationType().asElement().getSimpleName()+" annotation should take atleast one argument");
            return defaultSQL();
        }else
            return preparedSQL(userSQL);
    }

    protected abstract CharSequence[] defaultSQL();

    protected CharSequence[] preparedSQL(String value){
        CharSequence query;
        final StringBuilder params = new StringBuilder();

        TemplateMatcher matcher = new TemplateMatcher("#{", "}");
        value = matcher.replace(value, new TemplateMatcher.VariableResolver(){
            @Override
            public String resolve(String propertyName){
                String columnName = columns.columnName(propertyName);
                if(columnName==null)
                    throw new AnnotationError(method, mirror, "unknown property: "+propertyName);
                return columnName;
            }
        });
        matcher = new TemplateMatcher("${", "}");
        query = matcher.replace(value, new TemplateMatcher.VariableResolver(){
            @Override
            public String resolve(String paramName){
                VariableElement param = ModelUtil.getParameter(method, paramName);
                if(param==null)
                    throw new AnnotationError(method, mirror, "unknown parameter: "+paramName);
                if(params.length()>0)
                    params.append(", ");
                params.append(paramName);
                return "?";
            }
        });

        return new CharSequence[]{ query, params };
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/

    protected ColumnProperty getColumn(VariableElement param){
        return getColumn(param, param.getSimpleName().toString());
    }

    protected ColumnProperty getColumn(VariableElement param, String propertyName){
        ColumnProperty column = columns.findByProperty(propertyName);
        if(column==null)
            throw new AnnotationError(method, "invalid column property: "+propertyName);
        if(column.propertyType()!=param.asType())
            throw new AnnotationError(param, param.getSimpleName()+" must be of type "+ModelUtil.toString(column.propertyType(), true));
        return column;
    }
}
