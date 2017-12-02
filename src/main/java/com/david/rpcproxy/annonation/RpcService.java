package com.david.rpcproxy.annonation;

import java.lang.annotation.*;

/**
 * service name annotation
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RpcService {
    String value() default "";
}
