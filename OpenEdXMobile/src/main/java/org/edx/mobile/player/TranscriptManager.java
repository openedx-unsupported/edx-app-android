package org.edx.mobile.player;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.TranscriptModel;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
public class TranscriptManager {
    private final Logger logger = new Logger(getClass().getName());
    private final Context context;

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
     * @param downloadLink
     */
    public void startTranscriptDownload(final String downloadLink) {
        //Uri target = Uri.fromFile(new File(transcriptDir, Sha1Util.SHA1(downloadLink)));
        if(downloadLink==null){
            return;
        }

        //If file is not present in the Folder, then start downloading
        if(!has(downloadLink)) {
            TranscriptDownloader td = new TranscriptDownloader(context, downloadLink) {

                @Override
                public void onDownloadComplete(String response) {
                    try {
                        put(downloadLink, response);
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }

                @Override
                public void handle(Exception ex) {
                    logger.error(ex);
                }
            };
            Thread th = new Thread(td);
            th.start();
        }
    }

    /**
     * This function starts downloading all the srt files in a Transcript model
     * @param transcript
     */
    public void downloadTranscriptsForVideo(TranscriptModel transcript) {
        if (transcript == null) {
            return;
        }
        for (String value : transcript.values()) {
            if (value != null) {
                startTranscriptDownload(value);
            }
        }
    }


    /**
     * This function is used to fetch all language Transcripts of a particular Video in strings
     * @param transcript - This model contains links of the srt files
     * @return ArrayList<String> which is the list of srt response strings
     */
    public LinkedHashMap<String, InputStream> fetchTranscriptsForVideo(TranscriptModel transcript) {
        LinkedHashMap<String, InputStream> transcriptList = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : transcript.entrySet()) {
            if (entry.getValue() != null) {
                transcriptList.put(entry.getKey(), fetchTranscriptResponse(entry.getValue()));
            }
        }
        return transcriptList;
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
}
