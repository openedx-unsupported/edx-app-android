package org.edx.mobile.tta.task.search;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.search.FilterSection;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class SearchTask extends Task<List<Content>> {

    private int take, skip;
    private boolean isPriority;
    private long listId;
    private String searchText;
    private List<FilterSection> filterSections;

    @Inject
    private TaAPI taAPI;

    public SearchTask(Context context, int take, int skip, boolean isPriority, long listId, String searchText, List<FilterSection> filterSections) {
        super(context);

        this.take = take;
        this.skip = skip;
        this.isPriority = isPriority;
        this.listId = listId;
        this.searchText = searchText;
        this.filterSections = filterSections;
    }

    @Override
    public List<Content> call() throws Exception {
        return taAPI.search(take, skip, isPriority, listId, searchText, filterSections).execute().body();
    }
}
