package org.edx.mobile.util;

import android.content.Context;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.edx.mobile.http.Api;

import java.io.IOException;

public abstract class TranscriptDownloader implements Runnable {

    private String srtUrl;
    private Context context;

    public TranscriptDownloader(Context context, String url) {
        this.srtUrl = url;
        this.context = context;
    }

    @Override
    public void run() {
        Api localApi = new Api(context);
        try {
            String response = localApi.downloadTranscript(srtUrl);
            onDownloadComplete(response);
        } catch (ParseException localParseException) {
            handle(localParseException);
        } catch (ClientProtocolException localClientProtocolException) {
            handle(localClientProtocolException);
        } catch (IOException localIOException) {
            handle(localIOException);
        } catch (Exception localException) {
            handle(localException);
        }
    }

    public abstract void handle(Exception ex);

    public abstract void onDownloadComplete(String response);
}
