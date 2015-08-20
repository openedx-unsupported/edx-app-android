package org.edx.mobile;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;

/**
 * Extends {@link RobolectricGradleTestRunner} to add custom shadows to the mapping.
 */
public class CustomRobolectricTestRunner extends RobolectricGradleTestRunner {
    public CustomRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    public InstrumentationConfiguration createClassLoaderConfig() {
        return InstrumentationConfiguration.newBuilder()
                .addInstrumentedPackage("roboguice.util.")
                .build();
    }
}
