package org.edx.mobile.user;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.task.Task;
import org.edx.mobile.third_party.crop.CropUtil;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

import dagger.hilt.android.EntryPointAccessors;

public class SetAccountImageTask extends Task<Void> {

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
        this.userAPI = EntryPointAccessors.fromApplication(
                context, EdxDefaultModule.ProviderEntryPoint.class).getUserAPI();
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
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        EventBus.getDefault().post(new ProfilePhotoUpdatedEvent(username, uri));
    }

    @Override
    public void onException(Exception ex) {
        // nothing to do
    }
}
