package org.edx.mobile.util.images;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.view.custom.popup.SizedDrawable;
import org.edx.mobile.view.custom.popup.menu.PopupMenu;

import java.util.List;

public enum ShareUtils {
    ;

    public static Intent newShareIntent(@NonNull String text) {
        return new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, text)
                .setType("text/plain");
    }

    public static void showShareMenu(@NonNull Intent shareIntent, @NonNull View anchor, final @NonNull ShareMenuItemListener listener, @StringRes int menu_title) {
        final Context context = anchor.getContext();
        final PopupMenu popupMenu = new PopupMenu(context, anchor);
        final SubMenu subMenu = popupMenu.getMenu().addSubMenu(menu_title);
        final PackageManager packageManager = context.getPackageManager();
        final int iconSize = context.getResources().getDimensionPixelSize(R.dimen.popupMenuIconDefaultSize);
        final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(shareIntent, 0);
        for (final ResolveInfo resolveInfo : resolveInfoList) {
            final MenuItem shareItem = subMenu.add(resolveInfo.loadLabel(packageManager));
            shareItem.setIcon(new SizedDrawable(resolveInfo.loadIcon(packageManager), iconSize, iconSize));
            shareItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    listener.onMenuItemClick(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                    return false;
                }
            });
        }
        popupMenu.show();
    }

    public interface ShareMenuItemListener {
        void onMenuItemClick(@NonNull ComponentName componentName);
    }
}
