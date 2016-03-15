package org.edx.mobile.test.http;

import org.edx.mobile.view.ExtensionRegistry;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

public class ExtensionRegistryTest {

    @Test
    public void forType_withNoExtensions_returnsEmptyRegistry() {
        final ExtensionRegistry extensionRegistry = new ExtensionRegistry();
        assertThat(extensionRegistry.forType(TestExtension.class).iterator().hasNext(), is(false));
    }

    @Test
    public void forType_withAddedExtension_returnsRegistryWithAddedExtension() {
        final ExtensionRegistry extensionRegistry = new ExtensionRegistry();
        final TestExtension extension = mock(TestExtension.class);
        extensionRegistry.forType(TestExtension.class).add(extension);
        assertThat(extensionRegistry.forType(TestExtension.class).iterator().hasNext(), is(true));
        assertThat(extensionRegistry.forType(TestExtension.class).iterator().next(), is(extension));
    }

    @Test
    public void forType_withExtensionOfAlternateType_returnsEmptyRegistry() {
        final ExtensionRegistry extensionRegistry = new ExtensionRegistry();
        extensionRegistry.forType(AlternateTestExtension.class).add(mock(AlternateTestExtension.class));
        assertThat(extensionRegistry.forType(TestExtension.class).iterator().hasNext(), is(false));
    }

    public interface TestExtension extends ExtensionRegistry.Extension {
    }

    public interface AlternateTestExtension extends ExtensionRegistry.Extension {
    }
}
