package org.edx.mobile.base;

import android.os.Bundle;

import org.edx.mobile.event.NewRelicEvent;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;

public class BaseFragment extends RoboFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().post(new NewRelicEvent(getClass().getSimpleName()));
    }
}
