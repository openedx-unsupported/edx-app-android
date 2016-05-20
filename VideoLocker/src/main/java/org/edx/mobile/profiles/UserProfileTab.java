package org.edx.mobile.profiles;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

public class UserProfileTab {
    private final
    @StringRes int displayName;
    private final
    @NonNull
    Class<? extends Fragment> fragmentClass;

    public UserProfileTab(@StringRes int displayName, @NonNull Class<? extends Fragment> fragmentClass) {
        this.displayName = displayName;
        this.fragmentClass = fragmentClass;
    }

    @StringRes
    public int getDisplayName() {
        return displayName;
    }

    @NonNull
    public Class<? extends Fragment> getFragmentClass() {
        return fragmentClass;
    }
}
