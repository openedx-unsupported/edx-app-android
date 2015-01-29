package org.edx.mobile.task;

import android.content.Context;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.edx.mobile.http.Api;

import java.io.IOException;

public abstract class TranscriptsProcessingTask extends Task<String> {

    public TranscriptsProcessingTask(Context context) {
        super(context);
    }

    @Override
    protected String doInBackground(Object... params) {
        String url = (String)params[0];
        Api localApi = new Api(context);
        try
        {
            String response = localApi.downloadTranscript(url);
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
