package org.edx.mobile.user;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;

public abstract class GetAccountTask extends
        Task<Account> {

    @Inject
    private UserAPI userAPI;

    @NonNull
    private final String username;

    public GetAccountTask(@NonNull Context context, @NonNull String username) {
        super(context);
        this.username = username;
    }


    public Account call() throws Exception {
        return userAPI.getAccount(username);
    }
}
