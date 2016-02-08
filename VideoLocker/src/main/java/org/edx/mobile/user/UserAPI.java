package org.edx.mobile.user;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.LastAccessedSubsectionResponse;
import org.edx.mobile.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;
import retrofit.mime.TypedFile;

@Singleton
public class UserAPI {

    private UserService userService;

    private Logger logger = new Logger(UserAPI.class.getName());

    @Inject
    public UserAPI(@NonNull RestAdapter restAdapter) {
        userService = restAdapter.create(UserService.class);
    }

    public Account getAccount(@NonNull String username) throws RetroHttpException {
        return userService.getAccount(username);
    }

    public Account updateAccount(@NonNull String username, @NonNull String field, @Nullable Object value) throws RetroHttpException {
        return userService.updateAccount(username, Collections.singletonMap(field, value));
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

    public List<EnrolledCoursesResponse> getUserEnrolledCourses(@NonNull String username) throws RetroHttpException {
        return userService.getUserEnrolledCourses(username);
    }

    public LastAccessedSubsectionResponse getLastAccessedSubsection(@NonNull String username, @NonNull String courseId) {
        return userService.getLastAccessedSubsection(username, courseId);
    }

    public LastAccessedSubsectionResponse syncLastAccessedSubsection(@NonNull String username, @NonNull String courseId, @NonNull String lastVisitedModuleId) {
        Map<String, String> body = new HashMap<>();
        body.put("last_visited_module_id", lastVisitedModuleId);
        String date = DateUtil.getModificationDate();
        body.put("modification_date", date);
        return userService.syncLastAccessedSubsection(username, courseId, body);
    }
}
