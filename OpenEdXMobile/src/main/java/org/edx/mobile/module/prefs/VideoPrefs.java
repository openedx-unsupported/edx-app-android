package org.edx.mobile.module.prefs;

import android.content.Context;

import androidx.annotation.NonNull;

import org.edx.mobile.view.BulkDownloadFragment;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class VideoPrefs {

    @NonNull
    private final PrefManager pref;

    @Inject
    public VideoPrefs(@ApplicationContext @NonNull Context context) {
        pref = new PrefManager(context, PrefManager.Pref.VIDEOS);
    }


    public BulkDownloadFragment.SwitchState getBulkDownloadSwitchState(@NonNull String courseId) {
        final int ordinal = pref.getInt(String.format(PrefManager.Key.BULK_DOWNLOAD_FOR_COURSE_ID, courseId));
        return (ordinal == -1 ?
                BulkDownloadFragment.SwitchState.DEFAULT :
                BulkDownloadFragment.SwitchState.values()[ordinal]);
    }

    public void setBulkDownloadSwitchState(@NonNull BulkDownloadFragment.SwitchState state,
                                           @NonNull String courseId) {
        pref.put(String.format(PrefManager.Key.BULK_DOWNLOAD_FOR_COURSE_ID, courseId), state.ordinal());
    }
}
