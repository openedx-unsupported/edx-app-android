package com.github.axet.wget.info.ex;

public class DownloadIOCodeError extends DownloadError {

    private static final long serialVersionUID = 7835308901669107488L;

    int code;

    public DownloadIOCodeError() {
    }

    public DownloadIOCodeError(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
