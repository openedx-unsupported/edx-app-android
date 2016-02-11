package org.edx.mobile.view.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Implementation of {@link FragmentPagerAdapter} that takes a static list
 * of Fragments as the items.
 */
public final class StaticFragmentPagerAdapter extends FragmentPagerAdapter {
    private final Item[] items;

    public StaticFragmentPagerAdapter(@NonNull FragmentManager manager, @NonNull Item... items) {
        super(manager);
        this.items = items.clone();
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Fragment getItem(int position) {
        return items[position].generateFragment();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return items[position].title;
    }

    /**
     * Data class representing a page.
     */
    public static final class Item {
        private static String ERROR_MSG_INSTANTIATION = "Unable to instantiate fragment %s: make " +
                "sure class name exists, is public, and has an empty constructor that is public.";

        private final Class<? extends Fragment> fragmentClass;
        private final Bundle args;
        private final CharSequence title;

        public Item(Class<? extends Fragment> fragmentClass) {
            this(fragmentClass, null, null);
        }

        public Item(Class<? extends Fragment> fragmentClass, Bundle args) {
            this(fragmentClass, args, null);
        }

        public Item(Class<? extends Fragment> fragmentClass, CharSequence title) {
            this(fragmentClass, null, title);
        }

        public Item(Class<? extends Fragment> fragmentClass, Bundle args, CharSequence title) {
            if (args != null) {
                args.setClassLoader(fragmentClass.getClassLoader());
            }
            this.fragmentClass = fragmentClass;
            this.args = args;
            this.title = title;
        }

        private Fragment generateFragment() {
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
    }
}
