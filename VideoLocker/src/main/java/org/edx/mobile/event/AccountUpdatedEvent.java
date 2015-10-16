package org.edx.mobile.event;

import android.support.annotation.NonNull;

import org.edx.mobile.user.Account;

public class AccountUpdatedEvent {
    @NonNull
    private final Account account;

    public AccountUpdatedEvent(@NonNull Account account) {
        this.account = account;
    }

    @NonNull
    public Account getAccount() {
        return account;
    }
}
