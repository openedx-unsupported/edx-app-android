package org.edx.mobile.tta.ui.connect;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.connect.view_model.ConnectCommentsTabViewModel;
import org.edx.mobile.tta.ui.interfaces.CommentClickListener;
import org.edx.mobile.tta.wordpress_client.model.Comment;
import org.edx.mobile.tta.wordpress_client.model.Post;

import java.util.List;

public class ConnectCommentsTab extends TaBaseFragment {

    private ConnectCommentsTabViewModel viewModel;

    private Content content;
    private Post post;
    private List<Comment> comments;
    private CommentClickListener commentClickListener;

    public static ConnectCommentsTab newInstance(Content content, Post post, List<Comment> comments,
                                                 CommentClickListener commentClickListener){
        ConnectCommentsTab tab = new ConnectCommentsTab();
        tab.content = content;
        tab.post = post;
        tab.comments = comments;
        tab.commentClickListener = commentClickListener;
        return tab;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ConnectCommentsTabViewModel(getActivity(), this, content, post, comments, commentClickListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_comments_tab, viewModel)
                .getRoot();

        return view;
    }

    public void refreshList() {
        if (viewModel != null) {
            viewModel.refreshList();
        }
    }
}
