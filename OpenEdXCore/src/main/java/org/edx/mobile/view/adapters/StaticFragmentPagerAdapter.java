package org.edx.mobile.view.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link FragmentPagerAdapter} that takes a static list
 * of Fragments as the items.
 */
public class StaticFragmentPagerAdapter extends FragmentPagerAdapter {
    @NonNull
    private List<Item> items = Collections.emptyList();
    @NonNull
    private final Map<Integer, Fragment> positionToFragment = new HashMap<>();

    public StaticFragmentPagerAdapter(@NonNull FragmentManager manager, @NonNull Item... items) {
        super(manager);
        setItems(Arrays.asList(items));
    }

    public void setItems(@NonNull List<Item> items) {
        this.items = new LinkedList<>(items);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    // Despite the name, this method is actually used for *instantiating* each fragment
    @Override
    public Fragment getItem(int position) {
        return items.get(position).generateFragment();
    }

    public Fragment getFragment(int position) {
        return positionToFragment.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return items.get(position).title;
    }

    @Override
    public Fragment instantiateItem(ViewGroup container, int position) {
        final Fragment fragment = (Fragment)super.instantiateItem(container, position);
        positionToFragment.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        positionToFragment.remove(position);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            if (fragmentClass != null ? !fragmentClass.equals(item.fragmentClass) : item.fragmentClass != null)
                return false;
            if (args != null ? !args.equals(item.args) : item.args != null) return false;
            return title != null ? title.equals(item.title) : item.title == null;

        }

        @Override
        public int hashCode() {
            int result = fragmentClass != null ? fragmentClass.hashCode() : 0;
            result = 31 * result + (args != null ? args.hashCode() : 0);
            result = 31 * result + (title != null ? title.hashCode() : 0);
            return result;
        }
    }
}
