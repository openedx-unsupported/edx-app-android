package org.edx.mobile.module.serverapi.http;

import android.os.Bundle;

import org.edx.mobile.module.serverapi.IResponse;

/**
 * Created by rohan on 2/6/15.
 */
class IResponseImpl implements IResponse {

    private String response;
    private int statusCode;
    private Bundle headers;

    @Override
    public Bundle getCookies() {
        return headers;
    }

    public void setCookies(Bundle headers) {
        this.headers = headers;
    }

    @Override
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
