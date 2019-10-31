package org.humana.mobile.receivers;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.humana.mobile.R;
import org.humana.mobile.core.IEdxEnvironment;
import org.humana.mobile.event.NetworkConnectivityChangeEvent;
import org.humana.mobile.logger.Logger;
import org.humana.mobile.model.DownloadDescriptor;
import org.humana.mobile.services.DownloadSpeedService;

import de.greenrobot.event.EventBus;
import roboguice.receiver.RoboBroadcastReceiver;

/**
 * Created by yervant on 1/15/15.
 */
@Singleton
public class NetworkConnectivityReceiver extends RoboBroadcastReceiver {

    private static final Logger logger = new Logger(NetworkConnectivityReceiver.class);
    private static boolean isFirstStart = false;

    @Inject
    IEdxEnvironment environment;

    @Override
    public void handleReceive(Context context, Intent intent) {
        // speed-test is moved behind a flag in the configuration
        if(environment.getConfig().isSpeedTestEnabled()) {
            ConnectivityManager cm =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isAvailable();

            if (isConnected) {
                logger.debug("Have reconnected, testing download speed.");
                //start an instance of the download speed service so it can run in the background
                Intent speedTestIntent = new Intent(context, DownloadSpeedService.class);
                String downloadEndpoint = context.getString(R.string.speed_test_url);
                speedTestIntent.putExtra(DownloadSpeedService.EXTRA_FILE_DESC,
                        new DownloadDescriptor(downloadEndpoint, !isFirstStart));
                context.startService(speedTestIntent);
                isFirstStart = true;
            }
        }

        NetworkConnectivityChangeEvent event = new NetworkConnectivityChangeEvent();
        EventBus.getDefault().postSticky(event);

    }
}
