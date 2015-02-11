package org.edx.mobile.services;

import android.app.Service;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.DownloadDescriptor;
import org.edx.mobile.model.ProgressReport;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.NetworkUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by marcashman on 2014-12-01.
 */
public class DownloadSpeedService extends Service {

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

    private ISegment segIO;
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

        segIO = SegmentFactory.getInstance();

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

        final String url = file.getUrl();
        final byte[] buffer = new byte[BLOCK_SIZE];
        final long startTime;

        int timeoutMillis = getResources().getInteger(R.integer.speed_test_timeout_in_milliseconds);

        ArrayList<ProgressReport> progress = new ArrayList<ProgressReport>();

        try {
            HttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

            HttpGet get = new HttpGet(url);
            AndroidHttpClient.modifyRequestToAcceptGzipResponse(get);

            HttpConnectionParams.setConnectionTimeout(client.getParams(), timeoutMillis);

            startTime = System.nanoTime();
            HttpResponse response = client.execute(get);

            InputStream inputStream = AndroidHttpClient
                    .getUngzippedContent(response.getEntity());

            long received = 0;
            int read;

            //use the same buffer each time, we don't care what's in it
            while( (read = inputStream.read(buffer, 0, BLOCK_SIZE)) != -1 ) {

                //record and report progress
                received += read;
                progress.add(new ProgressReport(received, System.nanoTime() - startTime));

            }
        }catch (ConnectTimeoutException e){
            logger.error(e);

            //If it times out, set a low value for download speed
            setCurrentDownloadSpeed(0.01f);
            sendErrorBroadcast();
            return;

        } catch (IOException e) {
            logger.error(e);
            sendErrorBroadcast();
            return;

        }

        ProgressReport last = progress.get(progress.size() - 1);
        final double seconds = (double)last.getTime() / NS_PER_SEC;

        final float downloadSpeedKps = (float)((last.getDownloaded() / seconds) / 1024);

        setCurrentDownloadSpeed(downloadSpeedKps);

        logger.debug(String.format("+++Speed: %.1fKbps   Time: %.2fsec", downloadSpeedKps, seconds));

        Intent intent = new Intent();
        intent.setAction(ACTION_DOWNLOAD_DONE);
        intent.putExtra(EXTRA_SECONDS, seconds);
        intent.putExtra(EXTRA_KBPS, downloadSpeedKps);
        intent.putParcelableArrayListExtra(EXTRA_REPORT_PROGRESS, progress);

        try{

            if (NetworkUtil.isConnectedWifi(DownloadSpeedService.this)) {
                segIO.trackUserConnectionSpeed(ISegment.Values.WIFI, downloadSpeedKps);
            } else if (NetworkUtil.isConnectedMobile(DownloadSpeedService.this)) {
                segIO.trackUserConnectionSpeed(ISegment.Values.CELL_DATA, downloadSpeedKps);
            }

        }catch(Exception e){
            logger.error(e);
        }

        sendBroadcast(intent);
    }

    private void setCurrentDownloadSpeed(float downloadSpeedKps){
        PrefManager manager = new PrefManager(this, PrefManager.Pref.WIFI);
        manager.put(PrefManager.Key.SPEED_TEST_KBPS, downloadSpeedKps);
    }

    private void sendErrorBroadcast(){

        Intent intent = new Intent();
        intent.setAction(ACTION_DOWNLOAD_DONE);
        intent.putExtra(EXTRA_ERROR, true);
        sendBroadcast(intent);
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
