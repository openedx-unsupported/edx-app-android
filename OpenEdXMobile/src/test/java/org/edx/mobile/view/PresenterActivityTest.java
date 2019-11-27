
package org.edx.mobile.view;

import android.content.Intent;
import androidx.annotation.NonNull;

import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.test.GenericSuperclassUtils;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import static org.mockito.Mockito.mock;

public abstract class PresenterActivityTest<ActivityT extends PresenterActivity<PresenterT, ViewT>, PresenterT extends Presenter<ViewT>, ViewT> extends BaseTestCase {

    protected ActivityT activity;
    protected ViewT view;
    protected PresenterT presenter;

    protected void startActivity(@NonNull final Intent intent) {
        final ActivityController<ActivityT> controller = Robolectric.buildActivity(
                getActivityType(), intent);
        this.activity = controller.get();
        this.presenter = mock(getPresenterType());
        activity.presenter = presenter;
        controller.setup();
        this.view = activity.view;
    }

    @SuppressWarnings("unchecked")
    private Class<PresenterT> getPresenterType() {
        return (Class<PresenterT>) GenericSuperclassUtils.getTypeArguments(getClass(), PresenterActivityTest.class)[1];
    }

    @SuppressWarnings("unchecked")
    private Class<ActivityT> getActivityType() {
        return (Class<ActivityT>) GenericSuperclassUtils.getTypeArguments(getClass(), PresenterActivityTest.class)[0];
    }
}
