package org.edx.mobile.user;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;

public abstract class UpdateAccountTask extends
        Task<Account> {

    @Inject
    private UserAPI userAPI;

    @NonNull
    private final String username;

    @NonNull
    private final String field;

    @NonNull
    private final String value;

    public UpdateAccountTask(@NonNull Context context, @NonNull String username, @NonNull String field, @NonNull String value) {
        super(context);
        this.username = username;
        this.field = field;
        this.value = value;
    }


    public Account call() throws Exception {
        return userAPI.updateAccount(username, field, value);
    }
}
