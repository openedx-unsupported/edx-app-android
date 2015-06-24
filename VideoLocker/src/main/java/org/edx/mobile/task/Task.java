package org.edx.mobile.task;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.view.common.TaskProcessCallback;

import roboguice.util.RoboAsyncTask;

public abstract class Task<T> extends RoboAsyncTask<T> {

    private ProgressBar progressBar;
    private TaskProcessCallback taskProcessCallback;

    protected final Handler handler = new Handler();
    protected final Logger logger = new Logger(getClass().getName());

    @Inject
    protected IEdxEnvironment environment;
    
    public Task(Context context) { super(context); }
    
    public void setProgressDialog(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setTaskProcessCallback(TaskProcessCallback callback){
        this.taskProcessCallback = callback;
    }
    @Override
    protected void onPreExecute() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if ( taskProcessCallback != null )
            taskProcessCallback.startProcess();
    }

    @Override
    protected void onFinally() {
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

}
