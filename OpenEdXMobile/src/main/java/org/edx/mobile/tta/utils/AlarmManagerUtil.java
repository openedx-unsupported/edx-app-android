package org.edx.mobile.tta.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * This class is used to set the alarm
 *
 * @author anil
 */
public class AlarmManagerUtil {

    private static final String TAG = AlarmManagerUtil.class.getSimpleName();

    private static final int _1_SEC = 1000;
    private static final int _1_HOUR = 1000 * 60 * 60;

    private Context mContext;

    public AlarmManagerUtil(Context context) {
        mContext = context;
    }

    /**
     * get alarm manager instance
     *
     * @return
     */
    private AlarmManager getAlarmManager() {
        return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Check whether the alarm is set or not.
     *
     * @return
     */
    public boolean checkAlarmExist(Intent intent, int requestCode) {
        boolean isTrue = false;

        PendingIntent pendingIntent = PendingIntent.getService(mContext, requestCode, intent, PendingIntent.FLAG_NO_CREATE);

        if (pendingIntent != null) {
            isTrue = true;
        }

        Log.d(TAG, "Check alarm exist: " + isTrue);

        return isTrue;
    }

    /**
     * get alarm service pending intent
     *
     * @param intent
     * @param requestCode
     * @return
     */
    private PendingIntent getAlarmServiceIntent(Intent intent, int requestCode) {
        PendingIntent pendingIntent = PendingIntent.getService(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    /**
     * start alarm service
     *
     * @param intent
     * @param requestCode
     * @param triggerAtMillis
     */
    public void scheduleAlarm(Intent intent, int requestCode, long triggerAtMillis) {
        AlarmManager alarmManager = getAlarmManager();
        if (alarmManager != null) {
            Log.d(TAG, "Start AlarmManager");

            PendingIntent pendingIntent = getAlarmServiceIntent(intent, requestCode);

            if (Build.VERSION.SDK_INT < 23) {
                if (Build.VERSION.SDK_INT >= 19) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        }
    }

    public void scheduleRepeatingAlarm(Intent intent, int requestCode, long triggerAtMillis, long intervalInMillis){
        AlarmManager alarmManager = getAlarmManager();
        if (alarmManager != null) {
            Log.d(TAG, "Start AlarmManager");

            PendingIntent pendingIntent = getAlarmServiceIntent(intent, requestCode);

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalInMillis, pendingIntent);
        }
    }

    /**
     * cancel alarm manager..
     */
    public void cancelAlarm(Intent intent, int requestCode) {
        AlarmManager alarmManager = getAlarmManager();

        if (alarmManager != null) {
            alarmManager.cancel(getAlarmServiceIntent(intent, requestCode));
        }

        alarmManager = null;
    }
}
