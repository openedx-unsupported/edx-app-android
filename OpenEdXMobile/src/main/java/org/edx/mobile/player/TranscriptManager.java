package org.edx.mobile.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.FileUtil;
import org.edx.mobile.util.IOUtils;
import org.edx.mobile.util.Sha1Util;
import org.edx.mobile.util.TranscriptDownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import subtitleFile.FormatSRT;
import subtitleFile.TimedTextObject;

@Singleton
public class TranscriptManager {
    private final Logger logger = new Logger(getClass().getName());
    private final Context context;
    private AsyncTask<Void, Void, String> transcriptDownloader;

    @Inject
    public TranscriptManager(Context context) {
        this.context = context;
    }

    /**
     * This function checks if the file exists for that link
     * @param url
     * @return
     */
    public boolean has(String url) {
        final File transcriptDir = getTranscriptDir();
        if (transcriptDir == null) return false;

        String hash = Sha1Util.SHA1(url);
        File file = new File(transcriptDir, hash);
        return file.exists();
    }


    /**
     * This function is used to saved contents of a String to a file
     * @param url - Url of Transcript
     * @param response - This is the String which needs to be saved into a file
     * @throws IOException
     */
    public void put(String url, String response) throws IOException {
        final File transcriptDir = getTranscriptDir();
        if (transcriptDir == null) throw new IOException("Transcript directory not found");

        String hash = Sha1Util.SHA1(url);
        File file = new File(transcriptDir, hash);
        FileOutputStream out = new FileOutputStream(file);
        out.write(response.getBytes());
        out.close();
    }


    /**
     * This function helps to get the file contents in a String
     * @param url - This is the URL for SRT files
     * @return String - This is the response of the File contents
     * @throws IOException
     */
    public String get(String url) throws IOException {
        final File transcriptDir = getTranscriptDir();
        if (transcriptDir == null) throw new IOException("Transcript directory not found");

        String hash = Sha1Util.SHA1(url);
        File file = new File(transcriptDir, hash);
        if (!file.exists()) {
            // not in cache
            return null;
        }

        String cache = IOUtils.toString(file, Charset.defaultCharset());
        logger.debug("Cache.get=" + hash);
        return cache;
    }


    /**
     * This function helps to get the file contents in an InputStream
     * @param url - This is the URL for SRT files
     * @return String - This is the response of the File contents
     * @throws IOException
     */
    public InputStream getInputStream(String url) throws IOException {
        final File transcriptDir = getTranscriptDir();
        if (transcriptDir == null) throw new IOException("Transcript directory not found");

        String hash = Sha1Util.SHA1(url);
        File file = new File(transcriptDir, hash);
        if (!file.exists()) {
            // not in cache
            return null;
        }

        return new FileInputStream(file);
    }


    /**
     * This function is used to handle downloading of SRT files and saving them
     *
     * @param downloadLink     - transcript downloadable link
     * @param downloadListener - Callback on transcript download complete {@link OnTranscriptDownloadListener}
     */
    @SuppressLint("StaticFieldLeak")
    private void startTranscriptDownload(@NonNull final String downloadLink,
                                         @Nullable OnTranscriptDownloadListener downloadListener) {
        //If file is not present in the Folder, then start downloading
        if (!has(downloadLink)) {
            transcriptDownloader = new TranscriptDownloader(context, downloadLink) {

                @Override
                public void onDownloadComplete(String response) {
                    try {
                        if (response != null) {
                            put(downloadLink, response);
                            if (downloadListener != null) {
                                final InputStream transcriptInputStream = fetchTranscriptResponse(downloadLink);
                                TimedTextObject transcriptTimedTextObject = convertIntoTimedTextObject(transcriptInputStream);
                                downloadListener.onDownloadComplete(transcriptTimedTextObject);
                            }
                        }
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }

                @Override
                public void handle(Exception ex) {
                    logger.error(ex);
                }
            }.execute();
        }
    }

    /**
     * This function starts downloading all the srt files in a Transcript model
     *
     * @param transcriptUrl    - download url to download the transcript
     * @param downloadListener - Callback on transcript download complete {@link OnTranscriptDownloadListener}
     */
    public void downloadTranscriptsForVideo(@Nullable String transcriptUrl,
                                            @Nullable OnTranscriptDownloadListener downloadListener) {
        if (TextUtils.isEmpty(transcriptUrl)) {
            return;
        }
        InputStream transcriptsInputStream = fetchTranscriptResponse(transcriptUrl);
        if (transcriptsInputStream != null) {
            TimedTextObject transcriptTimedTextObject;
            try {
                transcriptTimedTextObject = convertIntoTimedTextObject(transcriptsInputStream);
                if (downloadListener != null) {
                    downloadListener.onDownloadComplete(transcriptTimedTextObject);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            startTranscriptDownload(transcriptUrl, downloadListener);
        }
    }

    /**
     * Utility method that convert the {@link InputStream} to {@link TimedTextObject} for the
     * transcript
     *
     * @param inputStream - that needs to convert
     * @return - {@link TimedTextObject} a transcript object
     */
    private TimedTextObject convertIntoTimedTextObject(@NonNull InputStream inputStream) throws IOException {
        TimedTextObject timedTextObject = new FormatSRT().parseFile("temp.srt", inputStream);
        inputStream.close();
        return timedTextObject;
    }

    /**
     * This function is used to get string as response for the contents of a file
     * @param url - URL of the srt
     * @return String contents of the File
     */
    public InputStream fetchTranscriptResponse(String url){
        if(url==null){
            return null;
        }

        InputStream response = null;
        try {
            if(has(url)){
                response = getInputStream(url);
            return response;
            }
        } catch (IOException e) {
            logger.error(e);
        }
        return null;
    }

    @Nullable
    private File getTranscriptDir() {
        final File externalAppDir = FileUtil.getExternalAppDir(context);
        if (externalAppDir != null) {
            final File videosDir = new File(externalAppDir, AppConstants.Directories.VIDEOS);
            final File transcriptDir = new File(videosDir, AppConstants.Directories.SUBTITLES);
            transcriptDir.mkdirs();
            return transcriptDir;
        }
        return null;
    }

    /**
     * Method to cancel the transcript downloading.
     */
    public void cancelTranscriptDownloading() {
        if (transcriptDownloader != null) {
            transcriptDownloader.cancel(true);
        }
    }

    public interface OnTranscriptDownloadListener {
        void onDownloadComplete(TimedTextObject transcriptTimedTextObject);
    }
}
