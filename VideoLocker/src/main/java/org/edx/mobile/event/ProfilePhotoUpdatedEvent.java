package org.edx.mobile.event;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ProfilePhotoUpdatedEvent {
    @NonNull
    private final String username;
    @Nullable
    private final Uri uri; // Null if photo was deleted

    public ProfilePhotoUpdatedEvent(@NonNull String username, @Nullable Uri uri) {
        this.username = username;
        this.uri = uri;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @Nullable
    public Uri getUri() {
        return uri;
    }
}
