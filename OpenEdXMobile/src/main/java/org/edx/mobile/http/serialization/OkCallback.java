package org.edx.mobile.http.serialization;

import android.support.annotation.NonNull;

import org.edx.mobile.http.HttpResponseStatusException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Abstract implementation of OkHttp's
 * {@link Callback} interface, which provides (and
 * delegates to) a simpler interface for subclasses,
 * stripping out unnecessary parameters, and
 * redirecting all responses with error codes to the
 * failure callback method.
 */
public abstract class OkCallback implements Callback {
    /**
     * Callback method for a successful HTTP response.
     *
     * @param response The response.
     */
    protected abstract void onResponse(@NonNull final Response response);

    /**
     * Callback method for when the HTTP response was not received successfully, whether due to
     * cancellation, a connectivity problem, or a timeout, or receiving an HTTP error status code.
     *
     * @param error An {@link IOException} if the request failed due to a network failure, or an
     *              {HttpResponseStatusException} if the failure was due to receiving an error code.
     */
    protected void onFailure(@NonNull final Throwable error) {}

    /**
     * The original callback method invoked by OkHttp upon receiving an HTTP response. This method
     * definition provides extra information that's not needed by most individual callback
     * implementations, and is also invoked when HTTP error status codes are encountered (forcing
     * the implementation to manually check for success in each case). Therefore this implementation
     * only delegates to {@link #onResponse(Response)} in the case where it receives a successful
     * HTTP status code, and to {@link #onFailure(Throwable)} otherwise, passing an instance of
     * {@link HttpResponseStatusException} with the relevant error status code. This method is
     * declared as final, as subclasses are meant to be implementing the abstract
     * {@link #onResponse(Response)} method instead of this one.
     *
     * @param call The Call object that was used to enqueue the request.
     * @param response The HTTP response data.
     */
    @Override
    public final void onResponse(@NonNull final Call call, @NonNull final Response response) {
        if (response.isSuccessful()) {
            onResponse(response);
        } else {
            onFailure(new HttpResponseStatusException(response));
        }
    }

    /**
     * The original callback method invoked by OkHttp upon failure to receive an HTTP response,
     * whether due to cancellation, a connectivity problem, or a timeout. This method definition
     * provides extra information that's not needed by most individual callback implementations, so
     * this implementation only delegates to {@link #onFailure(Throwable)}. This method is declared
     * as final, as subclasses are meant to be implementing the abstract
     * {@link #onResponse(Response)} method instead of this one.
     *
     * @param call The Call object that was used to enqueue the request.
     * @param error The cause of the request being interrupted.
     */
    @Override
    public final void onFailure(@NonNull final Call call, @NonNull final IOException error) {
        onFailure(error);
    }

    /**
     * A dummy implementation of OkHttp's {@link Callback} interface, to be used in one-off calls,
     * where the caller doesn't care about the result of the enqueued {@link Call}.
     */
    public static final Callback DUMMY_CALLBACK = new Callback() {
        @Override
        public void onResponse(@NonNull final Call call, @NonNull final Response response) {}
        @Override
        public void onFailure(@NonNull final Call call, @NonNull final IOException e) {}
    };
}
