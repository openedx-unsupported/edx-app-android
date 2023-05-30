package org.edx.mobile.util.images;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.util.ResourceUtil;

import java.util.HashMap;
import java.util.List;

public enum ShareUtils {
    ;

    public static Intent newShareIntent(@NonNull String text) {
        return new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, text)
                .setType("text/plain");
    }

    /**
     * Display a popup having a list of shareable apps through which a user can share course info.
     *
     * @param activity          Calling activity, pop up style will be based on this activity's style.
     * @param anchor            Anchor view around which popup will be shown.
     * @param courseData        Data of course which needs to be shared.
     * @param analyticsRegistry Analytics registry to fire required analytics events.
     * @param environment       Relevant configuration environment.
     */
    public static void showCourseShareMenu(@NonNull final Activity activity,
                                           @NonNull final View anchor,
                                           @NonNull final EnrolledCoursesResponse courseData,
                                           @NonNull final AnalyticsRegistry analyticsRegistry,
                                           @NonNull final IEdxEnvironment environment) {
        final String COURSE_ABOUT_URL = courseData.getCourse().getCourse_about();
        final String shareTextWithPlatformName = ResourceUtil.getFormattedString(
                activity.getResources(),
                R.string.share_course_message,
                "platform_name",
                activity.getString(R.string.platform_name)).toString() + "\n" + COURSE_ABOUT_URL;
        ShareUtils.showShareMenu(
                activity, ShareUtils.newShareIntent(shareTextWithPlatformName), anchor,
                new ShareUtils.ShareMenuItemListener() {
                    @Override
                    public void onMenuItemClick(@NonNull ComponentName componentName, @NonNull ShareUtils.ShareType shareType) {
                        final String shareText;
                        if (shareType == ShareUtils.ShareType.UNKNOWN) {
                            shareText = shareTextWithPlatformName;
                        } else {
                            shareText = getSharingText(shareType);
                        }
                        analyticsRegistry.courseDetailShared(courseData.getCourse().getId(), COURSE_ABOUT_URL, shareType);
                        final Intent intent = ShareUtils.newShareIntent(shareText);
                        intent.setComponent(componentName);
                        activity.startActivity(intent);
                    }

                    @NonNull
                    private String getSharingText(@NonNull ShareUtils.ShareType shareType) {
                        String courseUrl = COURSE_ABOUT_URL;
                        if (!TextUtils.isEmpty(shareType.getUtmParamKey())) {
                            final String utmParams = courseData.getCourse().getCourseSharingUtmParams(shareType.getUtmParamKey());
                            if (!TextUtils.isEmpty(utmParams)) {
                                courseUrl += "?" + utmParams;
                            }
                        }
                        final String platform;
                        final String twitterTag = environment.getConfig().getTwitterConfig().getHashTag();
                        if (shareType == ShareUtils.ShareType.TWITTER && !TextUtils.isEmpty(twitterTag)) {
                            platform = twitterTag;
                        } else {
                            platform = activity.getString(R.string.platform_name);
                        }
                        return ResourceUtil.getFormattedString(
                                activity.getResources(), R.string.share_course_message, "platform_name", platform).toString() +
                                "\n" + courseUrl;
                    }
                });
    }

    /**
     * Display a popup having a list of shareable apps that resolves the given sharing intent.
     *
     * @param activity    Calling activity, pop up style will be based on this activity's style.
     * @param shareIntent Sharing intent
     * @param anchor      Anchor view around which popup will be shown.
     * @param listener    Listener to take a callback when user select a particular app from
     *                    displayed popup.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressWarnings("RestrictedApi")
    public static void showShareMenu(@NonNull Activity activity, @NonNull Intent shareIntent,
                                     @NonNull View anchor, final @NonNull ShareMenuItemListener listener) {
        final PopupMenu popupMenu = new PopupMenu(activity, anchor);
        final PackageManager packageManager = activity.getPackageManager();
        final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(shareIntent, 0);
        //TODO: Temporary removed WPS Office Application because security risks, we'll check if the issue is still persist or not after 4.0.2 release
        resolveInfoList.removeIf(resolveInfo -> resolveInfo.activityInfo.processName.equalsIgnoreCase("cn.wps.moffice_eng"));
        for (final ResolveInfo resolveInfo : resolveInfoList) {
            final MenuItem shareItem = popupMenu.getMenu().add(resolveInfo.loadLabel(packageManager));
            shareItem.setIcon(resolveInfo.loadIcon(packageManager));
            shareItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final ComponentName componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                    listener.onMenuItemClick(componentName, getShareTypeFromComponentName(componentName));
                    return false;
                }
            });
        }

        // TODO: Find an alternative to following usage of support MenuPopupHelper which is hidden in the support package
        // As PopupMenu doesn't support to show icons in main menu, use MenuPopupHelper for it
        final MenuPopupHelper menuHelper = new MenuPopupHelper(activity, (MenuBuilder) popupMenu.getMenu(), anchor);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
    }

    public static void showCelebrationShareMenu(Activity activity, View anchor,
                                                EnrolledCoursesResponse course,
                                                ShareTypeSelectedListener shareTypeSelectedListener) {
        final String shareTextWithPlatformName = ResourceUtil.getFormattedString(
                activity.getResources(),
                R.string.celebration_share_message, new HashMap<String, CharSequence>() {{
                    put("course_name", course.getCourse().getName());
                    put("platform_name", activity.getString(R.string.platform_name));
                }}).toString() + "\n\n" + course.getCourse().getCourse_about();
        ShareUtils.showShareMenu(activity, ShareUtils.newShareIntent(shareTextWithPlatformName), anchor, (componentName, shareType) -> {
            final String shareText = shareTextWithPlatformName +
                    (shareType != ShareType.UNKNOWN ? "?utm_campaign=" +
                            activity.getString(R.string.platform_name).toLowerCase() +
                            "milestone&utm_medium=social&utm_source=" + shareType.utmParamKey : "");
            final Intent intent = ShareUtils.newShareIntent(shareText);
            intent.setComponent(componentName);
            activity.startActivity(intent);
            if (shareTypeSelectedListener != null) {
                shareTypeSelectedListener.onShareTypeSelected(shareType);
            }
        });

    }

    public interface ShareTypeSelectedListener {
        void onShareTypeSelected(ShareType shareType);
    }

    public interface ShareMenuItemListener {
        void onMenuItemClick(@NonNull ComponentName componentName, @NonNull ShareType shareType);
    }

    public enum ShareType {
        TWITTER("twitter"),
        FACEBOOK("facebook"),
        LINKEDIN("linkedin"),
        REDDIT("reddit"),
        WHATSAPP("whatsapp"),
        UNKNOWN(null);

        private String utmParamKey;

        ShareType(@Nullable String key) {
            utmParamKey = key;
        }

        @Nullable
        public String getUtmParamKey() {
            return utmParamKey;
        }
    }

    @NonNull
    private static ShareType getShareTypeFromComponentName(@NonNull ComponentName componentName) {
        switch (componentName.getPackageName()) {
            case "com.facebook.katana":
            case "com.facebook.lite":
                return ShareType.FACEBOOK;
            case "com.twitter.android":
                return ShareType.TWITTER;
            case "com.linkedin.android":
                return ShareType.LINKEDIN;
            case "com.reddit.frontpage":
                return ShareType.REDDIT;
            case "com.whatsapp":
            case "com.whatsapp.w4b":
                return ShareType.WHATSAPP;
            default:
                return ShareType.UNKNOWN;
        }
    }
}
