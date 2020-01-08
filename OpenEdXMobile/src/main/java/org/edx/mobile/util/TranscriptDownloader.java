package org.edx.mobile.util;

import android.content.Context;
import android.os.AsyncTask;

import com.google.inject.Inject;

import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.logger.Logger;

import okhttp3.Request;
import okhttp3.Response;
import roboguice.RoboGuice;

/**
 * {@link AsyncTask} to download the transcript in background thread and post the result on UI thread
 * that helps to update the UI component
 */
public abstract class TranscriptDownloader extends AsyncTask<Void, Void, String> {

    private final Logger logger = new Logger(TranscriptDownloader.class.getName());
    private String srtUrl;
    @Inject
    private OkHttpClientProvider okHttpClientProvider;

    public TranscriptDownloader(Context context, String url) {
        this.srtUrl = url;
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    protected String doInBackground(Void... voids) {
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
            return response.body().string();
        } catch (Exception localException) {
            handle(localException);
            logger.error(localException);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        onDownloadComplete(response);
    }

    public abstract void handle(Exception ex);

    public abstract void onDownloadComplete(String response);
}
