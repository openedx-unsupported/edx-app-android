package org.edx.mobile.base;

import androidx.annotation.NonNull;

import org.edx.mobile.view.Presenter;
import org.junit.After;
import org.mockito.Mockito;

public abstract class PresenterTest<P extends Presenter<V>, V> extends BaseTest {
    protected P presenter;
    protected V view;

    protected final void startPresenter(@NonNull P p) {
        presenter = p;
        view = Mockito.mock(getViewType());
        presenter.attachView(view);
    }

    @After
    public final void after() {
        if (null != view && null != presenter) {
            presenter.detachView();
            presenter.destroy();
        }
    }

    @SuppressWarnings("unchecked")
    private Class<V> getViewType() {
        return (Class<V>) GenericSuperclassUtils.getTypeArguments(getClass(), PresenterTest.class)[1];
    }
}
