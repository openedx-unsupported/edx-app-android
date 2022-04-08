package org.edx.mobile.discovery.model;

import retrofit2.Response;

public class ResponseError extends Exception{
    private Response response;
    private String msg="";

    public ResponseError(Response response) {
        response=response;
    }

    public int getCode() {
        return response.code();
    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getErrorType() {
        return response.raw().message();
    }



    public String getErrorDetail() {
        return response.message();
    }


}
