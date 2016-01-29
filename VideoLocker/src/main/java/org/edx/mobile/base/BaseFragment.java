package org.edx.mobile.base;

import android.os.Bundle;

import com.newrelic.agent.android.NewRelic;

import roboguice.fragment.RoboFragment;

public class BaseFragment extends RoboFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NewRelic.setInteractionName("Display " + getClass().getSimpleName());
    }
}
