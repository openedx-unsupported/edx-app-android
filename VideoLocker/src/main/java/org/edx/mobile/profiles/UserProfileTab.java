package org.edx.mobile.profiles;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

public class UserProfileTab {
    private final
    @StringRes int displayName;
    private final
    @NonNull
    String identifier;
    private final
    @NonNull
    Class<? extends Fragment> fragmentClass;

    public UserProfileTab(@NonNull String identifier, @StringRes int displayName, @NonNull Class<? extends Fragment> fragmentClass) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.fragmentClass = fragmentClass;
    }

    @StringRes
    public int getDisplayName() {
        return displayName;
    }

    @NonNull
    public String getIdentifier() {
        return identifier;
    }

    @NonNull
    public Class<? extends Fragment> getFragmentClass() {
        return fragmentClass;
    }
}
