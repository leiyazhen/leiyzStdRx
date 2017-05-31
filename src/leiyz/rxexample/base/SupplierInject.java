package leiyz.rxexample.base;

import java.lang.annotation.*;

/**
 * Created by lyz on 3/15/16.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface SupplierInject {
    boolean cached() default true;
}
