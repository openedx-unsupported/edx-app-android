package org.edx.mobile.util.images;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Manager for the queue
 */
public class RequestManager {
    
    /**
     * the queue
     */
    private static RequestQueue mRequestQueue;

    /**
     * Nothing to see here.
     */
    private RequestManager() {
     // no instances
    } 

    /**
     * @param context
     *          application context
     */
    public static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    /**
     * @return
     *      instance of the queue
     * @throws
     *      IllegalStateException if init has not yet been called
     */
    public static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("Not initialized");
        }
    }
}
