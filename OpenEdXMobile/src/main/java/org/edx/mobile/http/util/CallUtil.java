package org.edx.mobile.http.util;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import org.edx.mobile.http.HttpStatusException;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Utility class for dealing with the Retrofit {@link Call} APIs.
 */
public class CallUtil {
    // Make this class non-instantiable
    private CallUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * The Gson instance for converting the response body to the desired type.
     */
    @Inject
    private static Gson gson;

    /**
     * Synchronously send the request and return its response body, converted to an object of the
     * assosiated model class, and throwing an exception if the server returns an HTTP error code as
     * the status.
     *
     * @param call The HTTP call to execute.
     * @param <T> The response body type.
     *
     * @return The converted response body.
     *
     * @throws IOException if a problem occurred talking to the server.
     * @throws HttpStatusException if the server returns an error status.
     * @throws RuntimeException (and subclasses) if an unexpected error occurs creating the request
     * or decoding the response.
     */
    @NonNull
    public static <T> T executeStrict(@NonNull final Call<T> call)
            throws IOException, HttpStatusException {
        final Response<T> response = call.execute();
        if (!response.isSuccessful()) {
            throw new HttpStatusException(response);
        }
        return response.body();
    }

    /**
     * Synchronously send the request and return its response body, converted to an object of the
     * specified model class, and throwing an exception if the server returns an HTTP error code as
     * the status.
     *
     * @param responseBodyClass The response body class.
     * @param call The HTTP call to execute.
     * @param <T> The response body type.
     *
     * @return The converted response body.
     *
     * @throws IOException if a problem occurred talking to the server.
     * @throws HttpStatusException if the server returns an error status.
     * @throws RuntimeException (and subclasses) if an unexpected error occurs creating the request
     * or decoding the response.
     */
    @NonNull
    public static <T> T executeStrict(@NonNull final Class<T> responseBodyClass,
                                      @NonNull final okhttp3.Call call)
            throws IOException, HttpStatusException {
        final okhttp3.Response response = call.execute();
        if (!response.isSuccessful()) {
            throw new HttpStatusException(response);
        }
        return gson.fromJson(response.body().string(), responseBodyClass);
    }

    /**
     * Synchronously send the request and return its response body, converted to an object of the
     * specified model class, and throwing an exception if the server returns an HTTP error code as
     * the status.
     *
     * @param responseBodyTyoe The response body type.
     * @param call The HTTP call to execute.
     * @param <T> The response body type.
     *
     * @return The converted response body.
     *
     * @throws IOException if a problem occurred talking to the server.
     * @throws HttpStatusException if the server returns an error status.
     * @throws RuntimeException (and subclasses) if an unexpected error occurs creating the request
     * or decoding the response.
     */
    @NonNull
    public static <T> T executeStrict(@NonNull final TypeToken<T> responseBodyTyoe,
                                      @NonNull final okhttp3.Call call)
            throws IOException, HttpStatusException {
        final okhttp3.Response response = call.execute();
        if (!response.isSuccessful()) {
            throw new HttpStatusException(response);
        }
        return gson.fromJson(response.body().string(), responseBodyTyoe.getType());
    }
}
