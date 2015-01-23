package org.edx.mobile.instrumentation;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import org.edx.mobile.logger.OEXLogger;

// adb shell am broadcast -a com.example.pkg.END_EMMA
@SuppressLint("SdCardPath")
public class EndEmmaBroadcast extends BroadcastReceiver {
    protected final OEXLogger logger = new OEXLogger(getClass().getName());

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.debug("EndEmmaBroadcast broadcast received!");
        // reflection is used so emma doesn't cause problems for other build targets
        // that do not include emma.
        try {
            File ecFile = new File("/mnt/sdcard/coverage.ec");
            Class.forName("com.vladium.emma.rt.RT")
                    .getMethod("dumpCoverageData", File.class, boolean.class, boolean.class)
                    .invoke(null, ecFile, false, false);
        } catch (Exception e) {
            logger.error(e);
        }

        // once coverage is dumped, the processes is ended.
        Process.killProcess(Process.myPid());
    }
}