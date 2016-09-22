package org.edx.mobile.http;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
     * The trigger for initiating the call. This is used to determine the type of error message to
     * deliver.
     */
    @NonNull
    private final CallTrigger callTrigger;

    /**
     * The callback to invoke on start and finish of the request.
     */
    @Nullable
    private final TaskProgressCallback progressCallback;

    /**
     * The callback to invoke for delivering any error messages.
     */
    @Nullable
    private final TaskMessageCallback messageCallback;

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
     * @param callTrigger The trigger for initiating the call. This is used to determine the type of
     *                    error message to deliver.
     */
    public ErrorHandlingCallback(@NonNull final Context context,
                                 @NonNull final CallTrigger callTrigger) {
        this(context, callTrigger,
                context instanceof TaskProgressCallback ? (TaskProgressCallback) context : null,
                context instanceof TaskMessageCallback ? (TaskMessageCallback) context : null);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings. Note that for convenience,
     *                this will be checked to determine whether it's implementing the
     *                {@link TaskMessageCallback} interface, and will be registered as such if so.
     *                If this is not the desired outcome, then the other constructor should be used
     *                that takes this callback parameter, and it should be explicitly set as null.
     * @param callTrigger The trigger for initiating the call. This is used to determine the type of
     *                    error message to deliver.
     * @param progressCallback The callback to invoke on start and finish of the request. Note that
     *                         since no callback method in this class is invoked upon request
     *                         initiation, it assumes that it's being initiated immediately, and
     *                         thus invokes that start callback immediately as well.
     */
    public ErrorHandlingCallback(@NonNull final Context context,
                                 @NonNull final CallTrigger callTrigger,
                                 @Nullable final TaskProgressCallback progressCallback) {
        this(context, callTrigger,
                progressCallback,
                context instanceof TaskMessageCallback ? (TaskMessageCallback) context : null);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings. Note that for convenience,
     *                this will be checked to determine whether it's implementing the
     *                {@link TaskProgressCallback} interface, and will be registered as such if so.
     *                If this is not the desired outcome, then the other constructor should be used
     *                that takes this callback parameter, and it should be explicitly set as null.
     * @param callTrigger The trigger for initiating the call. This is used to determine the type of
     *                    error message to deliver.
     * @param messageCallback The callback to invoke for delivering any error messages.
     */
    public ErrorHandlingCallback(@NonNull final Context context,
                                 @NonNull final CallTrigger callTrigger,
                                 @Nullable final TaskMessageCallback messageCallback) {
        this(context, callTrigger,
                context instanceof TaskProgressCallback ? (TaskProgressCallback) context : null,
                messageCallback);
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context for resolving the error message strings.
     * @param callTrigger The trigger for initiating the call. This is used to determine the type of
     *                    error message to deliver.
     * @param progressCallback The callback to invoke on start and finish of the request. Note that
     *                         since no callback method in this class is invoked upon request
     *                         initiation, it assumes that it's being initiated immediately, and
     *                         thus invokes that start callback immediately as well.
     * @param messageCallback The callback to invoke for delivering any error messages.
     */
    public ErrorHandlingCallback(@NonNull final Context context,
                                 @NonNull final CallTrigger callTrigger,
                                 @Nullable final TaskProgressCallback progressCallback,
                                 @Nullable final TaskMessageCallback messageCallback) {
        this.context = context;
        this.callTrigger = callTrigger;
        this.progressCallback = progressCallback;
        this.messageCallback = messageCallback;
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
     * {@link HttpResponseStatusException} with the relevant error status code. This method is
     * declared as final, as subclasses are meant to be implementing the abstract
     * {@link #onResponse(Object)} method instead of this one.
     * <p>
     * This implementation takes care of invoking the callback for request process completion.
     *
     * @param call The Call object that was used to enqueue the request.
     * @param response The HTTP response data.
     */
    @Override
    public final void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (!response.isSuccessful()) {
            onFailure(call, new HttpResponseStatusException(response.code()));
        } else {
            if (progressCallback != null) {
                progressCallback.finishProcess();
            }
            onResponse(response.body());
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
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable error) {
        if (progressCallback != null) {
            progressCallback.finishProcess();
        }
        if (messageCallback != null) {
            messageCallback.onMessage(callTrigger.getMessageType(),
                    ErrorUtils.getErrorMessage(error, context));
        }
        onFailure(error);
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
}
