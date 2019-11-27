package org.edx.mobile.test;

import androidx.annotation.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class GenericSuperclassUtils {

    @NonNull
    public static <T extends V, V> Type[] getTypeArguments(@NonNull Class<T> clazz, @NonNull Class<V> superclass) {
        Type type = clazz.getGenericSuperclass();

        while (!(type instanceof ParameterizedType) || ((ParameterizedType) type).getRawType() != superclass) {
            if (type instanceof ParameterizedType) {
                type = ((Class<?>) ((ParameterizedType) type).getRawType()).getGenericSuperclass();
            } else {
                type = ((Class<?>) type).getGenericSuperclass();
            }
        }

        return ((ParameterizedType) type).getActualTypeArguments();
    }
}
