package org.edx.mobile.profiles;

import android.support.annotation.NonNull;

/**
 * Created by aleffert on 5/2/16.
 */
public class UserProfileTab {
    private final @NonNull String displayName;
    private final @NonNull String identifier;
    private final @NonNull Class fragmentClass;

    public UserProfileTab(@NonNull String identifier, @NonNull String displayName, @NonNull Class fragmentClass) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.fragmentClass = fragmentClass;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    @NonNull
    public String getIdentifier() {
        return identifier;
    }

    @NonNull
    public Class getFragmentClass() {
        return fragmentClass;
    }
}
