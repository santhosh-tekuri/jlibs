/*
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

package jlibs.xml.sax.binding.impl.processor;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;
import jlibs.xml.sax.binding.*;
import org.xml.sax.Attributes;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
abstract class BindingAnnotation{
    protected String methodDecl;
    protected Class annotation;

    BindingAnnotation(Class annotation, String methodDecl){
        this.annotation = annotation;
        this.methodDecl = methodDecl;
    }

    public void consume(Binding binding, ExecutableElement method, AnnotationMirror mirror){
        validate(method, mirror);
    }
    
    protected boolean matches(AnnotationMirror mirror){
        return ((TypeElement)mirror.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotation.getCanonicalName());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void validate(ExecutableElement method, AnnotationMirror mirror){
        validateModifiers(method);
    }

    public String lvalue(ExecutableElement method){
        return "";
    }

    public abstract String params(ExecutableElement method);

    protected void validateModifiers(ExecutableElement method){
        Collection<Modifier> modifiers = method.getModifiers();
        if(!modifiers.contains(Modifier.STATIC) && !modifiers.contains(Modifier.FINAL))
            throw new AnnotationError(method, "method with annotation "+annotation+" must be final");
    }

    protected boolean matches(ExecutableElement method, int paramIndex, Class expected){
        return matches(method.getParameters().get(paramIndex), expected);
    }

    protected boolean matches(VariableElement param, Class expected){
        if(param.asType().getKind()== TypeKind.DECLARED){
            Name paramType = ((TypeElement)((DeclaredType)param.asType()).asElement()).getQualifiedName();
            if(paramType.contentEquals(expected.getName()))
                return true;
        }
        return false;
    }

    protected String context(ExecutableElement method, int paramIndex, boolean parent){
        return context(method.getParameters().get(paramIndex), parent);
    }

    protected String context(VariableElement param, boolean parent){
        String str = ModelUtil.toString(param.asType());
        if(str.equals(SAXContext.class.getName()))
            return toString(parent);
        else
            return "("+str+")"+toString(parent)+".object";
    }

    private String toString(boolean parent){
        return parent ? "parent" : "current";
    }

    public void printMethod(Printer pw, Binding binding){
        List<ExecutableElement> methods = new ArrayList<ExecutableElement>();
        if(getMethods(binding, methods)){
            pw.println("@Override");
            pw.println(methodDecl);
            pw.indent++;

            pw.println("switch(state){");
            pw.indent++;
            int id = 0;
            for(ExecutableElement method : methods){
                if(method!=null){
                    pw.print("case "+id+":");

                    List<QName> path = binding.idMap.get(id);
                    if(path.size()>0)
                        pw.println(" // "+StringUtil.join(path.iterator(), "/"));
                    else
                        pw.println();

                    pw.indent++;
                    printCase(pw, method);
                    pw.println("break;");
                    pw.indent--;
                }
                id++;
            }
            pw.indent--;
            pw.println("}");

            pw.indent--;
            pw.println("}");
            pw.emptyLine(true);
        }
    }

    abstract boolean getMethods(Binding binding, List<ExecutableElement> methods);

    private void printCase(Printer pw, ExecutableElement method){
        String lvalue = lvalue(method);
        pw.print(lvalue);
        if(method.getModifiers().contains(Modifier.STATIC))
            pw.print(pw.clazz.getSimpleName()+"."+method.getSimpleName()+"("+ params(method)+")");
        else
            pw.print("handler."+method.getSimpleName()+"("+ params(method)+")");
        if(lvalue.length()>0 && !lvalue.endsWith("= "))
            pw.print(")");
        pw.println(";");
    }
    
}

class ElementAnnotation extends BindingAnnotation{
    ElementAnnotation(){
        super(jlibs.xml.sax.binding.Binding.Element.class, null);
    }

    @Override
    public void consume(Binding binding, ExecutableElement method, AnnotationMirror mirror){
        super.consume(binding, method, mirror);
        TypeElement bindingClazz = (TypeElement)((DeclaredType)ModelUtil.getAnnotationValue(method, mirror, "clazz")).asElement();
        if(ModelUtil.getAnnotationMirror(bindingClazz, jlibs.xml.sax.binding.Binding.class)==null)
            throw new AnnotationError(method, mirror, bindingClazz.getQualifiedName()+" should have annotation "+jlibs.xml.sax.binding.Binding.class.getCanonicalName());
        String element = ModelUtil.getAnnotationValue(method, mirror, "element");
        binding.getBinding(method, mirror, element).element = bindingClazz;
    }

    @Override
    public String params(ExecutableElement method){
        return null;
    }

    @Override
    boolean getMethods(Binding binding, List<ExecutableElement> methods){
        throw new UnsupportedOperationException();
    }
}

class BindingStartAnnotation extends BindingAnnotation{
    BindingStartAnnotation(){
        super(
            jlibs.xml.sax.binding.Binding.Start.class,
            "public void startElement(int state, SAXContext current, Attributes attributes) throws SAXException{"
        );
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void consume(Binding binding, ExecutableElement method, AnnotationMirror mirror){
        super.consume(binding, method, mirror);
        for(AnnotationValue xpath: (Collection<AnnotationValue>)ModelUtil.getAnnotationValue(method, mirror, "value"))
            binding.getBinding(method, mirror, (String)xpath.getValue()).startMethod = method;
    }

    public String lvalue(ExecutableElement method){
        if(method.getReturnType().getKind()== TypeKind.VOID)
            return "";
        else
            return "current.object = ";
    }

    @Override
    public String params(ExecutableElement method){
        List<String> params = new ArrayList<String>();
        for(VariableElement param: method.getParameters()){
            AnnotationMirror mirror = ModelUtil.getAnnotationMirror(param, Attr.class);
            if(mirror==null){
                if(matches(param, Attributes.class))
                    params.add("attributes");
                else
                    params.add(context(param, false));
            }else{
                String value = ModelUtil.getAnnotationValue(param, mirror, "value");
                if(value.length()==0)
                    value = param.getSimpleName().toString();

                QName qname = Binding.toQName(param, mirror, value);
                StringBuilder buff = new StringBuilder();
                buff.append("attributes.getValue(");
                if(qname.getNamespaceURI().length()>0)
                    buff.append('"').append(qname.getNamespaceURI()).append("\", ");
                buff.append('"').append(qname.getLocalPart()).append('"');
                buff.append(")");
                params.add(buff.toString());
            }
        }
        return StringUtil.join(params.iterator());
    }

    @Override
    boolean getMethods(Binding binding, List<ExecutableElement> methods){
        boolean nonEmpty = binding.startMethod!=null;
        methods.add(binding.startMethod);
        for(BindingRelation bindingRelation: binding.registry.values())
            nonEmpty |= getMethods(bindingRelation.binding, methods);
        return nonEmpty;
    }
}

class BindingTextAnnotation extends BindingAnnotation{
    BindingTextAnnotation(){
        super(
            jlibs.xml.sax.binding.Binding.Text.class,
            "public void text(int state, SAXContext current, String text) throws SAXException{"
        );
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void consume(Binding binding, ExecutableElement method, AnnotationMirror mirror){
        super.consume(binding, method, mirror);
        for(AnnotationValue xpath: (Collection<AnnotationValue>)ModelUtil.getAnnotationValue(method, mirror, "value"))
            binding.getBinding(method, mirror, (String)xpath.getValue()).textMethod = method;
    }

    public String lvalue(ExecutableElement method){
        if(method.getReturnType().getKind()== TypeKind.VOID)
            return "";
        else
            return "current.object = ";
    }

    @Override
    public String params(ExecutableElement method){
        if(method.getParameters().size()>0){
            if(!matches(method, method.getParameters().size()-1, String.class))
                throw new AnnotationError(method, "method annotated with "+annotation.getCanonicalName()+" must take String as last argument");
        }
        switch(method.getParameters().size()){
            case 1:
                return "text";
            case 2:
                return context(method, 0, false)+", text";
            default:
                throw new AnnotationError(method, "method annotated with "+annotation.getCanonicalName()+" must take either one or two argument(s)");
        }
    }

    @Override
    boolean getMethods(Binding binding, List<ExecutableElement> methods){
        boolean nonEmpty = binding.textMethod!=null;
        methods.add(binding.textMethod);
        for(BindingRelation bindingRelation: binding.registry.values())
            nonEmpty |= getMethods(bindingRelation.binding, methods);
        return nonEmpty; 
    }
}

class BindingFinishAnnotation extends BindingAnnotation{
    BindingFinishAnnotation(){
        super(
            jlibs.xml.sax.binding.Binding.Finish.class,
            "public void endElement(int state, SAXContext current) throws SAXException{"
        );
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void consume(Binding binding, ExecutableElement method, AnnotationMirror mirror){
        super.consume(binding, method, mirror);
        for(AnnotationValue xpath: (Collection<AnnotationValue>)ModelUtil.getAnnotationValue(method, mirror, "value"))
            binding.getBinding(method, mirror, (String)xpath.getValue()).finishMethod = method;
    }

    public String lvalue(ExecutableElement method){
        if(method.getReturnType().getKind()== TypeKind.VOID)
            return "";
        else
            return "current.object = ";
    }

    @Override
    public String params(ExecutableElement method){
        List<String> params = new ArrayList<String>();
        for(VariableElement param: method.getParameters()){
            AnnotationMirror mirror = ModelUtil.getAnnotationMirror(param, Temp.class);
            if(mirror==null)
                params.add(context(param, false));
            else{
                String value = ModelUtil.getAnnotationValue(param, mirror, "value");
                if(value.length()==0)
                    value = param.getSimpleName().toString();

                QName qname = Binding.toQName(param, mirror, value);
                StringBuilder buff = new StringBuilder();

                buff.append("(").append(ModelUtil.toString(param.asType())).append(")");
                buff.append("current.get(");
                buff.append('"').append(qname.getNamespaceURI()).append("\", ");
                buff.append('"').append(qname.getLocalPart()).append('"');
                buff.append(")");
                params.add(buff.toString());
            }
        }
        return StringUtil.join(params.iterator());
    }

    @Override
    boolean getMethods(Binding binding, List<ExecutableElement> methods){
        boolean nonEmpty = binding.finishMethod!=null;
        methods.add(binding.finishMethod);
        for(BindingRelation bindingRelation: binding.registry.values())
            nonEmpty |= getMethods(bindingRelation.binding, methods);
        return nonEmpty;
    }
}

class RelationAnnotation extends BindingAnnotation{
    boolean start;
    RelationAnnotation(boolean start){
        super(
            start ? jlibs.xml.sax.binding.Relation.Start.class : jlibs.xml.sax.binding.Relation.Finish.class,
            "public void "+(start ? "start" : "end")+"Relation(int state, SAXContext parent, SAXContext current) throws SAXException{"
        );
        this.start = start;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void consume(Binding binding, ExecutableElement method, AnnotationMirror mirror){
        super.consume(binding, method, mirror);
        for(AnnotationValue child: (Collection<AnnotationValue>)ModelUtil.getAnnotationValue(method, mirror, "value")){
            String xpath = (String)child.getValue();
            Binding parentBinding;
            int slash = xpath.lastIndexOf('/');
            if(slash==-1){
                parentBinding = binding;
            }else{
                parentBinding = binding.getBinding(method, mirror, xpath.substring(0, slash));
                xpath = xpath.substring(slash+1);
            }
            Relation childRelation = parentBinding.getRelation(method, mirror, xpath);

            if(start)
                childRelation.startedMethod = method;
            else
                childRelation.finishedMethod = method;
        }
    }

    @Override
    public String lvalue(ExecutableElement method){
        TypeMirror mirror = method.getReturnType();
        switch(mirror.getKind()){
            case VOID:
                return "";
            default:
                String m = ModelUtil.getAnnotationMirror(method, Temp.Add.class)==null ? "put" : "add";
                return "parent."+m+"(current.element(), ";
        }
    }

    @Override
    public String params(ExecutableElement method){
        boolean parent = method.getReturnType().getKind() == TypeKind.VOID;

        List<String> params = new ArrayList<String>();
        for(VariableElement param: method.getParameters()){
            AnnotationMirror mirror = ModelUtil.getAnnotationMirror(param, Temp.class);
            if(mirror==null){
                if(ModelUtil.getAnnotationMirror(param, Parent.class)!=null)
                    parent = true;
                else if(ModelUtil.getAnnotationMirror(param, Current.class)!=null)
                    parent = false;
                params.add(context(param, parent));
                parent = !parent;
            }else{
                String value = ModelUtil.getAnnotationValue(param, mirror, "value");
                if(value.length()==0)
                    value = param.getSimpleName().toString();

                QName qname = Binding.toQName(param, mirror, value);
                StringBuilder buff = new StringBuilder();

                buff.append("(").append(ModelUtil.toString(param.asType())).append(")");
                buff.append("current.get(");
                buff.append('"').append(qname.getNamespaceURI()).append("\", ");
                buff.append('"').append(qname.getLocalPart()).append('"');
                buff.append(")");
                params.add(buff.toString());
            }
        }
        return StringUtil.join(params.iterator());
    }
    
    @Override
    boolean getMethods(Binding binding, List<ExecutableElement> methods){
        methods.add(null);
        return _getMethods(binding, methods);
    }

    private boolean _getMethods(Binding binding, List<ExecutableElement> methods){
        boolean nonEmpty = false;
        for(BindingRelation bindingRelation: binding.registry.values()){
            ExecutableElement method = start ? bindingRelation.relation.startedMethod : bindingRelation.relation.finishedMethod;
            nonEmpty |= method!=null;
            methods.add(method);
            nonEmpty |= _getMethods(bindingRelation.binding, methods);
        }
        return nonEmpty;
    }
}
