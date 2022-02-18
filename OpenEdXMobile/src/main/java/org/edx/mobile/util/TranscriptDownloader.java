package org.edx.mobile.util;

import android.content.Context;
import android.os.AsyncTask;

import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.logger.Logger;

import dagger.hilt.android.EntryPointAccessors;
import okhttp3.Request;
import okhttp3.Response;

/**
 * {@link AsyncTask} to download the transcript in background thread and post the result on UI thread
 * that helps to update the UI component
 */
public abstract class TranscriptDownloader extends AsyncTask<Void, Void, String> {

    private final Logger logger = new Logger(TranscriptDownloader.class.getName());
    private String srtUrl;

    OkHttpClientProvider okHttpClientProvider;

    public TranscriptDownloader(Context context, String url) {
        okHttpClientProvider = EntryPointAccessors.fromApplication(context, EdxDefaultModule.ProviderEntryPoint.class).getOkHttpClientProvider();
        this.srtUrl = url;
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
