package org.edx.mobile.util;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.logger.Logger;

import okhttp3.Request;
import okhttp3.Response;
import roboguice.RoboGuice;

public abstract class TranscriptDownloader implements Runnable {

    private String srtUrl;
    @Inject
    private OkHttpClientProvider okHttpClientProvider;
    private final Logger logger = new Logger(TranscriptDownloader.class.getName());

    public TranscriptDownloader(Context context, String url) {
        this.srtUrl = url;
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public void run() {
        try {
            final Response response = okHttpClientProvider.getWithOfflineCache()
                    .newCall(new Request.Builder()
                            .url(srtUrl)
                            .get()
                            .build())
                    .execute();
            if (!response.isSuccessful()) {
                throw new HttpStatusException(response);
            }
            onDownloadComplete(response.body().string());
        } catch (Exception localException) {
            handle(localException);
            logger.error(localException);
        }
    }

    public abstract void handle(Exception ex);

    public abstract void onDownloadComplete(String response);
}
