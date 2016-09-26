package org.edx.mobile.test.util;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;


/**
 * Created by cleeedx on 4/12/16.
 */


public final class OkHttpTestUtil {
    private OkHttpTestUtil() {
    }

    private static final ConnectionPool connectionPool = new ConnectionPool();

    /**
     * Returns an OkHttpClient for all tests to use as a starting point.
     * <p/>
     * <p>The shared instance allows all tests to share a single connection pool, which prevents idle
     * connections from consuming unnecessary resources while connections wait to be evicted.
     * <p/>
     * <p>This client is also configured to be slightly more deterministic, returning a single IP
     * address for all hosts, regardless of the actual number of IP addresses reported by DNS.
     */
    public static OkHttpClient defaultClient() {
        return new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .build();
    }
}
