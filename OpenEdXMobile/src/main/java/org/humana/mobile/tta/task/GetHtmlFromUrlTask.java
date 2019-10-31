package org.humana.mobile.tta.task;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import okhttp3.HttpUrl;

public class GetHtmlFromUrlTask extends Task<Void> {

    private HttpUrl absoluteUrl;

    @Inject
    private TaAPI taAPI;

    public GetHtmlFromUrlTask(Context context, HttpUrl absoluteUrl) {
        super(context);
        this.absoluteUrl = absoluteUrl;
    }

    @Override
    public Void call() throws Exception {
        return taAPI.getHtmlFromUrl(absoluteUrl).execute().body();
    }
}
