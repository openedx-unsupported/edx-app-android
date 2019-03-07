package org.edx.mobile.tta.ui.agenda_items.view_model;

import android.Manifest;
import android.content.Context;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.GridLayout;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;

import org.edx.mobile.databinding.TRowAgendaContentBinding;
import org.edx.mobile.databinding.TRowContentBinding;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.agenda.AgendaItem;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.connect.ConnectDashboardActivity;
import org.edx.mobile.tta.ui.course.CourseDashboardFragment;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.utils.ContentSourceUtil;
import org.edx.mobile.util.PermissionsUtil;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class AgendaItemsTabViewModel extends BaseViewModel {
    public AgendaItem agendaItem;
    public ListingRecyclerAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;
    public List<Content> contents;
    private Content selectedContent;
    private String toolBarData;

    public AgendaItemsTabViewModel(Context context, TaBaseFragment fragment, AgendaItem agendaItem,String toolbarData) {
        super(context, fragment);
        this.agendaItem = agendaItem;
        this.toolBarData = toolbarData;
        contents = new ArrayList<>();

      adapter = new ListingRecyclerAdapter(mActivity);
        adapter.setItemClickListener((view, item) -> {
            selectedContent = item;
            mFragment.askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST);
        });

      getData();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new GridLayoutManager(mActivity, 2);
    }

    private void getData() {
        mActivity.showLoading();
        if (toolBarData.equalsIgnoreCase(mActivity.getString(R.string.my_agenda))){
            mDataManager.getrMyAgendaContent(agendaItem.getSource_id(), new OnResponseCallback<List<Content>>() {
                @Override
                public void onSuccess(List<Content> data) {
                    mActivity.hideLoading();
                    contents = data;
                    adapter.setItems(contents);
                }

                @Override
                public void onFailure(Exception e) {
                    mActivity.hideLoading();
                    mActivity.showLongSnack(e.getLocalizedMessage());
                }
            });
        }else{
            mDataManager.getStateAgendaContent(agendaItem.getSource_id(), new OnResponseCallback<List<Content>>() {
                @Override
                public void onSuccess(List<Content>data) {
                    mActivity.hideLoading();
                    contents = data;
                    adapter.setItems(contents);
                }

                @Override
                public void onFailure(Exception e) {
                    mActivity.hideLoading();
                    mActivity.showLongSnack(e.getLocalizedMessage());
                }
            });
        }
    }
    public void showContentDashboard(){
        if (selectedContent.getSource().getType().equalsIgnoreCase("edx") ||
                selectedContent.getSource().getType().equalsIgnoreCase("course")) {
            ActivityUtil.replaceFragmentInActivity(
                    mActivity.getSupportFragmentManager(),
                    CourseDashboardFragment.newInstance(selectedContent),
                    R.id.dashboard_fragment,
                    CourseDashboardFragment.TAG,
                    true,
                    null
            );
        } else {
            Bundle parameters = new Bundle();
            parameters.putParcelable(Constants.KEY_CONTENT, selectedContent);
            ActivityUtil.gotoPage(mActivity, ConnectDashboardActivity.class, parameters);
        }

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

            }
        }
    }
}
