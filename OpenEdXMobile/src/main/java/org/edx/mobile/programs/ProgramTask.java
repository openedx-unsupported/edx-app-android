package org.edx.mobile.programs;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.task.Task;

import java.util.List;

public class ProgramTask extends Task<List<Programs>> {

    @Inject
    private LoginAPI loginAPI;

    @NonNull
    private final String username;

    public ProgramTask(Context context, String username) {
        super(context);
        this.username = username;
    }

    @Override
    @NonNull
    public List<Programs> call() throws Exception {
        return loginAPI.getMyPrograms(username);
    }
}
