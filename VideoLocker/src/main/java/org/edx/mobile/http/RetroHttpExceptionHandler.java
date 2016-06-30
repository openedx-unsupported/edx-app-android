package org.edx.mobile.http;

import org.edx.mobile.logger.Logger;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class RetroHttpExceptionHandler implements ErrorHandler {
    protected final Logger logger = new Logger(getClass().getName());

    @Override
    public Throwable handleError(RetrofitError cause) {
        Response response = cause.getResponse();
        logger.warn("url = " + cause.getUrl());
        logger.warn("kind = " + cause.getKind().name());
        if (response != null) {
            String body = new String(((TypedByteArray) response.getBody()).getBytes());
            logger.warn("body = " + body);
            logger.warn("status and reason = " + response.getStatus() + ":" + response.getReason());
            if (RetrofitError.Kind.HTTP == cause.getKind()) {
                return new HttpResponseStatusException(cause, response.getStatus());
            }
        }
        if (RetrofitError.Kind.NETWORK == cause.getKind()) {
            return new HttpConnectivityException(cause);
        }
        return new RetroHttpException(cause);
    }
}