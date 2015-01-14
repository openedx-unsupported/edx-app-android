package com.github.axet.vget;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.axet.vget.info.VideoInfo;
import com.github.axet.vget.info.VideoInfo.States;
import com.github.axet.vget.info.VideoInfoUser;
import com.github.axet.wget.Direct;
import com.github.axet.wget.DirectMultipart;
import com.github.axet.wget.DirectRange;
import com.github.axet.wget.DirectSingle;
import com.github.axet.wget.RetryWrap;
import com.github.axet.wget.info.DownloadInfo;
import com.github.axet.wget.info.DownloadInfo.Part;
import com.github.axet.wget.info.ex.DownloadError;
import com.github.axet.wget.info.ex.DownloadIOCodeError;
import com.github.axet.wget.info.ex.DownloadIOError;
import com.github.axet.wget.info.ex.DownloadInterruptedError;
import com.github.axet.wget.info.ex.DownloadMultipartError;
import com.github.axet.wget.info.ex.DownloadRetry;

public class VGet {

    VideoInfo info;
    File targetDir;

    File targetForce = null;

    File targetFile = null;

    /**
     * extract video information constructor
     * 
     * @param source
     */
    public VGet(URL source) {
        this(new VideoInfo(source), null);
    }

    public VGet(URL source, File targetDir) {
        this(new VideoInfo(source), targetDir);
    }

    public VGet(VideoInfo info, File targetDir) {
        this.info = info;
        this.targetDir = targetDir;
    }

    public void setTarget(File file) {
        targetForce = file;
    }

    public void setTargetDir(File targetDir) {
        this.targetDir = targetDir;
    }

    /**
     * get output file on local file system
     * 
     * @return
     */
    public File getTarget() {
        return targetFile;
    }

    public VideoInfo getVideo() {
        return info;
    }

    public void download() {
        download(new VideoInfoUser(), new AtomicBoolean(false), new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    public void download(VideoInfoUser user) {
        download(user, new AtomicBoolean(false), new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    /**
     * Drop all foribiden characters from filename
     * 
     * @param f
     *            input file name
     * @return normalized file name
     */
    static String replaceBadChars(String f) {
        String replace = " ";
        f = f.replaceAll("/", replace);
        f = f.replaceAll("\\\\", replace);
        f = f.replaceAll(":", replace);
        f = f.replaceAll("\\?", replace);
        f = f.replaceAll("\\\"", replace);
        f = f.replaceAll("\\*", replace);
        f = f.replaceAll("<", replace);
        f = f.replaceAll(">", replace);
        f = f.replaceAll("\\|", replace);
        f = f.trim();
        f = StringUtils.removeEnd(f, ".");
        f = f.trim();

        String ff;
        while (!(ff = f.replaceAll("  ", " ")).equals(f)) {
            f = ff;
        }

        return f;
    }

    static String maxFileNameLength(String str) {
        int max = 255;
        if (str.length() > max)
            str = str.substring(0, max);
        return str;
    }

    boolean done(AtomicBoolean stop) {
        if (stop.get())
            throw new DownloadInterruptedError("stop");
        if (Thread.currentThread().isInterrupted())
            throw new DownloadInterruptedError("interrupted");

        return false;
    }

    void retry(VideoInfoUser user, AtomicBoolean stop, Runnable notify, Throwable e) {
        boolean retracted = false;

        while (!retracted) {
            for (int i = RetryWrap.RETRY_DELAY; i >= 0; i--) {
                if (stop.get())
                    throw new DownloadInterruptedError("stop");
                if (Thread.currentThread().isInterrupted())
                    throw new DownloadInterruptedError("interrupted");

                info.setDelay(i, e);
                notify.run();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ee) {
                    throw new DownloadInterruptedError(ee);
                }
            }

            try {
                // if we continue to download from old source, and this proxy
                // server is
                // down we have to try to extract new info and try to resume
                // download

                DownloadInfo infoOld = info.getInfo();
                info.extract(user, stop, notify);
                DownloadInfo infoNew = info.getInfo();

                if (infoOld != null && infoOld.resume(infoNew)) {
                    infoNew.copy(infoOld);
                } else {
                    if (targetFile != null) {
                        FileUtils.deleteQuietly(targetFile);
                        targetFile = null;
                    }
                }

                retracted = true;
            } catch (DownloadIOCodeError ee) {
                if (retry(ee)) {
                    info.setState(States.RETRYING, ee);
                    notify.run();
                } else {
                    throw ee;
                }
            } catch (DownloadRetry ee) {
                info.setState(States.RETRYING, ee);
                notify.run();
            }
        }
    }

    void target(DownloadInfo dinfo) {
        if (targetForce != null) {
            targetFile = targetForce;

            if (dinfo.multipart()) {
                if (!DirectMultipart.canResume(dinfo, targetFile))
                    targetFile = null;
            } else if (dinfo.getRange()) {
                if (!DirectRange.canResume(dinfo, targetFile))
                    targetFile = null;
            } else {
                if (!DirectSingle.canResume(dinfo, targetFile))
                    targetFile = null;
            }
        }

        if (targetFile == null) {
            File f;

            Integer idupcount = 0;

            String sfilename = replaceBadChars(info.getTitle());

            sfilename = maxFileNameLength(sfilename);

            String ct = dinfo.getContentType();
            if (ct == null)
                throw new DownloadRetry("null content type");

            String ext = ct.replaceFirst("video/", "").replaceAll("x-", "");

            do {
                String add = idupcount > 0 ? " (".concat(idupcount.toString()).concat(")") : "";

                f = new File(targetDir, sfilename + add + "." + ext);
                idupcount += 1;
            } while (f.exists());

            targetFile = f;

            // if we dont have resume file (targetForce==null) then we shall
            // start over.
            dinfo.reset();
        }
    }

    boolean retry(Throwable e) {
        if (e == null)
            return true;

        if (e instanceof DownloadIOCodeError) {
            DownloadIOCodeError c = (DownloadIOCodeError) e;
            switch (c.getCode()) {
            case HttpURLConnection.HTTP_FORBIDDEN:
            case 416:
                return true;
            default:
                return false;
            }
        }

        return false;
    }

    /**
     * return status of download information. subclassing for VideoInfo.empty();
     * 
     * @return
     */
    public boolean empty() {
        return getVideo().empty();
    }

    public void extract() {
        extract(new AtomicBoolean(false), new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    public void extract(AtomicBoolean stop, Runnable notify) {
        extract(new VideoInfoUser(), stop, notify);
    }

    /**
     * extract video information, retry until success
     * 
     * @param stop
     * @param notify
     */
    public void extract(VideoInfoUser user, AtomicBoolean stop, Runnable notify) {
        while (!done(stop)) {
            try {
                if (info.empty()) {
                    info.setState(States.EXTRACTING);
                    info.extract(user, stop, notify);
                    info.setState(States.EXTRACTING_DONE);
                    notify.run();
                }
                return;
            } catch (DownloadRetry e) {
                retry(user, stop, notify, e);
            } catch (DownloadMultipartError e) {
                checkFileNotFound(e);
                checkRetry(e);
                retry(user, stop, notify, e);
            } catch (DownloadIOCodeError e) {
                if (retry(e))
                    retry(user, stop, notify, e);
                else
                    throw e;
            } catch (DownloadIOError e) {
                retry(user, stop, notify, e);
            }
        }
    }

    void checkRetry(DownloadMultipartError e) {
        for (Part ee : e.getInfo().getParts()) {
            if (!retry(ee.getException())) {
                throw e;
            }
        }
    }

    /**
     * check if all parts has the same filenotfound exception. if so throw
     * DownloadError.FilenotFoundexcepiton
     * 
     * @param e
     */
    void checkFileNotFound(DownloadMultipartError e) {
        FileNotFoundException f = null;
        for (Part ee : e.getInfo().getParts()) {
            // no error for this part? skip it
            if (ee.getException() == null)
                continue;
            // this exception has no cause? then it is not FileNotFound
            // excpetion. then do noting. this is checking function. do not
            // rethrow
            if (ee.getException().getCause() == null)
                return;
            if (ee.getException().getCause() instanceof FileNotFoundException) {
                // our first filenotfoundexception?
                if (f == null) {
                    // save it for later checks
                    f = (FileNotFoundException) ee.getException().getCause();
                } else {
                    // check filenotfound error message is it the same?
                    FileNotFoundException ff = (FileNotFoundException) ee.getException().getCause();
                    if (!ff.getMessage().equals(f.getMessage())) {
                        // if the filenotfound exception message is not the
                        // same. then we cannot retrhow filenotfound exception.
                        // return and continue checks
                        return;
                    }
                }
            } else {
                break;
            }
        }
        if (f != null)
            throw new DownloadError(f);
    }

    public void download(final AtomicBoolean stop, final Runnable notify) {
        download(new VideoInfoUser(), stop, notify);
    }

    public void download(VideoInfoUser user, final AtomicBoolean stop, final Runnable notify) {
        if (targetFile == null && targetForce == null && targetDir == null) {
            throw new RuntimeException("Set download file or directory first");
        }

        try {
            if (empty()) {
                extract(user, stop, notify);
            }

            while (!done(stop)) {
                try {
                    final DownloadInfo dinfo = info.getInfo();

                    if (dinfo.getContentType() == null || !dinfo.getContentType().contains("video/")) {
                        throw new DownloadRetry("unable to download video, bad content");
                    }

                    target(dinfo);

                    Direct direct;

                    if (dinfo.multipart()) {
                        // multi part? overwrite.
                        direct = new DirectMultipart(dinfo, targetFile);
                    } else if (dinfo.getRange()) {
                        // range download? try to resume download from last
                        // position
                        if (targetFile.exists() && targetFile.length() != dinfo.getCount())
                            targetFile = null;
                        direct = new DirectRange(dinfo, targetFile);
                    } else {
                        // single download? overwrite file
                        direct = new DirectSingle(dinfo, targetFile);
                    }

                    direct.download(stop, new Runnable() {
                        @Override
                        public void run() {
                            switch (dinfo.getState()) {
                            case DOWNLOADING:
                                info.setState(States.DOWNLOADING);
                                notify.run();
                                break;
                            case RETRYING:
                                info.setDelay(dinfo.getDelay(), dinfo.getException());
                                notify.run();
                                break;
                            default:
                                // we can safely skip all statues. (extracting -
                                // already
                                // pased, STOP / ERROR / DONE i will catch up
                                // here
                            }
                        }
                    });

                    info.setState(States.DONE);
                    notify.run();

                    // break while()
                    return;
                } catch (DownloadRetry e) {
                    retry(user, stop, notify, e);
                } catch (DownloadMultipartError e) {
                    checkFileNotFound(e);
                    checkRetry(e);
                    retry(user, stop, notify, e);
                } catch (DownloadIOCodeError e) {
                    if (retry(e))
                        retry(user, stop, notify, e);
                    else
                        throw e;
                } catch (DownloadIOError e) {
                    retry(user, stop, notify, e);
                }
            }
        } catch (DownloadInterruptedError e) {
            info.setState(VideoInfo.States.STOP, e);
            notify.run();

            throw e;
        } catch (RuntimeException e) {
            info.setState(VideoInfo.States.ERROR, e);
            notify.run();

            throw e;
        }
    }
}
