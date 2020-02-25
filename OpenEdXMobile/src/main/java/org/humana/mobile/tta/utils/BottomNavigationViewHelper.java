package org.humana.mobile.tta.utils;

import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.humana.mobile.R;

import java.lang.reflect.Field;

import static android.view.View.VISIBLE;

public class BottomNavigationViewHelper {
    private static TextView notificationBadge;
    private static View badge;
    static BottomNavigationItemView item;
    static BottomNavigationMenuView menuView;

    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }

    public static void addBadgeToBottomNav(BottomNavigationView view, int position, long count) {
        if (count!=0) {
            menuView = (BottomNavigationMenuView) view.getChildAt(0);
            item = (BottomNavigationItemView) menuView.getChildAt(position);

            badge = LayoutInflater.from(view.getContext())
                    .inflate(R.layout.notification_badge, item, false);
            item.addView(badge);
            notificationBadge = item.findViewById(R.id.badge);
            if (count > 9) {
                notificationBadge.setText("9+");
            } else {
                notificationBadge.setText(String.valueOf(count));
            }
        }
    }

    public static void removeBadgeFromBottomNav(){

        if (item!=null) {
            item.removeView(badge);
        }

//            badge.setVisibility(GONE);
    }
}
