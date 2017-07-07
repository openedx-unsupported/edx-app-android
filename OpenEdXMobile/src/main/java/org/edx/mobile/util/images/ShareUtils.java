package org.edx.mobile.util.images;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

public enum ShareUtils {
    ;

    public static Intent newShareIntent(@NonNull String text) {
        return new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, text)
                .setType("text/plain");
    }

    @SuppressWarnings("RestrictedApi")
    public static void showShareMenu(@NonNull Intent shareIntent, @NonNull View anchor,
                                     final @NonNull ShareMenuItemListener listener) {
        final Context context = anchor.getContext();
        final PopupMenu popupMenu = new PopupMenu(context, anchor);
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(shareIntent, 0);
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
        final MenuPopupHelper menuHelper = new MenuPopupHelper(context, (MenuBuilder) popupMenu.getMenu(), anchor);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
    }

    public interface ShareMenuItemListener {
        void onMenuItemClick(@NonNull ComponentName componentName, @NonNull ShareType shareType);
    }

    public enum ShareType {
        TWITTER("twitter"),
        FACEBOOK("facebook"),
        UNKNOWN(null)
        ;

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
            default:
                return ShareType.UNKNOWN;
        }
    }
}
