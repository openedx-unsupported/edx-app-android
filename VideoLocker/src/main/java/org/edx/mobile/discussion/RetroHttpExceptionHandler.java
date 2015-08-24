package org.edx.mobile.discussion;

import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.logger.Logger;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 *
 */
public class RetroHttpExceptionHandler implements ErrorHandler {
    protected final Logger logger = new Logger(getClass().getName());
    @Override public Throwable handleError(RetrofitError cause) {
        Response r = cause.getResponse();
        logger.warn("url = " + cause.getUrl());
        logger.warn( "kind = " + cause.getKind().name());
        if ( r != null )
            logger.warn( "status and reason = " + r.getStatus()  + ":" + r.getReason());
       // int status = r.getStatus();
       // if (r != null && r.getStatus() == 401) {
            return new RetroHttpException(cause);
        //}
        //return cause;
    }
}