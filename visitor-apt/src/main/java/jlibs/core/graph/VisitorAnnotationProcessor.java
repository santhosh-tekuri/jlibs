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

package jlibs.core.graph;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.annotation.processing.AnnotationProcessor;
import jlibs.core.annotation.processing.Environment;
import jlibs.core.annotation.processing.Printer;
import jlibs.core.graph.sequences.FilteredSequence;
import jlibs.core.graph.sequences.IterableSequence;
import jlibs.core.lang.model.ModelUtil;
import org.kohsuke.MetaInfServices;

import javax.annotation.processing.*;
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
@MetaInfServices(javax.annotation.processing.Processor.class)
public class VisitorAnnotationProcessor extends AnnotationProcessor{
    private static final String METHOD_NAME = "accept";

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
                        pw = Printer.get(c, Visitor.Implement.class, VisitorUtil.FORMAT);
                        boolean implementing = ModelUtil.isAssignable(elem.asType(), Visitor.class);
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
            printer.println(prefix+METHOD_NAME+"(("+type+")obj);");
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
