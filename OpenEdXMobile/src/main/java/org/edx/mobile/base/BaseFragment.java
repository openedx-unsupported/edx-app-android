package org.edx.mobile.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.event.NewRelicEvent;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;

public class BaseFragment extends RoboFragment {
    private boolean isFirstVisit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().post(new NewRelicEvent(getClass().getSimpleName()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        isFirstVisit = true;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstVisit) {
            isFirstVisit = false;
        } else {
            onRevisit();
        }
    }

    /**
     * Called when a Fragment is re-displayed to the user (the user has navigated back to it).
     * Defined to mock the behavior of {@link Activity#onRestart() Activity.onRestart} function.
     */
    protected void onRevisit() {
    }
}
