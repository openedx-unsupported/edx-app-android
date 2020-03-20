package org.edx.mobile.user;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.task.Task;

import de.greenrobot.event.EventBus;

public class DeleteAccountImageTask extends
        Task<Void> {

    @Inject
    private UserService userService;

    @Inject
    private LoginPrefs loginPrefs;

    @NonNull
    private final String username;

    public DeleteAccountImageTask(@NonNull Context context, @NonNull String username) {
        super(context);
        this.username = username;
    }


    public Void call() throws Exception {
        userService.deleteProfileImage(username).execute();
        return null;
    }

    @Override
    protected void onSuccess(Void response) throws Exception {
        EventBus.getDefault().post(new ProfilePhotoUpdatedEvent(username, null));
        // Delete the logged in user's ProfileImage
        loginPrefs.setProfileImage(username, null);
    }
}
