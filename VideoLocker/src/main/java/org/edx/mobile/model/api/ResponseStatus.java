package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

class ResponseStatus {

    @SerializedName("status_code")
    private String code;
    
    @SerializedName("status_message")
    private String message;
    
    public ResponseStatus(){
    }

    public ResponseStatus(ResponseStatus resStat){
        code = resStat.getCode();
        message = resStat.getMessage();
    }
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    
}
