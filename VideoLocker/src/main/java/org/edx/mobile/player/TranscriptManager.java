package org.edx.mobile.player;

import android.content.Context;
import android.os.Environment;

import org.apache.commons.io.IOUtils;
import org.edx.mobile.R;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.util.Sha1Util;
import org.edx.mobile.util.TranscriptDownloader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import org.edx.mobile.logger.Logger;

public class TranscriptManager {

    private File transcriptFolder;
    private Context context;
    private final Logger logger = new Logger(getClass().getName());

    public TranscriptManager(Context context) {
        try{
            this.context = context;
            File android = new File(Environment.getExternalStorageDirectory(), "Android");
            File downloadsDir = new File(android, "data");
            File packDir = new File(downloadsDir, context.getPackageName());
            transcriptFolder = new File(packDir, "srtFolder");
            if(!transcriptFolder.exists()){
                transcriptFolder.mkdirs();
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * This function checks if the file exists for that link
     * @param url
     * @return 
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public boolean has(String url) throws NoSuchAlgorithmException,
    UnsupportedEncodingException {
        String hash = Sha1Util.SHA1(url);
        File file = new File(transcriptFolder, hash);
        return file.exists();
    }


    /**
     * This function is used to saved contents of a String to a file
     * @param url - Url of Transcript
     * @param response - This is the String which needs to be saved into a file
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public void put(String url, String response)
            throws NoSuchAlgorithmException, UnsupportedEncodingException,
            IOException {
        String hash = Sha1Util.SHA1(url);
        File file = new File(transcriptFolder, hash);
        FileOutputStream out = new FileOutputStream(file);
        out.write(response.getBytes());
        out.close();
    }


    /**
     * This function helps to get the file contents in a String
     * @param url - This is the URL for SRT files
     * @return String - This is the response of the File contents
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public String get(String url) throws IOException, NoSuchAlgorithmException {
        try{
            String hash = Sha1Util.SHA1(url);
            File file = new File(transcriptFolder, hash);
            if (!file.exists()) { 
                // not in cache
                return null;
            }

            FileInputStream in = new FileInputStream(file);
            String cache = IOUtils.toString(in, Charset.defaultCharset());
            in.close();
            logger.debug("Cache.get=" + hash);
            return cache;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }


    /**
     * This function helps to get the file contents in an InputStream
     * @param url - This is the URL for SRT files
     * @return String - This is the response of the File contents
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public InputStream getInputStream(String url) throws IOException, NoSuchAlgorithmException {
        try{
            String hash = Sha1Util.SHA1(url);
            File file = new File(transcriptFolder, hash);
            if (!file.exists()) { 
                // not in cache
                return null;
            }

            InputStream in = new FileInputStream(file);
            return in;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }


    /**
     * This function is used to handle downloading of SRT files and saving them
     * @param downloadLink
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public void startTranscriptDownload(final String downloadLink) 
            throws NoSuchAlgorithmException, UnsupportedEncodingException{
        //Uri target = Uri.fromFile(new File(transcriptFolder, Sha1Util.SHA1(downloadLink)));
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
                    } catch (NoSuchAlgorithmException e) {
                        logger.error(e);
                    } catch (UnsupportedEncodingException e) {
                        logger.error(e);
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
    public void downloadTranscriptsForVideo(TranscriptModel transcript){
        if(transcript==null){
            return;
        }

        if(transcript.chineseUrl!=null){
            try {
                startTranscriptDownload(transcript.chineseUrl);
            } catch (NoSuchAlgorithmException e) {
                logger.error(e);
            } catch (UnsupportedEncodingException e) {
                logger.error(e);
            }
        }
        if(transcript.englishUrl!=null){
            try {
                startTranscriptDownload(transcript.englishUrl);
            } catch (NoSuchAlgorithmException e) {
                logger.error(e);
            } catch (UnsupportedEncodingException e) {
                logger.error(e);
            }
        }
        if(transcript.frenchUrl!=null){
            try {
                startTranscriptDownload(transcript.frenchUrl);
            } catch (NoSuchAlgorithmException e) {
                logger.error(e);
            } catch (UnsupportedEncodingException e) {
                logger.error(e);
            }
        }
        if(transcript.germanUrl!=null){
            try {
                startTranscriptDownload(transcript.germanUrl);
            } catch (NoSuchAlgorithmException e) {
                logger.error(e);
            } catch (UnsupportedEncodingException e) {
                logger.error(e);
            }
        }
        if(transcript.portugueseUrl!=null){
            try {
                startTranscriptDownload(transcript.portugueseUrl);
            } catch (NoSuchAlgorithmException e) {
                logger.error(e);
            } catch (UnsupportedEncodingException e) {
                logger.error(e);
            }
        }
        if(transcript.spanishUrl!=null){
            try {
                startTranscriptDownload(transcript.spanishUrl);
            } catch (NoSuchAlgorithmException e) {
                logger.error(e);
            } catch (UnsupportedEncodingException e) {
                logger.error(e);
            }
        }
    }


    /**
     * This function is used to fetch all language Transcripts of a particular Video in strings
     * @param transcript - This model contains links of the srt files
     * @return ArrayList<String> which is the list of srt response strings
     */
    public LinkedHashMap<String, InputStream> fetchTranscriptsForVideo(
                TranscriptModel transcript, Context context){

        LinkedHashMap<String, InputStream> transcriptList = new LinkedHashMap<String, InputStream>();
        try{
            if(transcript.chineseUrl!=null){
                transcriptList.put(context.getString(R.string.cc_chinese_code), 
                        fetchTranscriptResponse(transcript.chineseUrl));
            }

            if(transcript.englishUrl!=null){
                transcriptList.put(context.getString(R.string.cc_english_code), 
                        fetchTranscriptResponse(transcript.englishUrl));
            }

            if(transcript.frenchUrl!=null){
                transcriptList.put(context.getString(R.string.cc_french_code), 
                        fetchTranscriptResponse(transcript.frenchUrl));
            }
            
            if(transcript.germanUrl!=null){
                transcriptList.put(context.getString(R.string.cc_german_code), 
                        fetchTranscriptResponse(transcript.germanUrl));
            }

            if(transcript.portugueseUrl!=null){
                transcriptList.put(context.getString(R.string.cc_portugal_code), 
                        fetchTranscriptResponse(transcript.portugueseUrl));
            }

            if(transcript.spanishUrl!=null){
                transcriptList.put(context.getString(R.string.cc_spanish_code), 
                        fetchTranscriptResponse(transcript.spanishUrl));
            }
            
            return transcriptList;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
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
        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }
}
