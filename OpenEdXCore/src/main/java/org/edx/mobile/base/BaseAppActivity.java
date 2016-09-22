package org.edx.mobile.base;

import android.content.Context;
import android.os.Bundle;

import com.newrelic.agent.android.NewRelic;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseAppActivity extends RoboAppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NewRelic.setInteractionName("Display " + getClass().getSimpleName());
    }
}
