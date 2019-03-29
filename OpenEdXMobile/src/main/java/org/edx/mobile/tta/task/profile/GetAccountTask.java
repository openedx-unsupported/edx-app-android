package org.edx.mobile.tta.task.profile;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.UserAPI;

public class GetAccountTask extends Task<Account> {

    private String username;

    @Inject
    private UserAPI userAPI;

    public GetAccountTask(Context context, String username) {
        super(context);
        this.username = username;
    }

    @Override
    public Account call() throws Exception {
        return userAPI.getAccount(username).execute().body();
    }
}
