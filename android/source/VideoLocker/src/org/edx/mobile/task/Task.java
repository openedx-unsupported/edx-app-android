package org.edx.mobile.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import org.edx.mobile.logger.Logger;

public abstract class Task<T> extends AsyncTask<Object, Object, T> {

    protected Context context;
    private ProgressBar progressBar;
    protected final Handler handler = new Handler();
    protected final Logger logger = new Logger(getClass().getName());
    
    public Task(Context context) { this.context = context; }
    
    public void setProgressDialog(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    protected void onPostExecute(T result) {
        super.onPostExecute(result);
        if (result != null) {
            onFinish(result);
        }else{
            onFinish(null);
        }
        
        stopProgress();
    }
    
    protected void stopProgress() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    protected void handle(final Exception ex) {
        handler.post(new Runnable() {
            public void run() {
                onException(ex);
            }
        });
    }

    public abstract void onFinish(T result);
    public abstract void onException(Exception ex);
}
