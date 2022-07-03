package org.edx.mobile.user;

import android.content.Context;

import androidx.annotation.NonNull;

import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.task.Task;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import dagger.hilt.android.EntryPointAccessors;

public class DeleteAccountImageTask extends Task<Void> {

    UserService userService;
    LoginPrefs loginPrefs;

    @NonNull
    private final String username;

    public DeleteAccountImageTask(@NonNull Context context, @NonNull String username) {
        super(context);
        this.username = username;
        EdxDefaultModule.ProviderEntryPoint provider = EntryPointAccessors.fromApplication(
                context, EdxDefaultModule.ProviderEntryPoint.class);
        userService = provider.getUserService();
        loginPrefs = provider.getLoginPrefs();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            userService.deleteProfileImage(username).execute();
        } catch (IOException e) {
            logger.error(e);
            handleException(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        EventBus.getDefault().post(new ProfilePhotoUpdatedEvent(username, null));
        // Delete the logged in user's ProfileImage
        loginPrefs.setProfileImage(username, null);
    }

    @Override
    public void onException(Exception ex) {
        // nothing to do
    }
}
