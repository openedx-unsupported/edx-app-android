package org.edx.mobile.tta.ui.course.discussion.view_model;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowDiscussionThreadBinding;
import org.edx.mobile.databinding.TRowDiscussionTopicBinding;
import org.edx.mobile.discussion.DiscussionPostsSort;
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionTopicDepth;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.course.discussion.DiscussionTopicFragment;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseDiscussionViewModel extends BaseViewModel {

    private Content content;
    private EnrolledCoursesResponse course;
    private Map<String, List<DiscussionThread>> topicThreadsMap;
    private List<DiscussionTopicDepth> topics;

    public DiscussionTopicsAdapter topicsAdapter;
    public RecyclerView.LayoutManager topicsLayoutManager;

    public CourseDiscussionViewModel(Context context, TaBaseFragment fragment, Content content, EnrolledCoursesResponse course) {
        super(context, fragment);
        this.content = content;
        this.course = course;

        topicsAdapter = new DiscussionTopicsAdapter(mActivity);
        topicsAdapter.setItemClickListener((view, item) -> {
            switch (view.getId()){
                case R.id.see_more_btn:

                    break;
            }
        });

        fetchDiscussionTopics();
    }

    @Override
    public void onResume() {
        super.onResume();
        topicsLayoutManager = new LinearLayoutManager(mActivity);
    }

    private void fetchDiscussionTopics() {
        mActivity.showLoading();

        if (course != null) {
            mDataManager.getDiscussionTopics(course.getCourse().getId(), new OnResponseCallback<List<DiscussionTopicDepth>>() {
                @Override
                public void onSuccess(List<DiscussionTopicDepth> data) {
                    topics = data;

                    List<String> topicIds = new ArrayList<>();
                    for (DiscussionTopicDepth topic : data) {
                        topicIds.add(topic.getDiscussionTopic().getIdentifier());
                    }

                    mDataManager.getDiscussionThreads(course.getCourse().getId(), topicIds, null,
                            DiscussionPostsSort.LAST_ACTIVITY_AT.name().toLowerCase(), 3, 1,
                            Collections.singletonList(DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue()),
                            new OnResponseCallback<List<DiscussionThread>>() {
                                @Override
                                public void onSuccess(List<DiscussionThread> data) {
                                    mActivity.hideLoading();

                                    if (topicThreadsMap == null){
                                        topicThreadsMap = new HashMap<>();
                                    }
                                    for (DiscussionThread thread: data){
                                        if (topicThreadsMap.containsKey(thread.getTopicId())){
                                            topicThreadsMap.get(thread.getTopicId()).add(thread);
                                        } else {
                                            List<DiscussionThread> threads = new ArrayList<>();
                                            threads.add(thread);
                                            topicThreadsMap.put(thread.getTopicId(), threads);
                                        }
                                    }

                                    populateTopicsList();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    mActivity.hideLoading();
                                    populateTopicsList();
                                }
                            });
                }

                @Override
                public void onFailure(Exception e) {
                    mActivity.hideLoading();
                    mActivity.showLongSnack(e.getLocalizedMessage());
                }
            });
        }

    }

    private void populateTopicsList() {
        topicsAdapter.setItems(topics);
    }

    public class DiscussionTopicsAdapter extends MxInfiniteAdapter<DiscussionTopicDepth> {
        public DiscussionTopicsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull DiscussionTopicDepth model, @Nullable OnRecyclerItemClickListener<DiscussionTopicDepth> listener) {
            if (binding instanceof TRowDiscussionTopicBinding) {
                TRowDiscussionTopicBinding topicBinding = (TRowDiscussionTopicBinding) binding;
                topicBinding.setViewModel(model.getDiscussionTopic());

                topicBinding.seeMoreBtn.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                if (topicThreadsMap.containsKey(model.getDiscussionTopic().getIdentifier())){
                    DiscussionThreadsAdapter threadsAdapter = new DiscussionThreadsAdapter(getContext());
                    threadsAdapter.setItemLayout(R.layout.t_row_discussion_thread);
                    threadsAdapter.setItems(topicThreadsMap.get(model.getDiscussionTopic().getIdentifier()));
                    threadsAdapter.setItemClickListener((view, item) -> {
                        mActivity.showShortSnack(item.getRawBody());
                    });
                    topicBinding.limitedThreadsList.setLayoutManager(new LinearLayoutManager(getContext()));
                    topicBinding.limitedThreadsList.setAdapter(threadsAdapter);
                }
            }
        }
    }

    public class DiscussionThreadsAdapter extends MxFiniteAdapter<DiscussionThread> {
        /**
         * Base constructor.
         * Allocate adapter-related objects here if needed.
         *
         * @param context Context needed to retrieve LayoutInflater
         */
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
