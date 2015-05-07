package org.edx.mobile.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.view.common.TaskProcessCallback;

public abstract class Task<T> extends AsyncTask<Object, Object, T> {

    protected Context context;
    private ProgressBar progressBar;
    private TaskProcessCallback taskProcessCallback;

    protected final Handler handler = new Handler();
    protected final Logger logger = new Logger(getClass().getName());
    
    public Task(Context context) { this.context = context; }
    
    public void setProgressDialog(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setTaskProcessCallback(TaskProcessCallback callback){
        this.taskProcessCallback = callback;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if ( taskProcessCallback != null )
            taskProcessCallback.startProcess();
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
        if( taskProcessCallback != null )
            taskProcessCallback.finishProcess();
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
