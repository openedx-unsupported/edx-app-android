package org.edx.mobile.view;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Allows adding new items to the settings screen.
 * Add your implementations to the {@link ExtensionRegistry}.
 */
public interface SettingsExtension extends ExtensionRegistry.Extension {
    /**
     * Inflate your custom settings views into the parent view. Horizontal dividers will be added automatically.
     * You should use {@link org.edx.mobile.R.dimen.edx_margin} as padding around each item.
     * Use LayoutInflater.from(parent.getContext()) if you need a layout inflater.
     * Use parent.getContext().startActivity() if you need to navigate somewhere or show a dialog.
     */
    void onCreateSettingsView(@NonNull ViewGroup parent);
}
