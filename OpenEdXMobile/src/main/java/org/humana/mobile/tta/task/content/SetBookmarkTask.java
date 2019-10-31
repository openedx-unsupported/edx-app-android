package org.humana.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.content.BookmarkResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class SetBookmarkTask extends Task<BookmarkResponse> {

    private long contentId;

    @Inject
    private TaAPI taAPI;

    public SetBookmarkTask(Context context, long contentId) {
        super(context);
        this.contentId = contentId;
    }

    @Override
    public BookmarkResponse call() throws Exception {
        return taAPI.setBookmark(contentId).execute().body();
    }
}
