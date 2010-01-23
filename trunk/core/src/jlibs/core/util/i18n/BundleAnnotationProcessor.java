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

package jlibs.core.util.i18n;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.annotation.processing.AnnotationProcessor;
import jlibs.core.annotation.processing.Environment;
import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import static jlibs.core.util.i18n.PropertiesUtil.*;

/**
 * @author Santhosh Kumar T
 */
@SuppressWarnings({"unchecked"})
@SupportedAnnotationTypes("jlibs.core.util.i18n.ResourceBundle")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions("ResourceBundle.basename")
public class BundleAnnotationProcessor extends AnnotationProcessor{
    public static final String FORMAT = "${package}._Bundle";
    private static String basename;

    private static class Info{
        private String pakage;
        private List<String> interfaces = new ArrayList<String>();
        private Printer printer;
        private BufferedWriter props;
        private Map<String, ExecutableElement> entries = new HashMap<String, ExecutableElement>();
        private Map<Element, Map<String, ExecutableElement>> classes = new HashMap<Element, Map<String, ExecutableElement>>();

        public Info(TypeElement clazz, Class annotation) throws IOException{
            printer = Printer.get(clazz, annotation, FORMAT);
            pakage = ModelUtil.getPackage(clazz);
            interfaces.add(clazz.getSimpleName().toString());
            FileObject resource = Environment.get().getFiler().createResource(StandardLocation.SOURCE_OUTPUT, pakage, basename+".properties");
            props = new BufferedWriter(resource.openWriter());
        }

        public void add(ExecutableElement method){
            AnnotationMirror mirror = ModelUtil.getAnnotationMirror(method, Message.class);
            if(mirror==null)
                throw new AnnotationError(method, Message.class.getName()+" annotation is missing on this method");
            if(!String.class.getName().equals(ModelUtil.toString(method.getReturnType(), true)))
                throw new AnnotationError(method, "method annotated with "+Message.class.getName()+" must return java.lang.String");

            String signature = ModelUtil.signature(method, false);
            for(ExecutableElement m : entries.values()){
                if(signature.equals(ModelUtil.signature(m, false)))
                    throw new AnnotationError(method, "clashes with similar method in class "+m.getEnclosingElement());
            }

            AnnotationMirror messageMirror = ModelUtil.getAnnotationMirror(method, Message.class);
            String key = ModelUtil.getAnnotationValue(method, messageMirror, "key");
            if(StringUtil.isEmpty(key))
                key = method.getSimpleName().toString();

            ExecutableElement clash = entries.put(key, method);
            Element interfase = method.getEnclosingElement();
            if(clash!=null)
                throw new AnnotationError(method, "key '"+key+"' is already used by \""+ModelUtil.signature(clash, false)+"\" in "+ clash.getEnclosingElement());

            Map<String, ExecutableElement> methods = classes.get(interfase);
            if(methods==null)
                classes.put(interfase, methods=new HashMap<String, ExecutableElement>());
            methods.put(key, method);
        }

        public void generate() throws IOException{
            printer.printPackage();

            printer.println("import java.util.ResourceBundle;");
            printer.println("import java.text.MessageFormat;");
            printer.emptyLine(true);

            printer.printClassDoc();
            printer.println("public class "+printer.generatedClazz +" implements "+StringUtil.join(interfaces.iterator(), ", ")+"{");
            printer.indent++;

            printer.println("public static final "+printer.generatedClazz +" INSTANCE = new "+printer.generatedClazz +"();");
            printer.emptyLine(true);
            printer.println("private final ResourceBundle BUNDLE = ResourceBundle.getBundle(\""+pakage.replace('.', '/')+"/"+basename+"\");");
            printer.emptyLine(true);

            writeComments(props, " DON'T EDIT THIS FILE. THIS IS GENERATED BY JLIBS");
            writeComments(props, " @author Santhosh Kumar T");
            props.newLine();

            Elements elemUtil = Environment.get().getElementUtils();
            for(Map.Entry<Element, Map<String, ExecutableElement>> methods : classes.entrySet()){
                printer.emptyLine(true);
                printer.println("/*-------------------------------------------------[ "+methods.getKey().getSimpleName()+" ]---------------------------------------------------*/");
                printer.emptyLine(true);
                writeComments(props, "-------------------------------------------------[ "+methods.getKey().getSimpleName()+" ]---------------------------------------------------");
                props.newLine();
                
                for(Map.Entry<String, ExecutableElement> entry : methods.getValue().entrySet()){
                    String key = entry.getKey();
                    ExecutableElement method = entry.getValue();

                    String doc = elemUtil.getDocComment(method);
                    String methodDoc = ModelUtil.getMethodDoc(doc);
                    if(!StringUtil.isEmpty(methodDoc))
                        writeComments(props, " "+methodDoc);

                    printer.println("@Override");
                    printer.print("public String "+method.getSimpleName()+"(");

                    int i = 0;
                    StringBuilder params = new StringBuilder();
                    Map<String, String> paramDocs = ModelUtil.getMethodParamDocs(doc);
                    for(VariableElement param : method.getParameters()){
                        String paramName = param.getSimpleName().toString();
                        String paramDoc = paramDocs.get(paramName);
                        if(StringUtil.isEmpty(paramDoc))
                            writeComments(props, " {"+i+"} "+paramName);
                        else
                            writeComments(props, " {"+i+"} "+paramName+" ==> "+paramDoc);

                        params.append(", ");
                        if(i>0)
                            printer.print(", ");
                        params.append(paramName);
                        printer.print(ModelUtil.toString(param.asType(), false)+" "+paramName);
                        i++;
                    }
                    if(params.length()==0)
                        params.append(", new Object[0]");

                    AnnotationMirror messageMirror = ModelUtil.getAnnotationMirror(method, Message.class);
                    String value = ModelUtil.getAnnotationValue(method, messageMirror, "value");

                    try{
                        new MessageFormat(value);
                    }catch(IllegalArgumentException ex){
                        throw new AnnotationError(method, messageMirror, ModelUtil.getRawAnnotationValue(method, messageMirror, "value"), "Invalid Message Format: "+ex.getMessage());
                    }

                    NavigableSet<Integer> args = findArgs(value);
                    int argCount = args.size()==0 ? 0 : (args.last()+1);
                    if(argCount!=method.getParameters().size())
                        throw new AnnotationError(method, "no of args in message format doesn't match with the number of parameters this method accepts");
                    for(i=0; i<argCount; i++){
                        if(!args.remove(i))
                            throw new AnnotationError(method, messageMirror, "{"+i+"} is missing in message");
                    }

                    writeProperty(props, key, value);
                    props.newLine();

                    printer.println("){");
                    printer.indent++;
                    printer.println("return MessageFormat.format(BUNDLE.getString(\""+key+"\")"+params+");");
                    printer.indent--;
                    printer.println("}");
                }
            }

            printer.indent--;
            printer.println("}");
            close();
        }

        public void close() throws IOException{
            if(printer!=null){
                printer.close();
                printer = null;
            }
            if(props!=null){
                props.close();
                props = null;
            }
        }
    }
    private static Map<String, Info> infos = new HashMap<String, Info>();
    
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
        basename = Environment.get().getOptions().get("ResourceBundle.basename");
        if(basename==null)
            basename = "Bundle";
        
        try{
            for(TypeElement annotation: annotations){
                for(Element elem: roundEnv.getElementsAnnotatedWith(annotation)){
                    TypeElement c = (TypeElement)elem;
                    String pakage = ModelUtil.getPackage(c);

                    if(c.getKind()!=ElementKind.INTERFACE)
                        throw new AnnotationError(elem, ResourceBundle.class.getName()+" annotation can be applied only for interface");

                    Info info = infos.get(pakage);
                    if(info==null)
                        infos.put(pakage, info=new Info(c, ResourceBundle.class));
                    else
                        info.interfaces.add(c.getSimpleName().toString());

                    while(c!=null && !c.getQualifiedName().contentEquals(Object.class.getName())){
                        for(ExecutableElement method: ElementFilter.methodsIn(c.getEnclosedElements()))
                            info.add(method);
                        c = ModelUtil.getSuper(c);
                    }
                }
            }

            for(Map.Entry<String, Info> entry : infos.entrySet())
                entry.getValue().generate();
        }catch(AnnotationError error){
            error.report();
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }finally{
            for(Info info : infos.values()){
                try{
                    info.close();
                }catch(IOException ignore){
                    // ignore
                }
            }
            infos.clear();
        }
        return true;
    }
}
