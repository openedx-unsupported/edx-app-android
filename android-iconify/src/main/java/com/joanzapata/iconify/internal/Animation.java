package com.joanzapata.iconify.internal;

import androidx.annotation.CheckResult;
import androidx.annotation.Nullable;

public enum Animation {
    SPIN("spin"), PULSE("pulse"), NONE(null);

    @Nullable
    private final String token;

    Animation(@Nullable String token) {
        this.token = token;
    }

    @CheckResult
    @Nullable
    String getToken() {
        return token;
    }
}
