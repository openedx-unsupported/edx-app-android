package org.edx.mobile.tta.task.search;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.search.SearchFilter;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class GetSearchFilterTask extends Task<SearchFilter> {

    @Inject
    private TaAPI taAPI;

    public GetSearchFilterTask(Context context) {
        super(context);
    }

    @Override
    public SearchFilter call() throws Exception {
        return taAPI.getSearchFilter().execute().body();
    }
}
