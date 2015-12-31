package org.edx.mobile.view.custom.popup.menu;

/*
 * This class is copied and modified according to our specifications from
 * the AOSP. It uses the appcompat implementation because it exposes it's
 * internal classes, so we only need to duplicate minimal code. We use a
 * custom attribute set to define the width and some other things, and a
 * custom adapter with it's own layout that automatically expands the first
 * level of submenus in order to support headers.
 *
 * Copyright (C) 2010 The Android Open Source Project
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuPresenter;
import android.support.v7.view.menu.MenuView;
import android.support.v7.view.menu.SubMenuBuilder;
import android.support.v7.widget.ListPopupWindow;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.edx.mobile.R;

import java.util.List;

/**
 * Presents a menu as a small, simple popup anchored to another view.
 */
class MenuPopupHelper implements AdapterView.OnItemClickListener, View.OnKeyListener,
        ViewTreeObserver.OnGlobalLayoutListener, PopupWindow.OnDismissListener,
        View.OnAttachStateChangeListener, MenuPresenter {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final MenuBuilder mMenu;
    private final MenuAdapter mAdapter;
    private final boolean mOverflowOnly;
    private final int mPopupMinWidth;
    private final int mPopupMaxWidth;
    private final int mPopupPaddingLeft;
    private final int mPopupPaddingRight;
    private final int mPopupPaddingStart;
    private final int mPopupPaddingEnd;
    private final int mPopupPaddingTop;
    private final int mPopupPaddingBottom;
    private final int mPopupItemVerticalPadding;
    private final int mPopupIconPadding;
    private final int mPopupIconDefaultSize;
    private final int mPopupStyleAttr;
    private final int mPopupStyleRes;

    private View mAnchorView;
    private ListPopupWindow mPopup;
    private ViewTreeObserver mTreeObserver;
    private Callback mPresenterCallback;

    boolean mForceShowIcon;

    /**
     * Dummy ListView to use as parent when measuring rows, in order to
     * generate the row layout params and resolve layout direction
     * inheritance (and thereby the compound drawable alignment in
     * TextView).
     */
    private ListView mMeasureListView;

    /** Cached content width from {@link #measureContentWidth}. */
    private int mContentWidth = Integer.MIN_VALUE;

    /** A MeasureSpec with UNSPECIFIED mode. */
    private static final int UNSPECIFIED_MEASURE_SPEC =
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

    private int mDropDownGravity = Gravity.NO_GRAVITY;

    public MenuPopupHelper(Context context, MenuBuilder menu) {
        this(context, menu, null, false, android.R.attr.popupMenuStyle, 0);
    }

    public MenuPopupHelper(Context context, MenuBuilder menu, View anchorView) {
        this(context, menu, anchorView, false, android.R.attr.popupMenuStyle, 0);
    }

    public MenuPopupHelper(Context context, MenuBuilder menu, View anchorView,
            boolean overflowOnly, int popupStyleAttr) {
        this(context, menu, anchorView, overflowOnly, popupStyleAttr, 0);
    }

    public MenuPopupHelper(Context context, MenuBuilder menu, View anchorView,
            boolean overflowOnly, int popupStyleAttr, int popupStyleRes) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mMenu = menu;
        mAdapter = new MenuAdapter();
        mOverflowOnly = overflowOnly;
        mPopupStyleAttr = popupStyleAttr;
        mPopupStyleRes = popupStyleRes;

        final Resources res = context.getResources();
        mPopupMaxWidth = Math.max(res.getDisplayMetrics().widthPixels / 2,
                res.getDimensionPixelSize(android.support.v7.appcompat.
                        R.dimen.abc_config_prefDialogWidth));

        TypedArray a = context.obtainStyledAttributes(null,
                R.styleable.PopupMenu, mPopupStyleAttr, mPopupStyleRes);
        mPopupMinWidth = a.getDimensionPixelSize(
                R.styleable.PopupMenu_android_minWidth, 0);
        int popupPadding = a.getDimensionPixelSize(
                R.styleable.PopupMenu_android_padding, -1);
        if (popupPadding >= 0) {
            mPopupPaddingLeft = popupPadding;
            mPopupPaddingRight = popupPadding;
            mPopupPaddingStart = popupPadding;
            mPopupPaddingEnd = popupPadding;
            mPopupPaddingTop = popupPadding;
            mPopupPaddingBottom = popupPadding;
        } else {
            int paddingStart = Integer.MIN_VALUE;
            int paddingEnd = Integer.MIN_VALUE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                paddingStart = a.getDimensionPixelSize(
                        R.styleable.PopupMenu_android_paddingStart, Integer.MIN_VALUE);
                paddingEnd = a.getDimensionPixelSize(
                        R.styleable.PopupMenu_android_paddingEnd, Integer.MIN_VALUE);
            }
            int paddingLeft = 0;
            int paddingRight = 0;
            if (paddingStart == Integer.MIN_VALUE && paddingEnd == Integer.MIN_VALUE) {
                paddingLeft = a.getDimensionPixelSize(
                        R.styleable.PopupMenu_android_paddingLeft, 0);
                paddingRight = a.getDimensionPixelSize(
                        R.styleable.PopupMenu_android_paddingRight, 0);
            } else {
                if (paddingStart == Integer.MIN_VALUE) {
                    paddingStart = 0;
                }
                if (paddingEnd == Integer.MIN_VALUE) {
                    paddingEnd = 0;
                }
            }
            mPopupPaddingLeft = paddingLeft;
            mPopupPaddingRight = paddingRight;
            mPopupPaddingStart = paddingStart;
            mPopupPaddingEnd = paddingEnd;
            mPopupPaddingTop = a.getDimensionPixelSize(
                    R.styleable.PopupMenu_android_paddingTop, 0);
            mPopupPaddingBottom = a.getDimensionPixelSize(
                    R.styleable.PopupMenu_android_paddingBottom, 0);
        }
        mPopupItemVerticalPadding = a.getDimensionPixelSize(
                R.styleable.PopupMenu_itemVerticalPadding, 0);
        mPopupIconPadding = a.getDimensionPixelSize(
                R.styleable.PopupMenu_iconPadding, 0);
        mPopupIconDefaultSize = a.getDimensionPixelSize(
                R.styleable.PopupMenu_iconDefaultSize, 0);
        a.recycle();

        mAnchorView = anchorView;

        // Present the menu using our context, not the menu builder's context.
        menu.addMenuPresenter(this, context);
    }

    public void setAnchorView(View anchor) {
        mAnchorView = anchor;
    }

    public void setForceShowIcon(boolean forceShow) {
        mForceShowIcon = forceShow;
    }

    public void setGravity(int gravity) {
        mDropDownGravity = gravity;
    }

    public int getGravity() {
        return mDropDownGravity;
    }

    public void show() {
        if (!tryShow()) {
            throw new IllegalStateException("MenuPopupHelper cannot be used without an anchor");
        }
    }

    public ListPopupWindow getPopup() {
        return mPopup;
    }

    public boolean tryShow() {
        mPopup = new ListPopupWindow(mContext, null, mPopupStyleAttr, mPopupStyleRes);
        mPopup.setOnDismissListener(this);
        mPopup.setOnItemClickListener(this);
        mPopup.setAdapter(mAdapter);
        mPopup.setModal(true);

        View anchor = mAnchorView;
        if (anchor != null) {
            final boolean addGlobalListener = mTreeObserver == null;
            mTreeObserver = anchor.getViewTreeObserver(); // Refresh to latest
            if (addGlobalListener) mTreeObserver.addOnGlobalLayoutListener(this);
            anchor.addOnAttachStateChangeListener(this);
            mPopup.setAnchorView(anchor);
            mPopup.setDropDownGravity(mDropDownGravity);
        } else {
            return false;
        }

        if (mContentWidth == Integer.MIN_VALUE) {
            mContentWidth = measureContentWidth();
        }
        mPopup.setContentWidth(mContentWidth);
        // Invert the horizontal offset in RTL mode.
        if (ViewCompat.getLayoutDirection(mAnchorView) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            mPopup.setHorizontalOffset(-mPopup.getHorizontalOffset());
        }
        // Implement right gravity manually through horizontal offset pre-KitKat.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT &&
                (Gravity.getAbsoluteGravity(mDropDownGravity,
                        ViewCompat.getLayoutDirection(anchor))
                        & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT) {
            mPopup.setHorizontalOffset(mPopup.getHorizontalOffset() +
                    (mAnchorView.getWidth() - mPopup.getWidth()));
        }
        // If vertical offset is defined as 0, then ListPopupWindow infers
        // it as the negative of the top padding of the background, in
        // order to anchor the content area. Since that is not the effect
        // we want, we'll force it to use only the explicitly defined
        // offset by explicitly setting it dynamically as well, and thus
        // forcing it to discard it's 'unset' flag.
        mPopup.setVerticalOffset(mPopup.getVerticalOffset());
        // Top/bottom padding will be applied on the background drawable,
        // as the ListView is both initialized and set up only after show()
        // is called on the ListPopupWindow. Left/right padding will be
        // set up on the list items from the adapter, to keep the correct
        // item boundaries for the selector.
        ShapeDrawable paddedDrawable = new ShapeDrawable();
        paddedDrawable.setAlpha(0);
        // Don't apply top padding if the first item is a header, to
        // comply with the design.
        paddedDrawable.setPadding(0, mAdapter.hasHeader() ? 0 :
                        (mPopupPaddingTop - mPopupItemVerticalPadding),
                0, mPopupPaddingBottom - mPopupItemVerticalPadding);
        Drawable background = mPopup.getBackground();
        mPopup.setBackgroundDrawable(background == null ? paddedDrawable :
                new LayerDrawable(new Drawable[] { background, paddedDrawable }));
        mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        mPopup.show();
        mPopup.getListView().setOnKeyListener(this);
        return true;
    }

    public void dismiss() {
        if (isShowing()) {
            mPopup.dismiss();
        }
    }

    @Override
    public void onDismiss() {
        mPopup = null;
        mMenu.close();
        if (mTreeObserver != null) {
            if (!mTreeObserver.isAlive()) mTreeObserver = mAnchorView.getViewTreeObserver();
            mTreeObserver.removeGlobalOnLayoutListener(this);
            mTreeObserver = null;
        }
        mAnchorView.removeOnAttachStateChangeListener(this);
    }

    public boolean isShowing() {
        return mPopup != null && mPopup.isShowing();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mMenu.performItemAction(mAdapter.getItem(position), 0);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_MENU) {
            dismiss();
            return true;
        }
        return false;
    }

    private int measureContentWidth() {
        // Menus don't tend to be long, so this is more sane than it looks.
        int maxWidth = mPopupMinWidth;
        final int count = mAdapter.getCount();
        if (count > 0) {
            View[] viewTypes = new View[mAdapter.getViewTypeCount()];
            if (mMeasureListView == null) {
                mMeasureListView = new ListView(mContext);
            }
            // The layout direction needs to be resolved in order for the row view
            // items to resolve layout direction inheritance, which is needed for
            // the TextView to resolve the icon compound drawable alignment (which
            // is a prerequisite for measuring it).
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mMeasureListView.setLayoutDirection(mAnchorView.getLayoutDirection());
            }
            for (int i = 0; i < count; i++) {
                final int positionType = mAdapter.getItemViewType(i);
                View itemView = viewTypes[positionType];
                itemView = mAdapter.getView(i, itemView, mMeasureListView);
                // Add row to the ListView to resolve layout direction inheritance.
                mMeasureListView.addHeaderView(itemView);
                itemView.measure(UNSPECIFIED_MEASURE_SPEC, UNSPECIFIED_MEASURE_SPEC);
                viewTypes[positionType] = itemView;
                final int itemWidth = itemView.getMeasuredWidth();
                if (itemWidth >= mPopupMaxWidth) {
                    return mPopupMaxWidth;
                }
                if (itemWidth > maxWidth) {
                    maxWidth = itemWidth;
                }
            }
        }
        return maxWidth;
    }

    @Override
    public void onGlobalLayout() {
        if (isShowing()) {
            final View anchor = mAnchorView;
            if (anchor == null || !anchor.isShown()) {
                dismiss();
            } else {
                // Recompute window size and position
                mPopup.show();
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {}

    @Override
    public void onViewDetachedFromWindow(View v) {
        if (mTreeObserver != null) {
            if (!mTreeObserver.isAlive()) mTreeObserver = v.getViewTreeObserver();
            mTreeObserver.removeGlobalOnLayoutListener(this);
        }
        v.removeOnAttachStateChangeListener(this);
    }

    @Override
    public void initForMenu(Context context, MenuBuilder menu) {
        // Don't need to do anything; we added as a presenter in the constructor.
    }

    @Override
    public MenuView getMenuView(ViewGroup root) {
        throw new UnsupportedOperationException("MenuPopupHelpers manage their own views");
    }

    @Override
    public void updateMenuView(boolean cleared) {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setCallback(Callback cb) {
        mPresenterCallback = cb;
    }

    @Override
    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (subMenu.hasVisibleItems()) {
            MenuPopupHelper subPopup = new MenuPopupHelper(mContext, subMenu, mAnchorView);
            subPopup.setCallback(mPresenterCallback);

            boolean preserveIconSpacing = false;
            final int count = subMenu.size();
            for (int i = 0; i < count; i++) {
                MenuItem childItem = subMenu.getItem(i);
                if (childItem.isVisible() && childItem.getIcon() != null) {
                    preserveIconSpacing = true;
                    break;
                }
            }
            subPopup.setForceShowIcon(preserveIconSpacing);

            if (subPopup.tryShow()) {
                if (mPresenterCallback != null) {
                    mPresenterCallback.onOpenSubMenu(subMenu);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        // Only care about the (sub)menu we're presenting.
        if (menu != mMenu) return;

        dismiss();
        if (mPresenterCallback != null) {
            mPresenterCallback.onCloseMenu(menu, allMenusAreClosing);
        }
    }

    @Override
    public boolean flagActionItems() {
        return false;
    }

    @Override
    public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    @Override
    public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return null;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {}

    private enum ItemType {
        HEADER(R.layout.popup_menu_header, false),
        ITEM(R.layout.popup_menu_item, true);

        final int mLayoutRes;
        final boolean mIsEnabled;

        ItemType(@LayoutRes int layoutRes, boolean isEnabled) {
            mLayoutRes = layoutRes;
            mIsEnabled = isEnabled;
        }
    }

    private class MenuAdapter extends BaseAdapter {
        boolean hasHeader() {
            return getCount() > 0 && getItemType(0) == ItemType.HEADER;
        }

        private List<? extends MenuItem> getMenuItems() {
            return mOverflowOnly ? mMenu.getNonActionItems() :
                    mMenu.getVisibleItems();
        }

        @Override
        public int getCount() {
            int count = 0;
            MenuItem expandedItem = mMenu.getExpandedItem();
            for (MenuItem item : getMenuItems()) {
                if (item != expandedItem) {
                    count++;
                    MenuBuilder subMenu = (MenuBuilder) item.getSubMenu();
                    if (subMenu != null) {
                        count += subMenu.size();
                        // Since the menu structure can be set up at any point,
                        // this is the only place where the presenter can be set
                        // up for the submenus. There is no method to query
                        // whether the presenter has already been set up, so it
                        // will be removed and added each time.
                        subMenu.removeMenuPresenter(MenuPopupHelper.this);
                        subMenu.addMenuPresenter(MenuPopupHelper.this, mContext);
                    }
                }
            }
            return count;
        }

        @Override
        public MenuItem getItem(int position) {
            int count = getCount();
            if (position < 0 || position >= count) {
                throw new IndexOutOfBoundsException();
            }
            int index = 0;
            MenuItem expandedItem = mMenu.getExpandedItem();
            for (MenuItem item : getMenuItems()) {
                if (item != expandedItem) {
                    if (index++ == position) return item;
                    Menu subMenu = item.getSubMenu();
                    if (subMenu != null) {
                        int subMenuCount = subMenu.size();
                        if (position < index + subMenuCount) {
                            return subMenu.getItem(position - index);
                        }
                        index += subMenuCount;
                    }
                }
            }
            throw new IllegalStateException();
        }

        @Override
        public long getItemId(int position) {
            // Since a menu item's ID is optional, we'll use the position as an
            // ID for the item in the AdapterView
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return getItemType(position).ordinal();
        }

        private ItemType getItemType(int position) {
            int count = getCount();
            if (position < 0 || position >= count) {
                throw new IndexOutOfBoundsException();
            }
            int index = 0;
            MenuItem expandedItem = mMenu.getExpandedItem();
            for (MenuItem item : getMenuItems()) {
                if (item != expandedItem) {
                    Menu subMenu = item.getSubMenu();
                    if (index++ == position) {
                        return subMenu == null ?
                                ItemType.ITEM : ItemType.HEADER;
                    }
                    if (subMenu != null) {
                        int subMenuCount = subMenu.size();
                        if (position < index + subMenuCount) {
                            return ItemType.ITEM;
                        }
                        index += subMenuCount;
                    }
                }
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemType(position).mIsEnabled;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            ItemType itemType = getItemType(position);
            if (convertView != null) {
                textView = (TextView) convertView;
            } else {
                textView = (TextView) mInflater.inflate(itemType.mLayoutRes, parent, false);
            }
            MenuItem item = getItem(position);
            textView.setText(item.getTitle());
            if (textView instanceof Checkable) {
                ((Checkable) textView).setChecked(item.isChecked());
            }
            Drawable icon = item.getIcon();
            if (icon != null) {
                textView.setCompoundDrawablePadding(mPopupIconPadding);
                int iconWidth = icon.getIntrinsicWidth();
                int iconHeight = icon.getIntrinsicHeight();
                if (iconWidth < 0 || iconHeight < 0) {
                    iconWidth = iconHeight = mPopupIconDefaultSize;
                }
                icon.setBounds(0, 0, iconWidth, iconHeight);
                TextViewCompat.setCompoundDrawablesRelative(textView, icon, null, null, null);
            }
            if (mPopupPaddingStart == Integer.MIN_VALUE && mPopupPaddingEnd == Integer.MIN_VALUE) {
                textView.setPadding(
                        mPopupPaddingLeft, mPopupItemVerticalPadding,
                        mPopupPaddingRight, mPopupItemVerticalPadding);
            } else {
                textView.setPaddingRelative(
                        mPopupPaddingStart, mPopupItemVerticalPadding,
                        mPopupPaddingEnd, mPopupItemVerticalPadding);
            }
            return textView;
        }
    }
}
