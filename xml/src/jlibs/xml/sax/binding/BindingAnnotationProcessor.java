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

import jlibs.core.lang.ImpossibleException;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
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
                    }
                }catch(ImpossibleException ex){
                    // ignore
                }
            }
        }
        return false;
    }

    private void process(Binding binding, TypeElement c, Map<String, String> map){
        for(ExecutableElement method: ElementFilter.methodsIn(c.getEnclosedElements())){
            for(AnnotationMirror mirror: method.getAnnotationMirrors()){
                if(matches(mirror, jlibs.xml.sax.binding.Binding.Element.class)){
                    validateMethod(method,  jlibs.xml.sax.binding.Binding.Element.class);
                    String element = getAnnotationValue(method, mirror, "element");
                    TypeElement bindingClazz = (TypeElement)((DeclaredType)getAnnotationValue(method, mirror, "clazz")).asElement();
                    if(getAnnotationMirror(bindingClazz, jlibs.xml.sax.binding.Binding.class)==null){
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                bindingClazz.getQualifiedName()+" should have annotation "+jlibs.xml.sax.binding.Binding.class.getCanonicalName(),
                                method, mirror);
                        throw new ImpossibleException();
                    }
                    getBinding(binding, method, mirror, map, element).element = bindingClazz;
                }else if(matches(mirror, jlibs.xml.sax.binding.Binding.Start.class)){
                    validateMethod(method,  jlibs.xml.sax.binding.Binding.Start.class);
                    for(AnnotationValue xpath: (Collection<AnnotationValue>)getAnnotationValue(method, mirror, "value"))
                        getBinding(binding, method, mirror, map, (String)xpath.getValue()).startMethod = method;
                }else if(matches(mirror, jlibs.xml.sax.binding.Binding.Text.class)){
                    validateMethod(method,  jlibs.xml.sax.binding.Binding.Text.class);
                    for(AnnotationValue xpath: (Collection<AnnotationValue>)getAnnotationValue(method, mirror, "value"))
                        getBinding(binding, method, mirror, map, (String)xpath.getValue()).textMethod = method;
                }else if(matches(mirror, jlibs.xml.sax.binding.Binding.Finish.class)){
                    validateMethod(method,  jlibs.xml.sax.binding.Binding.Finish.class);
                    for(AnnotationValue xpath: (Collection<AnnotationValue>)getAnnotationValue(method, mirror, "value"))
                        getBinding(binding, method, mirror, map, (String)xpath.getValue()).finishMethod = method;
                }else if(matches(mirror, jlibs.xml.sax.binding.Relation.Start.class)){
                    validateMethod(method,  jlibs.xml.sax.binding.Relation.Start.class);
                    Binding parentBinding = getBinding(binding, method, mirror, map, (String)getAnnotationValue(method, mirror, "parent"));
                    for(AnnotationValue child: (Collection<AnnotationValue>)getAnnotationValue(method, mirror, "current"))
                        getRelation(parentBinding, method, mirror, map, (String)child.getValue()).startedMethod = method;
                }else if(matches(mirror, jlibs.xml.sax.binding.Relation.Finish.class)){
                    validateMethod(method,  jlibs.xml.sax.binding.Relation.Finish.class);
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

    private void validateMethod(ExecutableElement method, Class annotation){
        Collection<Modifier> modifiers = method.getModifiers();
        if(!modifiers.contains(Modifier.STATIC) && !modifiers.contains(Modifier.FINAL))
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "method with annotation "+annotation+" must be final", method);
    }

    /*-------------------------------------------------[ Annotation ]---------------------------------------------------*/

    private boolean matches(AnnotationMirror mirror, Class annotation){
        return ((TypeElement)mirror.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotation.getCanonicalName());
    }

    private AnnotationMirror getAnnotationMirror(Element elem, Class annotation){
        for(AnnotationMirror mirror: elem.getAnnotationMirrors()){
            if(matches(mirror, annotation))
                return mirror;
        }
        return null;
    }

    private <T> T getAnnotationValue(Element pos, AnnotationMirror mirror, String method){
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : processingEnv.getElementUtils().getElementValuesWithDefaults(mirror).entrySet()){
            if(entry.getKey().getSimpleName().contentEquals(method))
                return (T)entry.getValue().getValue();
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "annotation "+((TypeElement)mirror.getAnnotationType().asElement()).getQualifiedName()+" is missing "+method, pos, mirror);
        throw new ImpossibleException("can't find method: "+method);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private <T> T getAnnotationValue(Element elem, Class annotation, String method){
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
            printMethod(pw, bindingStart, "public void startElement(int state, SAXContext current, Attributes attributes) throws SAXException{", "current, attributes");
            printMethod(pw, bindingText, "public void text(int state, SAXContext current, String text) throws SAXException{", "current, text");
            printMethod(pw, bindingFinish, "public void endElement(int state, SAXContext current) throws SAXException{", "current");
            printMethod(pw, relationStart, "public void startRelation(int state, SAXContext parent, SAXContext current) throws SAXException{", "parent, current");
            printMethod(pw, relationFinish, "public void endRelation(int state, SAXContext parent, SAXContext current) throws SAXException{", "parent, current");
        }

        void printMethod(Printer pw, Map<Integer, ExecutableElement> methods, String methodDecl, String args){
            if(methods.size()>0){
                pw.println("@Override");
                pw.println(methodDecl);
                pw.indent++;

                pw.println("switch(state){");
                pw.indent++;
                for(Map.Entry<Integer, ExecutableElement> entry : methods.entrySet()){
                    pw.println("case "+entry.getKey()+":");
                    pw.indent++;
                    printMethod(pw, entry.getValue(), args);
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

        void printMethod(Printer pw, ExecutableElement method, String args){
            if(method.getModifiers().contains(Modifier.STATIC))
                pw.println(pw.clazz.getSimpleName()+"."+method.getSimpleName()+"("+args+");");
            else
                pw.println("handler."+method.getSimpleName()+"("+args+");");
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
        if(bindingMirror!=null)
            pw.println("null;");
        else{
            String element = getAnnotationValue(binding.clazz, bindingMirror, "value");            
            pw.println(toJava(toQName(binding.clazz, bindingMirror, binding.nsContext, element))+";");
        }

        pw.println("public static final "+pw.generatedClazz +" INSTANCE = new "+pw.generatedClazz +"(new "+binding.clazz.getSimpleName()+"());");

        pw.emptyLine(true);
        pw.println("private final "+binding.clazz.getSimpleName()+" handler;");

        pw.println("private "+pw.generatedClazz +"("+binding.clazz.getSimpleName()+" handler){");
        pw.indent++;
        pw.println("this.handler = handler;");
        pw.println("init();");
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
}
