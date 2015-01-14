package com.github.axet.wget.info.ex;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadMoved extends DownloadRetry {
    private static final long serialVersionUID = 1L;

    URLConnection c;

    public DownloadMoved(URLConnection c) {
        this.c = c;
    }

    public DownloadMoved(Throwable e) {
        super(e);
    }

    public DownloadMoved(String msg) {
        super(msg);
    }

    public URL getMoved() {
        try {
            return new URL(c.getHeaderField("Location"));
        } catch (MalformedURLException e) {
            throw new DownloadError(e);
        }
    }

}
