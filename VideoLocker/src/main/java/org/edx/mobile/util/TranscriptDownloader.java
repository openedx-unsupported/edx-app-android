package org.edx.mobile.util;

import android.content.Context;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;

import java.io.IOException;

public abstract class TranscriptDownloader implements Runnable {

    private String srtUrl;
    private Context context;
    private final Logger logger = new Logger(TranscriptDownloader.class.getName());

    public TranscriptDownloader(Context context, String url) {
        this.srtUrl = url;
        this.context = context;
    }

    @Override
    public void run() {
        IApi api = ApiFactory.getCacheApiInstance(context);
        try {
            String response = api.doDownloadTranscript(srtUrl);
            onDownloadComplete(response);
        } catch (ParseException localParseException) {
            handle(localParseException);
            logger.error(localParseException);
        } catch (ClientProtocolException localClientProtocolException) {
            handle(localClientProtocolException);
            logger.error(localClientProtocolException);
        } catch (IOException localIOException) {
            handle(localIOException);
            logger.error(localIOException);
        } catch (Exception localException) {
            handle(localException);
            logger.error(localException);
        }
    }

    public abstract void handle(Exception ex);

    public abstract void onDownloadComplete(String response);
}
