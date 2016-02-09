package org.edx.mobile.view;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

import org.edx.mobile.util.Config;

/**
 * This class exists so you can easily add custom views to the settings screen.
 * This file should not be edited by edX unless absolutely necessary.
 */
public class CustomSettingsExtension {
    public CustomSettingsExtension(@NonNull Config config, @NonNull FragmentManager fragmentManager) {
    }

    /**
     * Inflate your custom settings views into the parent view. Dividers will be added automatically.
     * You should use {@link org.edx.mobile.R.dimen.edx_margin} as padding around each item.
     */
    public void onCreateSettingsView(@NonNull ViewGroup parent) {
        // Add custom code here
        // final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    }

    /**
     * If you have anything to clean up, do it here.
     */
    public void onDestroySettingsView() {
        // Add custom code here
    }
}
