package org.edx.mobile.profiles;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserProfileViewModel {
    @NonNull
    public final LimitedProfileMessage limitedProfileMessage;

    @Nullable
    public final String language;

    @Nullable
    public final String location;

    @NonNull
    public final UserProfileBioModel bio;

    public UserProfileViewModel(@NonNull LimitedProfileMessage message, @Nullable String language, @Nullable String location, UserProfileBioModel bio) {
        limitedProfileMessage = message;
        this.language = language;
        this.location = location;
        this.bio = bio;
    }

    public enum LimitedProfileMessage {
        NONE,
        OWN_PROFILE,
        OTHER_USERS_PROFILE
    }
}
