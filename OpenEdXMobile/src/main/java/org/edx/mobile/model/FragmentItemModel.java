package org.edx.mobile.model;

import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A class containing all data required for {@link androidx.core.view.PagerAdapter PagerAdapter}
 * and {@link com.google.android.material.tabs.TabLayout TabLayout} to initialize a fragment/tab.
 */
public class FragmentItemModel {
    private static String ERROR_MSG_INSTANTIATION = "Unable to instantiate fragment %s: make " +
            "sure class name exists, is public, and has an empty constructor that is public.";

    @NonNull
    private final Class<? extends Fragment> fragmentClass;
    @NonNull
    private final CharSequence title;
    @DrawableRes
    private int iconResId;
    @Nullable
    private final Bundle args;
    @Nullable
    private FragmentStateListener listener;

    public FragmentItemModel(@NonNull Class<? extends Fragment> fragmentClass, @NonNull CharSequence title) {
        this(fragmentClass, title, 0, null, null);
    }

    public FragmentItemModel(@NonNull Class<? extends Fragment> fragmentClass,
                             @NonNull CharSequence title, @DrawableRes int iconResId, FragmentStateListener listener) {
        this(fragmentClass, title, iconResId, null, listener);
    }

    public FragmentItemModel(@NonNull Class<? extends Fragment> fragmentClass, @NonNull CharSequence title,
                             @DrawableRes int iconResId, Bundle args, FragmentStateListener listener) {
        if (args != null) {
            args.setClassLoader(fragmentClass.getClassLoader());
        }
        this.fragmentClass = fragmentClass;
        this.title = title;
        this.iconResId = iconResId;
        this.args = args;
        this.listener = listener;
    }

    @NonNull
    public CharSequence getTitle() {
        return title;
    }

    @DrawableRes
    public int getIconResId() {
        return iconResId;
    }

    @Nullable
    public FragmentStateListener getListener() {
        return listener;
    }

    public Fragment generateFragment() {
        final Fragment fragment;
        try {
            fragment = fragmentClass.newInstance();
        } catch (InstantiationException e) {
            throw new Fragment.InstantiationException(
                    String.format(ERROR_MSG_INSTANTIATION, fragmentClass), e);
        } catch (IllegalAccessException e) {
            throw new Fragment.InstantiationException(
                    String.format(ERROR_MSG_INSTANTIATION, fragmentClass), e);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FragmentItemModel that = (FragmentItemModel) o;

        if (!fragmentClass.equals(that.fragmentClass)) return false;
        if (!title.equals(that.title)) return false;
        if (iconResId != 0 ? iconResId != that.iconResId : that.iconResId != 0) return false;
        if (args != null ? !args.equals(that.args) : that.args != null) return false;
        return listener != null ? listener.equals(that.listener) : that.listener == null;
    }

    @Override
    public int hashCode() {
        int result = fragmentClass.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + (iconResId != 0 ? Integer.valueOf(iconResId).hashCode() : 0);
        result = 31 * result + (args != null ? args.hashCode() : 0);
        result = 31 * result + (listener != null ? listener.hashCode() : 0);
        return result;
    }

    public interface FragmentStateListener {
        void onFragmentSelected();
    }
}
