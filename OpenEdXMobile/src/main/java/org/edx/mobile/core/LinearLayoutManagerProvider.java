package org.edx.mobile.core;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class LinearLayoutManagerProvider implements Provider<LinearLayoutManager> {

    @Inject
    Context context;

    @Override
    public LinearLayoutManager get() {
        return new LinearLayoutManager(context);
    }
}
