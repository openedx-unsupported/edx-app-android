package org.edx.mobile.http.util;

import android.os.Build;

import org.edx.mobile.logger.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

/**
 * Enables TLS v1.2 when creating SSLSockets.
 * <p/>
 * For some reason, android supports TLS v1.2 from API 16, but enables it by
 * default only from API 20.
 * <p/>
 * Note: This class has been copied from OkHttp's official
 * <a href="https://github.com/square/okhttp/issues/2372#issuecomment-244807676">GitHub Issues page.</a>
 * <br/>
 *
 * @link https://developer.android.com/reference/javax/net/ssl/SSLSocket.html
 * @see SSLSocketFactory
 */
public class Tls12SocketFactory extends SSLSocketFactory {
    private static final Logger logger = new Logger(Tls12SocketFactory.class);
    private static final String[] TLS_V12_ONLY = {"TLSv1.2"};

    private final SSLSocketFactory delegate;

    public Tls12SocketFactory(SSLSocketFactory base) {
        this.delegate = base;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return patch(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return patch(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return patch(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return patch(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return patch(delegate.createSocket(address, port, localAddress, localPort));
    }

    private Socket patch(Socket s) {
        if (s instanceof SSLSocket) {
            ((SSLSocket) s).setEnabledProtocols(TLS_V12_ONLY);
        }
        return s;
    }

    /**
     * Enables TLS v1.2 protocol on Pre-Lollipop devices for server sockets.
     *
     * @param client The builder object of OkHttpClient.
     * @return Updated builder object of OkHttpClient with TLS v1.2 enabled on it.
     */
    public static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                /*
                OkHttpClient#sslSocketFactory(SSLSocketFactory) has been deprecated in favor of
                OkHttpClient#sslSocketFactory(SSLSocketFactory, X509TrustManager) which requires an
                X509TrustManager object.
                Ref: https://github.com/square/okhttp/issues/2372#issuecomment-277596841

                Inspiration for the fix and update to code:
                1 - https://github.com/square/okhttp/issues/2372#issuecomment-277677604
                2 - https://square.github.io/okhttp/3.x/okhttp/okhttp3/OkHttpClient.Builder.html#sslSocketFactory-javax.net.ssl.SSLSocketFactory-javax.net.ssl.X509TrustManager-
                 */
                final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);
                final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new IllegalStateException("Unexpected default trust managers:"
                            + Arrays.toString(trustManagers));
                }
                final X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{trustManager}, null);
                client.sslSocketFactory(new Tls12SocketFactory(sslContext.getSocketFactory()), trustManager);

                final ConnectionSpec connectionSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                final List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(connectionSpec);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                logger.error(e);
            }
        }

        return client;
    }
}
