package org.edx.mobile.tta.binding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.lang.reflect.Constructor;


/**
 * Created by Arjun on 2018/9/18.
 */

public class BindingAdapters {
    @BindingAdapter({"android:src"})
    public static void imageSrcLoader(ImageView imageView, int id) {
        imageView.setImageResource(id);
    }

    @BindingAdapter({"android:background"})
    public static void backgroundLoader(View view, int id) {
        Context context = view.getContext();
        Drawable drawable = ContextCompat.getDrawable(context, id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    @BindingAdapter({"android:visibility"})
    public static void visibility(View view, int visibility) {
        view.setVisibility(visibility);
    }

    @BindingAdapter({"android:if"})
    public static void setViewShowOrGone(View view, Boolean show) {
        if (show) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @BindingAdapter({"android:show"})
    public static void setViewShowOrHide(View view, Boolean show) {
        if (show) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @BindingAdapter("android:layout_marginTop")
    public static void setLayoutMarginTop(View view, float margin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
        if (lp != null) {
            lp.setMargins(lp.leftMargin, (int)margin, lp.rightMargin, lp.bottomMargin);
            view.setLayoutParams(lp);
        }
    }

    @BindingAdapter("android:layout_marginLeft")
    public static void setLayoutMarginLeft(View view, float margin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
        if (lp != null) {
            lp.setMargins((int)margin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            view.setLayoutParams(lp);
        }
    }

    @BindingAdapter("android:layout_marginBottom")
    public static void setLayoutMarginBottom(View view, float margin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
        if (lp != null) {
            lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, (int)margin);
            view.setLayoutParams(lp);
        }
    }

    @BindingAdapter("android:layout_marginRight")
    public static void setLayoutMarginRight(View view, float margin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
        if (lp != null) {
            lp.setMargins(lp.leftMargin, lp.topMargin, (int)margin, lp.bottomMargin);
            view.setLayoutParams(lp);
        }
    }

    @BindingAdapter("android:layout_width")
    public static void setLayoutWidth(View view, int width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_height")
    public static void setLayoutHeight(View view, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter({"android:src", "android:placeholder"})
    public static void setImageSrcFromUrl(ImageView imageView, String url, int placeholder) {
        if (url == null) {
            return;
        }

        Glide.with(imageView.getContext())
            .load(url)
            .placeholder(placeholder)
            .into(imageView);
    }

    @BindingAdapter({"android:src"})
    public static void setImageSrcFromUrl(ImageView imageView, String url) {
        if (url == null) {
            return;
        }

        Glide.with(imageView.getContext())
            .load(url)
            .into(imageView);
    }

    @BindingAdapter({"android:widthScale", "android:heightScale"})
    public static void setViewRatio(View view, int widthScale, int heightScale) {
        view.post(() -> {
            int width = view.getWidth();
            Class<?> cls = view.getLayoutParams().getClass();
            Constructor<?> constructor = null;
            try {
                constructor = cls.getConstructor(int.class, int.class);
                view.setLayoutParams((ViewGroup.LayoutParams) constructor.newInstance(width,  width * heightScale / widthScale));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @BindingAdapter({"android:widthScale", "android:heightScale"})
    public static void setViewGroupRatio(ViewGroup viewGroup, int widthScale, int heightScale) {
        viewGroup.post(() -> {
            int width = viewGroup.getWidth();
            Class<?> cls = viewGroup.getLayoutParams().getClass();
            Constructor<?> constructor = null;
            try {
                constructor = cls.getConstructor(int.class, int.class);
                viewGroup.setLayoutParams((ViewGroup.LayoutParams) constructor.newInstance(width, width * heightScale / widthScale));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @BindingAdapter({"android:onKeyEvent"})
    public static void onKeyEvent(View view, View.OnKeyListener onKeyListener) {
        view.setOnKeyListener(onKeyListener);
    }

    @BindingAdapter("android:onEditorAction")
    public static void onEditorAction(TextView view, TextView.OnEditorActionListener onEditorActionListener) {
        view.setOnEditorActionListener(onEditorActionListener);
    }
}
