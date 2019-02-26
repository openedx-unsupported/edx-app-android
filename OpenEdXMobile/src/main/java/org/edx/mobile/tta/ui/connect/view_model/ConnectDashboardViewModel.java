package org.edx.mobile.tta.ui.connect.view_model;

import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.edx.mobile.R;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.DownloadCompletedEvent;
import org.edx.mobile.module.storage.DownloadedVideoDeletedEvent;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.tta.data.enums.DownloadType;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.model.content.BookmarkResponse;
import org.edx.mobile.tta.data.model.content.TotalLikeResponse;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.connect.ConnectCommentsTab;
import org.edx.mobile.tta.ui.interfaces.CommentClickListener;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.wordpress_client.model.Comment;
import org.edx.mobile.tta.wordpress_client.model.Post;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ConnectDashboardViewModel extends BaseViewModel
    implements CommentClickListener {

    public ConnectPagerAdapter adapter;
    private List<Fragment> fragments;
    private List<String> titles;
    private ConnectCommentsTab tab1;
    private ConnectCommentsTab tab2;
    private ConnectCommentsTab tab3;

    public Content content;
    private Post post;
    private List<Comment> allComments;
    public ObservableField<String> comment = new ObservableField<>("");
    public ObservableBoolean commentFocus = new ObservableBoolean();

    //Header details
    public ObservableInt headerImagePlaceholder = new ObservableInt(R.drawable.placeholder_course_card_image);
    public ObservableInt likeIcon = new ObservableInt(R.drawable.t_icon_like);
    public ObservableInt bookmarkIcon = new ObservableInt(R.drawable.t_icon_bookmark);
    public ObservableInt allDownloadStatusIcon = new ObservableInt(R.drawable.t_icon_download);
    public ObservableBoolean allDownloadIconVisible = new ObservableBoolean(true);
    public ObservableBoolean allDownloadProgressVisible = new ObservableBoolean(false);
    public ObservableField<String> duration = new ObservableField<>("");
    public ObservableField<String> description = new ObservableField<>("");
    public ObservableField<String> likes = new ObservableField<>("0");

    public ConnectDashboardViewModel(BaseVMActivity activity, Content content) {
        super(activity);
        this.content = content;
        adapter = new ConnectPagerAdapter(mActivity.getSupportFragmentManager());
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
    }

    public void fetchPost(OnResponseCallback<Post> callback) {
        mActivity.showLoading();
        mDataManager.getPostById(Long.parseLong(content.getSource_identity()), new OnResponseCallback<Post>() {
            @Override
            public void onSuccess(Post data) {
                post = data;
                callback.onSuccess(data);
                loadData();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                callback.onFailure(e);
            }
        });

    }

    private void loadData() {

        mDataManager.getTotalLikes(content.getId(), new OnResponseCallback<TotalLikeResponse>() {
            @Override
            public void onSuccess(TotalLikeResponse data) {
                likes.set(String.valueOf(data.getLike_count()));
            }

            @Override
            public void onFailure(Exception e) {
                likes.set("");
            }
        });

        mDataManager.isLike(content.getId(), new OnResponseCallback<StatusResponse>() {
            @Override
            public void onSuccess(StatusResponse data) {
                //TODO: Need filled like icon
                likeIcon.set(data.getStatus() ? R.drawable.t_icon_like_filled : R.drawable.t_icon_like);
            }

            @Override
            public void onFailure(Exception e) {
                likeIcon.set(R.drawable.t_icon_like);
            }
        });

        mDataManager.isContentMyAgenda(content.getId(), new OnResponseCallback<StatusResponse>() {
            @Override
            public void onSuccess(StatusResponse data) {
                bookmarkIcon.set(data.getStatus() ? R.drawable.t_icon_bookmark_filled : R.drawable.t_icon_bookmark);
            }

            @Override
            public void onFailure(Exception e) {
                bookmarkIcon.set(R.drawable.t_icon_bookmark);
            }
        });

        getPostDownloadStatus();

        duration.set(mActivity.getString(R.string.duration) + "-01:00");

        description.set(
                "अकसर शिक्षक होने के नाते हम अपनी कक्षाओं को रोचक बनाने की चुनौतियों से जूझते हैं| हम अलग-अलग गतिविधियाँ अपनाते हैं ताकि बच्चे मनोरंजक तरीकों से सीख सकें| लेकिन ऐसा करना हमेशा आसान नहीं होता| यह कोर्स एक कोशिश है जहां हम ‘गतिविधि क्या है’, ‘कैसी गतिविधियाँ चुनी जायें?’ और इन्हें कराने में क्या-क्या मुश्किलें आ सकती हैं, के बारे में बात कर रहे हैं| इस कोर्स में इन पहलुओं को टटोलने के लिए प्राइमरी कक्षा के EVS (पर्यावरण विज्ञान) विषय के उदाहरण लिए गए हैं| \n" +
                        "\n" +
                        "इस कोर्स को पर्यावरण-विज्ञान पढ़ानेवाले शिक्षक और वे शिक्षक जो ‘गतिविधियों को कक्षा में कैसे कराया जाये’ जानना चाहते हैं, कर सकते हैं| आशा है इस कोर्स को पढ़ने के बाद आपके लिए कक्षा में गतिविधियाँ कराना आसान हो जाएगा|"
        );

        fetchComments();

    }

    private void getPostDownloadStatus() {

        switch (mDataManager.getPostDownloadStatus(post)){
            case not_downloaded:
                allDownloadProgressVisible.set(false);
                allDownloadStatusIcon.set(R.drawable.t_icon_download);
                allDownloadIconVisible.set(true);
                break;

            case downloading:
                allDownloadIconVisible.set(false);
                allDownloadProgressVisible.set(true);
                break;

            case downloaded:
            case watching:
            case watched:
                allDownloadProgressVisible.set(false);
                allDownloadStatusIcon.set(R.drawable.t_icon_done);
                allDownloadIconVisible.set(true);
                break;
        }

    }

    private void fetchComments() {
        mDataManager.getCommentsByPost(post.getId(), new OnResponseCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> data) {
                mActivity.hideLoading();
                allComments = data;
                setTabs();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });
    }

    private void setTabs() {
        fragments.clear();
        titles.clear();

        tab1 = ConnectCommentsTab.newInstance(content, post, allComments, this);
        fragments.add(tab1);
        titles.add(mActivity.getString(R.string.all_list));

        tab2 = ConnectCommentsTab.newInstance(content, post, allComments, this);
        fragments.add(tab2);
        titles.add(mActivity.getString(R.string.recently_added_list));

        tab3 = ConnectCommentsTab.newInstance(content, post, allComments, this);
        fragments.add(tab3);
        titles.add(mActivity.getString(R.string.most_relevant_list));

        try {
            adapter.setFragments(fragments, titles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bookmark() {
        mActivity.showLoading();
        mDataManager.setBookmark(content.getId(), new OnResponseCallback<BookmarkResponse>() {
            @Override
            public void onSuccess(BookmarkResponse data) {
                mActivity.hideLoading();
                bookmarkIcon.set(data.isIs_active() ? R.drawable.t_icon_bookmark_filled : R.drawable.t_icon_bookmark);
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
//                bookmarkIcon.set(R.drawable.t_icon_bookmark);
            }
        });
    }

    public void like() {
        mActivity.showLoading();
        mDataManager.setLike(content.getId(), new OnResponseCallback<StatusResponse>() {
            @Override
            public void onSuccess(StatusResponse data) {
                mActivity.hideLoading();
                //TODO: Need filled like icon
                likeIcon.set(data.getStatus() ? R.drawable.t_icon_like_filled : R.drawable.t_icon_like);
                int n = 0;
                if (likes.get() != null) {
                    n = Integer.parseInt(likes.get());
                }
                if (data.getStatus()){
                    n++;
                } else {
                    n--;
                }
                likes.set(String.valueOf(n));
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
//                likeIcon.set(R.drawable.t_icon_like);
            }
        });
    }

    public void download(){
        if (allDownloadStatusIcon.get() == R.drawable.t_icon_download && allDownloadIconVisible.get()) {
            mActivity.showLoading();
            mDataManager.downloadPost(post, String.valueOf(content.getSource().getId()), content.getSource().getName(), mActivity,
                    new VideoDownloadHelper.DownloadManagerCallback() {
                        @Override
                        public void onDownloadStarted(Long result) {
                            mActivity.hideLoading();
                            allDownloadIconVisible.set(false);
                            allDownloadProgressVisible.set(true);
                        }

                        @Override
                        public void onDownloadFailedToStart() {
                            mActivity.hideLoading();
                            allDownloadProgressVisible.set(false);
                            allDownloadStatusIcon.set(R.drawable.t_icon_download);
                            allDownloadIconVisible.set(true);
                        }

                        @Override
                        public void showProgressDialog(int numDownloads) {

                        }

                        @Override
                        public void updateListUI() {

                        }

                        @Override
                        public boolean showInfoMessage(String message) {
                            return false;
                        }
                    });
        }
    }

    public void addCommentOnPost(){

        addReplyToComment(0);

    }

    public void addReplyToComment(int commentParentId){

        String comment = this.comment.get();
        if (comment == null || comment.trim().equals("")){
            mActivity.showShortToast("Comment cannot be empty");
            return;
        }

        mActivity.showLoading();
        mDataManager.addComment(comment.trim(), commentParentId, post.getId(),
                new OnResponseCallback<Comment>() {
                    @Override
                    public void onSuccess(Comment data) {
                        mActivity.hideLoading();
                        allComments.add(0, data);
                        tab1.refreshList();
                        tab2.refreshList();
                        tab3.refreshList();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });

    }

    private void playVideo()
    {
        DownloadEntry de=  mDataManager.getDownloadedVideo(post,
                String.valueOf(content.getSource().getId()), content.getSource().getName());
        if(de!=null && de.filepath!=null && !de.filepath.equals(""))
        {
            String filepath = null;

            if (de.isDownloaded()) {
                File f = new File(de.filepath);
                if (f.exists()) {
                    // play from local
                    filepath = de.filepath;
                    mActivity.logD("playing from local file");
                }
            }

            if (filepath == null || filepath.length() <= 0) {
                // not available on local, so play online
                mActivity.logD("Local file path not available");

                filepath = de.getBestEncodingUrl(mActivity);
            }
            if(filepath!=null)
            {
                ActivityUtil.playVideo(filepath, mActivity);
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadCompletedEvent e) {
        if (e.getType() != null && e.getType().equalsIgnoreCase(DownloadType.WP_VIDEO.name())){

            allDownloadProgressVisible.set(false);
            allDownloadStatusIcon.set(R.drawable.t_icon_done);
            allDownloadIconVisible.set(true);

        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadedVideoDeletedEvent e) {
        if (e.getType() != null && e.getType().equalsIgnoreCase(DownloadType.WP_VIDEO.name())) {
            allDownloadProgressVisible.set(false);
            allDownloadStatusIcon.set(R.drawable.t_icon_download);
            allDownloadIconVisible.set(true);
        }
    }

    public void registerEventBus(){
        EventBus.getDefault().registerSticky(this);
    }

    public void unregisterEvnetBus(){
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClickUser(Comment comment) {

    }

    @Override
    public void onClickLike(Comment comment) {

    }

    @Override
    public void onClickReply(Comment comment) {
        if (commentFocus.get()) {
            commentFocus.set(false);
        }
        commentFocus.set(true);
    }

    public class ConnectPagerAdapter extends BasePagerAdapter {
        public ConnectPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }
}
