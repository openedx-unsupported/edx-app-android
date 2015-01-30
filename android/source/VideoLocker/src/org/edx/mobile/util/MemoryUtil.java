package org.edx.mobile.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.File;

public class MemoryUtil {
    
    public static final long GB = 1024 * 1024 * 1024;

    /**
     * Returns available number of bytes in external memory.
     * @param context
     * @return
     */
    public static long getAvailableExternalMemory(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return (availableBlocks * blockSize);
    }
    
    /**
     * Returns available number of bytes in internal memory.
     * @param context
     * @return
     */
    public static long getAvailableInternalMemory(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return (availableBlocks * blockSize);
    }

    /**
     * Returns string formatted, user readable memory size text.
     * @param context
     * @param bytes
     * @return
     */
    public static String format(Context context, long bytes) {
        String result = Formatter.formatFileSize(context, bytes);
        if (result.contains(",")) {
            // on samsung S3, we get comma instead of dot,
            // That's an issue with Formatter class of Android
            // so, we replace comma with dot
            result = result.replace(',', '.');
        }
        return result;
    }
}
