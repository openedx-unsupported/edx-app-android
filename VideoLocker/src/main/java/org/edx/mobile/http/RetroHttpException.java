package org.edx.mobile.http;

import retrofit.RetrofitError;

/**
 * This sould be the base class of all http exception?
 */
public class RetroHttpException extends Exception{
    public final RetrofitError cause;
    public RetroHttpException(RetrofitError cause){
        this.cause = cause;
    }
}
