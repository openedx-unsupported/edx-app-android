package org.edx.mobile.tta.ui.connect.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowCommentsBinding;
import org.edx.mobile.databinding.TRowConnectReplyBinding;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.event.CommentRepliesReceivedEvent;
import org.edx.mobile.tta.event.FetchCommentRepliesEvent;
import org.edx.mobile.tta.event.LoadMoreConnectCommentsEvent;
import org.edx.mobile.tta.event.RepliedOnCommentEvent;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.interfaces.CommentClickListener;
import org.edx.mobile.tta.wordpress_client.model.Comment;
import org.edx.mobile.tta.wordpress_client.model.Post;
import org.edx.mobile.util.DateUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class ConnectCommentsTabViewModel extends BaseViewModel {

    private Content content;
    private Post post;
    private List<Comment> comments;
    private Map<Long, List<Comment>> repliesMap;

    public CommentsAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;
    private CommentClickListener commentClickListener;

    public ObservableBoolean emptyVisible = new ObservableBoolean();

    private boolean allLoaded;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        EventBus.getDefault().post(new LoadMoreConnectCommentsEvent());
        return true;
    };

    public ConnectCommentsTabViewModel(Context context, TaBaseFragment fragment, Content content, Post post, List<Comment> comments, Map<Long, List<Comment>> repliesMap, CommentClickListener commentClickListener) {
        super(context, fragment);
        this.content = content;
        this.post = post;
        this.comments = comments;
        this.repliesMap = repliesMap;
        this.commentClickListener = commentClickListener;
        allLoaded = false;
        adapter = new CommentsAdapter(mActivity);
        setListeners();
        layoutManager = new LinearLayoutManager(mActivity);
        adapter.setItems(comments);
    }

    private void setListeners() {

        adapter.setItemClickListener((view, item) -> {
            switch (view.getId()){
                case R.id.comment_like_layout:
                case R.id.reply_comment_like_layout:
                    if (commentClickListener != null){
                        commentClickListener.onClickLike(item);
                    }
                    break;

                case R.id.comment_reply_layout:
                    if (commentClickListener != null){
                        commentClickListener.onClickReply(item);
                    }
                    break;

                case R.id.user_image:
                case R.id.user_name:
                case R.id.reply_user_image:
                case R.id.reply_user_name:
                    if (commentClickListener != null){
                        commentClickListener.onClickUser(item);
                    }
                    break;

                case R.id.view_replies_text:
                    int position = adapter.getItemPosition(item);
                    if (adapter.expandedPositions.contains(position)) {
                        adapter.expandedPositions.remove(Integer.valueOf(position));
                    } else {
                        adapter.expandedPositions.add(position);
                    }
                    adapter.notifyItemChanged(position);

                    if (!repliesMap.containsKey(item.getId())){
                        EventBus.getDefault().post(new FetchCommentRepliesEvent(item));
                    }
                    break;

                default:
                    if (commentClickListener != null){
                        commentClickListener.onClickDefault(item);
                    }
                    break;
            }
        });

    }

    public void setLoaded(){
        allLoaded = true;
        adapter.setLoadingDone();
    }

    public void refreshList(){
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

    @SuppressWarnings("unused")
    public void onEventMainThread(CommentRepliesReceivedEvent event){
        Comment comment = event.getComment();
        int position = adapter.getItemPosition(comment);
        if (!repliesMap.containsKey(comment.getId())){
            adapter.expandedPositions.remove(Integer.valueOf(position));
            mActivity.showLongSnack("Replies are not available on this comment");
        }
        adapter.notifyItemChanged(position);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(RepliedOnCommentEvent event){
        adapter.notifyItemChanged(adapter.getItemPosition(event.getComment()));
    }

    public void registerEventBus(){
        EventBus.getDefault().register(this);
    }

    public void unregisterEvnetBus(){
        EventBus.getDefault().unregister(this);
    }

    public class CommentsAdapter extends MxInfiniteAdapter<Comment> {

        private List<Integer> expandedPositions;

        public CommentsAdapter(Context context) {
            super(context);
            expandedPositions = new ArrayList<>();
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Comment model, @Nullable OnRecyclerItemClickListener<Comment> listener) {
            if (binding instanceof TRowCommentsBinding){
                TRowCommentsBinding commentsBinding = (TRowCommentsBinding) binding;

                if (expandedPositions.contains(getItemPosition(model))){
                    commentsBinding.repliesFrame.setVisibility(View.VISIBLE);

                    if (repliesMap.containsKey(model.getId())){
                        commentsBinding.repliesProgressbar.setVisibility(View.GONE);
                        commentsBinding.repliesList.setVisibility(View.VISIBLE);

                        RepliesAdapter repliesAdapter = new RepliesAdapter(getContext());
                        repliesAdapter.setItemLimit(100);
                        repliesAdapter.setItemLayout(R.layout.t_row_connect_reply);
                        repliesAdapter.setItems(repliesMap.get(model.getId()));
                        repliesAdapter.setItemClickListener(listener);
                        commentsBinding.repliesList.setLayoutManager(new LinearLayoutManager(getContext()));
                        commentsBinding.repliesList.setAdapter(repliesAdapter);

                    } else {
                        commentsBinding.repliesList.setVisibility(View.GONE);
                        commentsBinding.repliesProgressbar.setVisibility(View.VISIBLE);
                    }
                } else {
                    commentsBinding.repliesFrame.setVisibility(View.GONE);
                }

                Glide.with(commentsBinding.userImage.getContext())
                        .load(content.getIcon())
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(commentsBinding.roundedUserImage);

                commentsBinding.userName.setText(model.getAuthorName());
                commentsBinding.date.setText(DateUtil.getDisplayTime(model.getDate()));
                commentsBinding.comment.setText(Html.fromHtml(model.getContent().getRendered()));
                commentsBinding.commentLikes.setText("80");
                commentsBinding.commentReplies.setText("5");

                commentsBinding.commentLikeLayout.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                commentsBinding.commentReplyLayout.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                commentsBinding.userImage.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                commentsBinding.userName.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                commentsBinding.viewRepliesText.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                commentsBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

            }
        }
    }

    public class RepliesAdapter extends MxFiniteAdapter<Comment> {
        /**
         * Base constructor.
         * Allocate adapter-related objects here if needed.
         *
         * @param context Context needed to retrieve LayoutInflater
         */
        public RepliesAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Comment model, @Nullable OnRecyclerItemClickListener<Comment> listener) {
            if (binding instanceof TRowConnectReplyBinding){
                TRowConnectReplyBinding replyBinding = (TRowConnectReplyBinding) binding;
                replyBinding.setViewModel(model);
                replyBinding.replyComment.setText(Html.fromHtml(model.getContent().getRendered()));
                replyBinding.replyDate.setText(DateUtil.getDisplayTime(model.getDate()));

                Glide.with(getContext())
                        .load(content.getIcon())
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(replyBinding.replyRoundedUserImage);

                replyBinding.replyCommentLikeLayout.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                replyBinding.replyUserImage.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                replyBinding.replyUserName.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}
