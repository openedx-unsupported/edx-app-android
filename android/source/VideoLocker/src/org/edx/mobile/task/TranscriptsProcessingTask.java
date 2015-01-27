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
            localParseException.printStackTrace();
            handle(localParseException);
        }
        catch (ClientProtocolException localClientProtocolException) {
            localClientProtocolException.printStackTrace();
            handle(localClientProtocolException);
        }
        catch (IOException localIOException) {
            localIOException.printStackTrace();
            handle(localIOException);
        }
        catch (Exception localException) {
            localException.printStackTrace();
            handle(localException);
        }
        return null;
    }
    
}
