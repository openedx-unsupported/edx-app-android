package com.github.axet.wget.info.ex;

import java.io.IOException;

public class DownloadIOError extends DownloadError {

    private static final long serialVersionUID = 7835308901669107488L;

    public DownloadIOError() {
    }

    public DownloadIOError(IOException e) {
        super(e);
    }

    public DownloadIOError(String str) {
        super(str);
    }

}
