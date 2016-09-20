package org.edx.mobile.test;

import android.support.annotation.NonNull;

import org.edx.mobile.CustomRobolectricTestRunner;
import org.edx.mobile.util.observer.MainThreadObservable;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Executor;

@Ignore
@RunWith(CustomRobolectricTestRunner.class)
public class BaseTest {
    @Before
    public final void beforeBaseTest() {
        MainThreadObservable.EXECUTOR = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                command.run();
            }
        };
        MockitoAnnotations.initMocks(this);
    }
}
