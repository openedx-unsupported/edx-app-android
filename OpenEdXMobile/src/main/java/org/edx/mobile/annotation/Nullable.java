package org.edx.mobile.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Added as an alternative of {@link roboguice.inject.Nullable} to support injecting of nullable
 * views using {@link roboguice.inject.InjectView}.
 * Ref: https://stackoverflow.com/a/29946794
 */
@Target({ElementType.FIELD, ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Nullable {
}
