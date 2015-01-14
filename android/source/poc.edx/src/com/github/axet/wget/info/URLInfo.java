package com.github.axet.wget.info;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.axet.wget.Direct;
import com.github.axet.wget.RetryWrap;
import com.github.axet.wget.info.ex.DownloadRetry;

/**
 * URLInfo - keep all information about source in one place. Thread safe.
 * 
 * @author axet
 * 
 */
public class URLInfo extends BrowserInfo {
    /**
     * source url
     */
    private URL source;

    /**
     * have been extracted?
     */
    private boolean extract = false;

    /**
     * null if size is unknown, which means we unable to restore downloads or do
     * multi thread downlaods
     */
    private Long length;

    /**
     * does server support for the range param?
     */
    private boolean range;

    /**
     * null if here is no such file or other error
     */
    private String contentType;

    /**
     * come from Content-Disposition: attachment; filename="fname.ext"
     */
    private String contentFilename;

    /**
     * Notify States
     */
    public enum States {
        EXTRACTING, EXTRACTING_DONE, DOWNLOADING, RETRYING, STOP, ERROR, DONE;
    }

    /**
     * download state
     */
    private States state;
    /**
     * downloading error / retry error
     */
    private Throwable exception;
    /**
     * retrying delay;
     */
    private int delay;

    public URLInfo(URL source) {
        this.source = source;
    }

    public void extract() {
        extract(new AtomicBoolean(false), new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    public void extract(final AtomicBoolean stop, final Runnable notify) {
        try {
            HttpURLConnection conn;

            conn = RetryWrap.wrap(stop, new RetryWrap.WrapReturn<HttpURLConnection>() {
                URL url = source;

                @Override
                public HttpURLConnection download() throws IOException {
                    setState(States.EXTRACTING);
                    notify.run();

                    try {
                        return extractRange(url);
                    } catch (DownloadRetry e) {
                        throw e;
                    } catch (RuntimeException e) {
                        return extractNormal(url);
                    }
                }

                @Override
                public void retry(int d, Throwable ee) {
                    setDelay(d, ee);
                    notify.run();
                }

                @Override
                public void moved(URL u) {
                    setReferer(url);

                    url = u;

                    setState(States.RETRYING);
                    notify.run();
                }
            });

            setContentType(conn.getContentType());

            String contentDisposition = conn.getHeaderField("Content-Disposition");
            if (contentDisposition != null) {
                // i support for two forms with and without quotes:
                //
                // 1) contentDisposition="attachment;filename="ap61.ram"";
                // 2) contentDisposition="attachment;filename=ap61.ram";

                Pattern cp = Pattern.compile("filename=[\"]*([^\"]*)[\"]*");
                Matcher cm = cp.matcher(contentDisposition);
                if (cm.find())
                    setContentFilename(cm.group(1));
            }

            setEmpty(true);

            setState(States.EXTRACTING_DONE);
            notify.run();
        } catch (RuntimeException e) {
            setState(States.ERROR, e);

            throw e;
        }
    }

    synchronized public boolean empty() {
        return !extract;
    }

    synchronized public void setEmpty(boolean b) {
        extract = b;
    }

    // if range failed - do plain download with no retrys's
    protected HttpURLConnection extractRange(URL source) throws IOException {
        URL url = source;
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setConnectTimeout(Direct.CONNECT_TIMEOUT);
        conn.setReadTimeout(Direct.READ_TIMEOUT);

        conn.setRequestProperty("User-Agent", getUserAgent());
        if (getReferer() != null)
            conn.setRequestProperty("Referer", getReferer().toExternalForm());

        // may raise an exception if not supported by server
        conn.setRequestProperty("Range", "bytes=" + 0 + "-" + 0);

        RetryWrap.check(conn);

        String range = conn.getHeaderField("Content-Range");
        if (range == null)
            throw new RuntimeException("range not supported");

        Pattern p = Pattern.compile("bytes \\d+-\\d+/(\\d+)");
        Matcher m = p.matcher(range);
        if (m.find()) {
            setLength(new Long(m.group(1)));
        } else {
            throw new RuntimeException("range not supported");
        }

        this.setRange(true);

        return conn;
    }

    // if range failed - do plain download with no retrys's
    protected HttpURLConnection extractNormal(URL source) throws IOException {
        URL url = source;
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setConnectTimeout(Direct.CONNECT_TIMEOUT);
        conn.setReadTimeout(Direct.READ_TIMEOUT);

        conn.setRequestProperty("User-Agent", getUserAgent());
        if (getReferer() != null)
            conn.setRequestProperty("Referer", getReferer().toExternalForm());

        setRange(false);

        RetryWrap.check(conn);

        int len = conn.getContentLength();
        if (len >= 0) {
            setLength(new Long(len));
        }

        return conn;
    }

    synchronized public String getContentType() {
        return contentType;
    }

    synchronized public void setContentType(String ct) {
        contentType = ct;
    }

    synchronized public Long getLength() {
        return length;
    }

    synchronized public void setLength(Long l) {
        length = l;
    }

    synchronized public URL getSource() {
        return source;
    }

    synchronized public String getContentFilename() {
        return contentFilename;
    }

    synchronized public void setContentFilename(String f) {
        contentFilename = f;
    }

    synchronized public States getState() {
        return state;
    }

    synchronized public void setState(States state) {
        this.state = state;
        this.exception = null;
        this.delay = 0;
    }

    synchronized public void setState(States state, Throwable e) {
        this.state = state;
        this.exception = e;
        this.delay = 0;
    }

    synchronized public Throwable getException() {
        return exception;
    }

    synchronized protected void setException(Throwable exception) {
        this.exception = exception;
    }

    synchronized public int getDelay() {
        return delay;
    }

    synchronized public void setDelay(int delay, Throwable e) {
        this.delay = delay;
        this.exception = e;
        this.state = URLInfo.States.RETRYING;
    }

    synchronized public boolean getRange() {
        return range;
    }

    synchronized public void setRange(boolean range) {
        this.range = range;
    }

}
