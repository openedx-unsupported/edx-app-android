package org.edx.mobile.tta.ui.course.discussion.view_model;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowDiscussionThreadBinding;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionTopicDepth;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscussionTopicViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 20;
    private static final int DEFAULT_PAGE = 1;

    public DiscussionTopicDepth topicDepth;
    private EnrolledCoursesResponse course;
    private List<DiscussionThread> threads;

    public ObservableField<String> threadText = new ObservableField<>("");

    public DiscussionThreadsAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    private int take, page;
    private boolean allLoaded;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.page++;
        fetchThreads();
        return true;
    };

    public DiscussionTopicViewModel(Context context, TaBaseFragment fragment, EnrolledCoursesResponse course, DiscussionTopicDepth topicDepth) {
        super(context, fragment);
        this.course = course;
        this.topicDepth = topicDepth;
        threads = new ArrayList<>();
        take = DEFAULT_TAKE;
        page = DEFAULT_PAGE;
        allLoaded = false;

        adapter = new DiscussionThreadsAdapter(mActivity);
        adapter.setItemClickListener((view, item) -> {
            mActivity.showShortSnack(item.getRawBody());
        });
        adapter.setItems(threads);

        fetchThreads();
    }

    private void fetchThreads() {

        mDataManager.getDiscussionThreads(course.getCourse().getId(),
                Collections.singletonList(topicDepth.getDiscussionTopic().getIdentifier()),
                null, DiscussionPostsSort.LAST_ACTIVITY_AT.name().toLowerCase(), take, page,
                Collections.singletonList(DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue()),
                new OnResponseCallback<List<DiscussionThread>>() {
                    @Override
                    public void onSuccess(List<DiscussionThread> data) {
                        mActivity.hideLoading();
                        if (data.size() < take){
                            allLoaded = true;
                        }
                        populateThreads(data);
                        adapter.setLoadingDone();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });

    }

    private void populateThreads(List<DiscussionThread> data) {
        for (DiscussionThread thread: data){
            if (!threads.contains(thread)){
                threads.add(thread);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
    }

    public void startDiscussion(){
        mActivity.showShortSnack("Start discussion");
    }

    public class DiscussionThreadsAdapter extends MxInfiniteAdapter<DiscussionThread> {
        public DiscussionThreadsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull DiscussionThread model, @Nullable OnRecyclerItemClickListener<DiscussionThread> listener) {
            if (binding instanceof TRowDiscussionThreadBinding){
                TRowDiscussionThreadBinding threadBinding = (TRowDiscussionThreadBinding) binding;
                threadBinding.setViewModel(model);

                threadBinding.date.setText(DateUtil.getDisplayTime(model.getUpdatedAt()));
                Glide.with(getContext())
                        .load(model.getUsers().get(model.getAuthor()).getProfile().getImage().getImageUrlMedium())
                        .placeholder(R.drawable.profile_photo_placeholder)
                        .into(threadBinding.roundedUserImage);

                threadBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}
