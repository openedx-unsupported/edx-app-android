package org.humana.mobile.tta.task.profile;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.profile.FollowStatus;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class GetFollowStatusTask extends Task<FollowStatus> {

    private String username;

    @Inject
    private TaAPI taAPI;

    public GetFollowStatusTask(Context context, String username) {
        super(context);
        this.username = username;
    }

    @Override
    public FollowStatus call() throws Exception {
        return taAPI.getFollowStatus(username).execute().body();
    }
}
