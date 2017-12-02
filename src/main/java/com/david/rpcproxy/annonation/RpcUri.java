package com.david.rpcproxy.annonation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * service uri annotation
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcUri {
    String value() default "";
}
