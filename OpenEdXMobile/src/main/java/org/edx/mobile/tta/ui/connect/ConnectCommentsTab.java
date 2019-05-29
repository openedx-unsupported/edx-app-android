package org.edx.mobile.tta.ui.connect;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.connect.view_model.ConnectCommentsTabViewModel;
import org.edx.mobile.tta.ui.interfaces.CommentClickListener;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.tta.wordpress_client.model.Comment;
import org.edx.mobile.tta.wordpress_client.model.Post;

import java.util.List;
import java.util.Map;

public class ConnectCommentsTab extends TaBaseFragment {
    private int RANK;

    private ConnectCommentsTabViewModel viewModel;

    private Content content;
    private Post post;
    private List<Comment> comments;
    private Map<Long, List<Comment>> repliesMap;
    private CommentClickListener commentClickListener;
    private Nav nav;

    public static ConnectCommentsTab newInstance(Content content, Post post, List<Comment> comments,
                                                 Map<Long, List<Comment>> repliesMap, Nav nav,
                                                 CommentClickListener commentClickListener){
        ConnectCommentsTab tab = new ConnectCommentsTab();
        tab.content = content;
        tab.post = post;
        tab.comments = comments;
        tab.repliesMap = repliesMap;
        tab.commentClickListener = commentClickListener;
        tab.nav = nav;
        tab.RANK = BreadcrumbUtil.getCurrentRank() + 1;
        return tab;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ConnectCommentsTabViewModel(getActivity(), this, content, post, comments, repliesMap, commentClickListener);
        viewModel.registerEventBus();
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

    public void setLoaded(){
        if (viewModel != null){
            viewModel.setLoaded();
        }
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, nav.name()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unregisterEvnetBus();
    }
}
