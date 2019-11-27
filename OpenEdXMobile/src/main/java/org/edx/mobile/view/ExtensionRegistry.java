package org.edx.mobile.view;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

/**
 * Keeps track of configured extensions that customize parts of the app.
 * Add your extensions in {@link org.edx.mobile.base.RuntimeApplication#onCreate()}.
 */
@Singleton
public class ExtensionRegistry {
    private final Map<Class<? extends Extension>, Registry<? extends Extension>> registries = new HashMap<>();

    public <T extends Extension> Registry<T> forType(Class<T> extensionType) {
        @SuppressWarnings("unchecked")
        Registry<T> registry = (Registry<T>) registries.get(extensionType);
        if (null == registry) {
            registry = new Registry<>();
            registries.put(extensionType, registry);
        }
        return registry;
    }

    public static class Registry<T> implements Iterable<T> {
        @NonNull
        private final Set<T> items = new LinkedHashSet<>();

        public void add(@NonNull T item) {
            items.add(item);
        }

        @Override
        @NonNull
        public Iterator<T> iterator() {
            return Collections.unmodifiableSet(items).iterator();
        }
    }

    public interface Extension {
    }
}
