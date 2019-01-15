package org.edx.mobile.tta.ui.library.view_model;

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
import com.maurya.mx.mxlib.core.MxBaseAdapter;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowContentBinding;
import org.edx.mobile.databinding.TRowContentListBinding;
import org.edx.mobile.databinding.TRowContentSliderBinding;
import org.edx.mobile.tta.data.enums.ContentListType;
import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.Source;
import org.edx.mobile.tta.data.model.ConfigurationResponse;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.local.db.table.ContentList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListingTabViewModel extends BaseViewModel {

    private ConfigurationResponse cr;
    private Category category;
    private List<ContentList> contentLists;
    private List<ContentList> emptyLists;

    private Map<Long, List<Content>> contentListMap;
    private Map<Long, Source> sourceMap;

    public ListingRecyclerAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public ListingTabViewModel(Context context, TaBaseFragment fragment, ConfigurationResponse cr, Category category, List<Content> contents) {
        super(context, fragment);
        this.cr = cr;
        this.category = category;

//        contentLists = cr.getList();
        contentLists = new ArrayList<>();
        for (ContentList list: cr.getList()){
            if (list.getCategory() == category.getId()){
                contentLists.add(list);
            }
        }
        Collections.sort(contentLists);
        contentListMap = new HashMap<>();
        sourceMap = new HashMap<>();
        setContents(contents);

        for (Source source: cr.getSource()){
            sourceMap.put(source.getId(), source);
        }

        adapter = new ListingRecyclerAdapter(mActivity);
        adapter.addAll(contentLists);
        layoutManager = new LinearLayoutManager(mActivity);
    }

    private void setContents(List<Content> allContents){

        for (Content content: allContents){
            if (content.getSource() == category.getSource() || category.getSource() == -1) {
                for (Long listId: content.getLists()){
                    if (contentListMap.containsKey(listId)){
                        contentListMap.get(listId).add(content);
                    } else {
                        List<Content> l = new ArrayList<>();
                        l.add(content);
                        contentListMap.put(listId, l);
                    }
                }
            }
        }

        emptyLists = new ArrayList<>();
        for (ContentList list: contentLists){
            if (!contentListMap.containsKey(list.getId())){
                emptyLists.add(list);
            }
        }

        for (ContentList list: emptyLists){
            contentLists.remove(list);
        }

    }

    public class ListingRecyclerAdapter extends MxBaseAdapter<ContentList> {
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
                        return contentListMap.get(model.getId()).size();
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
                        Glide.with(mActivity).load(contentListMap.get(model.getId()).get(position).getIcon()).into(imageView);
                        container.addView(view);
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(mActivity, contentListMap.get(model.getId()).get(position).getName(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        return view;
                    }
                });
                sliderBinding.contentTabLayout.setupWithViewPager(sliderBinding.contentViewPager);

            } else if (binding instanceof TRowContentListBinding){

                TRowContentListBinding listBinding = (TRowContentListBinding) binding;
                ContentListAdapter listAdapter = new ContentListAdapter(mActivity);
                listAdapter.addAll(contentListMap.get(model.getId()));
                listAdapter.setItemClickListener(new OnRecyclerItemClickListener<Content>() {
                    @Override
                    public void onItemClick(View view, Content item) {
                        Toast.makeText(mActivity, item.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
                listBinding.contentFiniteList.setTitleText(model.getName());
                listBinding.contentFiniteList.setOnMoreButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mActivity, "View more of " + model.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
                listBinding.contentFiniteList.setAdapter(listAdapter);

            }
        }

        @Override
        public int getItemLayout(int position) {
            if (getItem(position).getFormat_type().equals(ContentListType.feature.toString())){
                return R.layout.t_row_content_slider;
            } else {
                return R.layout.t_row_content_list;
            }
        }
    }

    public class ContentListAdapter extends MxFiniteAdapter<Content> {

        public ContentListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Content model, @Nullable OnRecyclerItemClickListener<Content> listener) {
            if (binding instanceof TRowContentBinding){
                TRowContentBinding contentBinding = (TRowContentBinding) binding;
                contentBinding.contentCategory.setText(sourceMap.get(model.getSource()).getName());
                contentBinding.contentTitle.setText(model.getName());
                Glide.with(mActivity).load(model.getIcon()).into(contentBinding.contentImage);
                contentBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}
