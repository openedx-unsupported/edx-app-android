package org.edx.mobile.event;

import android.net.Uri;
import android.support.annotation.NonNull;

import org.edx.mobile.user.Account;

public class ProfilePhotoUpdatedEvent {
    @NonNull
    private final String username;
    @NonNull
    private final Uri uri;

    public ProfilePhotoUpdatedEvent(@NonNull String username, @NonNull Uri uri) {
        this.username = username;
        this.uri = uri;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public Uri getUri() {
        return uri;
    }
}
