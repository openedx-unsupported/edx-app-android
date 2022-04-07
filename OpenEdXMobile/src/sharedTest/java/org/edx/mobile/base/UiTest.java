package org.edx.mobile.base;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.edx.mobile.base.http.HttpBaseTestCase;
import org.edx.mobile.core.IEdxEnvironment;
import org.junit.Before;
import org.junit.Ignore;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowNetworkInfo;

/**
 * Base class for all UI test suites.
 */
@Ignore
public class UiTest extends HttpBaseTestCase {
    protected IEdxEnvironment environment;

    @Override
    protected void inject(Injector injector) throws Exception {
        super.inject(injector);
        environment = injector.getInstance(IEdxEnvironment.class);
    }

    // Ensure login before tests.
    @Before
    @Override
    public void login() throws Exception {
        super.login();
    }

    // Ensure that the state is set to report network connectivity during tests, so that the
    // UI is set up in the default state without the offline indicators. Any actual downloads
    // will be performed over the local mock web server,
    @Before
    public void connectToNetwork() {
        ShadowConnectivityManager shadowConnectivityManager = Shadows.shadowOf(
                (ConnectivityManager) RuntimeEnvironment.application
                        .getSystemService(Context.CONNECTIVITY_SERVICE));
        shadowConnectivityManager.setNetworkInfo(ConnectivityManager.TYPE_MOBILE,
                ShadowNetworkInfo.newInstance(NetworkInfo.DetailedState.CONNECTED,
                        ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_MOBILE_MMS,
                        true, false));
        // Set the default network to WiFi, as downloads are always allowed over
        // WiFi regardless of the configuration settings.
        shadowConnectivityManager.setActiveNetworkInfo(
                ShadowNetworkInfo.newInstance(NetworkInfo.DetailedState.DISCONNECTED,
                        ConnectivityManager.TYPE_WIFI, 0, true, true));
    }

    public void disconnectFromNetwork() {
        ShadowConnectivityManager shadowConnectivityManager = Shadows.shadowOf(
                (ConnectivityManager) RuntimeEnvironment.application
                        .getSystemService(Context.CONNECTIVITY_SERVICE));
        shadowConnectivityManager.setActiveNetworkInfo(null);
    }
}
