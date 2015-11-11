package org.edx.mobile.user;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.inject.Inject;

import org.edx.mobile.event.AccountUpdatedEvent;
import org.edx.mobile.task.Task;

import de.greenrobot.event.EventBus;

public abstract class UpdateAccountTask extends
        Task<Account> {

    @Inject
    private UserAPI userAPI;

    @NonNull
    private final String username;

    @NonNull
    private final String field;

    @Nullable
    private final Object value;

    public UpdateAccountTask(@NonNull Context context, @NonNull String username, @NonNull String field, @Nullable Object value) {
        super(context);
        this.username = username;
        this.field = field;
        this.value = value;
    }


    public Account call() throws Exception {
        final Account updatedAccount = userAPI.updateAccount(username, field, value);
        EventBus.getDefault().post(new AccountUpdatedEvent(updatedAccount));
        return updatedAccount;
    }
}
