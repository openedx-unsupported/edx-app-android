package org.edx.mobile.http.serialization;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import org.edx.mobile.http.HttpStatusException;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import roboguice.RoboGuice;

/**
 * Abstract implementation of OkHttp's
 * {@link Callback} interface, that takes care of
 * converting the response body to an object of the
 * specified model class, and provides (and delegates to)
 * a simpler interface for subclasses, stripping out
 * unnecessary parameters, and redirecting all responses
 * with error codes to the failure callback method. The
 * callbacks are guaranteed to be invoked on the main
 * thread.
 */
public abstract class OkCallback<T> implements Callback {
    /**
     * A Handler for the main looper, for delivering messages on the main thread.
     */
    private static final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * The response body type.
     */
    @NonNull
    private final Type responseBodyType;

    /**
     * The Gson instance for converting the response body to the desired type.
     */
    @Inject
    private Gson gson;

    /**
     * Create a new instance of this class.
     *
     * @param context A Context to use for injecting the Gson instance.
     * @param responseBodyClass The response body class.
     */
    protected OkCallback(@NonNull final Context context,
                         @NonNull final Class<T> responseBodyClass) {
        RoboGuice.getInjector(context).injectMembers(this);
        responseBodyType = responseBodyClass;
    }

    /**
     * Create a new instance of this class.
     *
     * @param context A Context to use for injecting the Gson instance.
     * @param responseBodyTypeToken The response body type token.
     */
    protected OkCallback(@NonNull final Context context,
                         @NonNull final TypeToken<T> responseBodyTypeToken) {
        RoboGuice.getInjector(context).injectMembers(this);
        responseBodyType = responseBodyTypeToken.getType();
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
     * The original callback method invoked by OkHttp upon receiving an HTTP response. This method
     * definition provides extra information that's not needed by most individual callback
     * implementations, and is also invoked when HTTP error status codes are encountered (forcing
     * the implementation to manually check for success in each case). Therefore this implementation
     * only delegates to {@link #onResponse(T)} in the case where it receives a successful HTTP
     * status code, and to {@link #onFailure(Throwable)} otherwise, passing an instance of
     * {@link HttpStatusException} with the relevant error status code. This method is declared as
     * final, as subclasses are meant to be implementing the abstract {@link #onResponse(T)} method
     * instead of this one.
     *
     * @param call The Call object that was used to enqueue the request.
     * @param response The HTTP response data.
     */
    @Override
    public final void onResponse(@NonNull final Call call, @NonNull final Response response) {
        if (response.isSuccessful()) {
            final String responseBodyString;
            try {
                responseBodyString = response.body().string();
            } catch (IOException error) {
                onFailure(error);
                return;
            }
            final T responseBody = gson.fromJson(responseBodyString, responseBodyType);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onResponse(responseBody);
                }
            });
        } else {
            onFailure(new HttpStatusException(response));
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
    public final void onFailure(@NonNull final Call call, @NonNull final IOException error) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onFailure(error);
            }
        });
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
