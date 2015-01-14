package com.github.axet.wget;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;

import com.github.axet.wget.info.DownloadInfo;
import com.github.axet.wget.info.URLInfo;
import com.github.axet.wget.info.ex.DownloadInterruptedError;

public class DirectRange extends Direct {

    public DirectRange(DownloadInfo info, File target) {
        super(info, target);
    }

    public void downloadPart(DownloadInfo info, AtomicBoolean stop, Runnable notify) throws IOException {
        RandomAccessFile fos = null;
        BufferedInputStream binaryreader = null;

        try {
            URL url = info.getSource();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);

            conn.setRequestProperty("User-Agent", info.getUserAgent());
            if (info.getReferer() != null)
                conn.setRequestProperty("Referer", info.getReferer().toExternalForm());

            File f = target;
            if (!f.exists())
                f.createNewFile();
            info.setCount(FileUtils.sizeOf(f));

            if (info.getCount() >= info.getLength()) {
                notify.run();
                return;
            }

            fos = new RandomAccessFile(f, "rw");

            if (info.getCount() > 0) {
                conn.setRequestProperty("Range", "bytes=" + info.getCount() + "-");
                fos.seek(info.getCount());
            }

            byte[] bytes = new byte[BUF_SIZE];
            int read = 0;

            RetryWrap.check(conn);

            binaryreader = new BufferedInputStream(conn.getInputStream());

            while ((read = binaryreader.read(bytes)) > 0) {
                fos.write(bytes, 0, read);

                info.setCount(info.getCount() + read);
                notify.run();

                if (stop.get())
                    throw new DownloadInterruptedError("stop");
                if (Thread.interrupted())
                    throw new DownloadInterruptedError("interrupted");
            }

        } finally {
            if (fos != null)
                fos.close();
            if (binaryreader != null)
                binaryreader.close();
        }
    }

    @Override
    public void download(final AtomicBoolean stop, final Runnable notify) {
        info.setState(URLInfo.States.DOWNLOADING);
        notify.run();

        try {
            RetryWrap.wrap(stop, new RetryWrap.Wrap() {
                @Override
                public void download() throws IOException {
                    info.setState(URLInfo.States.DOWNLOADING);
                    notify.run();

                    downloadPart(info, stop, notify);
                }

                @Override
                public void retry(int delay, Throwable e) {
                    info.setDelay(delay, e);
                    notify.run();
                }

                @Override
                public void moved(URL url) {
                    info.setState(URLInfo.States.RETRYING);
                    notify.run();
                }
            });

            info.setState(URLInfo.States.DONE);
            notify.run();
        } catch (DownloadInterruptedError e) {
            info.setState(URLInfo.States.STOP);
            notify.run();

            throw e;
        } catch (RuntimeException e) {
            info.setState(URLInfo.States.ERROR);
            notify.run();

            throw e;
        }
    }

    /**
     * check existing file for download resume. for range download it will check
     * file size and inside state. they sould be equal.
     * 
     * @param info
     * @param targetFile
     * @return return true - if all ok, false - if download can not be restored.
     */
    public static boolean canResume(DownloadInfo info, File targetFile) {
        if (targetFile.exists()) {
            if (info.getCount() != targetFile.length())
                return false;
        } else {
            if (info.getCount() > 0)
                return false;
        }
        return true;
    }
}
