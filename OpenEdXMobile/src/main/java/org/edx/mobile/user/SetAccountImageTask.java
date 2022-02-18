package org.edx.mobile.user;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.task.Task;
import org.edx.mobile.third_party.crop.CropUtil;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class SetAccountImageTask extends Task<Object> {

    @Inject
    UserAPI userAPI;

    @NonNull
    private final String username;

    @NonNull
    private Uri uri;

    @NonNull
    private final Rect cropRect;

    public SetAccountImageTask(@NonNull Context context, @NonNull String username, @NonNull Uri uri, @NonNull Rect cropRect) {
        super(context);
        this.username = username;
        this.uri = uri;
        this.cropRect = cropRect;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final File cropped = new File(context.get().getExternalCacheDir(), "cropped-image" + System.currentTimeMillis() + ".jpg");
        try {
            CropUtil.crop(context.get(), uri, cropRect, 500, 500, cropped);
            userAPI.setProfileImage(username, cropped).execute();
            uri = Uri.fromFile(cropped);
        } catch (IOException e) {
            handleException(e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object unused) {
        super.onPostExecute(unused);
        EventBus.getDefault().post(new ProfilePhotoUpdatedEvent(username, uri));
    }

    @Override
    public void onException(Exception ex) {
        // nothing to do
    }
}
