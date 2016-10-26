package org.edx.mobile.http.interceptor;

import org.edx.mobile.logger.Logger;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * TODO used for network control
 */
public class ThrottlingInterceptor implements Interceptor {
    protected final Logger logger = new Logger(getClass().getName());

    private long lastRequest = 0L;
    private final long maxRequestSpeed = 1000;
    private final Lock requestLock = new ReentrantLock();

    @Override
    public Response intercept(Chain chain) throws IOException {
        requestLock.lock();

        try {
            long curTime = System.currentTimeMillis();
            long diff = curTime - lastRequest;

            if (diff < maxRequestSpeed)
                try {
                    Thread.sleep(maxRequestSpeed - diff);
                }
                catch (InterruptedException e) {
                    logger.error(e);
                }

            lastRequest = System.currentTimeMillis();
        }
        finally {
            requestLock.unlock();
        }

        return chain.proceed(chain.request());
    }
}
