package org.edx.mobile.services;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.DownloadDescriptor;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.NetworkUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import roboguice.service.RoboService;

/**
 * Created by marcashman on 2014-12-01.
 */
public class DownloadSpeedService extends RoboService {

    private static final String TAG = DownloadSpeedService.class.getCanonicalName();
    private static final long NS_PER_SEC = 1000000000;
    private static final int BLOCK_SIZE = 4096;

    public static final String EXTRA_FILE_DESC = TAG + ".file_desc";
    public static final String EXTRA_REPORT_PROGRESS = TAG + ".report_progress";
    public static final String EXTRA_KBPS = TAG + ".kbps";
    public static final String EXTRA_SECONDS = TAG + ".seconds";
    public static final String EXTRA_ERROR = TAG + ".error";
    public static final String ACTION_DOWNLOAD_DONE = TAG + ".download_done";

    private static final int RUN_SPEED_TEST_MESSAGE = 5555;

    private int DELAY_IN_MILLISECONDS = 5000;

    private static final Logger logger = new Logger(DownloadSpeedService.class);

    @Inject
    private OkHttpClientProvider okHttpClientProvider;

    @Inject
    private AnalyticsRegistry analyticsRegistry;

    SpeedTestHandler messageHandler;

    Timer timer = null;
    TimerTask timerTask = null;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null) {
            Message msg = messageHandler.obtainMessage();

            DownloadDescriptor descriptor = intent.getParcelableExtra(EXTRA_FILE_DESC);
            if (descriptor != null) {
                msg.obj = descriptor;
                msg.what = RUN_SPEED_TEST_MESSAGE;
                messageHandler.sendMessage(msg);
            } else {
                logger.warn("missing file description");
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        startThread();

        DELAY_IN_MILLISECONDS = getResources().getInteger(R.integer.delay_speed_test_in_milliseconds);

        super.onCreate();
    }

    private void startThread(){
        HandlerThread thread = new HandlerThread("SpeedTestThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Looper serviceLooper = thread.getLooper();
        messageHandler = new SpeedTestHandler(serviceLooper);
    }

    private synchronized void performDownload(DownloadDescriptor file) {

        final long startTime;

        try {
            startTime = System.nanoTime();

            OkHttpClient client = okHttpClientProvider.getNonOAuthBased().newBuilder()
                    .connectTimeout(getResources().getInteger(
                            R.integer.speed_test_timeout_in_milliseconds), TimeUnit.MILLISECONDS)
                    .build();

            Request request = new Request.Builder()
                .url(file.getUrl())
                .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException throwable) {
                    logger.error(throwable);
                    //If it times out, set a low value for download speed
                    setCurrentDownloadSpeed(0.01f);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        logger.debug("Download Speed Test Failed");
                    } else {
                        long length = response.body().string().length();
                        double seconds = (System.nanoTime() - startTime) / NS_PER_SEC;
                        if( seconds != 0 ) {
                            final float downloadSpeedKps = (float) ((length / seconds) / 1024);
                            setCurrentDownloadSpeed(downloadSpeedKps);
                            reportDownloadSpeed(downloadSpeedKps);
                        }
                    }
                }
            });
        }catch (Exception ex){
            logger.error(ex);
        }

    }

    private void reportDownloadSpeed(float downloadSpeedKps){
        try{

            if (NetworkUtil.isConnectedWifi(DownloadSpeedService.this)) {
                analyticsRegistry.trackUserConnectionSpeed(Analytics.Values.WIFI,   downloadSpeedKps);
            } else if (NetworkUtil.isConnectedMobile(DownloadSpeedService.this)) {
                analyticsRegistry.trackUserConnectionSpeed(Analytics.Values.CELL_DATA,   downloadSpeedKps);
            }

        }catch(Exception e){
            logger.error(e);
        }
    }

    private void setCurrentDownloadSpeed(float downloadSpeedKps){
        PrefManager manager = new PrefManager(this, PrefManager.Pref.WIFI);
        manager.put(PrefManager.Key.SPEED_TEST_KBPS, downloadSpeedKps);
    }

    public class SpeedTestHandler extends Handler {
        public SpeedTestHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int messageType = msg.what;

            if(messageType == RUN_SPEED_TEST_MESSAGE){
                final DownloadDescriptor file = (DownloadDescriptor) msg.obj;
                if(file != null){
                    scheduleNewDownload(file);
                }
            }
        }
    }

    private void scheduleNewDownload(final DownloadDescriptor file) {
        if(timerTask != null) {
            timerTask.cancel();
            timer.cancel();
            timerTask = null;
            timer = null;
        }

        if(file.shouldForceDownload()) {
            performDownload(file);
        } else {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    performDownload(file);
                }
            };

            timer = new Timer();

            timer.schedule(timerTask, DELAY_IN_MILLISECONDS);
        }
    }

}
