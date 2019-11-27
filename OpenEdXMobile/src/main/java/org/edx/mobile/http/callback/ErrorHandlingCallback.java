package org.edx.mobile.http.callback;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.notifications.ErrorNotification;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.images.ErrorUtils;
import org.edx.mobile.view.common.TaskMessageCallback;
import org.edx.mobile.view.common.TaskProgressCallback;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import roboguice.RoboGuice;

/**
 * Generic abstract implementation of Retrofit's
 * {@link retrofit2.Callback} interface, that takes care of delivering
 * status and error information to the proper callbacks. It also
 * provides (and delegates to) a simpler callback interface for
 * subclasses, stripping out unnecessary parameters, and redirecting
 * all responses with error codes to the failure callback method (as it
 * used to be in the implementation in Retrofit 1).
 *
 * @param <T> The successful response body type.
 */
public abstract class ErrorHandlingCallback<T> implements Callback<T> {
    /**
     * A Context for resolving the error message strings.
     */
    @NonNull
    private final Context context;

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

    //TODO: Remove this legacy code starting from here, when modern error design has been implemented on all screens i.e. SnackBar, FullScreen and Dialog based errors.
    @Nullable
    private TaskMessageCallback messageCallback;

    @Nullable
    private CallTrigger callTrigger;

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings. Note that for convenience,
     *                this will be checked to determine whether it's implementing the
     *                {@link TaskProgressCallback} interface, and will be registered as such if so.
     *                If this is not the desired outcome, then one of the alternative constructors
     *                should be used instead, with the relevant callback parameters explicitly
     *                passed as null (this may require casting the null in case of ambiguity when
     *                using a constructor that only sets one callback explicitly).
     * @param messageCallback The callback to invoke for delivering any error messages.
     */
    public ErrorHandlingCallback(@NonNull final Context context,
                                 @Nullable TaskProgressCallback progressCallback,
                                 @Nullable TaskMessageCallback messageCallback,
                                 @Nullable CallTrigger callTrigger) {
        this(context, progressCallback, null, null, null);
        this.messageCallback = messageCallback;
        this.callTrigger = callTrigger;
    }
    // LEGACY CODE ENDS HERE, all occurrences of {@link #messageCallback} should also be deleted in future

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings. Note that for convenience,
     *                this will be checked to determine whether it's implementing the
     *                {@link TaskProgressCallback} interface, and will be registered as such if so.
     *                If this is not the desired outcome, then one of the alternative constructors
     *                should be used instead, with the relevant callback parameters explicitly
     *                passed as null (this may require casting the null in case of ambiguity when
     *                using a constructor that only sets one callback explicitly).
     */
    public ErrorHandlingCallback(@NonNull final Context context) {
        this(context, context instanceof TaskProgressCallback ?
                        (TaskProgressCallback) context : null,
                null, null, null);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings.
     * @param progressCallback The callback to invoke on start and finish of the request. Note that
     *                         since no callback method in this class is invoked upon request
     *                         initiation, it assumes that it's being initiated immediately, and
     *                         thus invokes that start callback immediately as well.
     */
    public ErrorHandlingCallback(@NonNull final Context context,
                                 @Nullable final TaskProgressCallback progressCallback) {
        this(context, progressCallback, null, null, null);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings.
     * @param errorNotification The notification display to invoke upon encountering an error.
     */
    public ErrorHandlingCallback(@NonNull final Context context,
                                 @Nullable final ErrorNotification errorNotification) {
        this(context, null, errorNotification, null, null);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings.
     * @param errorNotification The notification display to invoke upon encountering an error.
     */
    public ErrorHandlingCallback(@NonNull final Context context,
                                 @Nullable final TaskProgressCallback progressCallback,
                                 @Nullable final ErrorNotification errorNotification) {
        this(context, progressCallback, errorNotification, null, null);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings.
     * @param errorNotification The notification display to invoke upon encountering an error.
     * @param snackbarErrorNotification The notification display to invoke when user is viewing cached content.
     * @param refreshListener The listener to invoke when user wants to refresh the content being viewed.
     */
    public ErrorHandlingCallback(@NonNull final Context context,
                                 @Nullable final ErrorNotification errorNotification,
                                 @Nullable final SnackbarErrorNotification snackbarErrorNotification,
                                 @Nullable final RefreshListener refreshListener) {
        this(context, context instanceof TaskProgressCallback ?
                        (TaskProgressCallback) context : null, errorNotification,
                snackbarErrorNotification, refreshListener);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings.
     * @param progressCallback The callback to invoke on start and finish of the request. Note that
     *                         since no callback method in this class is invoked upon request
     *                         initiation, it assumes that it's being initiated immediately, and
     *                         thus invokes that start callback immediately as well.
     * @param errorNotification The notification display to invoke upon encountering an error.
     * @param snackbarErrorNotification The notification display to invoke when user is viewing cached content.
     * @param refreshListener The listener to invoke when user wants to refresh the content being viewed.
     */
    public ErrorHandlingCallback(@NonNull final Context context,
                                 @Nullable final TaskProgressCallback progressCallback,
                                 @Nullable final ErrorNotification errorNotification,
                                 @Nullable final SnackbarErrorNotification snackbarErrorNotification,
                                 @Nullable final RefreshListener refreshListener) {
        this.context = context;
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
     * The original callback method invoked by Retrofit upon receiving an HTTP response. This method
     * definition provides extra information that's not needed by most individual callback
     * implementations, and is also invoked when HTTP error status codes are encountered (forcing
     * the implementation to manually check for success in each case). Therefore this implementation
     * delegates to {@link #onResponse(Object)} in the case where it receives a successful HTTP
     * status code, and to {@link #onFailure(Call, Throwable)} otherwise, passing an instance of
     * {@link HttpStatusException} with the relevant error status code. This method is
     * declared as final, as subclasses are meant to be implementing the abstract
     * {@link #onResponse(Object)} method instead of this one.
     * <p>
     * This implementation takes care of invoking the callback for request process completion.
     *
     * @param call The Call object that was used to enqueue the request.
     * @param response The HTTP response data.
     */
    @Override
    public final void onResponse(@NonNull final Call<T> call, @NonNull final Response<T> response) {
        if (!response.isSuccessful()) {
            onFailure(call, new HttpStatusException(response));
        } else {
            if (progressCallback != null) {
                progressCallback.finishProcess();
            }
            onResponse(response.body());

            // Show SnackBar if user is seeing cached content while being offline.
            if (response.raw().networkResponse() == null && !NetworkUtil.isConnected(context)) {
                if (snackbarErrorNotification != null && refreshListener != null) {
                    snackbarErrorNotification.showError(R.string.offline_text, FontAwesomeIcons.fa_wifi,
                            R.string.lbl_reload,
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
    }

    /**
     * The original callback method invoked by Retrofit upon failure to receive an HTTP response,
     * whether due to encountering a network error while waiting for the response, or some other
     * unexpected error while constructing the request or processing the response. It's also invoked
     * by the {@link #onResponse(Call, Response)} implementation when it receives an HTTP error
     * status code. However, this method definition provides extra information that's not needed by
     * most individual callback implementation, so this implementation only delegates to
     * {@link #onFailure(Throwable)}.
     * <p>
     * This implementation takes care of delivering the appropriate error message to it's registered
     * callback, and invoking the callback for request process completion. It should only be
     * overridden if the subclass wants to handle or control these actions itself; otherwise
     * subclasses should override the empty {@link #onFailure(Throwable)} callback method instead.
     *
     * @param call The Call object that was used to enqueue the request.
     * @param error An {@link IOException} if the request failed due to a network failure, an
     *              {HttpResponseStatusException} if the failure was due to receiving an error code,
     *              or any {@link Throwable} implementation if one was thrown unexpectedly while
     *              creating the request or processing the response.
     */
    @Override
    public void onFailure(@NonNull final Call<T> call, @NonNull final Throwable error) {
        if (progressCallback != null) {
            progressCallback.finishProcess();
        }
        if (messageCallback != null && callTrigger != null && !call.isCanceled()) {
            messageCallback.onMessage(callTrigger.getMessageType(),
                    ErrorUtils.getErrorMessage(error, callTrigger, context));
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

    /**
     * Callback method for a successful HTTP response.
     *
     * @param responseBody The response body, converted to an instance of it's associated Java
     *                     class.
     */
    protected abstract void onResponse(@NonNull final T responseBody);

    /**
     * Callback method for when the HTTP response was not received successfully, whether due to a
     * network failure, receiving an HTTP error status code, or encountering an unexpected exception
     * or error during the request creation or response processing phase.
     *
     * @param error An {@link IOException} if the request failed due to a network failure, an
     *              {HttpResponseStatusException} if the failure was due to receiving an error code,
     *              or any {@link Throwable} implementation if one was thrown unexpectedly while
     *              creating the request or processing the response.
     */
    protected void onFailure(@NonNull final Throwable error) {}

    /**
     * Callback method that gets invoked at last when {@link #onResponse(Object)} or
     * {@link #onResponse(Object)} have been called.
     */
    protected void onFinish() {}
}