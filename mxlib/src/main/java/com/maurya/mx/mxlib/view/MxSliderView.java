package com.maurya.mx.mxlib.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.R;

import java.util.ArrayList;


/**
 * Created by mukesh on 27/7/18.
 */

public class MxSliderView extends FrameLayout {
    private ViewPager mMxViewPager;
    private OnSliderViewClickListener mListener;
    private MxSliderPagerAdapter mAdapter;

    public MxSliderView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MxSliderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MxSliderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MxSliderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void addImage(String url) {
        mAdapter.addImage(url);

    }

    public void addImage(@DrawableRes int imageId) {
        mAdapter.addImage(imageId);
    }

    public void setOnSlideViewClickListener(OnSliderViewClickListener listener) {
        this.mListener = listener;
    }

    private void init(@NonNull Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.mx_slider_layout, this, true);
        mMxViewPager = (ViewPager) view.findViewById(R.id.mx_slider_pager);
        mAdapter = new MxSliderPagerAdapter(context);
        mMxViewPager.setAdapter(mAdapter);
    }

    public interface OnSliderViewClickListener {
        void onSliderViewClick(View view, int position);
    }

    class MxSliderPagerAdapter extends PagerAdapter {

        private ArrayList<Integer> imageIds;
        private ArrayList<String> imageUrls;
        private Context context;

        public MxSliderPagerAdapter(Context context) {
            this.context = context;
        }

        public void addImage(String url) {
            if (this.imageUrls == null)
                imageUrls = new ArrayList<>();
            this.imageUrls.add(url);
            notifyDataSetChanged();
        }

        public void addImage(@DrawableRes int imageId) {
            if (this.imageIds == null)
                imageIds = new ArrayList<>();
            this.imageIds.add(imageId);
            notifyDataSetChanged();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            LayoutInflater inflater = LayoutInflater.from(context);
            ViewGroup view = (ViewGroup) inflater.inflate(R.layout.mx_item_slider_layout, container, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.mx_slider_image);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onSliderViewClick(v, position);
                }
            });
            setImage(position, imageView);
            container.addView(view);
            return view;
        }

        private void setImage(int position, ImageView imageView) {
            if (imageUrls != null) {
                Glide.with(context).load(imageUrls.get(position)).into(imageView);
            } else if (imageIds != null) {
                Glide.with(context).load(imageIds.get(position)).into(imageView);
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }

        @Override
        public int getCount() {
            return imageUrls != null ? imageUrls.size() : (imageIds != null ? imageIds.size() : 0);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }
}
