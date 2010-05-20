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

import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.model.ModelUtil;
import jlibs.jdbc.annotations.*;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
// @enhancement allow to ignore nulls in where condition to make dynamic queries
abstract class DMLMethod{
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
        if(mirror!=null)
            return new SelectMethod(printer, method, mirror, columns);

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

    protected abstract String[] code();

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
}
