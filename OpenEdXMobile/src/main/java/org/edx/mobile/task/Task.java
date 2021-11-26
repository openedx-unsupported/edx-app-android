package org.edx.mobile.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskMessageCallback;
import org.edx.mobile.view.common.TaskProcessCallback;
import org.edx.mobile.view.common.TaskProgressCallback;

import java.lang.ref.WeakReference;

import dagger.hilt.android.EntryPointAccessors;

/**
 * This class is deprecated. Issues in it's
 * implementation include the lack of a guarantee of the
 * result not being delivered to the callback method
 * after cancellation.
 * <p>
 * New asynchronous HTTP request implementations should
 * consider using Retrofit's asynchronous API. If that's
 * not sufficient, or if the implementation is not of an
 * HTTP call, then AsyncTask or Loader implementations
 * should be considered instead.
 */
@Deprecated
public abstract class Task<T> extends AsyncTask<Void, Void, T> {

    protected final Handler handler = new Handler();
    protected final Logger logger = new Logger(getClass().getName());

    @Nullable
    private WeakReference<TaskProgressCallback> progressCallback;

    @Nullable
    private WeakReference<TaskMessageCallback> messageCallback;

    private WeakReference<View> progressView;

    @NonNull
    protected final WeakReference<Context> context;

    protected IEdxEnvironment environment;

    private final CallTrigger callTrigger;

    public Task(Context context) {
        this(context, CallTrigger.LOADING_UNCACHED);
    }

    @SuppressWarnings("deprecation")
    public Task(Context context, CallTrigger callTrigger) {
        this.context = new WeakReference<>(context);
        this.environment = EntryPointAccessors
                .fromApplication(context, EdxDefaultModule.ProviderEntryPoint.class).getEnvironment();
        if (context instanceof TaskProcessCallback) {
            setTaskProcessCallback((TaskProcessCallback) context);
        }
        this.callTrigger = callTrigger;
    }

    public void setProgressDialog(@Nullable View progressView) {
        if (progressView != null) {
            this.progressView = new WeakReference<>(progressView);
        }
        if (progressView != null) {
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
            progressView = null;
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
        if (progressView != null) {
            progressView.get().setVisibility(View.VISIBLE);
        }
        final TaskProgressCallback callback = getProgressCallback();
        if (callback != null) {
            callback.startProcess();
        }
    }

    @Override
    protected void onPostExecute(T unused) {
        super.onPostExecute(unused);
        stopProgress();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        stopProgress();
        final TaskMessageCallback callback = getMessageCallback();
        if (callback == null) {
            return;
        }

        callback.onMessage(getMessageType(),
                ErrorUtils.getErrorMessage(new Exception(getMessageType().name()),
                        callTrigger, context.get()));
    }

    @Override
    protected void onCancelled(T unused) {
        super.onCancelled(unused);
        this.onCancelled();
    }

    protected void stopProgress() {
        if (progressView != null) {
            progressView.get().setVisibility(View.GONE);
            progressView.get().setAnimation(null);
        }
        final TaskProgressCallback callback = getProgressCallback();
        if (callback != null) {
            callback.finishProcess();
        }
    }

    /**
     * @return The {@link MessageType} based on the {@link #callTrigger}.
     */
    private MessageType getMessageType() {
        switch (callTrigger) {
            case USER_ACTION:
                return MessageType.DIALOG;
            case LOADING_CACHED:
            case LOADING_UNCACHED:
            default:
                return MessageType.FLYIN_ERROR;
        }
    }

    protected void handleException(Exception e) {
        new Handler(Looper.getMainLooper()).post(() -> onException(e));
    }

    public abstract void onException(Exception ex);
}
