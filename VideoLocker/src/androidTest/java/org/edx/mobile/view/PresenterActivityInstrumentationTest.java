package org.edx.mobile.view;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.edx.mobile.test.EdxInstrumentationTestApplication;
import org.edx.mobile.test.GenericSuperclassUtils;
import org.junit.Rule;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public abstract class PresenterActivityInstrumentationTest<ActivityT extends PresenterActivity<PresenterT, ViewT>, PresenterT extends Presenter<ViewT>, ViewT> {

    protected ActivityT activity;
    protected ViewT view;
    protected PresenterT presenter;

    @Rule
    public ActivityTestRule<ActivityT> mActivityRule = new ActivityTestRule<>(getActivityType(), true, false);

    protected void startActivity(@Nullable final Intent intent) {
        this.presenter = mock(getPresenterType());
        ((EdxInstrumentationTestApplication) InstrumentationRegistry.getTargetContext().getApplicationContext()).setNextPresenter(presenter);
        this.activity = mActivityRule.launchActivity(intent);
        this.view = activity.view;
    }

    @SuppressWarnings("unchecked")
    private Class<PresenterT> getPresenterType() {
        return (Class<PresenterT>) GenericSuperclassUtils.getTypeArguments(getClass(), PresenterActivityInstrumentationTest.class)[1];
    }

    @SuppressWarnings("unchecked")
    private Class<ActivityT> getActivityType() {
        return (Class<ActivityT>) GenericSuperclassUtils.getTypeArguments(getClass(), PresenterActivityInstrumentationTest.class)[0];
    }
}
