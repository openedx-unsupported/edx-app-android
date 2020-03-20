package org.edx.mobile.module.prefs;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.view.BulkDownloadFragment;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VideoPrefs {
    @NonNull
    private final PrefManager pref;

    @Inject
    public VideoPrefs(@NonNull Context context) {
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
