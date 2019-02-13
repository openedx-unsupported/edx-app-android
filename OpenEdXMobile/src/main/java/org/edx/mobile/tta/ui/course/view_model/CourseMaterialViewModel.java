package org.edx.mobile.tta.ui.course.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowCourseMaterialFooterBinding;
import org.edx.mobile.databinding.TRowCourseMaterialHeaderBinding;
import org.edx.mobile.databinding.TRowCourseMaterialItemBinding;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.IBlock;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.model.content.BookmarkResponse;
import org.edx.mobile.tta.data.model.content.TotalLikeResponse;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.scorm.ScormBlockModel;
import org.edx.mobile.tta.ui.base.BaseRecyclerAdapter;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.interfaces.OnTaItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class CourseMaterialViewModel extends BaseViewModel {

    private Content content;
    private EnrolledCoursesResponse course;
    private CourseComponent assessmentComponent;
    private CourseComponent aboutComponent;
    private List<ScormBlockModel> remainingScorms;

    public CourseMaterialAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    //Header details
    public ObservableInt likeIcon = new ObservableInt(R.drawable.t_icon_like);
    public ObservableInt bookmarkIcon = new ObservableInt(R.drawable.t_icon_bookmark);
    public ObservableInt allDownloadStatusIcon = new ObservableInt(R.drawable.t_icon_download);
    public ObservableField<String> description = new ObservableField<>("");
    public ObservableField<String> likes = new ObservableField<>("0");

    //Footer details
    public ObservableField<String> footerImageUrl = new ObservableField<>();
    public ObservableBoolean footerTitleVisible = new ObservableBoolean();
    public ObservableField<String> footerTitle = new ObservableField<>();
    public ObservableInt footerDownloadIcon = new ObservableInt(R.drawable.t_icon_download);
    public ObservableField<String> footerBtnText = new ObservableField<>();


    public CourseMaterialViewModel(Context context, TaBaseFragment fragment, Content content, EnrolledCoursesResponse course) {
        super(context, fragment);
        this.content = content;
        this.course = course;
        adapter = new CourseMaterialAdapter();
        loadData();
    }

    private void loadData(){

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
                    mActivity.showShortSnack("Item delete");
                    break;
                default:
                    mActivity.showShortSnack("Item clicked");
                    break;
            }
        });

    }

    private void enableHeader(){

        allDownloadStatusIcon.set(R.drawable.t_icon_done);
        description.set(aboutComponent.getDisplayName() + "\n\n" +
                "अकसर शिक्षक होने के नाते हम अपनी कक्षाओं को रोचक बनाने की चुनौतियों से जूझते हैं| हम अलग-अलग गतिविधियाँ अपनाते हैं ताकि बच्चे मनोरंजक तरीकों से सीख सकें| लेकिन ऐसा करना हमेशा आसान नहीं होता| यह कोर्स एक कोशिश है जहां हम ‘गतिविधि क्या है’, ‘कैसी गतिविधियाँ चुनी जायें?’ और इन्हें कराने में क्या-क्या मुश्किलें आ सकती हैं, के बारे में बात कर रहे हैं| इस कोर्स में इन पहलुओं को टटोलने के लिए प्राइमरी कक्षा के EVS (पर्यावरण विज्ञान) विषय के उदाहरण लिए गए हैं| \n" +
                        "\n" +
                        "इस कोर्स को पर्यावरण-विज्ञान पढ़ानेवाले शिक्षक और वे शिक्षक जो ‘गतिविधियों को कक्षा में कैसे कराया जाये’ जानना चाहते हैं, कर सकते हैं| आशा है इस कोर्स को पढ़ने के बाद आपके लिए कक्षा में गतिविधियाँ कराना आसान हो जाएगा|"
        );

        adapter.setHeaderLayout(R.layout.t_row_course_material_header);
        adapter.setHeaderClickListener(v -> {
            switch (v.getId()) {
                case R.id.course_like_image:
                    like();
                    break;
                case R.id.course_bookmark_image:
                    bookmark();
                    break;
                case R.id.course_download_image:
                    downloadAllRemaining();
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
                    mActivity.showShortSnack("Item delete");
                    break;
                case R.id.item_btn:
                    mActivity.showShortSnack("Certicate view");
                    break;
                default:
                    mActivity.showShortSnack("Item clicked");
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
//                bookmarkIcon.set(R.drawable.t_icon_bookmark);
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
//                likeIcon.set(R.drawable.t_icon_like);
            }
        });
    }

    private void downloadAllRemaining(){

        mDataManager.downloadMultiple(remainingScorms, mActivity,
                new VideoDownloadHelper.DownloadManagerCallback() {
                    @Override
                    public void onDownloadStarted(Long result) {
                        Log.d("Download", "Started " + result);
                    }

                    @Override
                    public void onDownloadFailedToStart() {
                        Log.d("Download", "FailedToStart");
                    }

                    @Override
                    public void showProgressDialog(int numDownloads) {
                        Log.d("Download", "showProgressDialog " + numDownloads);
                    }

                    @Override
                    public void updateListUI() {
                        Log.d("Download", "updateListUI");
                    }

                    @Override
                    public boolean showInfoMessage(String message) {
                        Log.d("Download", "showInfoMessage " + message);
                        return false;
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
    }

    public void fetchCourseComponent() {
        mActivity.showLoading();
        mDataManager.getCourseComponent(course.getCourse().getId(),
                new OnResponseCallback<CourseComponent>() {
                    @Override
                    public void onSuccess(CourseComponent data) {
                        mActivity.hideLoading();
                        populateData(data);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });
    }

    private void populateData(CourseComponent component){

        adapter.setData(component);

    }

    public class CourseMaterialAdapter extends BaseRecyclerAdapter<CourseComponent> {

        private CourseComponent rootComponent;
        private int numOfTotalUnits = 0;
        private List<CourseComponent> components;

        public CourseMaterialAdapter() {
            components = new ArrayList<>();
        }

        private void setData(CourseComponent component){
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
                                    remainingScorms.add((ScormBlockModel) childComp);
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
            addAll(components);
        }

        @Override
        public void onBind(ViewDataBinding binding, CourseComponent item, OnHeaderClickListener headerClickListener, OnFooterClickListener footerClickListener, OnTaItemClickListener<CourseComponent> itemClickListener) {

            if (binding instanceof TRowCourseMaterialHeaderBinding) {
                TRowCourseMaterialHeaderBinding headerBinding = (TRowCourseMaterialHeaderBinding) binding;
                headerBinding.setViewModel(CourseMaterialViewModel.this);

                headerBinding.courseLikeImage.setOnClickListener(v -> {
                    if (headerClickListener != null) {
                        headerClickListener.onClick(v);
                    }
                });

                headerBinding.courseBookmarkImage.setOnClickListener(v -> {
                    if (headerClickListener != null) {
                        headerClickListener.onClick(v);
                    }
                });

                headerBinding.courseDownloadImage.setOnClickListener(v -> {
                    if (headerClickListener != null) {
                        headerClickListener.onClick(v);
                    }
                });

            } else if (binding instanceof TRowCourseMaterialFooterBinding) {
                TRowCourseMaterialFooterBinding footerBinding = (TRowCourseMaterialFooterBinding) binding;
                footerBinding.setViewModel(CourseMaterialViewModel.this);

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
