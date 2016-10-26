package org.edx.mobile.http.util;

import org.edx.mobile.http.HttpResponseStatusException;

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
     * Synchronously send the request and return its response body, converted to it's associated
     * Java object model, and throwing an exception if the server returns an HTTP error code as the
     * status.
     *
     * @param call The HTTP call to execute.
     * @param <T> The call type.
     *
     * @return The converted response body.
     *
     * @throws IOException if a problem occurred talking to the server.
     * @throws HttpResponseStatusException if the server returns an error status.
     * @throws RuntimeException (and subclasses) if an unexpected error occurs creating the request
     * or decoding the response.
     */
    public static <T> T executeStrict(Call<T> call)
            throws IOException, HttpResponseStatusException {
        final Response<T> response = call.execute();
        if (!response.isSuccessful()) {
            throw new HttpResponseStatusException(response);
        }
        return response.body();
    }
}
