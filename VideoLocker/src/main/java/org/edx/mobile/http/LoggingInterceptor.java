package org.edx.mobile.http;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.edx.mobile.logger.Logger;

import java.io.IOException;

/**
 * AOP style logging.. i like it:)
 */
public class LoggingInterceptor implements Interceptor {
    protected final Logger logger = new Logger(getClass().getName());

    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        logger.debug(String.format("Sending request %s on %s%n%s",
            request.url(), chain.connection(), request.headers()));

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        logger.debug(String.format("Received response for %s in %.1fms%n%s",
            response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        return response;
    }
}
