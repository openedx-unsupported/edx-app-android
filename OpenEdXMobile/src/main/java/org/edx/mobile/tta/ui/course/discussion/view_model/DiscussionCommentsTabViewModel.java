package org.edx.mobile.tta.ui.course.discussion.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowDiscussionCommentBinding;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.enums.SortType;
import org.edx.mobile.tta.event.LoadMoreDiscussionCommentsEvent;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.course.discussion.DiscussionCommentActivity;
import org.edx.mobile.tta.ui.interfaces.DiscussionCommentClickListener;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;

public class DiscussionCommentsTabViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_PAGE = 1;

    public DiscussionCommentsAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    private EnrolledCoursesResponse course;
    private DiscussionTopic topic;
    private DiscussionThread thread;
    private DiscussionCommentClickListener commentClickListener;
    private List<DiscussionComment> comments;
    private SortType sortType;

    public ObservableBoolean emptyVisible = new ObservableBoolean();

    private int take, page;
    private boolean allLoaded;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.page++;
//        fetchComments();
        EventBus.getDefault().post(new LoadMoreDiscussionCommentsEvent());
        return true;
    };

    public DiscussionCommentsTabViewModel(Context context, TaBaseFragment fragment,
                                          EnrolledCoursesResponse course, DiscussionTopic topic, DiscussionThread thread,
                                          DiscussionCommentClickListener listener,
                                          List<DiscussionComment> comments, SortType sortType) {
        super(context, fragment);
        this.course = course;
        this.topic = topic;
        this.thread = thread;
        this.commentClickListener = listener;
        this.comments = comments;
        this.sortType = sortType;
        if (this.comments == null){
            this.comments = new ArrayList<>();
        }
        sortComments();
        take = DEFAULT_TAKE;
        page = DEFAULT_PAGE;
        allLoaded = false;

        adapter = new DiscussionCommentsAdapter(mActivity);
        adapter.setItems(this.comments);
        adapter.setItemClickListener((view, item) -> {
            switch (view.getId()){
                case R.id.comment_like_layout:
                    if (commentClickListener != null){
                        commentClickListener.onClickLike(item);
                    }
                    break;

                case R.id.comment_reply_layout:
                    if (commentClickListener != null){
                        commentClickListener.onClickReply(item);
                    }
                    break;

                case R.id.user_name:
                case R.id.user_image:
                    if (commentClickListener != null){
                        commentClickListener.onClickUser(item);
                    }
                    break;

                default:
                    if (commentClickListener != null){
                        commentClickListener.onClickDefault(item);
                    }
                    break;
            }
        });

//        fetchComments();
    }

    private void fetchComments() {

        final List<String> requestedFields = Collections.singletonList(
                DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue());

        mDataManager.getThreadComments(thread.getIdentifier(), take, page, requestedFields,
                thread.getType() == DiscussionThread.ThreadType.QUESTION,
                new OnResponseCallback<List<DiscussionComment>>() {
                    @Override
                    public void onSuccess(List<DiscussionComment> data) {
                        mActivity.hideLoading();
                        if (data.size() < take){
                            allLoaded = true;
                        }
                        populateComments(data);
                        adapter.setLoadingDone();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        adapter.setLoadingDone();
                        toggleEmptyVisibility();
                    }
                });

    }

    private void populateComments(List<DiscussionComment> data) {
        boolean newItemsAdded = false;
        for (DiscussionComment comment: data){
            if (!comments.contains(comment)){
                comments.add(comment);
                newItemsAdded = true;
            }
        }
        if (newItemsAdded) {
            adapter.notifyDataSetChanged();
        }
        toggleEmptyVisibility();
    }

    public void setLoaded(){
        allLoaded = true;
        adapter.setLoadingDone();
    }

    public void refreshList(){
        sortComments();
        adapter.notifyDataSetChanged();
        adapter.setLoadingDone();
        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility(){
        if (comments == null || comments.isEmpty()){
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    private void sortComments() {

        switch (sortType){
            case recent:

                if (comments != null){
                    Collections.sort(comments, (o1, o2) -> o2.getUpdatedAt().compareTo(o1.getUpdatedAt()));
                }

                break;

            case relevant:

                if (comments != null){
                    Collections.sort(comments, (o1, o2) -> o2.getVoteCount() - o1.getVoteCount());
                }

                break;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
    }

    public class DiscussionCommentsAdapter extends MxInfiniteAdapter<DiscussionComment> {
        public DiscussionCommentsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull DiscussionComment model, @Nullable OnRecyclerItemClickListener<DiscussionComment> listener) {
            if (binding instanceof TRowDiscussionCommentBinding){
                TRowDiscussionCommentBinding commentBinding = (TRowDiscussionCommentBinding) binding;
                commentBinding.setViewModel(model);

                String name = model.getAuthorDisplayName();
                if (name == null){
                    name = mDataManager.getLoginPrefs().getDisplayName();
                }
                commentBinding.userName.setText(name);

                ProfileImage profileImage = model.getProfileImage();
                if (profileImage == null){
                    profileImage = mDataManager.getLoginPrefs().getProfileImage();
                }
                if (profileImage != null) {
                    Glide.with(getContext())
                            .load(profileImage.getImageUrlMedium())
                            .placeholder(R.drawable.profile_photo_placeholder)
                            .into(commentBinding.roundedUserImage);
                } else {
                    commentBinding.roundedUserImage.setImageResource(R.drawable.profile_photo_placeholder);
                }

                commentBinding.date.setText(DateUtil.getDisplayTime(model.getUpdatedAt()));

                commentBinding.commentLikeImage.setImageResource(
                        model.isVoted() ? R.drawable.t_icon_like_filled : R.drawable.t_icon_like
                );

                commentBinding.commentLikeLayout.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                commentBinding.commentReplyLayout.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                commentBinding.userImage.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                commentBinding.userName.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                commentBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}
