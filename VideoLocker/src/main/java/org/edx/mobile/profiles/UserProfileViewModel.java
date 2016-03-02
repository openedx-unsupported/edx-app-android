package org.edx.mobile.profiles;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class UserProfileViewModel {
    @NonNull
    public final LimitedProfileMessage limitedProfileMessage;

    @Nullable
    public final String language;

    @Nullable
    public final String location;

    @NonNull
    public final ContentType contentType;

    @Nullable
    public final String bio;

    public UserProfileViewModel(@NonNull LimitedProfileMessage message, @Nullable String language, @Nullable String location, @NonNull ContentType contentType, @Nullable String bio) {
        limitedProfileMessage = message;
        this.language = language;
        this.location = location;
        this.contentType = contentType;
        this.bio = bio;
    }

    public enum LimitedProfileMessage {
        NONE,
        OWN_PROFILE,
        OTHER_USERS_PROFILE
    }

    public enum ContentType {
        EMPTY,
        NO_ABOUT_ME,
        INCOMPLETE,
        PARENTAL_CONSENT_REQUIRED,
        ABOUT_ME
    }
}
