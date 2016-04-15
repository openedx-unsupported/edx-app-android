/*
 * The code for this class is copied and modified from AppCompatSpinner
 * in the appcompat library, which is available from the AOSP.
 *
 * Copyright (C) 2014 The Android Open Source Project
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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.TintTypedArray;
import android.support.v7.widget.ViewUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.edx.mobile.R;
import org.edx.mobile.view.compat.ViewTreeObserverCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@link Spinner} which defines an 'active' drawable
 * state for when the dropdown is being shown.
 */
@UiThread
public class StatefulSpinner extends AppCompatSpinner {
    /*
     * The code here is all copied directly from AppCompatSpinner, removing the
     * elements that are not directly associated with displaying the dropdown
     * popup, which can be handled by AppCompatSpinner itself.
     *
     * The only real change is the addition of the onCreateDrawableState() method,
     * and adding calls to refreshDrawableState() in the show() method of
     * DropDownAdapter (where it also sets the dismiss listener).
     *
     * Trivial changes include the following:
     *
     * - Removed pre-Honeycomb compatibility logic. A separate attribute set has
     *   been created to include the android:spinnerMode attribute, which was not
     *   available before Honeycomb, and was therefore resolved separately by
     *   AppCompatSpinner.
     *
     * - Eliminated stylistic warnings.
     *
     * - Replaced the call to the deprecated
     *   ViewTreeObserver#removeOnGlobalLayoutListener() method, in favor of using
     *   a locally defined API helper.
     *
     * - Added support annotations to field, parameters, and methods, for
     *   verifying internal and external code correctness.
     *
     * - Exposed the mode constants to subclasses, and defined an @IntDef
     *   annotation for them.
     *
     * - Small code optimizations and reordering where it makes sense after
     *   removing unneeded code that's handled by AppCompatSpinner.
     *
     * - Removed todo comments related to the appcompat library implementation.
     */

    private static final int MAX_ITEMS_MEASURED = 15;

    /**
     * Use the theme-supplied value to select the dropdown mode.
     */
    protected static final int MODE_THEME = -1;

    /*
     * These constants are defined publicly in Spinner, but for some reason if
     * they aren't explicitly defined here, then the Android Studio compiler tries
     * to resolve them from the closest parent class, and throws an error when it
     * finds that AppCompatSpinner has defined them privately (as they're not
     * available pre-Honeycomb). Defining static imports makes it compile, but
     * Studio still shows the usages as an error. Therefore these are being
     * explicitly defined here with protected access level, so that they can be
     * referenced simply by this class and all subclasses of it.
     */
    /**
     * Use a dialog window for selecting spinner options.
     */
    protected static final int MODE_DIALOG = Spinner.MODE_DIALOG;

    /**
     * Use a dropdown anchored to the Spinner for selecting spinner options.
     */
    protected static final int MODE_DROPDOWN = Spinner.MODE_DROPDOWN;

    /**
     * Spinner modes.
     */
    @IntDef({
            MODE_THEME,
            MODE_DIALOG,
            MODE_DROPDOWN
    })
    @Retention(RetentionPolicy.SOURCE)
    protected @interface Mode {}

    private static final int[] ACTIVE_STATE_SET = { android.R.attr.state_active };

    /** Forwarding listener used to implement drag-to-open. */
    @Nullable
    private ListPopupWindow.ForwardingListener mForwardingListener;

    /** Temporary holder for setAdapter() calls from the super constructor. */
    @Nullable
    private SpinnerAdapter mTempAdapter;

    private boolean mPopupSet;

    @Nullable
    private DropdownPopup mPopup;

    @IntRange(from = -2)
    private int mDropDownWidth;

    private final Rect mTempRect = new Rect();

    /**
     * Construct a new spinner with the given context's theme.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public StatefulSpinner(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Construct a new spinner with the given context's theme and the supplied
     * mode of displaying choices. <code>mode</code> may be one of
     * {@link #MODE_DIALOG} or {@link #MODE_DROPDOWN}.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param mode    Constant describing how the user will select choices from the spinner.
     * @see #MODE_DIALOG
     * @see #MODE_DROPDOWN
     */
    public StatefulSpinner(@NonNull Context context, @Mode int mode) {
        this(context, null, R.attr.spinnerStyle, mode);
    }

    /**
     * Construct a new spinner with the given context's theme and the supplied attribute set.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public StatefulSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.spinnerStyle);
    }

    /**
     * Construct a new spinner with the given context's theme, the supplied attribute set,
     * and default style attribute.
     *
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     */
    public StatefulSpinner(@NonNull Context context, @Nullable AttributeSet attrs,
                           @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, MODE_THEME);
    }

    /**
     * Construct a new spinner with the given context's theme, the supplied attribute set,
     * and default style. <code>mode</code> may be one of {@link #MODE_DIALOG} or
     * {@link #MODE_DROPDOWN} and determines how the user will select choices from the spinner.
     *
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @param mode         Constant describing how the user will select choices from the spinner.
     * @see #MODE_DIALOG
     * @see #MODE_DROPDOWN
     */
    public StatefulSpinner(@NonNull Context context, @Nullable AttributeSet attrs,
                           @AttrRes int defStyleAttr, @Mode int mode) {
        this(context, attrs, defStyleAttr, mode, null);
    }

    /**
     * Constructs a new spinner with the given context's theme, the supplied
     * attribute set, default styles, popup mode (one of {@link #MODE_DIALOG}
     * or {@link #MODE_DROPDOWN}), and the context against which the popup
     * should be inflated.
     *
     * @param context      The context against which the view is inflated, which
     *                     provides access to the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default
     *                     values for the view. Can be 0 to not look for
     *                     defaults.
     * @param mode         Constant describing how the user will select choices from
     *                     the spinner.
     * @param popupTheme   The theme against which the dialog or dropdown popup
     *                     should be inflated. May be {@code null} to use the
     *                     view theme. If set, this will override any value
     *                     specified by {@link R.styleable#Spinner_popupTheme}.
     * @see #MODE_DIALOG
     * @see #MODE_DROPDOWN
     */
    public StatefulSpinner(@NonNull Context context, @Nullable AttributeSet attrs,
                           @AttrRes int defStyleAttr, @Mode int mode,
                           @Nullable Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);

        switch (mode) {
            case MODE_THEME:
            case MODE_DROPDOWN:
                Context popupContext = getPopupContext();
                if (popupContext == null) break;

                TypedArray a = context.obtainStyledAttributes(
                        attrs, R.styleable.Spinner, defStyleAttr, 0);

                if (mode == MODE_THEME) {
                    //noinspection WrongConstant
                    mode = a.getInt(R.styleable.Spinner_android_spinnerMode, Spinner.MODE_DIALOG);
                }

                if (mode == MODE_DROPDOWN) {
                    final DropdownPopup popup = new DropdownPopup(popupContext, attrs,
                            defStyleAttr);
                    popup.setPromptText(a.getString(R.styleable.Spinner_android_prompt));
                    final TintTypedArray pa = TintTypedArray.obtainStyledAttributes(
                            popupContext, attrs, R.styleable.Spinner, defStyleAttr, 0);
                    mDropDownWidth = pa.getLayoutDimension(
                            R.styleable.Spinner_android_dropDownWidth, LayoutParams.WRAP_CONTENT);
                    popup.setBackgroundDrawable(
                            pa.getDrawable(R.styleable.Spinner_android_popupBackground));
                    pa.recycle();

                    mPopup = popup;
                    mForwardingListener = new ListPopupWindow.ForwardingListener(this) {
                        @Override
                        @CheckResult
                        @NonNull
                        public ListPopupWindow getPopup() {
                            return popup;
                        }

                        @Override
                        public boolean onForwardingStarted() {
                            if (!mPopup.isShowing()) {
                                mPopup.show();
                            }
                            return true;
                        }
                    };
                }

                a.recycle();
        }

        mPopupSet = true;

        // Base constructors can call setAdapter before we initialize mPopup.
        // Finish setting things up if this happened.
        if (mTempAdapter != null) {
            setAdapter(mTempAdapter);
            mTempAdapter = null;
        }
    }

    @Override
    public void setPopupBackgroundDrawable(@Nullable Drawable background) {
        if (mPopup != null) {
            mPopup.setBackgroundDrawable(background);
        } else {
            super.setPopupBackgroundDrawable(background);
        }
    }

    @Override
    @CheckResult
    @Nullable
    public Drawable getPopupBackground() {
        if (mPopup != null) {
            return mPopup.getBackground();
        } else {
            return super.getPopupBackground();
        }
    }

    @Override
    public void setDropDownVerticalOffset(int pixels) {
        if (mPopup != null) {
            mPopup.setVerticalOffset(pixels);
        } else {
            super.setDropDownVerticalOffset(pixels);
        }
    }

    @Override
    @CheckResult
    public int getDropDownVerticalOffset() {
        if (mPopup != null) {
            return mPopup.getVerticalOffset();
        } else  {
            return super.getDropDownVerticalOffset();
        }
    }

    @Override
    public void setDropDownHorizontalOffset(int pixels) {
        if (mPopup != null) {
            mPopup.setHorizontalOffset(pixels);
        } else {
            super.setDropDownHorizontalOffset(pixels);
        }
    }

    @Override
    @CheckResult
    public int getDropDownHorizontalOffset() {
        if (mPopup != null) {
            return mPopup.getHorizontalOffset();
        } else {
            return super.getDropDownHorizontalOffset();
        }
    }

    @Override
    public void setDropDownWidth(@IntRange(from = -2) int pixels) {
        if (mPopup != null) {
            mDropDownWidth = pixels;
        } else {
            super.setDropDownWidth(pixels);
        }
    }

    @Override
    @CheckResult
    @IntRange(from = -2)
    public int getDropDownWidth() {
        if (mPopup != null) {
            return mDropDownWidth;
        } else {
            return super.getDropDownWidth();
        }
    }

    @Override
    public void setAdapter(@Nullable SpinnerAdapter adapter) {
        // The super constructor may call setAdapter before we're prepared.
        // Postpone doing anything until we've finished construction.
        if (!mPopupSet) {
            mTempAdapter = adapter;
            return;
        }

        super.setAdapter(adapter);

        if (mPopup != null) {
            Context popupContext = getPopupContext();
            if (popupContext == null) {
                popupContext = getContext();
            }
            mPopup.setAdapter(new DropDownAdapter(adapter, popupContext.getTheme()));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return (mForwardingListener != null && mForwardingListener.onTouch(this, event))
                || super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mPopup != null && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            setMeasuredDimension(Math.min(Math.max(getMeasuredWidth(),
                    compatMeasureContentWidth(getAdapter(), getBackground())),
                    MeasureSpec.getSize(widthMeasureSpec)),
                    getMeasuredHeight());
        }
    }

    @Override
    public boolean performClick() {
        if (mPopup != null && !mPopup.isShowing()) {
            mPopup.show();
            return true;
        }
        return super.performClick();
    }

    @Override
    public void setPrompt(@Nullable CharSequence prompt) {
        if (mPopup != null) {
            mPopup.setPromptText(prompt);
        } else {
            super.setPrompt(prompt);
        }
    }

    @Override
    @CheckResult
    @Nullable
    public CharSequence getPrompt() {
        return mPopup != null ? mPopup.getHintText() : super.getPrompt();
    }

    @Override
    @CheckResult
    @NonNull
    protected int[] onCreateDrawableState(@IntRange(from = 0) int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (mPopup != null && mPopup.isShowing()) {
            mergeDrawableStates(drawableState, ACTIVE_STATE_SET);
        }
        return drawableState;
    }

    @CheckResult
    private int compatMeasureContentWidth(@Nullable SpinnerAdapter adapter,
                                          @Nullable Drawable background) {
        if (adapter == null) {
            return 0;
        }

        int width = 0;
        View itemView = null;
        int itemType = 0;
        final int widthMeasureSpec =
                MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec =
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.UNSPECIFIED);

        // Make sure the number of items we'll measure is capped. If it's a huge data set
        // with wildly varying sizes, oh well.
        int start = Math.max(0, getSelectedItemPosition());
        final int end = Math.min(adapter.getCount(), start + MAX_ITEMS_MEASURED);
        final int count = end - start;
        start = Math.max(0, start - (MAX_ITEMS_MEASURED - count));
        for (int i = start; i < end; i++) {
            final int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            itemView = adapter.getView(i, itemView, this);
            if (itemView.getLayoutParams() == null) {
                itemView.setLayoutParams(new LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT));
            }
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            width = Math.max(width, itemView.getMeasuredWidth());
        }

        // Add background padding to measured width
        if (background != null) {
            background.getPadding(mTempRect);
            width += mTempRect.left + mTempRect.right;
        }

        return width;
    }

    /**
     * <p>Wrapper class for an Adapter. Transforms the embedded Adapter instance
     * into a ListAdapter.</p>
     */
    @UiThread
    private static class DropDownAdapter implements ListAdapter, SpinnerAdapter {

        @Nullable
        private SpinnerAdapter mAdapter;

        @Nullable
        private ListAdapter mListAdapter;

        /**
         * Creates a new ListAdapter wrapper for the specified adapter.
         *
         * @param adapter       the SpinnerAdapter to transform into a ListAdapter
         * @param dropDownTheme the theme against which to inflate drop-down
         *                      views, may be <code>null</code> to use default theme
         */
        public DropDownAdapter(@Nullable SpinnerAdapter adapter,
                @Nullable Resources.Theme dropDownTheme) {
            mAdapter = adapter;

            if (adapter instanceof ListAdapter) {
                mListAdapter = (ListAdapter) adapter;
            }

            if (dropDownTheme != null) {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                         adapter instanceof android.widget.ThemedSpinnerAdapter) {
                    final android.widget.ThemedSpinnerAdapter themedAdapter =
                            (android.widget.ThemedSpinnerAdapter) adapter;
                    if (themedAdapter.getDropDownViewTheme() != dropDownTheme) {
                        themedAdapter.setDropDownViewTheme(dropDownTheme);
                    }
                } else if (adapter instanceof ThemedSpinnerAdapter) {
                    final ThemedSpinnerAdapter themedAdapter = (ThemedSpinnerAdapter) adapter;
                    if (themedAdapter.getDropDownViewTheme() == null) {
                        themedAdapter.setDropDownViewTheme(dropDownTheme);
                    }
                }
            }
        }

        @CheckResult
        @IntRange(from = 0)
        public int getCount() {
            //noinspection Range
            return mAdapter == null ? 0 : mAdapter.getCount();
        }

        @CheckResult
        @Nullable
        public Object getItem(@IntRange(from = 0) int position) {
            return mAdapter == null ? null : mAdapter.getItem(position);
        }

        @CheckResult
        public long getItemId(@IntRange(from = 0) int position) {
            return mAdapter == null ? -1 : mAdapter.getItemId(position);
        }

        @CheckResult
        @NonNull
        public View getView(@IntRange(from = 0) int position, @Nullable View convertView,
                            @NonNull ViewGroup parent) {
            return getDropDownView(position, convertView, parent);
        }

        @CheckResult
        @NonNull
        public View getDropDownView(@IntRange(from = 0) int position, @Nullable View convertView,
                                    @NonNull ViewGroup parent) {
            //noinspection ConstantConditions
            return (mAdapter == null) ? null
                    : mAdapter.getDropDownView(position, convertView, parent);
        }

        @CheckResult
        public boolean hasStableIds() {
            return mAdapter != null && mAdapter.hasStableIds();
        }

        public void registerDataSetObserver(@NonNull DataSetObserver observer) {
            if (mAdapter != null) {
                mAdapter.registerDataSetObserver(observer);
            }
        }

        public void unregisterDataSetObserver(@NonNull DataSetObserver observer) {
            if (mAdapter != null) {
                mAdapter.unregisterDataSetObserver(observer);
            }
        }

        /**
         * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call.
         * Otherwise, return true.
         */
        @CheckResult
        public boolean areAllItemsEnabled() {
            return mListAdapter == null || mListAdapter.areAllItemsEnabled();
        }

        /**
         * If the wrapped SpinnerAdapter is also a ListAdapter, delegate this call.
         * Otherwise, return true.
         */
        @CheckResult
        public boolean isEnabled(@IntRange(from = 0) int position) {
            return mListAdapter == null || mListAdapter.isEnabled(position);
        }

        @CheckResult
        @IntRange(from = 0)
        public int getItemViewType(@IntRange(from = 0) int position) {
            return 0;
        }

        @CheckResult
        @IntRange(from = 0)
        public int getViewTypeCount() {
            return 1;
        }

        @CheckResult
        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

    @UiThread
    private class DropdownPopup extends ListPopupWindow {
        @Nullable
        private CharSequence mHintText;
        @Nullable
        private ListAdapter mAdapter;
        private final Rect mVisibleRect = new Rect();

        public DropdownPopup(@NonNull Context context, @Nullable AttributeSet attrs,
                             @AttrRes int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            setAnchorView(StatefulSpinner.this);
            setModal(true);
            setPromptPosition(POSITION_PROMPT_ABOVE);

            setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    StatefulSpinner.this.setSelection(position);
                    if (getOnItemClickListener() != null) {
                        // We can be sure that the adapter is not null at this point.
                        //noinspection ConstantConditions
                        StatefulSpinner.this
                                .performItemClick(v, position, mAdapter.getItemId(position));
                    }
                    dismiss();
                }
            });
        }

        @Override
        public void setAdapter(@Nullable ListAdapter adapter) {
            super.setAdapter(adapter);
            mAdapter = adapter;
        }

        @CheckResult
        @Nullable
        public CharSequence getHintText() {
            return mHintText;
        }

        public void setPromptText(@Nullable CharSequence hintText) {
            // Hint text is ignored for dropdowns, but maintain it here.
            mHintText = hintText;
        }

        void computeContentWidth() {
            final Drawable background = getBackground();
            int hOffset = 0;
            if (background != null) {
                background.getPadding(mTempRect);
                hOffset = ViewUtils.isLayoutRtl(StatefulSpinner.this) ? mTempRect.right
                        : -mTempRect.left;
            } else {
                mTempRect.left = mTempRect.right = 0;
            }

            final int spinnerPaddingLeft = StatefulSpinner.this.getPaddingLeft();
            final int spinnerPaddingRight = StatefulSpinner.this.getPaddingRight();
            final int spinnerWidth = StatefulSpinner.this.getWidth();
            if (mDropDownWidth == WRAP_CONTENT) {
                int contentWidth = compatMeasureContentWidth(
                        (SpinnerAdapter) mAdapter, getBackground());
                final int contentWidthLimit = getContext().getResources()
                        .getDisplayMetrics().widthPixels - mTempRect.left - mTempRect.right;
                if (contentWidth > contentWidthLimit) {
                    contentWidth = contentWidthLimit;
                }
                setContentWidth(Math.max(
                        contentWidth, spinnerWidth - spinnerPaddingLeft - spinnerPaddingRight));
            } else if (mDropDownWidth == MATCH_PARENT) {
                setContentWidth(spinnerWidth - spinnerPaddingLeft - spinnerPaddingRight);
            } else {
                setContentWidth(mDropDownWidth);
            }
            if (ViewUtils.isLayoutRtl(StatefulSpinner.this)) {
                hOffset += spinnerWidth - spinnerPaddingRight - getWidth();
            } else {
                hOffset += spinnerPaddingLeft;
            }
            setHorizontalOffset(hOffset);
        }

        public void show() {
            final boolean wasShowing = isShowing();

            computeContentWidth();

            setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
            super.show();
            final ListView listView = getListView();
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            setSelection(StatefulSpinner.this.getSelectedItemPosition());

            if (wasShowing) {
                // Skip setting up the layout/dismiss listener below. If we were previously
                // showing it will still stick around.
                return;
            }

            refreshDrawableState();

            // Make sure we hide if our anchor goes away.
            final ViewTreeObserver.OnGlobalLayoutListener layoutListener
                    = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (!isVisibleToUser(StatefulSpinner.this)) {
                        dismiss();
                    } else {
                        computeContentWidth();

                        // Use super.show here to update; we don't want to move the selected
                        // position or adjust other things that would be reset otherwise.
                        DropdownPopup.super.show();
                    }
                }
            };
            getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

            setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    ViewTreeObserverCompat.removeOnGlobalLayoutListener(
                            getViewTreeObserver(), layoutListener);
                    refreshDrawableState();
                }
            });
        }

        /**
         * Simplified version of the the hidden View.isVisibleToUser()
         */
        @CheckResult
        private boolean isVisibleToUser(@NonNull View view) {
            return ViewCompat.isAttachedToWindow(view) && view.getGlobalVisibleRect(mVisibleRect);
        }
    }
}
