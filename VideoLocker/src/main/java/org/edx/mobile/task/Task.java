package org.edx.mobile.task;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import org.apache.http.HttpStatus;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskProcessCallback;

import java.lang.ref.WeakReference;

import retrofit.RetrofitError;
import roboguice.util.RoboAsyncTask;

public abstract class Task<T> extends RoboAsyncTask<T> {

    private ProgressBar progressBar;

    private WeakReference<TaskProcessCallback> taskProcessCallback;

    protected final Handler handler = new Handler();
    protected final Logger logger = new Logger(getClass().getName());

    @Inject
    protected IEdxEnvironment environment;
    
    public Task(Context context) {
        super(context);

        if ( context instanceof TaskProcessCallback ){
            setTaskProcessCallback((TaskProcessCallback) context);
        }
    }
    
    public void setProgressDialog(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setTaskProcessCallback(TaskProcessCallback callback){
        this.taskProcessCallback = new WeakReference<TaskProcessCallback>(callback);
    }

    public TaskProcessCallback getTaskProcessCallback(){
        return this.taskProcessCallback == null ? null : this.taskProcessCallback.get();
    }

    @Override
    protected void onPreExecute() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        TaskProcessCallback callback = getTaskProcessCallback();
        if ( callback != null )
            callback.startProcess();
    }

    @Override
    protected void onFinally() {
        stopProgress();
    }
    
    protected void stopProgress() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        TaskProcessCallback callback = getTaskProcessCallback();
        if( callback != null )
            callback.finishProcess();
    }

    protected void handle(final Exception ex) {
        handler.post(new Runnable() {
            public void run() {
                //TODO - we should be able to handle common exceptions here
                // provide user the common error message based on common error code
                if ( ex instanceof RetroHttpException){
                    String errorMessage = "";
                    RetrofitError cause = ((RetroHttpException)ex).cause;
                    if ( cause.getResponse() == null ){
                        errorMessage = "Service is not available. Please try it later.";
                    } else {
                        int status = cause.getResponse().getStatus();
                        //should we use HttpStatus?
                        //TODO - we should put error message in the xml file

                        if (status >= 400 && status < 500) {
                            errorMessage = "Error occurs during request. You may need permission for finish request";
                        } else if (status >= 500) {
                            errorMessage = "Service is not available. Please try it later.";
                        }
                    }
                    //TODO - should we show message from server response as the last option?
                    //how about the localization?
                    if ( errorMessage.length() > 0 ){
                        TaskProcessCallback callback = getTaskProcessCallback();
                        if( callback != null ){
                            callback.onMessage(MessageType.FLYIN_ERROR, errorMessage);
                        }
                    }
                }


                onException(ex);
            }
        });
    }

}
