package org.edx.mobile.tta.wordpress_client.util;

import android.content.Context;
import android.text.TextUtils;

import org.edx.mobile.R;
import org.edx.mobile.tta.wordpress_client.model.Media;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * @author Arjun Singh
 *         Created on 2016/01/08.
 */
public class ContentUtil {

    private static final String VIDEO_TYPE_MP4 = "video/mp4";
    private static final String VIDEO_TYPE_WEBM = "video/webm";
    private static final String VIDEO_TYPE_OGG = "video/ogg";

    private static final String VIDEO_CODECS_MP4 = "avc1.42E01E, mp4a.40.2";
    private static final String VIDEO_CODECS_WEBM = "vp8, vorbis";
    private static final String VIDEO_CODECS_OGG = "theora, vorbis";

    /* WordPress supported image types:
    *
    *   'jpg|jpeg|jpe' => 'image/jpeg',
	*   'gif' => 'image/gif',
	*   'png' => 'image/png',
	*   'bmp' => 'image/bmp',
	*   'tiff|tif' => 'image/tiff',
	*   'ico' => 'image/x-icon',
    *
    * */

    private static final String MIME_IMAGE_ALL = "image/*";
    private static final String MIME_IMAGE_JPG = "image/jpg";
    private static final String MIME_IMAGE_JPEG = "image/jpeg";
    private static final String MIME_IMAGE_GIF = "image/gif";
    private static final String MIME_IMAGE_PNG = "image/png";
    private static final String MIME_IMAGE_BMP = "image/bmp";
    private static final String MIME_IMAGE_TIFF = "image/tiff";
    private static final String MIME_IMAGE_ICO = "image/x-icon";

    private static final String IMAGE_TYPE_JPG = "jpg";
    private static final String IMAGE_TYPE_JPEG = "jpeg";
    private static final String IMAGE_TYPE_JPE = "jpe";
    private static final String IMAGE_TYPE_GIF = "gif";
    private static final String IMAGE_TYPE_PNG = "png";
    private static final String IMAGE_TYPE_BMP = "bmp";
    private static final String IMAGE_TYPE_TIFF = "tiff";
    private static final String IMAGE_TYPE_TIF = "tif";
    private static final String IMAGE_TYPE_ICO = "ico";


    /**
     * +
     * Returns a formatted image link for use in Post body
     *
     * @param context  Application context
     * @param imageUrl Uri for the image
     * @param altText  Alternate text
     * @return Formatted HTML url string
     */
    public static String getContentImageLinkUri(Context context, String imageUrl, String altText) {
        return context.getString(R.string.content_image_uri, imageUrl, altText == null ? "" : altText);
    }

    /**
     * Returns a formatted video link for use in Post body.
     * Currently video uses JWPlayer
     *
     * @param context  Application contenxt
     * @param videoUrl Absolute path to video file
     * @param index    Required for the div id name to allow multiple videos; has to be unique for each video link
     * @return Formatted HTML video link
     */
    public static String getContentVideoLinkUri(Context context, String videoUrl, int index) {
        return context.getString(R.string.content_video_uri, videoUrl, index);
    }

    public static String getContentVideoTranscodeLink(Context context, String videoFileName, String origBucketLocation, int index) {
        int extStart = videoFileName.lastIndexOf(".");
        String subFolder = context.getString(R.string.s3_bucket_folder);
        String name = subFolder + "/" + videoFileName.substring(0, extStart);

        return context.getString(R.string.content_video_uri_transcode, name, index);
    }

    public static String getContentVideoShortcode(Context context, String videoFilename) {
        int extStart = videoFilename.lastIndexOf(".");
        String subFolder = context.getString(R.string.s3_bucket_folder);
        String name = subFolder + "/" + videoFilename.substring(0, extStart);

        return context.getString(R.string.content_video_shortcode, name);
    }

    public static String getContentLocationShortcode(Context context, String address) {
        return context.getString(R.string.content_location, address);
    }

    public static String getContentAudioShortcode(Context context, String audioUrl) {
        return context.getString(R.string.content_audio_uri, audioUrl);
    }

    public static String getContentAudioLink(Context context, String fileName) {
        int extStart = fileName.lastIndexOf(".");
        String subFolder = context.getString(R.string.s3_bucket_folder);
        String name = subFolder + "/" + fileName.substring(0, extStart);

        return context.getString(R.string.content_audio_uri, name);
    }

    /**
     * Returns the MIME type for an image file based on it's extension.
     *
     * @param filename Name of file to check.
     * @return MIME type for image
     */
    public static String getImageMimeType(String filename) {
        if (TextUtils.isEmpty(filename)) {
            return "";
        }

        String[] parts = TextUtils.split(filename, "\\.");
        if (parts.length < 2) {
            LogUtils.w("Split filename has less than 2 parts=" + filename);
            return MIME_IMAGE_ALL;
        }

        String ext = parts[1];

        if (TextUtils.equals(ext, IMAGE_TYPE_JPG) || TextUtils.equals(ext, IMAGE_TYPE_JPEG)
                || TextUtils.equals(ext, IMAGE_TYPE_JPE)) {
            return MIME_IMAGE_JPG;
        }

        if (TextUtils.equals(ext, IMAGE_TYPE_GIF)) {
            return MIME_IMAGE_GIF;
        }

        if (TextUtils.equals(ext, IMAGE_TYPE_PNG)) {
            return MIME_IMAGE_PNG;
        }

        if (TextUtils.equals(ext, IMAGE_TYPE_BMP)) {
            return MIME_IMAGE_BMP;
        }

        if (TextUtils.equals(ext, IMAGE_TYPE_TIFF) || TextUtils.equals(ext, IMAGE_TYPE_TIF)) {
            return MIME_IMAGE_TIFF;
        }

        if (TextUtils.equals(ext, IMAGE_TYPE_ICO)) {
            return MIME_IMAGE_ICO;
        }

        return MIME_IMAGE_ALL;
    }

    /**
     * Checks if the given MIME type is a valid video media type.
     *
     * @param type MIME type to check
     * @return True if a valid type
     */
    public static boolean isVideoMedia(String type) {
        return type.equals(VIDEO_TYPE_MP4) || type.equals(VIDEO_TYPE_OGG) || type.equals(VIDEO_TYPE_WEBM);
    }

    /**
     * Checks if given MIME type is a valid image media type.
     *
     * @param type MIME type
     * @return True if type matches a valid image type.
     */
    public static boolean isImageMedia(String type) {
        return type.equals(MIME_IMAGE_BMP) || type.equals(MIME_IMAGE_GIF) || type.equals(MIME_IMAGE_ICO)
                || type.equals(MIME_IMAGE_JPG) || type.equals(MIME_IMAGE_PNG) || type.equals(MIME_IMAGE_TIFF)
                || type.equals(MIME_IMAGE_JPEG);
    }


    /**
     * Helper method to construct Map used to upload Media item to WordPress.
     *
     * @param media Media item details
     * @param file  File to upload
     * @return Map containing all relevant Media info needed for upload
     */
    public static Map<String, RequestBody> makeMediaItemUploadMap(Media media, File file) {
        Map<String, RequestBody> map = new HashMap<>();
        map.put(Media.JSON_FIELD_TITLE, toRequestBody(media.getTitle().getRendered()));
        /*if (Validate.notNull(media.getCaption())) {
            map.put(Media.JSON_FIELD_CAPTION, toRequestBody(media.getCaption()));
        }*/
        if (Validate.notNull(media.getAltText())) {
            map.put(Media.JSON_FIELD_ALT_TEXT, toRequestBody(media.getAltText()));
        }
        /*if (Validate.notNull(media.getDescription())) {
            map.put(Media.JSON_FIELD_DESCRIPTION, toRequestBody(media.getDescription()));
        }*/
        if (media.getPostId() != -1) {
            map.put(Media.JSON_FIELD_POST, toRequestBody(media.getPostId() + ""));
        }

        String ext = ContentUtil.getImageMimeType(file.getName());
        RequestBody fileBody = RequestBody.create(MediaType.parse(ext), file);
        map.put("file\"; filename=\"" + file.getName() + "\"", fileBody);

        return map;
    }

    private static RequestBody toRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }
}
