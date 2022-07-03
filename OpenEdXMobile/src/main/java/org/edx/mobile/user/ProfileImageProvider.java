package org.edx.mobile.user;

import androidx.annotation.Nullable;

import org.edx.mobile.model.user.ProfileImage;

public interface ProfileImageProvider {
    @Nullable
    ProfileImage getProfileImage();
}
