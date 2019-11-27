package org.edx.mobile.http.callback;

import androidx.annotation.NonNull;

import org.edx.mobile.http.HttpStatusException;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Abstract implementation of Retrofit's {@link retrofit2.Callback}
 * interface, which provides (and delegates to) a simpler interface
 * for subclasses, stripping out unnecessary parameters, and
 * redirecting all responses with error codes to the failure
 * callback method (as it used to be in the implementation in
 * Retrofit 1).
 *
 * @param <T> The successful response body type.
 */
public abstract class Callback<T> implements retrofit2.Callback<T> {
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
     * The original callback method invoked by Retrofit upon receiving an HTTP response. This method
     * definition provides extra information that's not needed by most individual callback
     * implementations, and is also invoked when HTTP error status codes are encountered (forcing
     * the implementation to manually check for success in each case). Therefore this implementation
     * only delegates to {@link #onResponse(Object)} in the case where it receives a successful HTTP
     * status code, and to {@link #onFailure(Call, Throwable)} otherwise, passing an instance of
     * {@link HttpStatusException} with the relevant error status code. This method is declared as
     * final, as subclasses are meant to be implementing the abstract {@link #onResponse(Object)}
     * method instead of this one.
     *
     * @param call The Call object that was used to enqueue the request.
     * @param response The HTTP response data.
     */
    @Override
    public final void onResponse(@NonNull final Call<T> call, @NonNull final Response<T> response) {
        if (response.isSuccessful()) {
            onResponse(response.body());
        } else {
            onFailure(call, new HttpStatusException(response));
        }
    }

    /**
     * The original callback method invoked by Retrofit upon failure to receive an HTTP response,
     * whether due to encountering a network error while waiting for the response, or some other
     * unexpected error while constructing the request or processing the response. It's also invoked
     * by the {@link #onResponse(Call, Response)} implementation when it receives an HTTP error
     * status code. However, this method definition provides extra information that's not needed by
     * most individual callback implementations, so this implementation only delegates to
     * {@link #onFailure(Throwable)}. This method is declared as final, as subclasses are meant to
     * be implementing the abstract {@link #onFailure(Throwable)} method instead of this one.
     *
     * @param call The Call object that was used to enqueue the request.
     * @param error An {@link IOException} if the request failed due to a network failure, an
     *              {HttpResponseStatusException} if the failure was due to receiving an error code,
     *              or any {@link Throwable} implementation if one was thrown unexpectedly while
     *              creating the request or processing the response.
     */
    @Override
    public final void onFailure(@NonNull final Call<T> call, @NonNull final Throwable error) {
        onFailure(error);
    }

    /**
     * A dummy implementation of Retrofit's {@link retrofit2.Callback} interface, to be used in one-
     * off calls, where the caller doesn't care about the result of the enqueued {@link Call}.
     */
    public static final retrofit2.Callback DUMMY_CALLBACK = new retrofit2.Callback() {
        @Override
        public void onResponse(@NonNull final Call call, @NonNull final Response response) {}
        @Override
        public void onFailure(@NonNull final Call call, @NonNull final Throwable t) {}
    };
}
