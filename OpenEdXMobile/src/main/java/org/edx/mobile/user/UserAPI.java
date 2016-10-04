package org.edx.mobile.user;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.edx.mobile.event.AccountDataLoadedEvent;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.ErrorNotification;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.common.TaskMessageCallback;
import org.edx.mobile.view.common.TaskProgressCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@Singleton
public class UserAPI {
    @Inject
    private UserService userService;

    @Inject
    private Config config;

    @Inject
    private Gson gson;

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
    }

    public
    @NonNull
    List<EnrolledCoursesResponse> getUserEnrolledCourses(@NonNull String username, String org) throws Exception {
        String json = null;

        // if we don't have a json yet, get it from userService
        if (json == null) {
            Response<ResponseBody> response = userService.getUserEnrolledCourses(username, org).execute();
            if (response.isSuccessful()) {
                json = userService.getUserEnrolledCourses(username, org).execute().body().string();
            }
        }

        // We aren't use TypeToken here because it throws NoClassDefFoundError
        final JsonArray ary = gson.fromJson(json, JsonArray.class);
        final List<EnrolledCoursesResponse> ret = new ArrayList<>(ary.size());
        for (int cnt = 0; cnt < ary.size(); ++cnt) {
            ret.add(gson.fromJson(ary.get(cnt), EnrolledCoursesResponse.class));
        }
        return ret;
    }

    public
    @NonNull
    String getUserEnrolledCoursesURL(@NonNull String username) {
        return config.getApiHostURL() + "/api/mobile/v0.5/users/" + username + "/course_enrollments";
    }

    public
    @NonNull
    List<EnrolledCoursesResponse> getUserEnrolledCourses(@NonNull String username, String org, boolean tryCache) throws Exception {
        String json = null;

        final String cacheKey = getUserEnrolledCoursesURL(username);

        // try to get from cache if we should
        if (tryCache) {
            try {
                json = cache.get(cacheKey);
            } catch (IOException | NoSuchAlgorithmException e) {
                logger.debug(e.toString());
            }
        }

        // if we don't have a json yet, get it from userService
        if (json == null) {
            Response<ResponseBody> response = userService.getUserEnrolledCourses(username, org).execute();
            if (response.isSuccessful()) {
                json = userService.getUserEnrolledCourses(username, org).execute().body().string();
                // cache result
                try {
                    cache.put(cacheKey, json);
                } catch (IOException | NoSuchAlgorithmException e) {
                    logger.debug(e.toString());
                }
            } else {
                // Cache has already been checked, and connectivity
                // can't be established, so throw an exception.
                if (tryCache) throw new HttpResponseStatusException(response.code());
                // Otherwise fall back to fetching from the cache
                try {
                    json = cache.get(cacheKey);
                } catch (IOException | NoSuchAlgorithmException e) {
                    logger.debug(e.toString());
                    throw new HttpResponseStatusException(response.code());
                }
                // If the cache is empty, then throw an exception.
                if (json == null) throw new HttpResponseStatusException(response.code());
            }
        }

        // We aren't use TypeToken here because it throws NoClassDefFoundError
        final JsonArray ary = gson.fromJson(json, JsonArray.class);
        final List<EnrolledCoursesResponse> ret = new ArrayList<>(ary.size());
        for (int cnt = 0; cnt < ary.size(); ++cnt) {
            ret.add(gson.fromJson(ary.get(cnt), EnrolledCoursesResponse.class));
        }
        return ret;
    }
}
