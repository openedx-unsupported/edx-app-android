package org.edx.mobile.task;

import android.content.Context;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.edx.mobile.services.ServiceManager;

import java.io.IOException;

public abstract class TranscriptsProcessingTask extends Task<String> {
    String url;
    public TranscriptsProcessingTask(Context context, String url) {
        super(context);
        this.url = url;
    }

    @Override
    public String call( ) throws Exception{

        ServiceManager localApi = environment.getServiceManager();
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
