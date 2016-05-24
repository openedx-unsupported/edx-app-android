package org.edx.mobile.view.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.util.AppStoreUtils;
import org.edx.mobile.util.ResourceUtil;

/**
 * The type of the banner message.
 */
public enum BannerType {
    /**
     * Offline mode.
     */
    OFFLINE(
        R.string.banner_offline_header, R.string.banner_offline_content,
            FontAwesomeIcons.fa_exclamation, null
    ),
    /**
     * Unsupported app version.
     */
    VERSION_UNSUPPORTED(
            R.string.banner_unsupported_header, R.string.banner_unsupported_content,
            FontAwesomeIcons.fa_wifi, new View.OnClickListener() {
        @Override
        public void onClick(@NonNull View v) {
            AppStoreUtils.openAppInAppStore(v.getContext());
        }
    }) {
        // Override the long message getter to resolve the platform name parameter
        @Override
        @NonNull
        public CharSequence getLongMessageRes(@NonNull Context context) {
            return ResourceUtil.getFormattedString(context.getResources(),
                    R.string.banner_unsupported_content,
                    "platform_name", context.getText(R.string.platform_name));
        }
    };

    @StringRes
    private final int shortMessageRes;
    @StringRes
    private final int longMessageRes;
    @NonNull
    private final Icon icon;
    @Nullable
    private final View.OnClickListener clickListener;

    /**
     * Display a banner with the specified message strings, icon, and click
     * listener.
     *
     * @param shortMessageRes The short message string
     * @param longMessageRes The long message string
     * @param icon The icon
     * @param clickListener The click listener
     */
    BannerType(@StringRes int shortMessageRes, @StringRes int longMessageRes,
               @NonNull Icon icon, @Nullable View.OnClickListener clickListener) {
        this.shortMessageRes = shortMessageRes;
        this.longMessageRes = longMessageRes;
        this.icon = icon;
        this.clickListener = clickListener;
    }

    /**
     * Resolve the short message string, and return it.
     *
     * @param context A Context to resolve the string
     * @return The short message string
     */
    @NonNull
    public CharSequence getShortMessageRes(@NonNull Context context) {
        return context.getText(shortMessageRes);
    }

    /**
     * Resolve the short message string, and return it.
     *
     * @param context A Context to resolve the string
     * @return The long message string
     */
    @NonNull
    public CharSequence getLongMessageRes(@NonNull Context context) {
        return context.getText(longMessageRes);
    }

    /**
     * @return The icon
     */
    @NonNull
    public final Icon getIcon() {
        return icon;
    }

    @Nullable
    public final View.OnClickListener getClickListener() {
        return clickListener;
    }
}
