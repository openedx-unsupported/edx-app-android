package org.edx.mobile.user;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.edx.mobile.event.AccountUpdatedEvent;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.http.ApiConstants;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.http.cache.CacheManager;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.Page;
import org.edx.mobile.profiles.BadgeAssertion;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.Config;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedInput;

@Singleton
public class UserAPI {
    @NonNull
    private final UserService userService;

    private Logger logger = new Logger(UserAPI.class.getName());

    @Inject
    Config config;

    @Inject
    CacheManager cache;

    @Inject Gson gson;

    @Inject
    public UserAPI(@NonNull RestAdapter restAdapter) {
        userService = restAdapter.create(UserService.class);
    }

    public Account getAccount(@NonNull String username) throws RetroHttpException {
        return userService.getAccount(username);
    }

    public Account updateAccount(@NonNull String username, @NonNull String field, @Nullable Object value) throws RetroHttpException {
        final Account updatedAccount = userService.updateAccount(username, Collections.singletonMap(field, value));
        EventBus.getDefault().post(new AccountUpdatedEvent(updatedAccount));
        return updatedAccount;
    }

    public void setProfileImage(@NonNull String username, @NonNull final File file) throws RetroHttpException, IOException {
        final String mimeType = "image/jpeg";
        logger.debug("Uploading file of type " + mimeType + " from " + file.toString());
        userService.setProfileImage(
                username,
                "attachment;filename=filename." + MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType),
                new TypedFile(mimeType, file));
        EventBus.getDefault().post(new ProfilePhotoUpdatedEvent(username, Uri.fromFile(file)));
    }

    public void deleteProfileImage(@NonNull String username) throws RetroHttpException {
        userService.deleteProfileImage(username);
        EventBus.getDefault().post(new ProfilePhotoUpdatedEvent(username, null));
    }

    public Page<BadgeAssertion> getBadges(@NonNull String username, int page) throws RetroHttpException {
        return userService.getBadges(username, page, ApiConstants.STANDARD_PAGE_SIZE);
    }

    public @NonNull String getUserEnrolledCoursesURL(@NonNull String username) {
        return config.getApiHostURL() + "/api/mobile/v0.5/users/" + username + "/course_enrollments";
    }

    public List<EnrolledCoursesResponse> getUserEnrolledCourses(@NonNull String username, boolean tryCache) throws RetroHttpException {
        List<EnrolledCoursesResponse> ret = null;
        String json = null;

        final String cacheKey = getUserEnrolledCoursesURL(username);

        // try to get from cache if we should
        if (tryCache) {
            try {
                json = cache.get(cacheKey);
            }
            catch (IOException | NoSuchAlgorithmException e) {
                logger.debug(e.toString());
            }
        }

        // if we don't have a json yet, get it from userService
        if (json == null) {
            Response response = userService.getUserEnrolledCourses(username);

            if (response != null) {
                TypedInput input = response.getBody();
                if (input != null) {
                    try {
                        json = IOUtils.toString(input.in());
                    }
                    catch (IOException e) {
                        json = null;
                        logger.debug(e.toString());
                    }
                }
            }
        }

        if (json != null) {

            // cache result
            try {
                cache.put(cacheKey, json);
            }
            catch (IOException | NoSuchAlgorithmException e) {
                logger.debug(e.toString());
            }

            // NOTE: trying to pass in List<EnrolledCourseResponse> (with the help of token type) yields NoClassDefFoundError
            JsonArray ary = gson.fromJson(json, JsonArray.class);
            ret = new ArrayList<>(ary.size());
            for (int cnt=0; cnt<ary.size(); ++cnt) {
                ret.add(gson.fromJson(ary.get(cnt), EnrolledCoursesResponse.class));
            }
        }

        return ret;
    }
}
