package org.edx.mobile.task;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.view.common.BannerDisplayCallback;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskMessageCallback;
import org.edx.mobile.view.common.TaskProcessCallback;
import org.edx.mobile.view.common.TaskProgressCallback;

import java.lang.ref.WeakReference;

import retrofit.RetrofitError;
import roboguice.util.RoboAsyncTask;

public abstract class Task<T> extends RoboAsyncTask<T> {

    private ProgressBar progressBar;

    @Nullable
    private WeakReference<TaskProgressCallback> progressCallback;
    @Nullable
    private WeakReference<TaskMessageCallback> messageCallback;
    @Nullable
    private WeakReference<BannerDisplayCallback> bannerDisplayCallback;

    protected final Handler handler = new Handler();
    protected final Logger logger = new Logger(getClass().getName());

    @Inject
    protected IEdxEnvironment environment;

    public Task(Context context) {
        super(context);

        if (context instanceof TaskProcessCallback) {
            setTaskProcessCallback((TaskProcessCallback) context);
        }
    }

    public void setProgressDialog(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setTaskProcessCallback(@Nullable TaskProcessCallback callback) {
        setProgressCallback(callback);
        setMessageCallback(callback);
        setBannerDisplayCallback(callback);
    }

    public void setProgressCallback(@Nullable TaskProgressCallback callback) {
        progressCallback = callback == null ? null : new WeakReference<>(callback);
    }

    public void setMessageCallback(@Nullable TaskMessageCallback callback) {
        messageCallback = callback == null ? null : new WeakReference<>(callback);
    }

    public void setBannerDisplayCallback(@Nullable BannerDisplayCallback callback) {
        bannerDisplayCallback = callback == null ? null : new WeakReference<>(callback);
    }

    @Nullable
    private TaskProgressCallback getProgressCallback() {
        return progressCallback == null ? null : progressCallback.get();
    }

    @Nullable
    private TaskMessageCallback getMessageCallback() {
        return messageCallback == null ? null : messageCallback.get();
    }

    @Nullable
    private BannerDisplayCallback getBannerDisplayCallback() {
        return bannerDisplayCallback == null ? null : bannerDisplayCallback.get();
    }

    @Override
    protected void onPreExecute() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        final TaskProgressCallback callback = getProgressCallback();
        if (callback != null) {
            callback.startProcess();
        }
    }

    @Override
    protected void onFinally() {
        stopProgress();
    }

    protected void stopProgress() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        final TaskProgressCallback callback = getProgressCallback();
        if (callback != null) {
            callback.finishProcess();
        }
    }

    @Override
    protected void onException(Exception ex) {
        ErrorUtils.displayErrorMessage(ex, context,
                getMessageCallback(), getBannerDisplayCallback());
    }
}
