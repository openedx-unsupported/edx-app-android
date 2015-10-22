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
    }

    public void setProgressCallback(@Nullable TaskProgressCallback callback) {
        progressCallback = callback == null ? null : new WeakReference<>(callback);
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

    protected void handle(final Exception ex) {
        handler.post(new Runnable() {
            public void run() {
                showErrorMessage(ex);
                onException(ex);
            }
        });
    }

    // TODO: Make this the default behaviour?
    protected void showErrorMessage(final Exception ex) {
        final TaskMessageCallback callback = getMessageCallback();
        if (callback == null) {
            return;
        }
        String errorMessage = null;
        if (ex instanceof RetroHttpException) {
            RetrofitError cause = ((RetroHttpException) ex).getCause();
            switch (cause.getKind()) {
                case NETWORK: {
                    if (NetworkUtil.isConnected(getContext())) {
                        errorMessage = getContext().getString(R.string.network_connected_error);
                    } else {
                        errorMessage = getContext().getString(R.string.reset_no_network_message);
                    }
                    break;
                }
                case HTTP: {
                    if (cause.getResponse() != null) {
                        final int status = cause.getResponse().getStatus();
                        if (status == 503) {
                            errorMessage = getContext().getString(R.string.network_service_unavailable);
                        }
                    }
                }
                case CONVERSION:
                case UNEXPECTED: {
                    // Use default message
                    break;
                }
            }
        }
        if (null == errorMessage) {
            logger.error(ex, true /* Submit crash report since this is an unknown type of error */);
            errorMessage = getContext().getString(R.string.error_unknown);
        }
        callback.onMessage(MessageType.FLYIN_ERROR, errorMessage);
    }
}
