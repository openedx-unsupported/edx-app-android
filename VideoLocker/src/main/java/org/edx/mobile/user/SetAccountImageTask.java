package org.edx.mobile.user;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;

public abstract class SetAccountImageTask extends
        Task<Void> {

    @Inject
    private UserAPI userAPI;

    @NonNull
    private final String username;

    @NonNull
    private final Uri uri;

    public SetAccountImageTask(@NonNull Context context, @NonNull String username, @NonNull Uri uri) {
        super(context);
        this.username = username;
        this.uri = uri;
    }


    public Void call() throws Exception {
        userAPI.setProfileImage(username, getContext().getContentResolver(), uri);
        return null;
    }
}
