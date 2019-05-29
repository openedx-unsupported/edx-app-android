package org.edx.mobile.tta.ui.agenda_items.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowAgendaContentBinding;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.data.enums.SourceName;
import org.edx.mobile.tta.data.enums.SourceType;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.local.db.table.ContentStatus;
import org.edx.mobile.tta.data.model.agenda.AgendaItem;
import org.edx.mobile.tta.data.model.agenda.AgendaList;
import org.edx.mobile.tta.event.ContentBookmarkChangedEvent;
import org.edx.mobile.tta.event.ContentStatusReceivedEvent;
import org.edx.mobile.tta.event.ContentStatusesReceivedEvent;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.connect.ConnectDashboardActivity;
import org.edx.mobile.tta.ui.course.CourseDashboardActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.utils.ContentSourceUtil;
import org.edx.mobile.tta.utils.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class AgendaItemViewModel extends BaseViewModel {
    public AgendaItem agendaItem;
    public ListingRecyclerAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;
    public List<Content> contents;
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableInt emptyImage = new ObservableInt(R.drawable.t_icon_course_130);
    public SpannableString emptyMessage;
    private Content selectedContent;
    private String toolBarData;
    private AgendaList agendaList;
    private Map<Long, ContentStatus> contentStatusMap;

    public AgendaItemViewModel(Context context, TaBaseFragment fragment, AgendaItem agendaItem, String toolbarData, AgendaList agendaList) {
        super(context, fragment);
        this.agendaItem = agendaItem;
        this.toolBarData = toolbarData;
        this.agendaList = agendaList;
        contents = new ArrayList<>();
        contentStatusMap = new HashMap<>();

        adapter = new ListingRecyclerAdapter(mActivity);
        adapter.setItemClickListener((view, item) -> {
            selectedContent = item;
            showContentDashboard();
        });

        setEmptyView();
        getData();
    }

    private void setEmptyView() {

        if (toolBarData.equalsIgnoreCase(mActivity.getString(R.string.state_wise_list))) {
            emptyMessage = new SpannableString(String.format(
                    mActivity.getString(R.string.empty_region_wise_list),
                    toolBarData, agendaItem.getSource_title()));
        } else if (toolBarData.equalsIgnoreCase(mActivity.getString(R.string.my_agenda))) {
            emptyMessage = new SpannableString(String.format(
                    mActivity.getString(R.string.empty_my_agenda_list),
                    toolBarData, agendaItem.getSource_title(), agendaItem.getSource_title()));
            Drawable d = ContextCompat.getDrawable(mActivity, R.drawable.t_icon_bookmark);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
            int imageIndex = emptyMessage.toString().indexOf("@");
            emptyMessage.setSpan(span, imageIndex, imageIndex+1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            emptyMessage = new SpannableString(String.format(
                    mActivity.getString(R.string.empty_download_list),
                    toolBarData, agendaItem.getSource_title()));
        }

        try {
            switch (SourceName.valueOf(agendaItem.getSource_name())){
                case course:
                    emptyImage.set(R.drawable.t_icon_course_130);
                    break;
                case chatshala:
                    emptyImage.set(R.drawable.t_icon_chatshala_130);
                    break;
                case toolkit:
                    emptyImage.set(R.drawable.t_icon_toolkit_130);
                    break;
                default:
                    emptyImage.set(R.drawable.t_icon_course_130);
                    break;
            }
        } catch (IllegalArgumentException e) {
            emptyImage.set(R.drawable.t_icon_course_130);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new GridLayoutManager(mActivity, 2);
    }

    private void getData() {
        mActivity.showLoading();
        if (toolBarData.equalsIgnoreCase(mActivity.getString(R.string.my_agenda))) {
            mDataManager.getMyAgendaContent(agendaItem.getSource_id(), new OnResponseCallback<List<Content>>() {
                @Override
                public void onSuccess(List<Content> data) {
                    mActivity.hideLoading();
                    contents = data;
                    adapter.setItems(contents);
                    toggleEmptyVisibility();
                }

                @Override
                public void onFailure(Exception e) {
                    mActivity.hideLoading();
                    toggleEmptyVisibility();
//                    mActivity.showLongSnack(e.getLocalizedMessage());
                }
            });
        } else if (toolBarData.equalsIgnoreCase(mActivity.getString(R.string.state_wise_list))) {
            if (agendaList != null) {
                mDataManager.getStateAgendaContent(agendaItem.getSource_id(), agendaList.getList_id(), new OnResponseCallback<List<Content>>() {
                    @Override
                    public void onSuccess(List<Content> data) {
                        mActivity.hideLoading();
                        contents = data;
                        adapter.setItems(contents);
                        toggleEmptyVisibility();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        toggleEmptyVisibility();
                        //                    mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });
            } else {
                mActivity.hideLoading();
                toggleEmptyVisibility();
            }
        } else {
            mDataManager.getDownloadedContent(agendaItem.getSource_name(), new OnResponseCallback<List<Content>>() {
                @Override
                public void onSuccess(List<Content> data) {
                    mActivity.hideLoading();
                    contents = data;
                    adapter.setItems(contents);
                    toggleEmptyVisibility();
                }

                @Override
                public void onFailure(Exception e) {
                    mActivity.hideLoading();
                    toggleEmptyVisibility();
//                    mActivity.showLongSnack(e.getLocalizedMessage());
                }
            });
        }
    }

    private void toggleEmptyVisibility() {
        if (contents == null || contents.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    public void showContentDashboard() {

        Bundle parameters = new Bundle();
        parameters.putParcelable(Constants.KEY_CONTENT, selectedContent);
        if (selectedContent.getSource().getType().equalsIgnoreCase(SourceType.course.name()) ||
                selectedContent.getSource().getType().equalsIgnoreCase(SourceType.edx.name())) {
            ActivityUtil.gotoPage(mActivity, CourseDashboardActivity.class, parameters);

            Nav nav;
            if (toolBarData.equalsIgnoreCase(mActivity.getString(R.string.state_wise_list))){
                nav = Nav.state_agenda;
            } else if (toolBarData.equalsIgnoreCase(mActivity.getString(R.string.my_agenda))) {
                nav = Nav.my_agenda;
            } else {
                nav = Nav.download_agenda;
            }

            mActivity.analytic.addMxAnalytics_db(
                    selectedContent.getName() , Action.CourseOpen, nav.name(),
                    org.edx.mobile.tta.analytics.analytics_enums.Source.Mobile, selectedContent.getSource_identity());

        } else {
            ActivityUtil.gotoPage(mActivity, ConnectDashboardActivity.class, parameters);
        }

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ContentStatusesReceivedEvent event){

        for (ContentStatus status: event.getStatuses()){
            contentStatusMap.put(status.getContent_id(), status);
        }
        adapter.notifyDataSetChanged();

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ContentStatusReceivedEvent event){
        contentStatusMap.put(event.getContentStatus().getContent_id(), event.getContentStatus());
        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ContentBookmarkChangedEvent event){
        if (toolBarData.equalsIgnoreCase(mActivity.getString(R.string.my_agenda)) &&
                agendaItem.getSource_id() == event.getContent().getSource().getId()
        ) {
            if (!event.isBookmarked()){
                contents.remove(event.getContent());
            } else if (!contents.contains(event.getContent())){
                contents.add(event.getContent());
            }
            adapter.notifyDataSetChanged();
            toggleEmptyVisibility();
        }
    }

    public void registerEventBus(){
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus(){
        EventBus.getDefault().unregister(this);
    }

    public class ListingRecyclerAdapter extends MxInfiniteAdapter<Content> {
        public ListingRecyclerAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Content model, @Nullable OnRecyclerItemClickListener<Content> listener) {
            if (binding instanceof TRowAgendaContentBinding) {
                TRowAgendaContentBinding contentBinding = (TRowAgendaContentBinding) binding;
//                CardView.LayoutParams layoutParams = (CardView.LayoutParams) contentBinding.cardView.getLayoutParams();
//                layoutParams.width = 180;
//                   // contentBinding.cardView.setMinimumWidth(250);
                contentBinding.contentCategory.setText(model.getSource().getTitle());
                contentBinding.contentCategory.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContentSourceUtil.getSourceDrawable_10x10(model.getSource().getName()),
                        0, 0, 0);
                contentBinding.contentTitle.setText(model.getName());
                Glide.with(getContext())
                        .load(model.getIcon())
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(contentBinding.contentImage);

                contentBinding.getRoot().setOnClickListener(v -> listener.onItemClick(v, model));

                if (contentStatusMap.containsKey(model.getId())){
                    ContentStatus status = contentStatusMap.get(model.getId());
                    if (status.getCompleted() != null){
                        contentBinding.contentStatusImage.setImageResource(R.drawable.t_icon_done);
                        contentBinding.contentStatusImage.setVisibility(View.VISIBLE);
                    } else if (status.getStarted() != null){
                        contentBinding.contentStatusImage.setImageResource(R.drawable.t_icon_refresh);
                        contentBinding.contentStatusImage.setVisibility(View.VISIBLE);
                    } else {
                        contentBinding.contentStatusImage.setVisibility(View.GONE);
                    }
                } else {
                    contentBinding.contentStatusImage.setVisibility(View.GONE);
                }

            }
        }
    }
}
