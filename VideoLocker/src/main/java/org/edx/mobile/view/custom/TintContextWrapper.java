/*
 * This class is copied and slightly modified from the appcompat
 * library, which is available from the AOSP.
 *
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edx.mobile.view.custom;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.AppCompatDrawableManager;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A {@link ContextWrapper} which returns a tint-aware
 * {@link Resources} instance from {@link #getResources()}.
 */
/*
 * The TintContextWrapper implementation in appcompat v23.3.0
 * only took care of setting up tinting for standard hardcoded
 * drawables defined in appcompat, and did not handle resolving
 * backported drawable implementations (e.g. VectorDrawable) at
 * all. Moreover, the class was defined as package-private. The
 * implementation in first alpha release of version 24 of the
 * appcompat library handles this along with a few other small
 * fixes, and it's made the class public as well, though not part
 * of the official API. This class has been copied from there
 * with a few small modifications, to be used where needed until
 * there is a stable release of appcompat v24 and so we can
 * upgrade to it, and use it's final implementation directly.
 *
 * The modifications are as follows:
 *
 * - Moved the independent TintResources class to a private
 *   inner class, to avoid cluttering the custom views package
 *   with something that would probably not need to be used
 *   directly by external classes. This being the case, the
 *   shouldWrap() method has been removed. It added a check for
 *   the Resource type as well, to determine if it was an
 *   instance of TintResources, but now that should only happen
 *   if the Context is wrapped as well.
 *
 * - Converted the cache from an ArrayList of weak references to
 *   a WeakHashMap with the Context objects as the keys, and weak
 *   references to the wrappers as the values. This provides the
 *   benefits of both having a fast lookup time, and automatic
 *   clearing of the mapping entries and the weak references upon
 *   garbage collection of the Context objects.
 *
 * - Changed the getDrawable() override in TintResources to call
 *   the public getDrawable() method in AppCompatDrawableManager
 *   with the base Context, since the special callback written
 *   for this case in AppCompatDrawableManager in the v24 alpha
 *   is not available yet. Since this isn't set up specially to
 *   avoid recursive calls back to the wrapper when falling back
 *   to loading from the platform loader, the currently loading
 *   drawable's resource ID is saved to detect this scenario and
 *   call through to the super implementation in that case.
 *
 * - Added more support annotations for better compile-time
 *   correctness checks.
 *
 * - Small code restructuring and optimizations.
 */
class TintContextWrapper extends ContextWrapper {

    @SuppressWarnings("unchecked")
    private static final Map<Context, WeakReference<TintContextWrapper>> cache =
            new WeakHashMap<>();

    public static Context wrap(@NonNull final Context context) {
        if (!(context instanceof TintContextWrapper)) {
            // First check our instance cache
            final WeakReference<TintContextWrapper> ref = cache.get(context);
            TintContextWrapper wrapper = ref != null ? ref.get() : null;
            if (wrapper == null) {
                // If we reach here then the cache didn't have a hit, so create a new instance
                // and add it to the cache
                wrapper = new TintContextWrapper(context);
                cache.put(context, new WeakReference<>(wrapper));
            }
            return wrapper;
        }

        return context;
    }

    @Nullable
    private Resources mResources;
    @NonNull
    private final Resources.Theme mTheme;

    private TintContextWrapper(@NonNull final Context base) {
        super(base);

        // We need to create a copy of the Theme so that the Theme references our Resources
        // instance
        mTheme = getResources().newTheme();
        mTheme.setTo(base.getTheme());
    }

    @Override
    @NonNull
    public Resources.Theme getTheme() {
        return mTheme;
    }

    @Override
    public void setTheme(@StyleRes int resid) {
        mTheme.applyStyle(resid, true);
    }

    @Override
    @NonNull
    public Resources getResources() {
        if (mResources == null) {
            mResources = new TintResources(super.getResources());
        }
        return mResources;
    }

    /**
     * This class allows us to intercept calls so that we can tint resources (if applicable).
     */
    private class TintResources extends Resources {
        private final AppCompatDrawableManager drawableManager = AppCompatDrawableManager.get();

        /*
         * Save the resource ID of the Drawable that is currently being loaded, in order to detect
         * when getDrawable() is called recursively by the drawable manager.
         */
        @DrawableRes
        private int loadingDrawableResId;

        public TintResources(@NonNull final Resources res) {
            super(res.getAssets(), res.getDisplayMetrics(), res.getConfiguration());
        }

        /**
         * We intercept this call so that we tint the result (if applicable). This is needed for
         * things like {@link android.graphics.drawable.DrawableContainer}s which can retrieve
         * their children via this method.
         */
        @Override
        @SuppressWarnings("deprecation")
        public Drawable getDrawable(@DrawableRes int id) throws NotFoundException {
            if (id == loadingDrawableResId) {
                /* This is probably a recursive call by the drawable manager after
                 * failing to resolve one of the backported drawables (either that or a
                 * recursive reference in the drawable definition itself). Just call
                 * through to the system implementation to load the drawable.
                 */
                return super.getDrawable(id);
            }
            int loadingParentDrawableResId = loadingDrawableResId;
            loadingDrawableResId = id;
            Drawable drawable = drawableManager.getDrawable(TintContextWrapper.this, id);
            loadingDrawableResId = loadingParentDrawableResId;
            return drawable;
        }
    }
}
