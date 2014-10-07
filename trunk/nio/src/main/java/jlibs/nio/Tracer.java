/*
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

package jlibs.nio;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Tracer implements Translator{
    @Override
    public void start(ClassPool pool){}

    @Override
    public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException{
        if(!className.startsWith("jlibs.nio."))
            return;
        CtClass clazz = pool.get(className);
        if(clazz.isEnum() || clazz.isInterface() || clazz.isAnnotation())
            return;
        try{
            Set<CtClass> classes = getClasses(clazz, new LinkedHashSet<>());
            CtMethod[] methods = clazz.getDeclaredMethods();
            for(CtMethod method: methods){
                if(Modifier.isAbstract(method.getModifiers()))
                    continue;
                Trace trace = getTraceAnnotation(method, classes);
                if(trace!=null && trace.condition()){
                    CtMethod copy = CtNewMethod.copy(method, clazz, null);

                    // copy annotations from original method
                    AttributeInfo attr = method.getMethodInfo().getAttribute(AnnotationsAttribute.invisibleTag);
                    if(attr!=null)
                        copy.getMethodInfo().addAttribute(attr);
                    attr = method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
                    if(attr!=null)
                        copy.getMethodInfo().addAttribute(attr);

                    method.setName(method.getName()+"_orig");
                    StringBuilder body = new StringBuilder();
                    body.append("{\n");
                    body.append("jlibs.nio.Debugger.enter(this+\"."+copy.getName()+"(\"+"+trace.args()+"+\")\");\n");
                    body.append("try{\n");
                    if(method.getReturnType().getName().equals("void")){
                        body.append("$proceed($$);\n");
                        body.append("jlibs.nio.Debugger.exit();\n");
                    }else{
                        body.append(method.getReturnType().getName()+" returnValue = $proceed($$);\n");
                        body.append("jlibs.nio.Debugger.exit(\"return \"+returnValue);\n");
                        body.append("return returnValue;\n");
                    }
                    body.append("}catch(Throwable thr){\n");
                    body.append("jlibs.nio.Debugger.exit(\"throw \"+thr);\n");
                    body.append("throw thr;\n");
                    body.append("}\n");
                    body.append("}");
                    copy.setBody(body.toString(), "this", method.getName());
                    clazz.addMethod(copy);
                }
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    private Set<CtClass> getClasses(CtClass clazz, Set<CtClass> classes){
        while(clazz!=null){
            classes.add(clazz);
            try{
                for(CtClass interfase: clazz.getInterfaces())
                    getClasses(interfase, classes);
            }catch(NotFoundException ignore){
                // ignore.printStackTrace();
            }
            try{
                clazz = clazz.getSuperclass();
            }catch(NotFoundException ignore){
                break;
            }
        }
        return classes;
    }

    private Trace getTraceAnnotation(CtMethod method, Set<CtClass> classes) throws ClassNotFoundException{
        for(CtClass clazz: classes){
            try{
                CtMethod m = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
                Trace trace = (Trace)m.getAnnotation(Trace.class);
                if(trace!=null)
                    return trace;
            }catch(NotFoundException ignore){
                // ignore.printStackTrace();
            }
        }
        return null;
    }

    public static boolean runWithTrace(Class clazz, String args[]){
        if(clazz.getClassLoader() instanceof Loader)
            return false;
        if(!Debugger.DEBUG && !Debugger.IO && !Debugger.HTTP)
            return false;
        try{
            Loader loader = new Loader();
            loader.addTranslator(ClassPool.getDefault(), new Tracer());
            loader.run(clazz.getName(), args);
            return true;
        }catch(Throwable thr){
            throw new RuntimeException(thr);
        }
    }
}
