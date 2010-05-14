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

package jlibs.core.graph;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.annotation.processing.AnnotationProcessor;
import jlibs.core.annotation.processing.Environment;
import jlibs.core.annotation.processing.Printer;
import jlibs.core.graph.sequences.FilteredSequence;
import jlibs.core.graph.sequences.IterableSequence;
import jlibs.core.lang.model.ModelUtil;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.util.*;

import static jlibs.core.annotation.processing.Printer.*;

/**
 * @author Santhosh Kumar T
 */
@SupportedAnnotationTypes("jlibs.core.graph.Visitor.Implement")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class VisitorAnnotationProcessor extends AnnotationProcessor{
    private static final String METHOD_NAME = "doVisit";
    private static final String SUFFIX = "Impl";
    public static final String FORMAT = "${package}.${class}"+SUFFIX;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
        for(TypeElement annotation: annotations){
            for(Element elem: roundEnv.getElementsAnnotatedWith(annotation)){
                Map<TypeMirror, ExecutableElement> classes = new LinkedHashMap<TypeMirror, ExecutableElement>();

                try{
                    TypeElement c = (TypeElement)elem;
                    while(c!=null && !c.getQualifiedName().contentEquals(Object.class.getName())){
                        process(classes, c);
                        c = ModelUtil.getSuper(c);
                    }
                    c = (TypeElement)elem;

                    Printer pw = null;
                    try{
                        pw = Printer.get(c, Visitor.Implement.class, FORMAT);
                        boolean implementing = Environment.get().getTypeUtils().isAssignable(elem.asType(), Environment.get().getElementUtils().getTypeElement(Visitor.class.getCanonicalName()).asType());
                        boolean isFinal = c.getModifiers().contains(Modifier.FINAL);
                        if(!isFinal && implementing)
                            generateExtendingClass(classes, pw);
                        else
                            generateDelegatingClass(classes, pw);
                    }catch(IOException ex){
                        throw new RuntimeException(ex);
                    }finally{
                        if(pw!=null)
                            pw.close();
                    }
                }catch(AnnotationError error){
                    error.report();
                }
            }
        }
        return true;
    }

    private boolean accept(ExecutableElement method){
        return method.getSimpleName().contentEquals(METHOD_NAME) && method.getParameters().size() == 1;
    }

    private void process(Map<TypeMirror, ExecutableElement> classes, TypeElement c){
        for(ExecutableElement method: ElementFilter.methodsIn(c.getEnclosedElements())){
            if(accept(method)){
                TypeMirror mirror = method.getParameters().get(0).asType();
                if(!classes.containsKey(mirror))
                    classes.put(mirror, method);
            }
        }
    }

    private void generateExtendingClass(Map<TypeMirror, ExecutableElement> classes, Printer printer){
        printer.printPackage();

        printer.printClassDoc();
        printer.printlns(
            "@SuppressWarnings(\"unchecked\")",
            "public class "+printer.generatedClazz +" extends "+ printer.clazz.getSimpleName()+"{",
                PLUS
        );
        printVisitMethod("", classes, printer);
        printer.printlns(
                MINUS,
            "}"
        );
    }

    private void generateDelegatingClass(Map<TypeMirror, ExecutableElement> classes, Printer printer){
        printer.printPackage();

        printer.printClassDoc();
        printer.printlns(
            "public class "+printer.generatedClazz +" implements "+ Visitor.class.getCanonicalName()+"{",
                PLUS,
                "private final "+printer.clazz.getSimpleName()+" delegate;",
                "public "+printer.generatedClazz+"("+printer.clazz.getSimpleName()+" delegate){",
                    PLUS,
                    "this.delegate = delegate;",
                    MINUS,
                "}"
        );
        printer.emptyLine(true);
        printVisitMethod("delegate.", classes, printer);
        printer.printlns(
                MINUS,
            "}"
        );
    }

    private void printVisitMethod(String prefix, Map<TypeMirror, ExecutableElement> classes, Printer printer){
        printer.println("@Override");
        printer.println("public Object visit(Object obj){");
        printer.indent++;

        List<TypeMirror> list = new ArrayList<TypeMirror>(classes.keySet());
        Collections.reverse(list);
        boolean addElse = false;
        for(TypeMirror mirror: sort(list)){
            String type = ModelUtil.toString(mirror, true);
            if(addElse)
                printer.print("else ");
            else
                addElse = true;
            printer.println("if(obj instanceof "+type+")");
            printer.indent++;
            if(classes.get(mirror).getReturnType().getKind()!=TypeKind.VOID)
                printer.print("return ");
            printer.println(prefix+"doVisit(("+type+")obj);");
            printer.indent--;
        }
        printer.println();
        printer.println("return null;");

        printer.indent--;
        printer.println("}");
    }

    public static List<TypeMirror> sort(final Sequence<TypeMirror> classes){
        return WalkerUtil.topologicalSort(classes, new Navigator<TypeMirror>(){
            @Override
            public Sequence<TypeMirror> children(final TypeMirror parent){
                return new FilteredSequence<TypeMirror>(classes.copy(), new Filter<TypeMirror>(){
                    @Override
                    public boolean select(TypeMirror child){
                        return Environment.get().getTypeUtils().isSubtype(parent, child);
                    }
                });
            }
        });
    }

    public static List<TypeMirror> sort(Collection<TypeMirror> classes){
        return sort(new IterableSequence<TypeMirror>(classes));
    }
}
