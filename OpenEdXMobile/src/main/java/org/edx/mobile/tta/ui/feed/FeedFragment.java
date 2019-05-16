package org.edx.mobile.tta.ui.feed;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TFragmentFeedBinding;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.feed.view_model.FeedViewModel;
import org.edx.mobile.tta.utils.BreadcrumbUtil;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends TaBaseFragment {
    public static final String TAG = FeedFragment.class.getCanonicalName();
    private static final int RANK = 2;

    private FeedViewModel viewModel;

    private PagerAdapter pagerAdapter;
    private List<Content> featuredContents;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new FeedViewModel(getActivity(), this);
        featuredContents = new ArrayList<>();
        pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return featuredContents.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
                container.removeView((View) view);
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                View view = LayoutInflater.from(getActivity())
                        .inflate(R.layout.t_row_slider_item, container, false);
                ImageView imageView = view.findViewById(R.id.slider_image);
                Glide.with(getActivity())
                        .load(featuredContents.get(position).getIcon())
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(imageView);
                container.addView(view);
                view.setOnClickListener(v -> {
                    viewModel.showContentDashboard(featuredContents.get(position));
                });
                return view;
            }
        };

        viewModel.getFeatureList(new OnResponseCallback<List<Content>>() {
            @Override
            public void onSuccess(List<Content> data) {
                featuredContents.addAll(data);
                pagerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewDataBinding binding = binding(inflater, container, R.layout.t_fragment_feed, viewModel);

        if (binding instanceof TFragmentFeedBinding){
            TFragmentFeedBinding feedBinding = (TFragmentFeedBinding) binding;
            feedBinding.featuredContentSlider.contentViewPager.setAdapter(pagerAdapter);
            feedBinding.featuredContentSlider.contentTabLayout.setupWithViewPager(
                    feedBinding.featuredContentSlider.contentViewPager);
        }

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.feed.name()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
