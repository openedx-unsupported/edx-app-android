package org.edx.mobile.user;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.event.AccountDataLoadedEvent;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.view.common.TaskMessageCallback;
import org.edx.mobile.view.common.TaskProgressCallback;

import java.io.File;

import de.greenrobot.event.EventBus;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

@Singleton
public class UserAPI {
    @Inject
    private UserService userService;

    public static class AccountDataUpdatedCallback extends ErrorHandlingCallback<Account> {
        @Inject
        private LoginPrefs loginPrefs;
        @NonNull
        private final String username;

        public AccountDataUpdatedCallback(@NonNull final Context context,
                                          @NonNull final String username,
                                          @NonNull final CallTrigger type) {
            super(context, type);
            this.username = username;
        }

        public AccountDataUpdatedCallback(@NonNull final Context context,
                                          @NonNull final String username,
                                          @NonNull final CallTrigger type,
                                          @Nullable final TaskProgressCallback progressCallback) {
            super(context, type, progressCallback);
            this.username = username;
        }

        public AccountDataUpdatedCallback(@NonNull final Context context,
                                          @NonNull final String username,
                                          @NonNull final CallTrigger type,
                                          @Nullable final TaskMessageCallback messageCallback) {
            super(context, type, messageCallback);
            this.username = username;
        }

        public AccountDataUpdatedCallback(@NonNull final Context context,
                                          @NonNull final String username,
                                          @NonNull final CallTrigger type,
                                          @Nullable final TaskProgressCallback progressCallback,
                                          @Nullable final TaskMessageCallback messageCallback) {
            super(context, type, progressCallback, messageCallback);
            this.username = username;
        }

        @Override
        protected void onResponse(@NonNull final Account account) {
            EventBus.getDefault().post(new AccountDataLoadedEvent(account));
            // Store the logged in user's ProfileImage
            loginPrefs.setProfileImage(username, account.getProfileImage());
        }
    }

    public Call<ResponseBody> setProfileImage(@NonNull String username, @NonNull final File file) {
        final String mimeType = "image/jpeg";
        return userService.setProfileImage(
                username,
                "attachment;filename=filename." + MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType),
                RequestBody.create(MediaType.parse(mimeType), file));
    }

    public static class ProfileImageUpdatedCallback extends ErrorHandlingCallback<ResponseBody> {
        @Inject
        private LoginPrefs loginPrefs;
        @NonNull
        private final String username;
        @Nullable
        private final Uri profileImageUri;

        public ProfileImageUpdatedCallback(@NonNull final Context context,
                                           @NonNull final String username,
                                           @Nullable final File profileImageFile,
                                           @NonNull final CallTrigger type) {
            super(context, type);
            this.username = username;
            profileImageUri = profileImageFile == null ? null : Uri.fromFile(profileImageFile);
        }

        public ProfileImageUpdatedCallback(@NonNull final Context context,
                                           @NonNull final String username,
                                           @Nullable final File profileImageFile,
                                           @NonNull final CallTrigger type,
                                           @Nullable final TaskProgressCallback progressCallback) {
            super(context, type, progressCallback);
            this.username = username;
            profileImageUri = profileImageFile == null ? null : Uri.fromFile(profileImageFile);
        }

        public ProfileImageUpdatedCallback(@NonNull final Context context,
                                           @NonNull final String username,
                                           @Nullable final File profileImageFile,
                                           @NonNull final CallTrigger type,
                                           @Nullable final TaskMessageCallback messageCallback) {
            super(context, type, messageCallback);
            this.username = username;
            profileImageUri = profileImageFile == null ? null : Uri.fromFile(profileImageFile);
        }

        public ProfileImageUpdatedCallback(@NonNull final Context context,
                                           @NonNull final String username,
                                           @Nullable final File profileImageFile,
                                           @NonNull final CallTrigger type,
                                           @Nullable final TaskProgressCallback progressCallback,
                                           @Nullable final TaskMessageCallback messageCallback) {
            super(context, type, progressCallback, messageCallback);
            this.username = username;
            profileImageUri = profileImageFile == null ? null : Uri.fromFile(profileImageFile);
        }

        @Override
        protected void onResponse(@NonNull final ResponseBody response) {
            EventBus.getDefault().post(new ProfilePhotoUpdatedEvent(username, profileImageUri));
            if (profileImageUri == null) {
                // Delete the logged in user's ProfileImage
                loginPrefs.setProfileImage(username, null);
            }
        }
    }
}
