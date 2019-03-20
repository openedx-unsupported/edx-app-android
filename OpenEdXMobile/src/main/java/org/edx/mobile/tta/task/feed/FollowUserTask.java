package org.edx.mobile.tta.task.feed;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class FollowUserTask extends Task<StatusResponse> {

    private String username;

    @Inject
    private TaAPI taAPI;

    public FollowUserTask(Context context, String username) {
        super(context);
        this.username = username;
    }

    @Override
    public StatusResponse call() throws Exception {
        return taAPI.followUser(username).execute().body();
    }
}
