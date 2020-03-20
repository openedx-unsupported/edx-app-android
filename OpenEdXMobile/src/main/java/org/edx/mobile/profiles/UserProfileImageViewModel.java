package org.edx.mobile.profiles;

import android.net.Uri;
import androidx.annotation.Nullable;

public class UserProfileImageViewModel {
    @Nullable
    public final Uri uri;

    public final boolean shouldReadFromCache;

    public UserProfileImageViewModel(@Nullable Uri uri, boolean shouldReadFromCache) {
        this.uri = uri;
        this.shouldReadFromCache = shouldReadFromCache;
    }
}
