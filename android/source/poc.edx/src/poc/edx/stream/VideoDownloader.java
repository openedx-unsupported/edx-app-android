package poc.edx.stream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class VideoDownloader implements Runnable {

    private String strUrl;
    private IDownloadCallback callback;
    private boolean isStreaming = false;
    private boolean isStreamingStopped = false;
    private File destFile;

    public VideoDownloader(String url, File destFile) {
        this.strUrl = url;
        this.destFile = destFile;
    }

    public void setCallback(IDownloadCallback callback) {
        this.callback = callback;
    }

    public void start() {
        Thread th = new Thread(this);
        th.start();
    }

    public void stop() {
        isStreamingStopped = true;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    @Override
    public void run() {
        try {
            // mark streaming started
            isStreaming = true;
            isStreamingStopped = false;

            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();

            Log.e("TAG", connection.getContentLength() + "");
            final int videoLength = connection.getContentLength();
            InputStream inputStream = connection.getInputStream();

            byte[] buffer = new byte[16 * 1024];
            int length = 0;
            long bufferSize = 0;
            int percent = 0;

            if (callback != null) {
                callback.onStarted(destFile);
            }

            FileOutputStream out = new FileOutputStream(destFile);

            // PipedOutputStream pipedOut = new PipedOutputStream();
            // PipedInputStream pipedIn = new PipedInputStream(pipedOut);
            // if (callback != null) {
            // callback.onStreamingStarted(pipedIn);
            // }

            try {
                while ((length = inputStream.read(buffer)) != -1) {
                    bufferSize += length;

                    int bufferPercentage = (int) (bufferSize * 100 / videoLength);

                    // write this data to the file
                    out.write(buffer, 0, length);

                    // write to piped stream
                    // pipedOut.write(buffer, 0, length);

                    if (bufferPercentage > percent) {
                        percent = bufferPercentage;

                        if (callback != null) {
                            final int p = percent;
                            Thread th = new Thread() {
                                @Override
                                public void run() {
                                    callback.onProgress(p);
                                }
                            };
                            th.start();
                        }
                    }

                    if (callback != null) {
                        final int len = length;
                        final int p = bufferPercentage;
                        final byte[] buff = buffer;
                        Thread th = new Thread() {
                            @Override
                            public void run() {
                                callback.onReceived(buff, len, p);
                            }
                        };
                        th.start();
                    }

                    if (isStreamingStopped) {
                        // streaming has been stopped, so end here
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // pipedIn.close();
            // pipedOut.close();
            out.close();
            inputStream.close();
            connection.disconnect();

            if (callback != null) {
                callback.onFinished(destFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (callback != null) {
                callback.onError(destFile, ex.getMessage());
            }
        } finally {
            isStreaming = false;
        }
    }

    public static interface IDownloadCallback {
        public void onReceived(byte[] buffer, int length, int progressPercentage);

        public void onProgress(int percent);

        public void onError(File destFile, String error);

        public void onStarted(File destFile);

        public void onFinished(File destFile);

        // public void onStreamingStarted(PipedInputStream in);
    }
}
