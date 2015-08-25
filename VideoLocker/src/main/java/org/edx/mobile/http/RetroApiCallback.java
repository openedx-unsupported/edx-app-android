package org.edx.mobile.http;

import android.content.Context;
import org.edx.mobile.view.common.TaskProcessCallback;

import java.lang.ref.WeakReference;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@Deprecated
public  abstract class RetroApiCallback <T>
        implements Callback<T> {

    private final WeakReference<Context> mContextRef;
    private  WeakReference<TaskProcessCallback> mCallback;

    public RetroApiCallback(Context activity) {
        this( activity, null);
    }

    public RetroApiCallback(Context activity, TaskProcessCallback callback) {
        mContextRef = new WeakReference<Context>(activity);
        if ( callback != null ) {
            mCallback = new WeakReference<TaskProcessCallback>(callback);
        } else if (activity instanceof  TaskProcessCallback){
            mCallback = new WeakReference<TaskProcessCallback>((TaskProcessCallback)activity);
        }
    }


    public Context getContext() {
        return mContextRef.get();
    }

    public TaskProcessCallback getTaskProcessCallback() {
        return mCallback == null ? null : mCallback.get();
    }


    public void onPreExecute() {
        TaskProcessCallback taskProcessCallback = getTaskProcessCallback();
        if ( taskProcessCallback != null )
            taskProcessCallback.startProcess();
    }


    /**
     * Successful HTTP response. if subclass overwrite this method, it should call super.success()
     * */
    @Override
    public void success(T t, Response response){
        TaskProcessCallback taskProcessCallback = getTaskProcessCallback();
        if( taskProcessCallback != null )
            taskProcessCallback.finishProcess();
    }


    /**
     * if subclass overwrite this method, it should call super.failure()
     */
    @Override
    public void failure(RetrofitError error) {
        TaskProcessCallback taskProcessCallback = getTaskProcessCallback();
        if( taskProcessCallback != null )
            taskProcessCallback.finishProcess();

        //TODO : this is the common place to handle exception.
        //we can get response status code and reason,
//        final Context activity = mContextRef.get();
//
//        Response response = error.getResponse();
//        if (response != null) {
//          //  Log.e(getLogTag(),   "code:" + response.getStatus() + ", reason:" + response.getReason());
//        }

    }


}