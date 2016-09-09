package org.edx.mobile.view;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.facebook.testing.screenshot.Screenshot;

import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.test.EdxInstrumentationTestApplication;
import org.edx.mobile.test.GenericSuperclassUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public abstract class PresenterActivityInstrumentationTest<ActivityT extends PresenterActivity<PresenterT, ViewT>, PresenterT extends Presenter<ViewT>, ViewT> {

    protected ActivityT activity;
    protected ViewT view;
    protected PresenterT presenter;

    @Rule
    public ActivityTestRule<ActivityT> mActivityRule = new ActivityTestRule<>(getActivityType(), true, false);

    @Rule
    public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @Rule
    public TestName testName = new TestName();

    @Before
    public void before() {
        this.presenter = mock(getPresenterType());
        ((EdxInstrumentationTestApplication) InstrumentationRegistry.getTargetContext().getApplicationContext()).setNextPresenter(presenter);
        this.activity = mActivityRule.launchActivity(null);
        // To simplify tests, we automatically execute view methods on the application's UI thread.
        this.view = UiThreadInvocationHandler.newProxyInstance(uiThreadTestRule, activity.view, getViewType());
    }

    @After
    public void after() {
        Screenshot.snap(activity.findViewById(android.R.id.content)).setName(getClass().getName() + "_" + testName.getMethodName()).record();
    }

    @SuppressWarnings("unchecked")
    private Class<ActivityT> getActivityType() {
        return (Class<ActivityT>) GenericSuperclassUtils.getTypeArguments(getClass(), PresenterActivityInstrumentationTest.class)[0];
    }

    @SuppressWarnings("unchecked")
    private Class<PresenterT> getPresenterType() {
        return (Class<PresenterT>) GenericSuperclassUtils.getTypeArguments(getClass(), PresenterActivityInstrumentationTest.class)[1];
    }

    @SuppressWarnings("unchecked")
    private Class<ViewT> getViewType() {
        return (Class<ViewT>) GenericSuperclassUtils.getTypeArguments(getClass(), PresenterActivityInstrumentationTest.class)[2];
    }

    public static class UiThreadInvocationHandler<ViewT> implements InvocationHandler {

        @NonNull
        private final UiThreadTestRule uiThreadTestRule;

        @NonNull
        private final ViewT impl;

        public UiThreadInvocationHandler(@NonNull UiThreadTestRule uiThreadTestRule, @NonNull ViewT impl) {
            this.uiThreadTestRule = uiThreadTestRule;
            this.impl = impl;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final AsyncTaskResult<Object> result = new AsyncTaskResult<>();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        result.setResult(method.invoke(impl, args));
                    } catch (Exception e) {
                        result.setEx(e);
                    }
                }
            };
            if (Looper.myLooper() == Looper.getMainLooper()) {
                // Already on UI thread
                runnable.run();
            } else {
                uiThreadTestRule.runOnUiThread(runnable);
            }
            if (null != result.getEx()) {
                throw result.getEx();
            }
            return result.getResult();
        }

        @SuppressWarnings("unchecked")
        @NonNull
        public static <ViewT> ViewT newProxyInstance(@NonNull UiThreadTestRule uiThreadTestRule, @NonNull ViewT view, @NonNull Class<ViewT> viewType) {
            return (ViewT) Proxy.newProxyInstance(viewType.getClassLoader(), new Class<?>[]{viewType}, new UiThreadInvocationHandler(uiThreadTestRule, view));
        }
    }
}
