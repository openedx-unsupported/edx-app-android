package poc.edx.stream;

import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.axet.vget.VGet;

public class StreamDownloader {

    private String url;
    private File destFile;

    public StreamDownloader(String url, File destFile) {
        this.url = url;
        this.destFile = destFile;
    }

    public void download(final IStreamDownloadCallback callback)
            throws Exception {
        VGet v = new VGet(new URL(url), destFile);
        v.download(new AtomicBoolean(), new Runnable() {

            @Override
            public void run() {
                if (callback != null) {
                    callback.onProgress();
                }
            }
        });
    }

    public static interface IStreamDownloadCallback {
        public void onProgress();
    }
}
