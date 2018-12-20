package org.edx.mobile.tta.ui.listing.view_model;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowContentBinding;
import org.edx.mobile.databinding.TRowContentListBinding;
import org.edx.mobile.databinding.TRowContentSliderBinding;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseRecyclerAdapter;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.interfaces.OnRecyclerItemClickListener;
import org.edx.mobile.tta.ui.listing.model.Content;
import org.edx.mobile.tta.ui.listing.model.ContentList;

import java.util.ArrayList;
import java.util.List;

public class ListingTabViewModel extends BaseViewModel {

    private List<Content> contents;
    private List<ContentList> contentLists;
    public ListingRecyclerAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public ListingTabViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
        contents = new ArrayList<>();
        contents.add(new Content("Content 1", null, "Category"));
        contents.add(new Content("Content 2", null, "Category"));
        contents.add(new Content("Content 3", null, "Category"));
        contents.add(new Content("Content 4", null, "Category"));
        contents.add(new Content("Content 5", null, "Category"));
        contents.add(new Content("Content 6", null, "Category"));
        contents.add(new Content("Content 7", null, "Category"));
        contents.add(new Content("Content 8", null, "Category"));
        contents.add(new Content("Content 9", null, "Category"));
        contents.add(new Content("Content 10", null, "Category"));

        contentLists = new ArrayList<>();
        contentLists.add(new ContentList("Slider list"));
        contentLists.add(new ContentList("Content List 1"));
        contentLists.add(new ContentList("Content List 2"));
        contentLists.add(new ContentList("Content List 3"));
        contentLists.add(new ContentList("Content List 4"));
        contentLists.add(new ContentList("Content List 5"));

        adapter = new ListingRecyclerAdapter(mActivity);
        adapter.addAll(contentLists);
        layoutManager = new LinearLayoutManager(mActivity);
    }

    public class ListingRecyclerAdapter extends BaseRecyclerAdapter<ContentList> {
        public ListingRecyclerAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ContentList model, @Nullable OnRecyclerItemClickListener<ContentList> listener) {
            if (binding instanceof TRowContentSliderBinding){

                TRowContentSliderBinding sliderBinding = (TRowContentSliderBinding) binding;
                sliderBinding.contentViewPager.setAdapter(new PagerAdapter() {
                    @Override
                    public int getCount() {
                        return contents.size();
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
                        View view = LayoutInflater.from(mActivity)
                                .inflate(R.layout.t_row_slider_item, container, false);
                        ImageView imageView = view.findViewById(R.id.slider_image);
                        Glide.with(mActivity).load(R.drawable.slider_image).into(imageView);
                        container.addView(view);
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(mActivity, contents.get(position).getName(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        return view;
                    }
                });
                sliderBinding.contentTabLayout.setupWithViewPager(sliderBinding.contentViewPager);

            } else if (binding instanceof TRowContentListBinding){

                TRowContentListBinding listBinding = (TRowContentListBinding) binding;
                listBinding.contentListTitle.setText(model.getTitle());
                ContentListAdapter listAdapter = new ContentListAdapter(mActivity);
                listAdapter.addAll(contents);
                listAdapter.setItemClickListener(new OnRecyclerItemClickListener<Content>() {
                    @Override
                    public void onItemClick(View view, Content item) {
                        Toast.makeText(mActivity, item.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
                listBinding.contentFiniteList.setLayoutManager(
                        new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
                listBinding.contentFiniteList.setAdapter(listAdapter);

            }
        }

        @Override
        public int getItemLayout(int position) {
            if (position == 0){
                return R.layout.t_row_content_slider;
            } else {
                return R.layout.t_row_content_list;
            }
        }
    }

    public class ContentListAdapter extends BaseRecyclerAdapter<Content>{

        public ContentListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Content model, @Nullable OnRecyclerItemClickListener<Content> listener) {
            if (binding instanceof TRowContentBinding){
                TRowContentBinding contentBinding = (TRowContentBinding) binding;
                contentBinding.contentCategory.setText(model.getCategory());
                contentBinding.contentTitle.setText(model.getName());
                Glide.with(mActivity).load(R.drawable.content_image).into(contentBinding.contentImage);
                contentBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(v, model);
                    }
                });
            }
        }

        @Override
        public int getItemLayout(int position) {
            return R.layout.t_row_content;
        }
    }
}
