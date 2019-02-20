package org.edx.mobile.tta.ui.course.view_model;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowCourseMaterialFooterBinding;
import org.edx.mobile.databinding.TRowCourseMaterialHeaderBinding;
import org.edx.mobile.databinding.TRowCourseMaterialItemBinding;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.IBlock;
import org.edx.mobile.module.storage.DownloadCompletedEvent;
import org.edx.mobile.module.storage.DownloadedVideoDeletedEvent;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.model.content.BookmarkResponse;
import org.edx.mobile.tta.data.model.content.TotalLikeResponse;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.scorm.ScormBlockModel;
import org.edx.mobile.tta.ui.base.BaseRecyclerAdapter;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.course.CourseScormViewActivity;
import org.edx.mobile.tta.ui.interfaces.OnTaItemClickListener;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.view.custom.AuthenticatedWebView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.ThreadMode;
import okhttp3.HttpUrl;

public class CourseMaterialViewModel extends BaseViewModel {

    private static final String javascript = "javascript:document.getElementsByid('xblock.xblock-student_view.xblock-student_view-html.xmodule_display.xmodule_HtmlModule.xblock-initialized').html();";

    private Content content;
    private EnrolledCoursesResponse course;
    private CourseComponent rootComponent;
    private CourseComponent assessmentComponent;
    private CourseComponent aboutComponent;
    private List<ScormBlockModel> remainingScorms;

    public CourseMaterialAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    //Header details
    public ObservableInt likeIcon = new ObservableInt(R.drawable.t_icon_like);
    public ObservableInt bookmarkIcon = new ObservableInt(R.drawable.t_icon_bookmark);
    public ObservableInt allDownloadStatusIcon = new ObservableInt(R.drawable.t_icon_download);
    public ObservableBoolean allDownloadIconVisible = new ObservableBoolean(false);
    public ObservableBoolean allDownloadProgressVisible = new ObservableBoolean(false);
    public ObservableField<String> description = new ObservableField<>("");
    public ObservableField<String> likes = new ObservableField<>("0");

    //Footer details
    public ObservableField<String> footerImageUrl = new ObservableField<>();
    public ObservableBoolean footerTitleVisible = new ObservableBoolean();
    public ObservableField<String> footerTitle = new ObservableField<>();
    public ObservableInt footerDownloadIcon = new ObservableInt(R.drawable.t_icon_download);
    public ObservableBoolean footerDownloadIconVisible = new ObservableBoolean(false);
    public ObservableBoolean footerDownloadProgressVisible = new ObservableBoolean(false);
    public ObservableField<String> footerBtnText = new ObservableField<>();

    private int numberOfDownloadingVideos;
    private int numberOfDownloadedVideos;
    private boolean downloadModeIsAll;

    public CourseMaterialViewModel(Context context, TaBaseFragment fragment, Content content, EnrolledCoursesResponse course, CourseComponent rootComponent) {
        super(context, fragment);
        this.content = content;
        this.course = course;
        this.rootComponent = rootComponent;
        adapter = new CourseMaterialAdapter();
        loadData();
    }

    public void loadData(){

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
                likeIcon.set(data.getStatus() ? R.drawable.t_icon_like : R.drawable.t_icon_like);
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

        fetchCourseComponent();

        adapter.setItemClickListener((view, item) -> {
            switch (view.getId()) {
                case R.id.item_delete_download:
                    if (item.isContainer()){
                        CourseComponent component = (CourseComponent) item.getChildren().get(0);
                        if (component instanceof ScormBlockModel){
                            ScormBlockModel scorm = (ScormBlockModel) component;
                            switch (mDataManager.getScormStatus(scorm)){
                                case not_downloaded:
                                    downloadSingle(scorm);
                                    break;
                                case downloaded:
                                case watching:
                                case watched:
                                    deleteScorm(scorm);
                                    break;
                                case downloading:
                                    //Do nothing
                                    break;
                            }
                        }
                    }
                    break;
                default:
                    if (item.isContainer()){
                        CourseComponent component = (CourseComponent) item.getChildren().get(0);
                        if (component instanceof ScormBlockModel){
                            ScormBlockModel scorm = (ScormBlockModel) component;
                            switch (mDataManager.getScormStatus(scorm)){
                                case not_downloaded:
                                    downloadSingle(scorm);
                                    break;
                                case downloaded:
                                case watching:
                                case watched:
                                    showScorm(scorm);
                                    break;
                                case downloading:
                                    //Do nothing
                                    break;
                            }
                        }
                    }
                    break;
            }
        });

    }

    private void deleteScorm(ScormBlockModel scorm) {
        mActivity.showAlertDailog("Delete",
                "Are you sure you want to delete \"" + scorm.getParent().getDisplayName() + "\"?",
                (dialog, which) -> {
                    mDataManager.deleteScorm(scorm);
                },
                null);
    }

    private void showScorm(ScormBlockModel scorm) {
        Bundle parameters = new Bundle();
        String filePath = scorm.getDownloadEntry(mDataManager.getEdxEnvironment().getStorage()).getFilePath();
        if (filePath.contains(".zip")){
            filePath = filePath.substring(0, filePath.length()-4);
        }
        parameters.putString(Constants.KEY_FILE_PATH, filePath);
        parameters.putString(Constants.KEY_COURSE_NAME, scorm.getRoot().getDisplayName());
        parameters.putString(Constants.KEY_COURSE_ID, scorm.getRoot().getId());
        parameters.putString(Constants.KEY_UNIT_ID, scorm.getId());
        ActivityUtil.gotoPage(mActivity, CourseScormViewActivity.class, parameters);
    }

    private void enableHeader(){

        allDownloadStatusIcon.set(R.drawable.t_icon_done);
        String aboutUrl = aboutComponent.getChildren().get(0).getBlockUrl();

//        AuthenticatedWebView webView = new AuthenticatedWebView(mActivity);
//        webView.initWebView(mActivity, false, false);
//        webView.setHtmlCallback(new OnResponseCallback<String>() {
//            @Override
//            public void onSuccess(String data) {
//                description.set(data);
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                description.set("");
//            }
//        });
//        webView.loadUrlWithJavascript(true, aboutUrl,
//                "javascript:(function() { $(\".xblock.xblock-student_view.xblock-student_view-html.xmodule_display.xmodule_HtmlModule.xblock-initialized\").html() })()");

        /*description.set(aboutComponent.getDisplayName() + "\n\n" +
                "अकसर शिक्षक होने के नाते हम अपनी कक्षाओं को रोचक बनाने की चुनौतियों से जूझते हैं| हम अलग-अलग गतिविधियाँ अपनाते हैं ताकि बच्चे मनोरंजक तरीकों से सीख सकें| लेकिन ऐसा करना हमेशा आसान नहीं होता| यह कोर्स एक कोशिश है जहां हम ‘गतिविधि क्या है’, ‘कैसी गतिविधियाँ चुनी जायें?’ और इन्हें कराने में क्या-क्या मुश्किलें आ सकती हैं, के बारे में बात कर रहे हैं| इस कोर्स में इन पहलुओं को टटोलने के लिए प्राइमरी कक्षा के EVS (पर्यावरण विज्ञान) विषय के उदाहरण लिए गए हैं| \n" +
                        "\n" +
                        "इस कोर्स को पर्यावरण-विज्ञान पढ़ानेवाले शिक्षक और वे शिक्षक जो ‘गतिविधियों को कक्षा में कैसे कराया जाये’ जानना चाहते हैं, कर सकते हैं| आशा है इस कोर्स को पढ़ने के बाद आपके लिए कक्षा में गतिविधियाँ कराना आसान हो जाएगा|"
        );*/

        adapter.setHeaderLayout(R.layout.t_row_course_material_header);
        adapter.setHeaderClickListener(v -> {
            switch (v.getId()) {
                case R.id.like_layout:
                    like();
                    break;
                case R.id.bookmark_layout:
                    bookmark();
                    break;
                case R.id.all_download_layout:
                    if (allDownloadStatusIcon.get() == R.drawable.t_icon_download && allDownloadIconVisible.get()) {
                        downloadAllRemaining();
                    }
                    break;
            }
        });

    }

    private void enableFooter(){

        footerImageUrl.set("http://theteacherapp.org/asset-v1:Mathematics+M01+201706_Mat_01+type@asset+block@Math_sample2.png");
        footerTitleVisible.set(true);
        footerTitle.set(assessmentComponent.getDisplayName());
        footerDownloadIcon.set(R.drawable.t_icon_download);
        footerBtnText.set(mActivity.getString(R.string.assessment));

        adapter.setFooterLayout(R.layout.t_row_course_material_footer);
        adapter.setFooterClickListener(v -> {
            switch (v.getId()) {
                case R.id.item_delete_download:
                    if (assessmentComponent.isContainer()){
                        CourseComponent component = (CourseComponent) assessmentComponent.getChildren().get(0);
                        if (component instanceof ScormBlockModel){
                            ScormBlockModel scorm = (ScormBlockModel) component;
                            switch (mDataManager.getScormStatus(scorm)){
                                case not_downloaded:
                                    downloadSingle(scorm);
                                    break;
                                case downloaded:
                                case watching:
                                case watched:
                                    deleteScorm(scorm);
                                    break;
                                case downloading:
                                    //Do nothing
                                    break;
                            }
                        }
                    }
                    break;
                case R.id.item_btn:
                    if (assessmentComponent.isContainer()){
                        CourseComponent component = (CourseComponent) assessmentComponent.getChildren().get(0);
                        if (component instanceof ScormBlockModel){
                            ScormBlockModel scorm = (ScormBlockModel) component;
                            switch (mDataManager.getScormStatus(scorm)){
                                case not_downloaded:
                                    downloadSingle(scorm);
                                    break;
                                case downloaded:
                                case watching:
                                case watched:
                                    showScorm(scorm);
                                    break;
                                case downloading:
                                    //Do nothing
                                    break;
                            }
                        }
                    }
                    break;
                default:
                    if (assessmentComponent.isContainer()){
                        CourseComponent component = (CourseComponent) assessmentComponent.getChildren().get(0);
                        if (component instanceof ScormBlockModel){
                            ScormBlockModel scorm = (ScormBlockModel) component;
                            switch (mDataManager.getScormStatus(scorm)){
                                case not_downloaded:
                                    downloadSingle(scorm);
                                    break;
                                case downloaded:
                                case watching:
                                case watched:
                                    showScorm(scorm);
                                    break;
                                case downloading:
                                    //Do nothing
                                    break;
                            }
                        }
                    }
                    break;
            }
        });

    }

    private void bookmark() {
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
            }
        });
    }

    private void like() {
        mActivity.showLoading();
        mDataManager.setLike(content.getId(), new OnResponseCallback<StatusResponse>() {
            @Override
            public void onSuccess(StatusResponse data) {
                mActivity.hideLoading();
                //TODO: Need filled like icon
                likeIcon.set(data.getStatus() ? R.drawable.t_icon_like : R.drawable.t_icon_like);
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
            }
        });
    }

    private void downloadSingle(ScormBlockModel scorm){
        mActivity.showLoading();
        downloadModeIsAll = false;
        mDataManager.downloadSingle(scorm, mActivity, new VideoDownloadHelper.DownloadManagerCallback() {
            @Override
            public void onDownloadStarted(Long result) {
                mActivity.hideLoading();
                remainingScorms.remove(scorm);
                if (adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onDownloadFailedToStart() {
                mActivity.hideLoading();
                if (adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void showProgressDialog(int numDownloads) {

            }

            @Override
            public void updateListUI() {
                if (adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public boolean showInfoMessage(String message) {
                return false;
            }
        });
    }

    private void downloadAllRemaining(){
        mActivity.showLoading();
        downloadModeIsAll = true;
        numberOfDownloadingVideos = remainingScorms.size();
        numberOfDownloadedVideos = 0;

        mDataManager.downloadMultiple(remainingScorms, mActivity,
                new VideoDownloadHelper.DownloadManagerCallback() {
                    @Override
                    public void onDownloadStarted(Long result) {
                        mActivity.hideLoading();
                        numberOfDownloadingVideos = remainingScorms.size();
                        numberOfDownloadedVideos = 0;
                        allDownloadIconVisible.set(false);
                        allDownloadProgressVisible.set(true);
                        if (adapter != null){
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onDownloadFailedToStart() {
                        mActivity.hideLoading();
                        numberOfDownloadingVideos = 0;
                        allDownloadIconVisible.set(true);
                        allDownloadProgressVisible.set(false);
                        if (adapter != null){
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void showProgressDialog(int numDownloads) {
                        Log.d("Download", "showProgressDialog " + numDownloads);
                    }

                    @Override
                    public void updateListUI() {
                        if (adapter != null){
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public boolean showInfoMessage(String message) {
                        Log.d("Download", "showInfoMessage " + message);
                        return false;
                    }
                });

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadCompletedEvent e) {
        if (downloadModeIsAll) {
            numberOfDownloadedVideos++;
            if (numberOfDownloadedVideos == numberOfDownloadingVideos){
                numberOfDownloadingVideos = 0;
                allDownloadProgressVisible.set(false);
                allDownloadStatusIcon.set(R.drawable.t_icon_done);
                allDownloadIconVisible.set(true);
            }
        } else if (remainingScorms == null || remainingScorms.isEmpty()){
            allDownloadProgressVisible.set(false);
            allDownloadStatusIcon.set(R.drawable.t_icon_done);
            allDownloadIconVisible.set(true);
        }
        if (adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadedVideoDeletedEvent e) {
        populateData();
        allDownloadStatusIcon.set(R.drawable.t_icon_download);
    }

    public void registerEventBus(){
        EventBus.getDefault().registerSticky(this);
    }

    public void unregisterEvnetBus(){
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
    }

    public void fetchCourseComponent() {
        populateData();
        if (remainingScorms.isEmpty()){
            allDownloadStatusIcon.set(R.drawable.t_icon_done);
            allDownloadIconVisible.set(true);
        } else {
            allDownloadStatusIcon.set(R.drawable.t_icon_download);
            allDownloadIconVisible.set(true);
        }
    }

    private void populateData(){

        adapter.setData(rootComponent);

    }

    public class CourseMaterialAdapter extends BaseRecyclerAdapter<CourseComponent> {

        private CourseComponent rootComponent;
        private int numOfTotalUnits = 0;
        private List<CourseComponent> components;

        public CourseMaterialAdapter() {
            components = new ArrayList<>();
        }

        private void setData(CourseComponent component){
            components.clear();
            if (remainingScorms == null){
                remainingScorms = new ArrayList<>();
            } else {
                remainingScorms.clear();
            }
            rootComponent = component;
            if (rootComponent != null){
                List<IBlock> children = rootComponent.getChildren();
                this.numOfTotalUnits = children.size();

                for (IBlock block : children) {
                    CourseComponent comp = (CourseComponent) block;

                    if (comp.isContainer()){
                        for (IBlock childBlock : comp.getChildren()) {
                            CourseComponent child = (CourseComponent) childBlock;
                            if (child.getDisplayName().contains("अपनी समझ")){
                                assessmentComponent = child;
                                enableFooter();
                            } else if (child.getDisplayName().contains("कोर्स के बारे में")){
                                aboutComponent = child;
                                enableHeader();
                            } else {
                                components.add(child);
                            }

                            if (child.isContainer()){
                                CourseComponent childComp = (CourseComponent) child.getChildren().get(0);
                                if (childComp instanceof ScormBlockModel){
                                    if (mDataManager.scormNotDownloaded((ScormBlockModel) childComp)){
                                        remainingScorms.add((ScormBlockModel) childComp);
                                    }
                                }
                            }
                        }
                    }else {
                        if (comp.getDisplayName().contains("अपनी समझ")){
                            assessmentComponent = comp;
                            enableFooter();
                        } else if (comp.getDisplayName().contains("कोर्स के बारे में")){
                            aboutComponent = comp;
                            enableHeader();
                        } else {
                            components.add(comp);
                        }
                    }
                }
            }
            set(components);
        }

        @Override
        public void onBind(ViewDataBinding binding, CourseComponent item, OnHeaderClickListener headerClickListener, OnFooterClickListener footerClickListener, OnTaItemClickListener<CourseComponent> itemClickListener) {

            if (binding instanceof TRowCourseMaterialHeaderBinding) {
                TRowCourseMaterialHeaderBinding headerBinding = (TRowCourseMaterialHeaderBinding) binding;
                headerBinding.setViewModel(CourseMaterialViewModel.this);
                headerBinding.descriptionWebview.setOnTouchListener((v, event) -> (event.getAction() == MotionEvent.ACTION_MOVE));
                if (!headerBinding.descriptionWebview.isInitiated()){
                    headerBinding.descriptionWebview.initWebView(mActivity, false, false);
                    headerBinding.descriptionWebview.loadUrl(true, aboutComponent.getChildren().get(0).getBlockUrl());
                }

                headerBinding.likeLayout.setOnClickListener(v -> {
                    if (headerClickListener != null) {
                        headerClickListener.onClick(v);
                    }
                });

                headerBinding.bookmarkLayout.setOnClickListener(v -> {
                    if (headerClickListener != null) {
                        headerClickListener.onClick(v);
                    }
                });

                headerBinding.allDownloadLayout.setOnClickListener(v -> {
                    if (headerClickListener != null) {
                        headerClickListener.onClick(v);
                    }
                });

            } else if (binding instanceof TRowCourseMaterialFooterBinding) {
                TRowCourseMaterialFooterBinding footerBinding = (TRowCourseMaterialFooterBinding) binding;
                footerBinding.setViewModel(CourseMaterialViewModel.this);
                footerDownloadProgressVisible.set(false);

                if (assessmentComponent.isContainer()){
                    CourseComponent component = (CourseComponent) assessmentComponent.getChildren().get(0);
                    if (component instanceof ScormBlockModel){
                        ScormBlockModel scorm = (ScormBlockModel) component;
                        switch (mDataManager.getScormStatus(scorm)){
                            case not_downloaded:
                                footerDownloadIcon.set(R.drawable.t_icon_download);
                                footerDownloadIconVisible.set(true);
                                footerBtnText.set(mActivity.getString(R.string.assessment));
                                break;
                            case downloading:
                                footerDownloadIconVisible.set(false);
                                footerDownloadProgressVisible.set(true);
                                footerBtnText.set(mActivity.getString(R.string.assessment));
                                break;
                            case downloaded:
                                footerDownloadIcon.set(R.drawable.t_icon_delete);
                                footerDownloadIconVisible.set(true);
                                footerBtnText.set(mActivity.getString(R.string.assessment));
                                break;
                            case watching:
                                footerDownloadIcon.set(R.drawable.t_icon_delete);
                                footerDownloadIconVisible.set(true);
                                footerBtnText.set(mActivity.getString(R.string.assessment));
                                break;
                            case watched:
                                footerDownloadIcon.set(R.drawable.t_icon_delete);
                                footerDownloadIconVisible.set(true);
                                footerBtnText.set(mActivity.getString(R.string.assessment));
                                break;
                        }
                    }
                }

                footerBinding.itemDeleteDownload.setOnClickListener(v -> {
                    if (footerClickListener != null) {
                        footerClickListener.onClick(v);
                    }
                });

                footerBinding.itemBtn.setOnClickListener(v -> {
                    if (footerClickListener != null) {
                        footerClickListener.onClick(v);
                    }
                });

                footerBinding.getRoot().setOnClickListener(v -> {
                    if (footerClickListener != null) {
                        footerClickListener.onClick(v);
                    }
                });

            } else {
                TRowCourseMaterialItemBinding itemBinding = (TRowCourseMaterialItemBinding) binding;
                itemBinding.loadingIndicator.setVisibility(View.GONE);
                if (item.isContainer()){
                    CourseComponent component = (CourseComponent) item.getChildren().get(0);
                    if (component instanceof ScormBlockModel){
                        ScormBlockModel scorm = (ScormBlockModel) component;
                        switch (mDataManager.getScormStatus(scorm)){
                            case not_downloaded:
                                itemBinding.itemDeleteDownload.setVisibility(View.VISIBLE);
                                itemBinding.itemDeleteDownload
                                        .setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.t_icon_download));
                                itemBinding.itemStatus.setVisibility(View.GONE);
                                break;
                            case downloading:
                                itemBinding.itemDeleteDownload.setVisibility(View.GONE);
                                itemBinding.itemStatus.setVisibility(View.GONE);
                                itemBinding.loadingIndicator.setVisibility(View.VISIBLE);
                                break;
                            case downloaded:
                                itemBinding.itemDeleteDownload.setVisibility(View.VISIBLE);
                                itemBinding.itemDeleteDownload
                                        .setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.t_icon_delete));
                                itemBinding.itemStatus.setVisibility(View.GONE);
                                break;
                            case watching:
                                itemBinding.itemDeleteDownload.setVisibility(View.VISIBLE);
                                itemBinding.itemDeleteDownload
                                        .setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.t_icon_delete));
                                itemBinding.itemStatus.setVisibility(View.VISIBLE);
                                itemBinding.itemStatus.setText(R.string.viewing);
                                break;
                            case watched:
                                itemBinding.itemDeleteDownload.setVisibility(View.VISIBLE);
                                itemBinding.itemDeleteDownload
                                        .setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.t_icon_delete));
                                itemBinding.itemStatus.setVisibility(View.VISIBLE);
                                itemBinding.itemStatus.setText(R.string.viewed);
                                break;
                        }
                    }
                }

                itemBinding.itemDuration.setText(mActivity.getString(R.string.estimated_duration) + ": 01:00");
                Glide.with(mActivity)
                        .load("http://theteacherapp.org/asset-v1:Mathematics+M01+201706_Mat_01+type@asset+block@Math_sample2.png")
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(itemBinding.itemImage);
                itemBinding.itemTitle.setText(item.getDisplayName());

                itemBinding.itemDeleteDownload.setOnClickListener(v -> {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(v, item);
                    }
                });
                itemBinding.getRoot().setOnClickListener(v -> {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(v, item);
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isHeaderEnabled() && position == 0) {
                return R.layout.t_row_course_material_header;
            } else if (isFooterEnabled() && position == getItemCount() - 1) {
                return R.layout.t_row_course_material_footer;
            } else {
                return R.layout.t_row_course_material_item;
            }
        }

    }

}
