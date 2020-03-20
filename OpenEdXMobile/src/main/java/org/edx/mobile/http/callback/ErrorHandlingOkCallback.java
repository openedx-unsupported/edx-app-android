package org.edx.mobile.http.callback;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.notifications.ErrorNotification;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.common.TaskProgressCallback;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import roboguice.RoboGuice;

/**
 * Generic abstract implementation of OkHttps's {@link Callback}
 * interface, that takes care of converting the response body to an
 * object of the specified model class, and delivering status and
 * error information to the proper callbacks. It also provides (and
 * delegates to) a simpler callback interface for subclasses,
 * stripping out unnecessary parameters, and redirecting all responses
 * with error codes to the failure callback method. The callbacks are
 * guaranteed to be invoked on the main thread.
 */
public abstract class ErrorHandlingOkCallback<T> implements Callback {
    /**
     * A Handler for the main looper, for delivering messages on the main thread.
     */
    private static final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * A Context for resolving the error message strings.
     */
    @NonNull
    private final Context context;

    /**
     * The response body type.
     */
    @NonNull
    private final Type responseBodyType;

    /**
     * The callback to invoke on start and finish of the request.
     */
    @Nullable
    private final TaskProgressCallback progressCallback;

    /**
     * The notification display to invoke upon encountering an error.
     */
    @Nullable
    private final ErrorNotification errorNotification;

    /**
     * The notification display to invoke when user is viewing cached content.
     */
    @Nullable
    private final SnackbarErrorNotification snackbarErrorNotification;

    /**
     * The listener to invoke when user wants to refresh the content being viewed.
     */
    @Nullable
    private final RefreshListener refreshListener;

    /**
     * The Gson instance for converting the response body to the desired type.
     */
    @Inject
    private Gson gson;

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings.
     * @param responseBodyClass The response body class.
     * @param progressCallback The callback to invoke on start and finish of the request. Note that
     *                         since no callback method in this class is invoked upon request
     *                         initiation, it assumes that it's being initiated immediately, and
     *                         thus invokes that start callback immediately as well.
     * @param errorNotification The notification display to invoke upon encountering an error.
     */
    public ErrorHandlingOkCallback(@NonNull final Context context,
                                   @NonNull final Class<T> responseBodyClass,
                                   @Nullable final TaskProgressCallback progressCallback,
                                   @Nullable final ErrorNotification errorNotification) {
        this(context, (Type) responseBodyClass, progressCallback, errorNotification, null, null);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings.
     * @param responseBodyTypeToken The response body type token.
     * @param progressCallback The callback to invoke on start and finish of the request. Note that
     *                         since no callback method in this class is invoked upon request
     *                         initiation, it assumes that it's being initiated immediately, and
     *                         thus invokes that start callback immediately as well.
     * @param errorNotification The notification display to invoke upon encountering an error.
     */
    public ErrorHandlingOkCallback(@NonNull final Context context,
                                   @NonNull final TypeToken<T> responseBodyTypeToken,
                                   @Nullable final TaskProgressCallback progressCallback,
                                   @Nullable final ErrorNotification errorNotification) {
        this(context, responseBodyTypeToken.getType(), progressCallback, errorNotification, null, null);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings. Note that for convenience,
     *                this will be checked to determine whether it's implementing any of the
     *                callback interfaces, and will be registered as such if so. If this is not the
     *                desired outcome, then one of the alternative constructors should be used
     *                instead, with the relevant callback parameters explicitly passed as null (this
     *                may require casting the null in case of ambiguity when using a constructor
     *                that only sets one callback explicitly).
     * @param responseBodyClass The response body class.
     * @param errorNotification The notification display to invoke upon encountering an error.
     */
    public ErrorHandlingOkCallback(@NonNull final Context context,
                                   @NonNull final Class<T> responseBodyClass,
                                   @Nullable final ErrorNotification errorNotification) {
        this(context, responseBodyClass,
                context instanceof TaskProgressCallback ? (TaskProgressCallback) context : null,
                errorNotification, null, null);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings. Note that for convenience,
     *                this will be checked to determine whether it's implementing any of the
     *                callback interfaces, and will be registered as such if so. If this is not the
     *                desired outcome, then one of the alternative constructors should be used
     *                instead, with the relevant callback parameters explicitly passed as null (this
     *                may require casting the null in case of ambiguity when using a constructor
     *                that only sets one callback explicitly).
     * @param responseBodyTypeToken The response body type token.
     * @param errorNotification The notification display to invoke upon encountering an error.
     */
    public ErrorHandlingOkCallback(@NonNull final Context context,
                                   @NonNull final TypeToken<T> responseBodyTypeToken,
                                   @Nullable final ErrorNotification errorNotification) {
        this(context, responseBodyTypeToken.getType(),
                context instanceof TaskProgressCallback ? (TaskProgressCallback) context : null,
                errorNotification, null, null);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings. Note that for convenience,
     *                this will be checked to determine whether it's implementing any of the
     *                callback interfaces, and will be registered as such if so. If this is not the
     *                desired outcome, then one of the alternative constructors should be used
     *                instead, with the relevant callback parameters explicitly passed as null (this
     *                may require casting the null in case of ambiguity when using a constructor
     *                that only sets one callback explicitly).
     * @param responseBodyClass The response body class.
     * @param errorNotification The notification display to invoke upon encountering an error.
     * @param snackbarErrorNotification The notification display to invoke when user is viewing cached content.
     * @param refreshListener The listener to invoke when user wants to refresh the content being viewed.
     */
    public ErrorHandlingOkCallback(@NonNull final Context context,
                                   @NonNull final Class<T> responseBodyClass,
                                   @Nullable final ErrorNotification errorNotification,
                                   @Nullable final SnackbarErrorNotification snackbarErrorNotification,
                                   @Nullable final RefreshListener refreshListener) {
        this(context, responseBodyClass,
                context instanceof TaskProgressCallback ? (TaskProgressCallback) context : null,
                errorNotification, snackbarErrorNotification, refreshListener);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings. Note that for convenience,
     *                this will be checked to determine whether it's implementing any of the
     *                callback interfaces, and will be registered as such if so. If this is not the
     *                desired outcome, then one of the alternative constructors should be used
     *                instead, with the relevant callback parameters explicitly passed as null (this
     *                may require casting the null in case of ambiguity when using a constructor
     *                that only sets one callback explicitly).
     * @param responseBodyTypeToken The response body type token.
     * @param errorNotification The notification display to invoke upon encountering an error.
     * @param snackbarErrorNotification The notification display to invoke when user is viewing cached content.
     * @param refreshListener The listener to invoke when user wants to refresh the content being viewed.
     */
    public ErrorHandlingOkCallback(@NonNull final Context context,
                                   @NonNull final TypeToken<T> responseBodyTypeToken,
                                   @Nullable final ErrorNotification errorNotification,
                                   @Nullable final SnackbarErrorNotification snackbarErrorNotification,
                                   @Nullable final RefreshListener refreshListener) {
        this(context, responseBodyTypeToken.getType(),
                context instanceof TaskProgressCallback ? (TaskProgressCallback) context : null,
                errorNotification, snackbarErrorNotification, refreshListener);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings.
     * @param responseBodyType The response body type.
     * @param progressCallback The callback to invoke on start and finish of the request. Note that
     *                         since no callback method in this class is invoked upon request
     *                         initiation, it assumes that it's being initiated immediately, and
     *                         thus invokes that start callback immediately as well.
     * @param errorNotification The notification display to invoke upon encountering an error.
     * @param snackbarErrorNotification The notification display to invoke when user is viewing cached content.
     * @param refreshListener The listener to invoke when user wants to refresh the content being viewed.
     */
    public ErrorHandlingOkCallback(@NonNull final Context context,
                                   @NonNull final Type responseBodyType,
                                   @Nullable final TaskProgressCallback progressCallback,
                                   @Nullable final ErrorNotification errorNotification,
                                   @Nullable final SnackbarErrorNotification snackbarErrorNotification,
                                   @Nullable final RefreshListener refreshListener) {
        this.context = context;
        this.responseBodyType = responseBodyType;
        this.progressCallback = progressCallback;
        this.errorNotification = errorNotification;
        this.snackbarErrorNotification = snackbarErrorNotification;
        this.refreshListener = refreshListener;
        // For the convenience of subclasses
        RoboGuice.injectMembers(context, this);
        if (progressCallback != null) {
            progressCallback.startProcess();
        }
    }

    /**
     * The original callback method invoked by OkHttp upon receiving an HTTP response. This method
     * definition provides extra information that's not needed by most individual callback
     * implementations, and is also invoked when HTTP error status codes are encountered (forcing
     * the implementation to manually check for success in each case). Therefore this implementation
     * delegates to {@link #onResponse(T)} in the case where it receives a successful HTTP status
     * code, and to {@link #onFailure(Throwable)} otherwise, passing an instance of
     * {@link HttpStatusException} with the relevant error status code. This method is declared as
     * final, as subclasses are meant to be implementing the abstract {@link #onResponse(T)} method
     * instead of this one.
     * <p>
     * This implementation takes care of delivering the appropriate error message to it's registered
     * callback, and invoking the callback for request process completion.
     *
     * @param call The Call object that was used to enqueue the request.
     * @param response The HTTP response data.
     */
    @Override
    public final void onResponse(@NonNull Call call, @NonNull final Response response) {
        if (!response.isSuccessful()) {
            deliverFailure(new HttpStatusException(response));
        } else {
            final String responseBodyString;
            try {
                responseBodyString = response.body().string();
            } catch (IOException error) {
                deliverFailure(error);
                return;
            }
            final T responseBody = gson.fromJson(responseBodyString, responseBodyType);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (progressCallback != null) {
                        progressCallback.finishProcess();
                    }
                    onResponse(responseBody);

                    // Show SnackBar if user is seeing cached content while being offline.
                    if (response.networkResponse() == null && !NetworkUtil.isConnected(context)) {
                        if (snackbarErrorNotification != null && refreshListener != null) {
                            snackbarErrorNotification.showError(R.string.offline_text,
                                    FontAwesomeIcons.fa_wifi, R.string.lbl_reload,
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (NetworkUtil.isConnected(context)) {
                                                refreshListener.onRefresh();
                                                snackbarErrorNotification.hideError();
                                            }
                                        }
                                    });
                        }
                    }
                    onFinish();
                }
            });
        }
    }

    /**
     * The original callback method invoked by OkHttp upon failure to receive an HTTP response,
     * whether due to cancellation, a connectivity problem, or a timeout. This method definition
     * provides extra information that's not needed by most individual callback implementations, so
     * this implementation only delegates to {@link #onFailure(Throwable)}. This method is declared
     * as final, as subclasses are meant to be implementing the abstract {@link #onResponse(T)}
     * method instead of this one.
     *
     * @param call The Call object that was used to enqueue the request.
     * @param error The cause of the request being interrupted.
     */
    @Override
    public final void onFailure(@NonNull Call call, @NonNull IOException error) {
        deliverFailure(error);
    }

    /**
     * Convenience method for taking care of invoking the failure callbacks and invoking the new
     * failure callback method in the case on an error, used by both the original failure callback
     * method, and the success callback method in the case where it encounters an HTTP error status
     * code.
     *
     * @param error An {@link IOException} if the request failed due to a network failure, or an
     *              {HttpResponseStatusException} if the failure was due to receiving an error code.
     */
    private void deliverFailure(@NonNull final Throwable error) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (progressCallback != null) {
                    progressCallback.finishProcess();
                }
                if (errorNotification != null) {
                    if (refreshListener != null) {
                        errorNotification.showError(context, error, R.string.lbl_reload,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (NetworkUtil.isConnected(context)) {
                                            refreshListener.onRefresh();
                                        }
                                    }
                                });
                    } else {
                        errorNotification.showError(context, error);
                    }
                }
                onFailure(error);
                onFinish();
            }
        });
    }

    /**
     * Callback method for a successful HTTP response.
     *
     * @param responseBody The response body, converted to an instance of it's associated Java
     *                     class.
     */
    protected abstract void onResponse(@NonNull final T responseBody);

    /**
     * Callback method for when the HTTP response was not received successfully, whether due to
     * cancellation, a connectivity problem, or a timeout, or receiving an HTTP error status code.
     *
     * @param error An {@link IOException} if the request failed due to a network failure, or an
     *              {HttpResponseStatusException} if the failure was due to receiving an error code.
     */
    protected void onFailure(@NonNull final Throwable error) {}

    /**
     * Callback method that gets invoked at last when {@link #onResponse(Object)} or
     * {@link #onResponse(Object)} have been called.
     */
    protected void onFinish() {}
}
