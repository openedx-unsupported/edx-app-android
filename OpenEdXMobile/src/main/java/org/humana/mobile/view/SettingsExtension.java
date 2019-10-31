package org.humana.mobile.view;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

/**
 * Allows adding new items to the settings screen.
 * Add your implementations to the {@link ExtensionRegistry}.
 */
public interface SettingsExtension extends ExtensionRegistry.Extension {
    /**
     * Inflate your custom settings views into the parent view. Horizontal dividers will be added automatically.
     * You should use {@link org.humana.mobile.R.dimen.edx_margin} as padding around each item.
     * Use LayoutInflater.from(parent.getContext()) if you need a layout inflater.
     * Use parent.getContext().startActivity() if you need to navigate somewhere or showLoading a dialog.
     */
    void onCreateSettingsView(@NonNull ViewGroup parent);
}
