package jlibs.core.graph;

import jlibs.core.lang.model.ModelUtil;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Santhosh Kumar T
 */
public class VisitorUtil{
    @SuppressWarnings({"unchecked"})
    public static <E, R> Visitor<E, R> createVisitor(Object delegate){
        try{
            Class implClass = ModelUtil.findClass(delegate.getClass(), VisitorAnnotationProcessor.FORMAT);
            return (Visitor<E, R>)implClass.getConstructor(delegate.getClass()).newInstance(delegate);
        }catch(ClassNotFoundException ex){
            throw new RuntimeException(ex);
        } catch(InvocationTargetException ex){
            throw new RuntimeException(ex);
        } catch(NoSuchMethodException ex){
            throw new RuntimeException(ex);
        } catch(InstantiationException ex){
            throw new RuntimeException(ex);
        } catch(IllegalAccessException ex){
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <E, R> Visitor<E, R> createVisitor(Class clazz){
        try{
            return (Visitor<E, R>)ModelUtil.findClass(clazz, VisitorAnnotationProcessor.FORMAT).newInstance();
        }catch(ClassNotFoundException ex){
            throw new RuntimeException(ex);
        } catch(InstantiationException ex){
            throw new RuntimeException(ex);
        } catch(IllegalAccessException ex){
            throw new RuntimeException(ex);
        }
    }
}
