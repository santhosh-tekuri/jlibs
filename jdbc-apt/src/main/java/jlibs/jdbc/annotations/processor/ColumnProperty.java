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

package jlibs.jdbc.annotations.processor;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.lang.ClassUtil;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;
import jlibs.jdbc.JavaType;
import jlibs.jdbc.SQLType;
import jlibs.jdbc.annotations.References;
import jlibs.jdbc.annotations.Table;
import jlibs.jdbc.annotations.TypeMapper;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * @author Santhosh Kumar T
 */
abstract class ColumnProperty<E extends Element>{
    public E element;
    public AnnotationMirror annotation;
    protected ColumnProperty(E element, AnnotationMirror annotation){
        this.element = element;
        this.annotation = annotation;
    }

    public String columnName(){
        String columnName = ModelUtil.getAnnotationValue((Element) element, annotation, "name");
        return columnName.length()==0 ? StringUtil.underscore(propertyName()) : columnName;
    }

    public String columnName(boolean quoted){
        String columnName = columnName();
        if(quoted)
            return String.format("jdbc.quote(\"%s\")", StringUtil.toLiteral(columnName, true));
        else
            return StringUtil.toLiteral(columnName, true);
    }

    public boolean primary(){
        return (Boolean)ModelUtil.getAnnotationValue((Element)element, annotation, "primary");
    }

    public boolean auto(){
        return (Boolean)ModelUtil.getAnnotationValue((Element)element, annotation, "auto");
    }

    public boolean nativeType(){
        return (Boolean)ModelUtil.getAnnotationValue((Element)element, annotation, "nativeType");
    }

    protected AnnotationMirror typeMapperMirror(){
        return ModelUtil.getAnnotationMirror(element, TypeMapper.class);
    }
    
    public TypeMirror javaTypeMirror(){
        AnnotationMirror typeMapperMirror = typeMapperMirror();

        if(typeMapperMirror==null)
            return propertyType();
        else
            return (DeclaredType)ModelUtil.getAnnotationValue(element, typeMapperMirror, "mapsTo");
    }

    public String resultSetType(){
        TypeMirror propertyType = javaTypeMirror();
        boolean primitive = ModelUtil.isPrimitive(propertyType);
        boolean primitiveWrapper = ModelUtil.isPrimitiveWrapper(propertyType);

        if(primitive)
            return ModelUtil.toString(propertyType, false);
        else if(primitiveWrapper){
            String type = ModelUtil.toString(propertyType, false);
            return ModelUtil.getPrimitive(type);
        }else if(javaType()==JavaType.OTHER)
            return Object.class.getName();
        else
            return ModelUtil.toString(propertyType, false);
    }

    public JavaType javaType(){
        try{
            Class propertyType = Class.forName(ModelUtil.toString(javaTypeMirror(), true));
            propertyType = ClassUtil.unbox(propertyType);
            JavaType javaType = JavaType.valueOf(propertyType);
            if(javaType==null)
                return nativeType() ? JavaType.OTHER : null;
            else
                return javaType;
        }catch(ClassNotFoundException ex){
            return null;
        }
    }

    public SQLType sqlType(){
        return javaType().sqlTypes[0];
    }

    public TypeMirror typeMapper(){
        AnnotationMirror mirror = typeMapperMirror();
        return mirror==null ? null : (DeclaredType)ModelUtil.getAnnotationValue(element, mirror, "mapper");
    }
    
    public abstract String propertyName();
    public abstract TypeMirror propertyType();
    public abstract String getPropertyCode(String object);
    public abstract String setPropertyCode(String object, String value);

    public String[] getValueFromResultSet(int index){
        String resultSetType = resultSetType();
        int dot = resultSetType.lastIndexOf('.');
        if(dot!=-1)
            resultSetType = resultSetType.substring(dot+1);

        if(ModelUtil.isPrimitive(javaTypeMirror()) || ModelUtil.isPrimitiveWrapper(javaTypeMirror())){
            String name = propertyName();
            return new String[]{
                resultSetType+' '+name+" = rs.get"+StringUtil.capitalize(resultSetType)+'('+index+");",
                "rs.wasNull() ? null : "+name
            };
        }else
            return new String[]{ "rs.get"+StringUtil.capitalize(resultSetType)+'('+index+')' };
    }

    @Override
    public int hashCode(){
        return propertyName().hashCode();
    }

    @Override
    public boolean equals(Object that){
        return that instanceof ColumnProperty && ((ColumnProperty)that).propertyName().equals(this.propertyName());
    }

    // ensure that propertyType is valid javaType that can be fetched from ResultSet
    public void validateType(){
        if(javaType()==null)
            throw new AnnotationError(element, annotation, resultSetType()+" has no mapping SQL Type");
    }

    public String toNativeTypeCode(String value){
        TypeMirror typeMirror = typeMapper();
        if(typeMirror!=null)
            value = "TYPE_MAPPER_"+StringUtil.underscore(propertyName()).toUpperCase()+".userToNative("+value+")";
        return value;
    }

    public String toUserTypeCode(String value){
        TypeMirror typeMirror = typeMapper();
        if(typeMirror!=null)
            value = "TYPE_MAPPER_"+StringUtil.underscore(propertyName()).toUpperCase()+".nativeToUser("+value+")";
        return value;
    }

    public Reference reference;
}

class Reference{
    public final TypeElement tableClass;
    public final String columnName;

    Reference(TypeElement tableClass, String columnName){
        this.tableClass = tableClass;
        this.columnName = columnName;
    }

    public Columns table(){
        return Columns.ALL.get(tableClass);
    }

    public ColumnProperty column(){
        return table().findByProperty(columnName);
    }

    public static Reference find(Element element){
        AnnotationMirror mirror = ModelUtil.getAnnotationMirror(element, References.class);
        if(mirror!=null){
            AnnotationValue tableAnnotationValue = ModelUtil.getRawAnnotationValue(element, mirror, "table");
            TypeElement tableClass = (TypeElement)((DeclaredType)tableAnnotationValue.getValue()).asElement();
            if(ModelUtil.getAnnotationMirror(tableClass, Table.class)==null)
                throw new AnnotationError(element, mirror, tableAnnotationValue, "Reference class "+tableClass.getQualifiedName()+" doesn't has @Table annotation");

            String columnName = ModelUtil.getAnnotationValue(element, mirror, "column");
            return new Reference(tableClass, columnName);
        }else
            return null;
    }
}
