package org.edx.mobile.task;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskMessageCallback;
import org.edx.mobile.view.common.TaskProcessCallback;
import org.edx.mobile.view.common.TaskProgressCallback;

import java.lang.ref.WeakReference;

import roboguice.util.RoboAsyncTask;

public abstract class Task<T> extends RoboAsyncTask<T> {

    public enum Type {
        USER_INITIATED, LOADING_CACHED, LOADING_NON_CACHED
    }

    protected final Handler handler = new Handler();
    protected final Logger logger = new Logger(getClass().getName());

    @Nullable
    private WeakReference<TaskProgressCallback> progressCallback;

    @Nullable
    private WeakReference<TaskMessageCallback> messageCallback;

    private ProgressBar progressBar;

    @Inject
    protected IEdxEnvironment environment;

    private final Type taskType;

    public Task(Context context) {
        this(context, Type.LOADING_NON_CACHED);
    }

    public Task(Context context, Type type) {
        super(context);

        if (context instanceof TaskProcessCallback) {
            setTaskProcessCallback((TaskProcessCallback) context);
        }
        this.taskType = type;
    }

    public void setProgressDialog(@Nullable ProgressBar progressBar) {
        this.progressBar = progressBar;
        if (progressBar != null) {
            this.progressCallback = null;
        }
    }

    public void setTaskProcessCallback(@Nullable TaskProcessCallback callback) {
        setProgressCallback(callback);
        setMessageCallback(callback);
    }

    public void setProgressCallback(@Nullable TaskProgressCallback callback) {
        if (callback == null) {
            progressCallback = null;
        } else {
            progressCallback = new WeakReference<>(callback);
            progressBar = null;
        }
    }

    public void setMessageCallback(@Nullable TaskMessageCallback callback) {
        messageCallback = callback == null ? null : new WeakReference<>(callback);
    }

    @Nullable
    private TaskProgressCallback getProgressCallback() {
        return progressCallback == null ? null : progressCallback.get();
    }

    @Nullable
    private TaskMessageCallback getMessageCallback() {
        return messageCallback == null ? null : messageCallback.get();
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
        final TaskMessageCallback callback = getMessageCallback();
        if (callback == null) {
            return;
        }

        callback.onMessage(getMessageType(), ErrorUtils.getErrorMessage(ex, context));
    }

    /**
     * @return The {@link MessageType} based on the {@link #taskType}.
     */
    private MessageType getMessageType() {
        switch (taskType) {
            case USER_INITIATED:
                return MessageType.DIALOG;
            case LOADING_CACHED:
            case LOADING_NON_CACHED:
            default:
                return MessageType.FLYIN_ERROR;
        }
    }
}
