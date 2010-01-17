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

package jlibs.xml.sax.binding;

import jlibs.core.lang.BeanUtil;
import jlibs.core.lang.ImpossibleException;
import org.xml.sax.Attributes;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
@SuppressWarnings({"unchecked"})
@SupportedAnnotationTypes("jlibs.xml.sax.binding.Binding")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BindingAnnotationProcessor extends AbstractProcessor{
    public static final String SUFFIX = "Impl";

    private static final BindingAnnotation BINDING_ELEMENT = new ElementAnnotation();
    private static final BindingAnnotation BINDING_START = new BindingStartAnnotation();
    private static final BindingAnnotation BINDING_TEXT = new BindingTextAnnotation();
    private static final BindingAnnotation BINDING_FINISH = new BindingFinishAnnotation();
    private static final BindingAnnotation RELATION_START = new RelationAnnotation(true);
    private static final BindingAnnotation RELATION_FINISH = new RelationAnnotation(false);

    private static ProcessingEnvironment processingEnv;
    public synchronized void init(ProcessingEnvironment processingEnv) {
    	BindingAnnotationProcessor.processingEnv = processingEnv;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
        for(TypeElement annotation: annotations){
            for(Element elem: roundEnv.getElementsAnnotatedWith(annotation)){
                TypeElement c = (TypeElement)elem;
                Binding binding = new Binding(c);

                try{
                    while(c!=null && !c.getQualifiedName().contentEquals(Object.class.getName())){
                        Map<String, String> nsContext = createNamespaceContext(c);
                        if(binding.nsContext==null)
                            binding.nsContext = nsContext;
                        process(binding, c, nsContext);
                        c = (TypeElement)((DeclaredType)c.getSuperclass()).asElement();
                    }

                    try{
                        generate(binding);
                    }catch(IOException ex){
                        throw new RuntimeException(ex);
                    }finally{
                        try{
                            Printer.get(processingEnv, binding.clazz).ps.close();
                        } catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }catch(ImpossibleException ex){
                    // ignore
                }
            }
        }
        return true;
    }

    private void process(Binding binding, TypeElement c, Map<String, String> map){
        for(ExecutableElement method: ElementFilter.methodsIn(c.getEnclosedElements())){
            for(AnnotationMirror mirror: method.getAnnotationMirrors()){
                if(BINDING_ELEMENT.matches(mirror)){
                    BINDING_ELEMENT.validate(method, mirror);
                    TypeElement bindingClazz = (TypeElement)((DeclaredType)getAnnotationValue(method, mirror, "clazz")).asElement();
                    if(getAnnotationMirror(bindingClazz, jlibs.xml.sax.binding.Binding.class)==null){
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                bindingClazz.getQualifiedName()+" should have annotation "+jlibs.xml.sax.binding.Binding.class.getCanonicalName(),
                                method, mirror);
                        throw new ImpossibleException();
                    }
                    String element = getAnnotationValue(method, mirror, "element");
                    getBinding(binding, method, mirror, map, element).element = bindingClazz;
                }else if(BINDING_START.matches(mirror)){
                    BINDING_START.validate(method, mirror);
                    for(AnnotationValue xpath: (Collection<AnnotationValue>)getAnnotationValue(method, mirror, "value"))
                        getBinding(binding, method, mirror, map, (String)xpath.getValue()).startMethod = method;
                }else if(BINDING_TEXT.matches(mirror)){
                    BINDING_TEXT.validate(method, mirror);
                    for(AnnotationValue xpath: (Collection<AnnotationValue>)getAnnotationValue(method, mirror, "value"))
                        getBinding(binding, method, mirror, map, (String)xpath.getValue()).textMethod = method;
                }else if(BINDING_FINISH.matches(mirror)){
                    BINDING_FINISH.validate(method, mirror);
                    for(AnnotationValue xpath: (Collection<AnnotationValue>)getAnnotationValue(method, mirror, "value"))
                        getBinding(binding, method, mirror, map, (String)xpath.getValue()).finishMethod = method;
                }else if(RELATION_START.matches(mirror)){
                    RELATION_START.validate(method, mirror);
                    Binding parentBinding = getBinding(binding, method, mirror, map, (String)getAnnotationValue(method, mirror, "parent"));
                    for(AnnotationValue child: (Collection<AnnotationValue>)getAnnotationValue(method, mirror, "current"))
                        getRelation(parentBinding, method, mirror, map, (String)child.getValue()).startedMethod = method;
                }else if(RELATION_FINISH.matches(mirror)){
                    RELATION_FINISH.validate(method, mirror);
                    Binding parentBinding = getBinding(binding, method, mirror, map, (String)getAnnotationValue(method, mirror, "parent"));
                    for(AnnotationValue child: (Collection<AnnotationValue>)getAnnotationValue(method, mirror, "current"))
                        getRelation(parentBinding, method, mirror, map, (String)child.getValue()).finishedMethod = method;
                }
            }
        }
    }

    public static String getPackage(TypeElement clazz){
        return ((PackageElement)clazz.getEnclosingElement()).getQualifiedName().toString();
    }

    /*-------------------------------------------------[ Annotation ]---------------------------------------------------*/

    private static boolean matches(AnnotationMirror mirror, Class annotation){
        return ((TypeElement)mirror.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotation.getCanonicalName());
    }

    private static AnnotationMirror getAnnotationMirror(Element elem, Class annotation){
        for(AnnotationMirror mirror: elem.getAnnotationMirrors()){
            if(matches(mirror, annotation))
                return mirror;
        }
        return null;
    }

    private static <T> T getAnnotationValue(Element pos, AnnotationMirror mirror, String method){
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : processingEnv.getElementUtils().getElementValuesWithDefaults(mirror).entrySet()){
            if(entry.getKey().getSimpleName().contentEquals(method))
                return (T)entry.getValue().getValue();
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "annotation "+((TypeElement)mirror.getAnnotationType().asElement()).getQualifiedName()+" is missing "+method, pos, mirror);
        throw new ImpossibleException("can't find method: "+method);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private static <T> T getAnnotationValue(Element elem, Class annotation, String method){
        AnnotationMirror mirror = getAnnotationMirror(elem, annotation);
        if(mirror!=null)
            return (T)getAnnotationValue(elem, mirror, method);
        else
            return null;
    }

    /*-------------------------------------------------[ NamespaceContext ]---------------------------------------------------*/

    private Map<String, String> createNamespaceContext(TypeElement clazz){
        Map<String, String> map = new HashMap<String, String>();
        map.put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
        map.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        map.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);

        AnnotationMirror mirror = getAnnotationMirror(clazz, NamespaceContext.class);
        if(mirror!=null){
            for(AnnotationValue entry: (Collection<AnnotationValue>)getAnnotationValue(clazz, mirror, "value")){
                AnnotationMirror entryMirror = (AnnotationMirror)entry.getValue();
                map.put((String)getAnnotationValue(clazz, entryMirror, "prefix"), (String)getAnnotationValue(clazz, entryMirror, "uri"));
            }
        }
        return map;
    }

    /*-------------------------------------------------[ QName ]---------------------------------------------------*/

    private QName toQName(Element pos1, AnnotationMirror pos2, Map<String, String> nsContext, String token){
        String prefix, localName;
        int colon = token.indexOf(':');
        if(colon==-1){
            prefix = "";
            localName = token;
        }else{
            prefix = token.substring(0, colon);
            localName = token.substring(colon+1);
        }
        String uri = nsContext.get(prefix);
        if(uri==null)
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "no namespace mapping found for prefix "+prefix, pos1, pos2);
        return new QName(uri, localName);
    }

    private static String toJava(QName qname){
        if(qname==null)
            return null;
        else if(qname.getNamespaceURI().length()==0)
            return "new QName(\""+qname.getLocalPart()+"\")";
        else
            return "new QName(\""+qname.getNamespaceURI()+"\", \""+qname.getLocalPart()+"\")";
    }

    /*-------------------------------------------------[ Binding Classes ]---------------------------------------------------*/

    private static class Binding{
        TypeElement clazz;
        Map<String, String> nsContext;
        ExecutableElement startMethod;
        ExecutableElement textMethod;
        ExecutableElement finishMethod;
        TypeElement element;

        Map<QName, BindingRelation> registry = new HashMap<QName, BindingRelation>();

        Binding(TypeElement clazz){
            this.clazz = clazz;
        }

        void process(int id){
            Methods methods = Methods.get(clazz);
            if(startMethod!=null)
                methods.bindingStart.put(id, startMethod);
            if(textMethod!=null)
                methods.bindingText.put(id, textMethod);
            if(finishMethod!=null)
                methods.bindingFinish.put(id, finishMethod);
        }
    }

    private static class Relation{
        TypeElement clazz;
        ExecutableElement startedMethod;
        ExecutableElement finishedMethod;

        Relation(TypeElement clazz){
            this.clazz = clazz;
        }

        void process(int id){
            Methods methods = Methods.get(clazz);
            if(startedMethod!=null)
                methods.relationStart.put(id, startedMethod);
            if(finishedMethod!=null)
                methods.relationFinish.put(id, finishedMethod);
        }
    }

    private static class BindingRelation{
        Binding binding;
        Relation relation;

        BindingRelation(TypeElement clazz){
            binding = new Binding(clazz);
            relation = new Relation(clazz);
        }
    }

    /*-------------------------------------------------[ Binding Helpers ]---------------------------------------------------*/

    private Binding getBinding(Binding binding, Element pos1, AnnotationMirror pos2, Map<String, String> nsContext, String xpath){
        if(xpath.length()==0)
            return binding;
        else
            return getBindingRelation(binding, pos1, pos2, nsContext, xpath).binding;
    }

    private Relation getRelation(Binding binding, Element pos1, AnnotationMirror pos2, Map<String, String> nsContext, String xpath){
        return getBindingRelation(binding, pos1, pos2, nsContext, xpath).relation;
    }

    private BindingRelation getBindingRelation(Binding binding, Element pos1, AnnotationMirror pos2, Map<String, String> nsContext, String xpath){
        BindingRelation bindingRelation = null;

        StringTokenizer stok = new StringTokenizer(xpath, "/");
        Map<QName, BindingRelation> registry = binding.registry;
        while(stok.hasMoreTokens()){
            String token = stok.nextToken();
            QName qname = toQName(pos1, pos2, nsContext, token);
            bindingRelation = registry.get(qname);
            if(bindingRelation==null){
                registry.put(qname, new BindingRelation(binding.clazz));
                bindingRelation = registry.get(qname);
            }
            registry = bindingRelation.binding.registry;
        }

        return bindingRelation;
    }

    /*-------------------------------------------------[ Printer ]---------------------------------------------------*/

    private static class Printer{
        TypeElement clazz;
        String generatedPakage;
        String generatedClazz;

        int indent;
        PrintWriter ps;

        public Printer(int indent, PrintWriter ps){
            this.indent = indent;
            this.ps = ps;
        }

        private boolean doIndent = true;
        private void indent(){
            if(doIndent){
                if(emptyLine){
                    println();
                    emptyLine = false;
                }
                for(int i=0; i<indent; i++)
                    ps.print("    ");
            }
        }
        public void println(String str){
            indent();
            ps.println(str);
            doIndent = true;
        }

        public void print(String str){
            indent();
            ps.print(str);
            doIndent = false;
        }

        private boolean emptyLine;
        public void emptyLine(boolean reqd){
            emptyLine = reqd;
        }

        public void println(){
            ps.println();
            doIndent = true;
        }

        private static Map<TypeElement, Printer> registry = new HashMap<TypeElement, Printer>();
        public static Printer get(ProcessingEnvironment env, TypeElement clazz) throws IOException{
            Printer printer = registry.get(clazz);
            if(printer==null){
                String generatedPakage = getPackage(clazz);
                String generatedClazz = clazz.getSimpleName()+ SUFFIX;

                String generatedClazzQName;
                if(generatedPakage==null||generatedPakage.length()==0)
                    generatedClazzQName = generatedClazz;
                else
                    generatedClazzQName = generatedPakage+"."+generatedClazz;

                PrintWriter writer = new PrintWriter(env.getFiler().createSourceFile(generatedClazzQName).openWriter());
                printer=new Printer(0, writer);
                printer.clazz = clazz;
                printer.generatedPakage = generatedPakage;
                printer.generatedClazz = generatedClazz;
                registry.put(clazz, printer);
            }
            return printer;
        }
    }    

    /*-------------------------------------------------[ Code Generation ]---------------------------------------------------*/

    private static class Methods{
        private static Map<TypeElement, Methods> registry = new HashMap<TypeElement, Methods>();
        static Methods get(TypeElement clazz){
            Methods methods = registry.get(clazz);
            if(methods==null)
                registry.put(clazz, methods=new Methods());
            return methods;
        }

        Map<Integer, ExecutableElement> bindingStart = new TreeMap<Integer, ExecutableElement>();
        Map<Integer, ExecutableElement> bindingText = new TreeMap<Integer, ExecutableElement>();
        Map<Integer, ExecutableElement> bindingFinish = new TreeMap<Integer, ExecutableElement>();
        Map<Integer, ExecutableElement> relationStart = new TreeMap<Integer, ExecutableElement>();
        Map<Integer, ExecutableElement> relationFinish = new TreeMap<Integer, ExecutableElement>();

        void print(Printer pw){
            printMethod(pw, bindingStart, BINDING_START);
            printMethod(pw, bindingText, BINDING_TEXT);
            printMethod(pw, bindingFinish, BINDING_FINISH);
            printMethod(pw, relationStart, RELATION_START);
            printMethod(pw, relationFinish, RELATION_FINISH);
        }

        void printMethod(Printer pw, Map<Integer, ExecutableElement> methods, BindingAnnotation bindingAnnotation){
            if(methods.size()>0){
                pw.println("@Override");
                pw.println(bindingAnnotation.methodDecl);
                pw.indent++;

                pw.println("switch(state){");
                pw.indent++;
                for(Map.Entry<Integer, ExecutableElement> entry : methods.entrySet()){
                    pw.println("case "+entry.getKey()+":");
                    pw.indent++;
                    printMethod(pw, entry.getValue(), bindingAnnotation);
                    pw.println("break;");
                    pw.indent--;
                }
                pw.indent--;
                pw.println("}");

                pw.indent--;
                pw.println("}");
                pw.emptyLine(true);
            }
        }

        void printMethod(Printer pw, ExecutableElement method, BindingAnnotation bindingAnnotation){
            pw.print(bindingAnnotation.lvalue(method));
            if(method.getModifiers().contains(Modifier.STATIC))
                pw.println(pw.clazz.getSimpleName()+"."+method.getSimpleName()+"("+ bindingAnnotation.params(method)+");");
            else
                pw.println("handler."+method.getSimpleName()+"("+ bindingAnnotation.params(method)+");");
        }
    }

    private void generate(Binding binding) throws IOException{
        Printer pw = Printer.get(processingEnv, binding.clazz);

        if(pw.generatedPakage !=null && pw.generatedPakage.length()>0){
            pw.println("package "+pw.generatedPakage +";");
            pw.emptyLine(true);
        }

        pw.println("import jlibs.xml.sax.binding.impl.*;");
        pw.println("import jlibs.xml.sax.binding.SAXContext;");
        pw.println("import org.xml.sax.Attributes;");
        pw.println("import org.xml.sax.SAXException;");
        pw.println("import javax.xml.namespace.QName;");
        pw.println();

        pw.println("/**");
        pw.println(" * DON'T EDIT THIS FILE. THIS IS GENERATED BY JLIBS");
        pw.println(" *");
        pw.println(" * @author Santhosh Kumar T");
        pw.println(" */");

        pw.println("@SuppressWarnings({\"unchecked\"})");
        pw.println("public class "+pw.generatedClazz +" extends BindingCumRelation{");
        pw.indent++;

        pw.print("public static final QName ELEMENT = ");
        AnnotationMirror bindingMirror = getAnnotationMirror(binding.clazz, jlibs.xml.sax.binding.Binding.class);
        if(bindingMirror==null)
            pw.println("null;");
        else{
            String element = getAnnotationValue(binding.clazz, bindingMirror, "value");            
            pw.println(toJava(toQName(binding.clazz, bindingMirror, binding.nsContext, element))+";");
        }

        pw.println("public static final "+pw.generatedClazz +" INSTANCE = new "+pw.generatedClazz +"(new "+binding.clazz.getSimpleName()+"());");
        pw.println("static{");
        pw.indent++;
        pw.println("INSTANCE.init();");
        pw.indent--;
        pw.println("}");
        pw.emptyLine(true);

        pw.println("private final "+binding.clazz.getSimpleName()+" handler;");

        pw.println("private "+pw.generatedClazz +"("+binding.clazz.getSimpleName()+" handler){");
        pw.indent++;
        pw.println("this.handler = handler;");
        pw.indent--;
        pw.println("}");
        pw.emptyLine(true);

        pw.print("private void init()");
        generate(0, binding, pw);

        Methods.get(binding.clazz).print(pw);

        pw.indent--;
        pw.emptyLine(false);
        pw.println("}");
        pw.ps.close();
    }

    private int generate(int id, Binding binding, Printer pw){
        int thisID = id;
        if(binding.registry.size()>0){
            pw.println("{");
            pw.indent++;
            if(id==0)
                pw.println("Delegate leaf = new Delegate(this);");
            for(Map.Entry<QName, BindingRelation> entry: binding.registry.entrySet()){
                QName qname = entry.getKey();
                BindingRelation bindingRelation = entry.getValue();
                Binding childBinding = bindingRelation.binding;

                ++id;

                boolean useDelegate = false;
                for(Map.Entry<QName, BindingRelation> entry1: childBinding.registry.entrySet()){
                    BindingRelation bindingRelation1 = entry1.getValue();
                    Binding childBinding1 = bindingRelation1.binding;
                    if(childBinding1.element==null)
                        useDelegate = true;
                }

                if(useDelegate)
                    pw.print("Registry registry"+id+" = ");
                pw.print("registry"+(thisID>0?thisID:"")+".register(");
                if(childBinding.element!=null){
                    pw.print(toJava(qname)+", 0, ");
                    String className = getPackage(childBinding.element).equals(getPackage(binding.clazz))
                                            ? childBinding.element.getSimpleName().toString()
                                            : childBinding.element.getQualifiedName().toString();
                    pw.print(className+SUFFIX+".INSTANCE, ");
                    childBinding = null;
                }else
                    pw.print(toJava(qname)+", "+id+", "+(useDelegate ?"new Delegate(this)":"leaf")+", ");

                pw.println(id+", this);");
                if(childBinding!=null)
                    id = generate(id, childBinding, pw);
                bindingRelation.relation.process(id);
            }
            pw.indent--;
            pw.println("}");
            pw.emptyLine(true);
        }
        id = thisID;
        binding.process(id);
        return id;
    }

    /*-------------------------------------------------[ Binding Annotations ]---------------------------------------------------*/
    
    private static abstract class BindingAnnotation{
        protected String methodDecl;
        protected Class annotation;

        private BindingAnnotation(Class annotation, String methodDecl){
            this.annotation = annotation;
            this.methodDecl = methodDecl;
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
            if(!modifiers.contains(Modifier.STATIC) && !modifiers.contains(Modifier.FINAL)){
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "method with annotation "+annotation+" must be final", method);
                throw new ImpossibleException();
            }
        }

        protected boolean matches(ExecutableElement method, int paramIndex, Class expected){
            VariableElement param = method.getParameters().get(paramIndex);
            if(param.asType().getKind()== TypeKind.DECLARED){
                Name paramType = ((TypeElement)((DeclaredType)param.asType()).asElement()).getQualifiedName();
                if(paramType.contentEquals(expected.getName()))
                    return true;
            }
            return false;
        }
        
        protected String context(ExecutableElement method, int paramIndex, String defaultArg){
            VariableElement param = method.getParameters().get(paramIndex);
            switch(param.asType().getKind()){
                case DECLARED:
                    Name paramType = ((TypeElement)((DeclaredType)param.asType()).asElement()).getQualifiedName();
                    if(paramType.contentEquals(SAXContext.class.getName()))
                        return defaultArg;
                    else
                        return "("+paramType+")"+defaultArg+".object";
                case INT:
                    return "(java.lang.Integer)"+defaultArg+".object";
                case BOOLEAN:
                case FLOAT:
                case DOUBLE:
                case LONG:
                case BYTE:
                    return "(java.lang."+BeanUtil.firstLetterToUpperCase(param.asType().getKind().toString().toLowerCase())+")"+defaultArg+".object";
                default:
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "method annotated with "+annotation.getCanonicalName()+" can't take "+param.asType().getKind()+" as argument",
                            method);
                    throw new ImpossibleException();
            }
        }
    }

    private static class ElementAnnotation extends BindingAnnotation{
        private ElementAnnotation(){
            super(jlibs.xml.sax.binding.Binding.Element.class, null);
        }

        @Override
        public String params(ExecutableElement method){
            return null;
        }
    }

    private static class BindingStartAnnotation extends BindingAnnotation{
        private BindingStartAnnotation(){
            super(
                jlibs.xml.sax.binding.Binding.Start.class,
                "public void startElement(int state, SAXContext current, Attributes attributes) throws SAXException{"
            );
        }

        public String lvalue(ExecutableElement method){
            if(method.getReturnType().getKind()== TypeKind.VOID)
                return "";
            else
                return "current.object = ";
        }

        private String param(ExecutableElement method, int paramIndex){
            if(matches(method, paramIndex, Attributes.class))
                return "attributes";
            else
                return context(method, paramIndex, "current");
        }
        
        @Override
        public String params(ExecutableElement method){
            switch(method.getParameters().size()){
                case 0:
                    return "";
                case 1:
                    return param(method, 0);
                case 2:
                    return param(method, 0)+", "+param(method, 1);
                default:
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "method annotated with "+annotation.getCanonicalName()+" must not take more than two arguments",
                            method);
                    throw new ImpossibleException();
            }
        }
    }

    private static class BindingTextAnnotation extends BindingAnnotation{
        private BindingTextAnnotation(){
            super(
                jlibs.xml.sax.binding.Binding.Text.class,
                "public void text(int state, SAXContext current, String text) throws SAXException{"
            );
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
                if(!matches(method, method.getParameters().size()-1, String.class)){
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "method annotated with "+annotation.getCanonicalName()+" must take String as last argument",
                            method);
                    throw new ImpossibleException();
                }
            }
            switch(method.getParameters().size()){
                case 1:
                    return "text";
                case 2:
                    return context(method, 0, "current")+", text";
                default:
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "method annotated with "+annotation.getCanonicalName()+" must take either one or two argument(s)",
                            method);
                    throw new ImpossibleException();
            }
        }
    }

    private static class BindingFinishAnnotation extends BindingAnnotation{
        private BindingFinishAnnotation(){
            super(
                jlibs.xml.sax.binding.Binding.Finish.class,
                "public void endElement(int state, SAXContext current) throws SAXException{"
            );
        }

        public String lvalue(ExecutableElement method){
            if(method.getReturnType().getKind()== TypeKind.VOID)
                return "";
            else
                return "current.object = ";
        }

        @Override
        public String params(ExecutableElement method){
            switch(method.getParameters().size()){
                case 0:
                    return "";
                case 1:
                    return context(method, 0, "current");
                default:
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "method annotated with "+annotation.getCanonicalName()+" must not take more than one argument",
                            method);
                    throw new ImpossibleException();
            }
        }
    }

    private static class RelationAnnotation extends BindingAnnotation{
        private RelationAnnotation(boolean start){
            super(
                start ? jlibs.xml.sax.binding.Relation.Start.class : jlibs.xml.sax.binding.Relation.Finish.class,
                "public void "+(start ? "start" : "end")+"Relation(int state, SAXContext parent, SAXContext current) throws SAXException{"
            );
        }

        @Override
        public String params(ExecutableElement method){
            switch(method.getParameters().size()){
                case 2:
                    return context(method, 0, "parent")+", "+ context(method, 1, "current");
                default:
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "method annotated with "+annotation.getCanonicalName()+" must take exactly two arguments",
                            method);
                    throw new ImpossibleException();
            }

        }
    }
}
