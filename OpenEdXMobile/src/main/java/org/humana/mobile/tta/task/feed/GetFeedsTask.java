package org.humana.mobile.tta.task.feed;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Feed;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetFeedsTask extends Task<List<Feed>> {

    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetFeedsTask(Context context, int take, int skip) {
        super(context);
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<Feed> call() throws Exception {
        return taAPI.getFeeds(take, skip).execute().body();
    }
}
