/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

package jlibs.jdbc.annotations.processor;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;
import jlibs.core.util.CollectionUtil;
import jlibs.jdbc.Order;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
// @enhancement allow to return single/listOf column values
public class WhereMethod extends DMLMethod{
    protected WhereMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        super(printer, method, mirror, columns);
    }

    @Override
    protected CharSequence[] defaultSQL(){
        return defaultSQL(new ArrayList<VariableElement>(method.getParameters()).iterator());
    }

    protected String initialQuery = null;
    protected CharSequence[] defaultSQL(Iterator<VariableElement> iter){
        int paramCount = method.getParameters().size();
        List<String> code = new ArrayList<String>();
        CollectionUtil.addAll(code,
            "java.util.List<String> __conditions = new java.util.ArrayList<String>("+paramCount+");",
            "java.util.List<Object> __params = new java.util.ArrayList<Object>("+paramCount+");"
        );

        List<String> params = new ArrayList<String>();
        List<String> where = new ArrayList<String>();
        while(iter.hasNext()){
            VariableElement param = iter.next();
            String paramName = param.getSimpleName().toString();
            boolean primitive = ModelUtil.isPrimitive(param.asType());
            if(paramName.indexOf('_')==-1){
                ColumnProperty column = getColumn(param);
                where.add("\"+"+column.columnName(true)+"+\"=?");
                params.add(column.toNativeTypeCode(paramName));
                if(!primitive)
                    CollectionUtil.addAll(code,
                        "if("+paramName+"!=null){",
                            PLUS
                    );
                CollectionUtil.addAll(code,
                    "__conditions.add("+where.get(where.size()-1).substring(2)+"\");",
                    "__params.add("+column.toNativeTypeCode(paramName)+");"
                );
                if(!primitive)
                    CollectionUtil.addAll(code,
                            MINUS,
                        "}"
                    );
                iter.remove();
            }else{
                int underscore = paramName.indexOf('_');
                String hint = paramName.substring(0, underscore);
                String propertyName = paramName.substring(underscore+1);
                ColumnProperty column = getColumn(param, propertyName);

                String hintValue = HINTS.get(hint);
                if(hintValue!=null){
                    where.add("\"+"+column.columnName(true)+"+\""+hintValue);
                    params.add(column.toNativeTypeCode(paramName));
                    if(!primitive)
                        CollectionUtil.addAll(code,
                            "if("+paramName+"!=null){",
                                PLUS
                        );
                    CollectionUtil.addAll(code,
                        "__conditions.add("+where.get(where.size()-1).substring(2)+"\");",
                        "__params.add("+column.toNativeTypeCode(paramName)+");"
                    );
                    if(!primitive)
                        CollectionUtil.addAll(code,
                                MINUS,
                            "}"
                        );
                    iter.remove();
                }else if("from".equals(hint)){
                    iter.remove();
                    final VariableElement nextParam = iter.next();
                    final String nextParamName = nextParam.getSimpleName().toString();
                    boolean nextPrimitive = ModelUtil.isPrimitive(nextParam.asType());
                    if(!nextParamName.equals("to_"+propertyName))
                        throw new AnnotationError(method, "the next parameter of "+paramName+" must be to_"+propertyName);
                    if(param.asType()!=nextParam.asType())
                        throw new AnnotationError(method, paramName+" and "+nextParamName+" must be of same type");
                    where.add("\"+"+column.columnName(true)+"+\" BETWEEN ? and ?");
                    params.add(column.toNativeTypeCode(paramName));
                    params.add(column.toNativeTypeCode(nextParamName));
                    if(!primitive || !nextPrimitive){
                        String condition = "";
                        if(!primitive)
                            condition = paramName+"!=null";
                        if(!nextPrimitive){
                            if(condition.length()>0)
                                condition += " && ";
                            condition += nextParamName+"!=null";
                        }
                        CollectionUtil.addAll(code,
                            "if("+condition+"){",
                                PLUS
                        );
                    }
                    CollectionUtil.addAll(code,
                        "__conditions.add("+where.get(where.size()-1).substring(2)+"\");",
                        "__params.add("+column.toNativeTypeCode(paramName)+");",
                        "__params.add("+column.toNativeTypeCode(nextParamName)+");"
                    );
                    if(!primitive || !nextPrimitive)
                        CollectionUtil.addAll(code,
                                MINUS,
                            "}"
                        );
                    iter.remove();
                }else
                    throw new AnnotationError(param, "invalid hint: "+hint);
            }
        }

        Boolean ignoreNullConditions = false;
        try{
            ignoreNullConditions = ModelUtil.getAnnotationValue(method, mirror, "ignoreNullConditions");
        }catch(AnnotationError ex){
            // ignore
        }

        if(ignoreNullConditions){
            String orderByPhrase = orderByPhrase();
            if(orderByPhrase.length()>0)
                initialQuery = "";

            String queryInitialValue;
            if(initialQuery==null)
                queryInitialValue = "null";
            else
                queryInitialValue = '"'+ initialQuery +'"';
            CollectionUtil.addAll(code,
                "String __query = "+queryInitialValue+";",
                "if(__conditions.size()>0)",
                    PLUS,
                        "__query "+(initialQuery==null?"":"+")+"= \" WHERE \" + "+StringUtil.class.getName()+".join(__conditions.iterator(), \" AND \");",
                    MINUS
            );


            if(orderByPhrase.length()>0){
                CollectionUtil.addAll(code,
                    "__query += \""+orderByPhrase+"\";"
                );
            }
            
            CollectionUtil.addAll(code,
                "__query",
                "__params.toArray()"
            );
            return code.toArray(new CharSequence[code.size()]);
        }else{
            StringBuilder query = new StringBuilder();
            if(initialQuery!=null)
                query.append(initialQuery).append(' ');
            if(where.size()>0)
                query.append("WHERE ").append(StringUtil.join(where.iterator(), " AND "));
            query.append(orderByPhrase());
            return new CharSequence[]{
                query,
                StringUtil.join(params.iterator(), ", ")
            };
        }
    }

    private String orderByPhrase(){
        List<String> orderByList = new ArrayList<String>();
        try{
            Collection<AnnotationValue> orderBys = ModelUtil.getAnnotationValue(method, mirror, "orderBy");
            for(AnnotationValue orderByValue: orderBys){
                AnnotationMirror orderByMirror = (AnnotationMirror)orderByValue.getValue();
                String columnProperty = ModelUtil.getAnnotationValue(method, orderByMirror, "column");
                ColumnProperty column = columns.findByProperty(columnProperty);
                if(column==null)
                    throw new AnnotationError(method, mirror, "invalid column property: "+columnProperty);
                Order order = Order.valueOf(((VariableElement)ModelUtil.getAnnotationValue(method, orderByMirror, "order")).getSimpleName().toString());
                orderByList.add("\"+"+column.columnName(true)+"+\" "+order.keyword);
            }
        }catch(AnnotationError ex){
            // ignore
        }
        
        return orderByList.size()>0 ? " ORDER BY "+StringUtil.join(orderByList.iterator(), ", ") : "";
    }
}