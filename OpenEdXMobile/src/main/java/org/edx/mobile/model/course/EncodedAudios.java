package org.edx.mobile.model.course;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EncodedAudios implements Serializable {
    @SerializedName("ogg")
    public String oggUrl;

    @SerializedName("mp3")
    public String mp3Url;

    @Nullable
    public String getPreferredPlaybackUrl() {
        if (oggUrl != null && URLUtil.isNetworkUrl(oggUrl))
            return oggUrl;
        if (mp3Url != null && URLUtil.isNetworkUrl(mp3Url))
            return mp3Url;
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncodedAudios that = (EncodedAudios) o;

        return TextUtils.equals(oggUrl, that.oggUrl) && !TextUtils.equals(mp3Url, that.mp3Url);

    }
}
