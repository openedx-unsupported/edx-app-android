package org.edx.mobile.user;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.edx.mobile.discussion.RetroHttpExceptionHandler;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.DateUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.http.Path;
import retrofit.mime.TypedOutput;

@Singleton
public class UserAPI {

    private UserService userService;

    private Logger logger = new Logger(UserAPI.class.getName());

    @Inject
    public UserAPI(@NonNull Config config, @NonNull OkHttpClient client) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat(DateUtil.ISO_8601_DATE_TIME_FORMAT)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setEndpoint(config.getApiHostURL())
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(new RetroHttpExceptionHandler())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        userService = restAdapter.create(UserService.class);
    }

    public Account getAccount(@NonNull String username) throws RetroHttpException {
        return userService.getAccount(username);
    }

    public Account updateAccount(@NonNull String username, @NonNull String field, @NonNull Object value) throws RetroHttpException {
        return userService.updateAccount(username, Collections.singletonMap(field, value));
    }

    public void setProfileImage(@Path("username") String username, @NonNull final ContentResolver contentResolver, @NonNull final Uri uri) throws RetroHttpException, IOException {
        final String mimeType;
        {
            String resolverMimeType = contentResolver.getType(uri);
            if (null == resolverMimeType) {
                // Content resolver might not expose mime type from certain apps, so guess it from the filename
                String ext = FilenameUtils.getExtension(uri.toString());
                if (TextUtils.isEmpty(ext)) {
                    // Oh well, let's assume it's a jpeg
                    mimeType = "image/jpeg";
                } else {
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                }
            } else {
                mimeType = resolverMimeType;
            }
        }
        logger.debug("Uploading file of type " + mimeType + " from " + uri);
        userService.setProfileImage(
                username,
                "attachment;filename=filename." + MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType),
                new TypedOutput() {
                    @Override
                    public String fileName() {
                        return null;
                    }

                    @Override
                    public String mimeType() {
                        return mimeType;
                    }

                    @Override
                    public long length() {
                        return -1;
                    }

                    @Override
                    public void writeTo(OutputStream out) throws IOException {
                        try (InputStream inputStream = contentResolver.openInputStream(uri)) {
                            IOUtils.copy(inputStream, out);
                        }
                    }
                });
        EventBus.getDefault().post(new ProfilePhotoUpdatedEvent(username, uri));
    }
}
