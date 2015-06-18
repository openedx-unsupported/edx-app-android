package org.edx.mobile.http;

import org.edx.mobile.logger.Logger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import retrofit.RequestInterceptor;

/**
 * TODO used for network control
 */
public class ThrottlingInterceptor implements RequestInterceptor {
    protected final Logger logger = new Logger(getClass().getName());

    private long lastRequest = 0L;
    private final long maxRequestSpeed = 1000;
    private final Lock requestLock = new ReentrantLock();

    @Override
    public void intercept(RequestFacade request) {
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
    }
}