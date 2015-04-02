package org.edx.mobile.task;

import android.content.Context;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;

import java.io.IOException;

public abstract class TranscriptsProcessingTask extends Task<String> {

    public TranscriptsProcessingTask(Context context) {
        super(context);
    }

    @Override
    protected String doInBackground(Object... params) {
        String url = (String)params[0];
        IApi localApi = ApiFactory.getCacheApiInstance(context);
        try
        {
            String response = localApi.doDownloadTranscript(url);
            return response;
        }
        catch (ParseException localParseException) {
            logger.error(localParseException);
            handle(localParseException);
        }
        catch (ClientProtocolException localClientProtocolException) {
            logger.error(localClientProtocolException);
            handle(localClientProtocolException);
        }
        catch (IOException localIOException) {
            logger.error(localIOException);
            handle(localIOException);

        }
        catch (Exception localException) {
            logger.error(localException);
            handle(localException);
        }
        return null;
    }
    
}
