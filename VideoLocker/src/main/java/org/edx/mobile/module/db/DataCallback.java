package org.edx.mobile.module.db;

import android.os.Handler;
import android.os.Looper;

public abstract class DataCallback<T> implements IDbCallback<T> {
    
    private Handler handler;
    private final boolean resultOnUIThread;
    
    public DataCallback() {
        this(false);
    }

    /**
     * If resultOnUIThread is true, then callbacks are sent on caller's thread, otherwise
     * in a background thread.
     * 
     * @param resultOnUIThread
     */
    public DataCallback(boolean resultOnUIThread) {
        this.resultOnUIThread = resultOnUIThread;
        
        if (resultOnUIThread) {
            try {
                handler = new Handler();
            } catch(RuntimeException ex) {
                new LooperThread().start();
            }
        }
    }
    
    /**
     * This method send the result to the callback on caller's message queue.
     */
    public final void sendResult(final T result) {
        if (resultOnUIThread) {
            handler.post(new Runnable() {
                public void run() {
                    onResult(result);
                }
            });
        } else {
            onResult(result);
        }
    }
    
    /**
     * This method send the exception to the callback on caller's message queue.
     */
    public final void sendException(final Exception ex) {
        if (resultOnUIThread) {
            handler.post(new Runnable() {
                public void run() {
                    onFail(ex);
                }
            });
        } else {
            onFail(ex);
        }
    }

    private class LooperThread extends Thread {
        
        @Override
        public void run() {
            Looper.prepare();
            
            handler = new Handler();
            
            Looper.loop();
        }
    }


    /**
     * Callback that gets invoked when database operation returns result.
     * @param result
     */
    public abstract void onResult(T result);

    /**
     * Callback that gets invoked when database operation fails.
     * @param ex
     */
    public abstract void onFail(Exception ex);
}
