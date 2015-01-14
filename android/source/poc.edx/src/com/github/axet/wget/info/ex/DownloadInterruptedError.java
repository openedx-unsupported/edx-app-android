package com.github.axet.wget.info.ex;


public class DownloadInterruptedError extends DownloadError {

    private static final long serialVersionUID = 7835308901669107488L;

    public DownloadInterruptedError() {
    }

    public DownloadInterruptedError(Throwable e) {
        super(e);
    }

    public DownloadInterruptedError(String str) {
        super(str);
    }

}
