package org.humana.mobile.user;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.humana.mobile.event.AccountDataLoadedEvent;
import org.humana.mobile.http.callback.CallTrigger;
import org.humana.mobile.http.callback.ErrorHandlingCallback;
import org.humana.mobile.http.notifications.ErrorNotification;
import org.humana.mobile.module.prefs.LoginPrefs;
import org.humana.mobile.util.Config;
import org.humana.mobile.view.common.TaskMessageCallback;
import org.humana.mobile.view.common.TaskProgressCallback;

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
    @Inject
    private Config config;

    public static class AccountDataUpdatedCallback extends ErrorHandlingCallback<Account> {
        @Inject
        private LoginPrefs loginPrefs;
        @NonNull
        private final String username;

        public AccountDataUpdatedCallback(@NonNull final Context context,
                                          @NonNull final String username,
                                          @Nullable final ErrorNotification errorNotification) {
            this(context, username, null, errorNotification);
        }

        public AccountDataUpdatedCallback(@NonNull final Context context,
                                          @NonNull final String username,
                                          @Nullable final TaskProgressCallback progressCallback,
                                          @Nullable final ErrorNotification errorNotification) {
            super(context, progressCallback, errorNotification);
            this.username = username;
        }

        //TODO: Remove this legacy code starting from here, when modern error design has been implemented on all screens i.e. SnackBar, FullScreen and Dialog based errors.
        public AccountDataUpdatedCallback(@NonNull final Context context,
                                          @NonNull final String username,
                                          @Nullable final TaskProgressCallback progressCallback,
                                          @Nullable TaskMessageCallback messageCallback,
                                          @Nullable CallTrigger callTrigger) {
            super(context, progressCallback, messageCallback, callTrigger);
            this.username = username;
        }
        // LEGACY CODE ENDS HERE, all occurrences of this constructor should also be updated in future

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

    public
    @NonNull
    String getUserEnrolledCoursesURL(@NonNull String username) {
        return config.getApiHostURL() + "/api/mobile/v0.5/users/" + username + "/course_enrollments";
        //return config.getApiHostURL() + "/api/mobile/v0.5/users/" + "ArjunManprax" + "/course_enrollments";
    }


    //TTA

    public Call<Account> getAccount(String username){
        return userService.getAccount(username);
    }
}
