package org.edx.mobile.tta.ui.connect.view_model;

import android.Manifest;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.DownloadCompletedEvent;
import org.edx.mobile.module.storage.DownloadedVideoDeletedEvent;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.analytics.analytics_enums.Page;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.data.enums.DownloadType;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.local.db.table.ContentStatus;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.model.content.BookmarkResponse;
import org.edx.mobile.tta.data.model.content.TotalLikeResponse;
import org.edx.mobile.tta.data.model.feed.SuggestedUser;
import org.edx.mobile.tta.data.model.profile.FollowStatus;
import org.edx.mobile.tta.event.CommentRepliesReceivedEvent;
import org.edx.mobile.tta.event.ContentBookmarkChangedEvent;
import org.edx.mobile.tta.event.ContentStatusReceivedEvent;
import org.edx.mobile.tta.event.FetchCommentRepliesEvent;
import org.edx.mobile.tta.event.LoadMoreConnectCommentsEvent;
import org.edx.mobile.tta.event.RepliedOnCommentEvent;
import org.edx.mobile.tta.event.UserFollowingChangedEvent;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.connect.ConnectCommentsTab;
import org.edx.mobile.tta.ui.interfaces.CommentClickListener;
import org.edx.mobile.tta.ui.profile.OtherProfileActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.tta.utils.JsonUtil;
import org.edx.mobile.tta.wordpress_client.model.Comment;
import org.edx.mobile.tta.wordpress_client.model.CustomFilter;
import org.edx.mobile.tta.wordpress_client.model.Post;
import org.edx.mobile.tta.wordpress_client.model.User;
import org.edx.mobile.tta.wordpress_client.util.MxFilterType;
import org.edx.mobile.user.Account;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.PermissionsUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.images.ShareUtils;
import org.edx.mobile.view.common.PageViewStateCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class ConnectDashboardViewModel extends BaseViewModel
    implements CommentClickListener {

    private static final int ACTION_DOWNLOAD = 1;
    private static final int ACTION_DELETE = 2;
    private static final int ACTION_PLAY = 3;

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_PAGE = 1;

    public ConnectPagerAdapter adapter;
    private List<Fragment> fragments;
    private List<String> titles;
    private ConnectCommentsTab tab1;
    private ConnectCommentsTab tab2;
    private ConnectCommentsTab tab3;
    private int actionMode;

    public Content content;
    private Post post;
    private List<Comment> comments;
    private Map<Long, List<Comment>> repliesMap;
    public ObservableField<String> comment = new ObservableField<>("");
    public ObservableBoolean commentFocus = new ObservableBoolean();
    public ObservableField<String> replyingToText = new ObservableField<>();
    public ObservableBoolean replyingToVisible = new ObservableBoolean();
    public ObservableBoolean offlineVisible = new ObservableBoolean();
    private long commentParentId = 0;
    private Comment selectedComment;
    private ContentStatus contentStatus;
    private boolean firstDownload;
    private boolean followed;
    private String username;

    //Header details
    public ObservableInt headerImagePlaceholder = new ObservableInt(R.drawable.placeholder_course_card_image);
    public ObservableInt likeIcon = new ObservableInt(R.drawable.t_icon_like);
    public ObservableInt bookmarkIcon = new ObservableInt(R.drawable.t_icon_bookmark);
    public ObservableBoolean downloadPlayOptionVisible = new ObservableBoolean(false);
    public ObservableBoolean allDownloadOptionVisible = new ObservableBoolean(true);
    public ObservableBoolean allDownloadIconVisible = new ObservableBoolean(true);
    public ObservableBoolean allDownloadProgressVisible = new ObservableBoolean(false);
    public ObservableField<String> duration = new ObservableField<>("");
    public ObservableField<String> description = new ObservableField<>("");
    public ObservableField<String> likes = new ObservableField<>("0");
    public ObservableBoolean userVisible = new ObservableBoolean();
    public ObservableField<String> userImageUrl = new ObservableField<>();
    public ObservableInt userImagePlaceholder = new ObservableInt(R.drawable.profile_photo_placeholder);
    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> date = new ObservableField<>();
    public ObservableField<String> followBtnText = new ObservableField<>();
    public ObservableInt followBtnBackground = new ObservableInt();
    public ObservableInt followTextColor = new ObservableInt();

    public ObservableInt initialPosition = new ObservableInt();

    private int take, page;

    public ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            initialPosition.set(i);
            PageViewStateCallback callback = (PageViewStateCallback) fragments.get(i);
            if (callback != null){
                callback.onPageShow();
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    public ConnectDashboardViewModel(BaseVMActivity activity, Content content) {
        super(activity);
        this.content = content;
        comments = new ArrayList<>();
        repliesMap = new HashMap<>();
        adapter = new ConnectPagerAdapter(mActivity.getSupportFragmentManager());
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
        take = DEFAULT_TAKE;
        page = DEFAULT_PAGE;

        firstDownload = true;
        mDataManager.getUserContentStatus(Collections.singletonList(content.getId()),
                new OnResponseCallback<List<ContentStatus>>() {
                    @Override
                    public void onSuccess(List<ContentStatus> data) {
                        if (data.size() > 0){
                            firstDownload = false;
                            contentStatus = data.get(0);
                            EventBus.getDefault().post(new ContentStatusReceivedEvent(contentStatus));
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });

        setTabs();
    }

    @Override
    public void onResume() {
        super.onResume();
        onEventMainThread(new NetworkConnectivityChangeEvent());
    }

    private void fetchUser(){

        mDataManager.getOtherUserAccount(username, new OnResponseCallback<Account>() {
            @Override
            public void onSuccess(Account data) {
                userImageUrl.set(data.getProfileImage().getImageUrlFull());
                name.set(data.getName());
                date.set(DateUtil.getDisplayTime(post.getDate()));
                toggleFollowBtn();
                fetchFollowStatus();
                userVisible.set(true);
            }

            @Override
            public void onFailure(Exception e) {
                userVisible.set(false);
            }
        });

    }

    private void fetchFollowStatus() {

        mDataManager.getFollowStatus(username, new OnResponseCallback<FollowStatus>() {
            @Override
            public void onSuccess(FollowStatus data) {
                followed = data.is_followed();
                toggleFollowBtn();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    public void fetchPost(OnResponseCallback<Post> callback) {
        loadData();
        /*long postId = 0;
        try {
            postId = Long.parseLong(content.getSource_identity());
        } catch (NumberFormatException e) {
            return;
        }*/
        mActivity.showLoading();
        mDataManager.getPostBySlug(content.getSource_identity(), new OnResponseCallback<Post>() {
            @Override
            public void onSuccess(Post data) {
                post = data;
                String downloadUrl = getDownloadUrl();
                username = getUsername();
                if (username != null){
                    fetchUser();
                } else {
                    userVisible.set(false);
                }
                if (downloadUrl == null || downloadUrl.equals("")){
                    downloadPlayOptionVisible.set(false);

                    if (contentStatus == null){
                        contentStatus = new ContentStatus();
                        contentStatus.setContent_id(content.getId());
                        contentStatus.setStarted(String.valueOf(System.currentTimeMillis()));
                        contentStatus.setCompleted(String.valueOf(System.currentTimeMillis()));
                        mDataManager.setUserContent(Collections.singletonList(contentStatus),
                                new OnResponseCallback<List<ContentStatus>>() {
                                    @Override
                                    public void onSuccess(List<ContentStatus> data) {
                                        if (data.size() > 0){
                                            contentStatus = data.get(0);
                                            EventBus.getDefault().post(new ContentStatusReceivedEvent(contentStatus));
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {

                                    }
                                });
                    }

                } else {
                    downloadPlayOptionVisible.set(true);
                }
                callback.onSuccess(data);
                getPostDownloadStatus();
                fetchComments();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                callback.onFailure(e);
            }
        });

    }

    public void openUserProfile(){
        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_USERNAME, username);
        ActivityUtil.gotoPage(mActivity, OtherProfileActivity.class, parameters);
    }

    public void followUnfollow(){
        mActivity.showLoading();
        mDataManager.followUnfollowUser(username, new OnResponseCallback<StatusResponse>() {
            @Override
            public void onSuccess(StatusResponse data) {
                mActivity.hideLoading();
                followed = data.getStatus();
                toggleFollowBtn();
                SuggestedUser user = new SuggestedUser();
                user.setUsername(username);
                user.setName(name.get());
                user.setFollowed(followed);
                EventBus.getDefault().post(new UserFollowingChangedEvent(user));

                if (data.getStatus()){
                    mActivity.analytic.addMxAnalytics_db(username, Action.FollowUser,
                            Page.ProfilePage.name(), Source.Mobile, username);
                } else {
                    mActivity.analytic.addMxAnalytics_db(username, Action.UnfollowUser,
                            Page.ProfilePage.name(), Source.Mobile, username);
                }
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    private String getDownloadUrl(){
        String download_url=null;
        //find the downloaded obj
        if(post.getFilter()!=null && post.getFilter().size()>0)
        {
            for (CustomFilter item:post.getFilter())
            {
                if(item==null || TextUtils.isEmpty(item.getName()))
                    continue;

                if(item.getName().toLowerCase().equals(String.valueOf(MxFilterType.MX_VIDEODOWNLOAD).toLowerCase())
                        && item.getChoices()!=null && item.getChoices().length > 0)
                {
                    download_url=item.getChoices()[0];
                    break;
                }
            }
        }
        return download_url;
    }

    private String getUsername(){
        String username = null;
        if(post.getFilter()!=null && post.getFilter().size()>0)
        {
            for (CustomFilter item:post.getFilter())
            {
                if(item==null || TextUtils.isEmpty(item.getName()))
                    continue;

                if(item.getName().toLowerCase().equals(String.valueOf(MxFilterType.MX_PROFILE).toLowerCase())
                        && item.getChoices()!=null && item.getChoices().length > 0)
                {
                    username=item.getChoices()[0];
                    break;
                }
            }
        }
        return username;
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

        duration.set(mActivity.getString(R.string.duration) + "-01:00");

        description.set(
                "अकसर शिक्षक होने के नाते हम अपनी कक्षाओं को रोचक बनाने की चुनौतियों से जूझते हैं| हम अलग-अलग गतिविधियाँ अपनाते हैं ताकि बच्चे मनोरंजक तरीकों से सीख सकें| लेकिन ऐसा करना हमेशा आसान नहीं होता| यह कोर्स एक कोशिश है जहां हम ‘गतिविधि क्या है’, ‘कैसी गतिविधियाँ चुनी जायें?’ और इन्हें कराने में क्या-क्या मुश्किलें आ सकती हैं, के बारे में बात कर रहे हैं| इस कोर्स में इन पहलुओं को टटोलने के लिए प्राइमरी कक्षा के EVS (पर्यावरण विज्ञान) विषय के उदाहरण लिए गए हैं| \n" +
                        "\n" +
                        "इस कोर्स को पर्यावरण-विज्ञान पढ़ानेवाले शिक्षक और वे शिक्षक जो ‘गतिविधियों को कक्षा में कैसे कराया जाये’ जानना चाहते हैं, कर सकते हैं| आशा है इस कोर्स को पढ़ने के बाद आपके लिए कक्षा में गतिविधियाँ कराना आसान हो जाएगा|"
        );

    }

    private void getPostDownloadStatus() {

        switch (mDataManager.getPostDownloadStatus(post)){
            case not_downloaded:
                allDownloadProgressVisible.set(false);
                allDownloadIconVisible.set(true);
                allDownloadOptionVisible.set(true);
                break;

            case downloading:
                allDownloadIconVisible.set(false);
                allDownloadProgressVisible.set(true);
                allDownloadOptionVisible.set(true);
                break;

            case downloaded:
            case watching:
            case watched:
                allDownloadProgressVisible.set(false);
                allDownloadIconVisible.set(true);
                allDownloadOptionVisible.set(false);
                break;
        }

    }

    private void fetchComments() {
        mDataManager.getCommentsByPost(post.getId(), take, page, new OnResponseCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> data) {
                mActivity.hideLoading();
                if (data.size() < take){
                    setLoaded();
                }
                populateComments(data);
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                setLoaded();
            }
        });
    }

    private void fetchReplies(Comment comment){

        mDataManager.getRepliesOnComment(post.getId(), comment.getId(), new OnResponseCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> data) {
                if (!data.isEmpty()) {
                    if (!repliesMap.containsKey(comment.getId())){
                        repliesMap.put(comment.getId(), new ArrayList<>());
                    }

                    List<Comment> replies = repliesMap.get(comment.getId());
                    for (Comment reply: data){
                        if (!replies.contains(reply)){
                            replies.add(reply);
                        }
                    }
                }

                EventBus.getDefault().post(new CommentRepliesReceivedEvent(comment));
            }

            @Override
            public void onFailure(Exception e) {
                EventBus.getDefault().post(new CommentRepliesReceivedEvent(comment));
            }
        });

    }

    private void populateComments(List<Comment> data) {
        boolean newItemsAdded = false;
        for (Comment comment: data){
            if (!comments.contains(comment)) {
                comments.add(comment);
                newItemsAdded = true;
            }
        }

        if (newItemsAdded) {
            refreshComments();
        }
    }

    private void setTabs() {

        tab1 = ConnectCommentsTab.newInstance(content, post, comments, repliesMap, Nav.all, this);
        fragments.add(tab1);
        titles.add(mActivity.getString(R.string.all_list));

        tab2 = ConnectCommentsTab.newInstance(content, post, comments, repliesMap, Nav.recently_added, this);
        fragments.add(tab2);
        titles.add(mActivity.getString(R.string.recently_added_list));

        tab3 = ConnectCommentsTab.newInstance(content, post, comments, repliesMap, Nav.most_relevant, this);
        fragments.add(tab3);
        titles.add(mActivity.getString(R.string.most_relevant_list));

        try {
            adapter.setFragments(fragments, titles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initialPosition.set(0);
        tab1.onPageShow();
    }

    public void bookmark() {
        mActivity.showLoading();
        mDataManager.setBookmark(content.getId(), new OnResponseCallback<BookmarkResponse>() {
            @Override
            public void onSuccess(BookmarkResponse data) {
                mActivity.hideLoading();
                bookmarkIcon.set(data.isIs_active() ? R.drawable.t_icon_bookmark_filled : R.drawable.t_icon_bookmark);

                if (data.isIs_active()) {
                    mActivity.analytic.addMxAnalytics_db(
                            content.getName() , Action.BookmarkPost, content.getSource().getName(),
                            Source.Mobile, content.getSource_identity());
                } else {
                    mActivity.analytic.addMxAnalytics_db(
                            content.getName() , Action.UnbookmarkPost, content.getSource().getName(),
                            Source.Mobile, content.getSource_identity());
                }

                EventBus.getDefault().post(new ContentBookmarkChangedEvent(content, data.isIs_active()));
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

                mActivity.analytic.addMxAnalytics_db(
                        content.getName() ,
                        data.getStatus() ? Action.LikePost : Action.UnlikePost,
                        content.getSource().getName(),
                        Source.Mobile, content.getSource_identity());
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
//                likeIcon.set(R.drawable.t_icon_like);
            }
        });
    }

    public void performReadWriteOperation(){

        switch (actionMode){
            case ACTION_DOWNLOAD:
                downloadPost();
                break;
            case ACTION_DELETE:
                deletePost();
                break;
            case ACTION_PLAY:
                playVideo();
                break;
        }

    }

    public void download(){
        actionMode = ACTION_DOWNLOAD;
        mActivity.askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
    }

    private void downloadPost() {

        if (allDownloadIconVisible.get()) {
            mActivity.showLoading();
            mDataManager.downloadPost(post, content.getId(),
                    String.valueOf(content.getSource().getId()), content.getSource().getName(),
                    mActivity,
                    new VideoDownloadHelper.DownloadManagerCallback() {
                        @Override
                        public void onDownloadStarted(Long result) {
                            mActivity.hideLoading();
                            allDownloadIconVisible.set(false);
                            allDownloadProgressVisible.set(true);

                            if (contentStatus == null && firstDownload){
                                firstDownload = false;
                                ContentStatus status = new ContentStatus();
                                status.setContent_id(content.getId());
                                status.setStarted(String.valueOf(System.currentTimeMillis()));
                                mDataManager.setUserContent(Collections.singletonList(status),
                                        new OnResponseCallback<List<ContentStatus>>() {
                                            @Override
                                            public void onSuccess(List<ContentStatus> data) {
                                                if (data.size() > 0){
                                                    contentStatus = data.get(0);
                                                    EventBus.getDefault().post(new ContentStatusReceivedEvent(contentStatus));
                                                }
                                            }

                                            @Override
                                            public void onFailure(Exception e) {

                                            }
                                        });
                            }
                        }

                        @Override
                        public void onDownloadFailedToStart() {
                            mActivity.hideLoading();
                            allDownloadProgressVisible.set(false);
                            allDownloadIconVisible.set(true);
                            allDownloadOptionVisible.set(true);
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

    public void delete(){
        actionMode = ACTION_DELETE;
        mActivity.askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
    }

    private void deletePost(){
        mActivity.showAlertDailog("Delete",
                "Are you sure you want to delete \"" + content.getName() + "\"?",
                (dialog, which) -> {
                    mDataManager.deletePost(post);
                },
                null);
    }

    public void comment(){

        String comment = this.comment.get();
        if (comment == null || comment.trim().equals("")){
            mActivity.showShortToast("Comment cannot be empty");
            return;
        }

        mActivity.showLoading();
        mDataManager.addComment(comment.trim(), (int) commentParentId, post.getId(),
                new OnResponseCallback<Comment>() {
                    @Override
                    public void onSuccess(Comment data) {
                        mActivity.hideLoading();
                        if (commentParentId == 0) {
                            mActivity.showLongSnack("Commented successfully");
                            comments.add(0, data);
                            post.setTotal_comments(post.getTotal_comments() + 1);
                            tab1.refreshList();
                            tab2.refreshList();
                            tab3.refreshList();

                            mActivity.analytic.addMxAnalytics_db(
                                    content.getName() , Action.CommentPost, content.getSource().getName(),
                                    Source.Mobile, content.getSource_identity());

                        } else {
                            mActivity.showLongSnack("Replied successfully");
                            replyingToVisible.set(false);
                            commentParentId = 0;

                            mActivity.analytic.addMxAnalytics_db(
                                    content.getName() , Action.ReplyComment, content.getSource().getName(),
                                    Source.Mobile, String.valueOf(selectedComment.getId()));

                            if (repliesMap.containsKey(selectedComment.getId())){
                                repliesMap.get(selectedComment.getId()).add(0, data);
                                EventBus.getDefault().post(new RepliedOnCommentEvent(selectedComment));
                            }

                        }
                        ConnectDashboardViewModel.this.comment.set("");
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

    public void play(){
        actionMode = ACTION_PLAY;
        mActivity.askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
    }

    private void playVideo() {
        DownloadEntry de=  mDataManager.getDownloadedVideo(post, content.getId(),
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

            if (contentStatus == null){
                contentStatus = new ContentStatus();
            }
            if (contentStatus.getCompleted() == null){
                contentStatus.setContent_id(content.getId());
                contentStatus.setCompleted(String.valueOf(System.currentTimeMillis()));
                mDataManager.setUserContent(Collections.singletonList(contentStatus),
                        new OnResponseCallback<List<ContentStatus>>() {
                            @Override
                            public void onSuccess(List<ContentStatus> data) {
                                if (data.size() > 0){
                                    contentStatus = data.get(0);
                                    EventBus.getDefault().post(new ContentStatusReceivedEvent(contentStatus));
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
            }
        }
    }

    //share post link with other apps
    public void openShareMenu(View anchor) {
        if (post == null){
            return;
        }
        final String shareTextWithPlatformName = ResourceUtil.getFormattedString(
                mActivity.getResources(),
                R.string.share_wp_post_message,
                "course_name",
                //getString(R.string.platform_name)).toString() + "\n" + courseData.getCourse().getCourse_about();
                post.getTitle().getRendered()).toString() + "\n" + post.getLink();
        ShareUtils.showShareMenu(
                mActivity,
                ShareUtils.newShareIntent(shareTextWithPlatformName),
                anchor,
                (componentName, shareType) -> {
                    final String shareText;
                    final String twitterTag = mDataManager.getConfig().getTwitterConfig().getHashTag();
                    if (shareType == ShareUtils.ShareType.TWITTER && !TextUtils.isEmpty(twitterTag)) {
                        shareText = ResourceUtil.getFormattedString(
                                mActivity.getResources(),
                                R.string.share_wp_post_message,
                                "course_name",
                                //twitterTag).toString() + "\n" + courseData.getCourse().getCourse_about();
                                twitterTag).toString() + "\n" + post.getLink();

                    } else {
                        shareText = shareTextWithPlatformName;
                    }

                    mActivity.analytic.addMxAnalytics_db(content.getName(), Action.Share,
                            content.getSource().getName(), Source.Mobile, content.getSource_identity(),
                            BreadcrumbUtil.getBreadcrumb() + "/" + shareType.name());

//                    segIO.courseDetailShared(post.getLink(), shareText, shareType);
                    if (!shareType.equals(ShareUtils.ShareType.TTA)) {
                        final Intent intent = ShareUtils.newShareIntent(shareText);
                        intent.setComponent(componentName);
                        mActivity.startActivity(intent);
                    } else {
                        mActivity.showLongToast("Post shared on TheTeacherApp");
                    }


                });

    }

    private void setLoaded(){
        tab1.setLoaded();
        tab2.setLoaded();
        tab3.setLoaded();
    }

    private void refreshComments() {
        tab1.refreshList();
        tab2.refreshList();
        tab3.refreshList();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(UserFollowingChangedEvent event){
        followed = event.getUser().isFollowed();
        toggleFollowBtn();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadMoreConnectCommentsEvent event){
        page++;
        fetchComments();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(FetchCommentRepliesEvent event){
        fetchReplies(event.getComment());
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadCompletedEvent e) {
        if (e.getEntry() != null && e.getEntry().content_id == content.getId() &&
                e.getEntry().type != null && e.getEntry().type.equalsIgnoreCase(DownloadType.WP_VIDEO.name())){

            allDownloadProgressVisible.set(false);
            allDownloadIconVisible.set(true);
            allDownloadOptionVisible.set(false);

            mActivity.analytic.addMxAnalytics_db(
                    content.getName() , Action.DownloadPostComplete, content.getSource().getName(),
                    Source.Mobile, content.getSource_identity());

        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadedVideoDeletedEvent e) {
        if (e.getModel() != null && e.getModel().getContent_id() == content.getId() &&
                e.getModel().getDownloadType() != null &&
                e.getModel().getDownloadType().equalsIgnoreCase(DownloadType.WP_VIDEO.name())) {
            allDownloadProgressVisible.set(false);
            allDownloadIconVisible.set(true);
            allDownloadOptionVisible.set(true);

            mActivity.analytic.addMxAnalytics_db(
                    content.getName() , Action.DeletePost, content.getSource().getName(),
                    Source.Mobile, content.getSource_identity());
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event){
        if (NetworkUtil.isConnected(mActivity)){
            offlineVisible.set(false);
        } else {
            offlineVisible.set(true);
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
        mActivity.showLoading();
        mDataManager.getWpUser(comment.getAuthor(), new OnResponseCallback<User>() {
            @Override
            public void onSuccess(User data) {
                mActivity.hideLoading();
                Bundle parameters = new Bundle();
                parameters.putString(Constants.KEY_USERNAME, data.getUsername());
                ActivityUtil.gotoPage(mActivity, OtherProfileActivity.class, parameters);
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    @Override
    public void onClickLike(Comment comment) {

    }

    @Override
    public void onClickReply(Comment comment) {
        selectedComment = comment;
        replyingToText.set("Replying to " + comment.getAuthorName());
        replyingToVisible.set(true);
        commentParentId = comment.getId();

        if (commentFocus.get()) {
            commentFocus.set(false);
        }
        commentFocus.set(true);
    }

    @Override
    public void onClickDefault(Comment comment) {

    }

    public void resetReplyToComment(){
        replyingToVisible.set(false);
        commentParentId = 0;
    }

    private void toggleFollowBtn(){

        if (followed){
            followBtnText.set(mActivity.getString(R.string.following));
            followBtnBackground.set(R.drawable.btn_selector_filled);
            followTextColor.set(R.color.white);
        } else {
            followBtnText.set(mActivity.getString(R.string.follow));
            followBtnBackground.set(R.drawable.btn_selector_hollow);
            followTextColor.set(R.color.primary_cyan);
        }

    }

    public class ConnectPagerAdapter extends BasePagerAdapter {
        public ConnectPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }
}
