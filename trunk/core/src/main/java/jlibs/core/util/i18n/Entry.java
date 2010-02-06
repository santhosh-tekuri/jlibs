package jlibs.core.util.i18n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Santhosh Kumar T
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Entry{
    String value() default "";

    String lhs() default "";
    String hintName() default "";
    Hint hint() default Hint.NONE;

    String rhs() default "";
}
