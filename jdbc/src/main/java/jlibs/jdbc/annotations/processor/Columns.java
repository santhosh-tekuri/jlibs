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

import java.util.ArrayList;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
class Columns extends ArrayList<ColumnProperty>{
    public String tableName;
    
    public ColumnProperty findByProperty(String propertyName){
        for(ColumnProperty prop: this){
            if(prop.propertyName().equals(propertyName))
                return prop;
        }
        return null;
    }

    public ColumnProperty findByColumn(String columnName){
        for(ColumnProperty prop: this){
            if(prop.columnName().equals(columnName))
                return prop;
        }
        return null;
    }

    public String columnName(String propertyName){
        ColumnProperty column = findByProperty(propertyName);
        return column!=null ? column.columnName() : null;
    }

    public String propertyName(String columnName){
        ColumnProperty column = findByColumn(columnName);
        return column!=null ? column.propertyName() : null;
    }

    public int autoColumn = -1;
    
    @Override
    public boolean add(ColumnProperty columnProperty){
        ColumnProperty clash = findByProperty(columnProperty.propertyName());
        if(clash!=null)
            throw new AnnotationError(columnProperty.element, columnProperty.annotation, "this property is already used by: "+clash.element);
        clash = findByColumn(columnProperty.columnName());
        if(clash!=null)
            throw new AnnotationError(columnProperty.element, columnProperty.annotation, "this column is already used by: "+clash.element);
        if(columnProperty.auto()){
            if(autoColumn!=-1)
                throw new AnnotationError(columnProperty.element, columnProperty.annotation, "only one column can be auto");
            autoColumn = size();
        }
        return super.add(columnProperty);
    }

    private void addDefaultCase(Printer printer){
        printer.printlns(
                "default:",
                    PLUS,
                    "throw new ImpossibleException();",
                    MINUS,
                    MINUS,
                "}",
             MINUS,
            "}"
        );
    }

    public void generateGetColumnValue(Printer printer){
        printer.printlns(
            "@Override",
            "public Object getColumnValue(int i, "+printer.clazz.getSimpleName()+" record){",
                PLUS,
                "switch(i){",
                    PLUS
        );
        int i = 0;
        for(ColumnProperty column: this){
            printer.printlns(
                "case "+i+":",
                    PLUS,
                    "return "+column.getPropertyCode("record")+';',
                    MINUS
            );
            i++;
        }
        addDefaultCase(printer);
    }

    public void generateSetColumnValue(Printer printer){
        printer.printlns(
            "@Override",
            "public void setColumnValue(int i, "+printer.clazz.getSimpleName()+" record, Object value){",
                PLUS,
                "switch(i){",
                    PLUS
        );
        int i = 0;
        for(ColumnProperty column: this){
            printer.printlns(
                "case "+i+":",
                    PLUS,
                    column.setPropertyCode("record", "value")+';',
                    "break;",
                    MINUS
            );
            i++;
        }
        addDefaultCase(printer);
    }
}
