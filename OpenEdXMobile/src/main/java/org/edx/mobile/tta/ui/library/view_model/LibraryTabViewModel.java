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
import org.edx.mobile.tta.data.enums.CategoryType;
import org.edx.mobile.tta.data.enums.ContentListType;
import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.model.CollectionConfigResponse;
import org.edx.mobile.tta.data.model.CollectionItemsResponse;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.local.db.table.ContentList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryTabViewModel extends BaseViewModel {

    private Category category;
    private List<ContentList> contentLists;

    private Map<Long, List<Content>> contentListMap;

    public ListingRecyclerAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public LibraryTabViewModel(Context context, TaBaseFragment fragment, CollectionConfigResponse cr, Category category) {
        super(context, fragment);
        this.category = category;

        contentLists = new ArrayList<>();
        for (ContentList list: cr.getContent_list()){
            if (list.getCategory_id() == category.getId()){
                contentLists.add(list);
            }
        }
        Collections.sort(contentLists);
        getContents();

        adapter = new ListingRecyclerAdapter(mActivity);
        layoutManager = new LinearLayoutManager(mActivity);
    }

    private void getContents(){

        Long[] listIds = new Long[contentLists.size()];
        for (int i = 0; i < contentLists.size(); i++){
            listIds[i] = contentLists.get(i).getId();
        }
        mDataManager.getCollectionItems(listIds, 0, 5,
                new OnResponseCallback<List<CollectionItemsResponse>>() {
                    @Override
                    public void onSuccess(List<CollectionItemsResponse> data) {
                        setContents(data);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.showShortSnack(e.getLocalizedMessage());
                    }
                });

    }

    private void setContents(List<CollectionItemsResponse> data){
        contentListMap = new HashMap<>();
        List<ContentList> emptyLists = new ArrayList<>();

        if (data != null){
            for (CollectionItemsResponse response: data){

                if (response.getContent() != null && !response.getContent().isEmpty()){
                    List<Content> contents = new ArrayList<>();

                    for (Content content: response.getContent()){
                        if (category.getName().equalsIgnoreCase(CategoryType.all.toString()) || content.getSource().getId() == category.getSource_id()){
                            contents.add(content);
                        }
                    }

                    if (contents.isEmpty()){
                        for (ContentList contentList: contentLists){
                            if (contentList.getId() == response.getId()){
                                emptyLists.add(contentList);
                                break;
                            }
                        }
                    } else {
                        contentListMap.put(response.getId(), contents);
                    }
                } else {

                    for (ContentList contentList: contentLists){
                        if (contentList.getId() == response.getId()){
                            emptyLists.add(contentList);
                            break;
                        }
                    }

                }
            }
        }
        for (ContentList contentList: emptyLists){
            contentLists.remove(contentList);
        }

        adapter.setItems(contentLists);
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
                        view.setOnClickListener(v ->
                                Toast.makeText(mActivity, contentListMap.get(model.getId()).get(position).getName(), Toast.LENGTH_SHORT).show());
                        return view;
                    }
                });
                sliderBinding.contentTabLayout.setupWithViewPager(sliderBinding.contentViewPager);

            } else if (binding instanceof TRowContentListBinding){

                TRowContentListBinding listBinding = (TRowContentListBinding) binding;
                ContentListAdapter listAdapter = new ContentListAdapter(mActivity);
                listAdapter.addAll(contentListMap.get(model.getId()));
                listAdapter.setItemClickListener((view, item) ->
                        Toast.makeText(mActivity, item.getName(), Toast.LENGTH_SHORT).show());
                listBinding.contentFiniteList.setTitleText(model.getName());
                if (listAdapter.getItemCount() > listBinding.contentFiniteList.getmMaxItem()) {
                    listBinding.contentFiniteList.setOnMoreButtonClickListener(v ->
                            Toast.makeText(mActivity, "View more of " + model.getName(), Toast.LENGTH_SHORT).show());
                } else {
                    listBinding.contentFiniteList.setmMoreButtonVisible(false);
                }
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
                contentBinding.contentCategory.setText(model.getSource().getTitle());
                contentBinding.contentTitle.setText(model.getName());
                Glide.with(mActivity).load(model.getIcon()).into(contentBinding.contentImage);
                contentBinding.getRoot().setOnClickListener(v -> listener.onItemClick(v, model));
            }
        }
    }
}
