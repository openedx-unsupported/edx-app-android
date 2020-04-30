package org.humana.mobile.tta.ui.programs.curricullam;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowCurricullamBinding;
import org.humana.mobile.model.download.PDFDownloadModel;
import org.humana.mobile.services.DownloadService;
import org.humana.mobile.tta.data.local.db.table.CurricullamChaptersModel;
import org.humana.mobile.tta.data.local.db.table.CurricullamModel;
import org.humana.mobile.tta.data.local.db.table.Period;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.utils.AppUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CurricullamViewModel extends BaseViewModel {

    public CurricullamAdapter curricullamAdapter;
    private List<CurricullamChaptersModel> curricullamModelList;
    private String programId;
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public RecyclerView.LayoutManager layoutManager;
    private List<CurricullamChaptersModel> savedChapters = new ArrayList<>();


    public CurricullamViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
        curricullamModelList = new ArrayList<>();
        mActivity.showLoading();
        programId = encode("course-v1:Humana+NT101+NeTT_FNK-A");
        layoutManager = new LinearLayoutManager(mActivity);

        curricullamAdapter = new CurricullamAdapter(mActivity);
        curricullamAdapter.setItems(curricullamModelList);

        fetchPrograms();

        curricullamAdapter.setItemClickListener(((view, item) -> {
            if (AppUtil.isReadStoragePermissionGranted(mActivity)
                    && AppUtil.isWriteStoragePermissionGranted(mActivity)) {
                if (item.getDownloadStatus() != null) {
                    if (!item.getDownloadStatus().equals("Downloaded")) {
                        item.setDownloadStatus(mActivity.getString(R.string.downloading));
                        curricullamAdapter.notifyItemChanged(curricullamAdapter.getItemPosition(item));
                        DownloadService.getDownloadService(mActivity,
                                item.getUrl(),
                                item.getTitle(),
                                item,
                                true);

                    } else {
                        Uri uri = Uri.parse(item.getUrl());
                        DownloadService.openDownloadedFile(uri, mActivity);
                    }
                } else {
                    item.setDownloadStatus(mActivity.getString(R.string.downloading));
                    curricullamAdapter.notifyItemChanged(curricullamAdapter.getItemPosition(item));
                    DownloadService.getDownloadService(mActivity,
                            item.getUrl(),
                            item.getTitle(),
                            item,
                            true);
                }
            }
        }));
    }


    public void fetchPrograms() {
        mActivity.showLoading();
        mDataManager.getCurricullam(programId, new OnResponseCallback<CurricullamModel>() {
            @Override
            public void onSuccess(CurricullamModel data) {

                List<CurricullamChaptersModel> ps = new ArrayList<>();
                ps = data.getChapters();
                if (savedChapters.size() != 0) {
                    for (CurricullamChaptersModel savedChapter : savedChapters) {
                        for (int i = 0; i < data.getChapters().size(); i++) {
                            if (data.getChapters().get(i).getTitle().equals(savedChapter.getTitle()) &&
                                    data.getChapters().get(i).getUrl().equalsIgnoreCase(savedChapter.getUrl())) {
                                ps.remove(data.getChapters().get(i));
                                ps.add(i, savedChapter);
                                break;
                            }
                        }
                    }
                    populateCurricullam(ps);
                } else {
                    populateCurricullam(ps);
                }
//                populateCurricullam(data.getChapters());
                curricullamAdapter.setLoadingDone();
                mActivity.hideLoading();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                toggleEmptyVisibility();
            }
        });

    }

    private void populateCurricullam(List<CurricullamChaptersModel> data) {
        boolean newItemsAdded = false;
        int n = 0;

            for (CurricullamChaptersModel user : data) {
                if (!curricullamModelList.contains(user)) {
                    curricullamModelList.add(user);
                    newItemsAdded = true;
                    n++;
                }
            }


            if (newItemsAdded) {
                curricullamAdapter.notifyItemRangeInserted(curricullamModelList.size() - n, n);
            }

        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility() {
        if (curricullamModelList == null || curricullamModelList.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDownloadCurricullamDesc(mDataManager.getLoginPrefs().getUsername());
    }

    public class CurricullamAdapter extends MxInfiniteAdapter<CurricullamChaptersModel> {

        public CurricullamAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull CurricullamChaptersModel model,
                           @Nullable OnRecyclerItemClickListener<CurricullamChaptersModel> listener) {
            if (binding instanceof TRowCurricullamBinding) {
                TRowCurricullamBinding itemBinding = (TRowCurricullamBinding) binding;
                itemBinding.textName.setText(model.getTitle());

                if (model.getDownloadStatus() != null) {
                    if (model.getDownloadStatus()
                            .equalsIgnoreCase(mActivity.getString(R.string.downloaded))) {
                        itemBinding.txtReadMore.setBackground(
                                ContextCompat.getDrawable(mActivity, R.drawable.ic_after_dnd));
                        itemBinding.pbDownload.setVisibility(View.GONE);
                        itemBinding.llReadMore.setVisibility(View.VISIBLE);
                        itemBinding.tvReadMore.setVisibility(View.VISIBLE);
                    } else if (model.getDownloadStatus().equals(mActivity.getString(R.string.downloading))) {
                        itemBinding.pbDownload.setVisibility(View.VISIBLE);
                        itemBinding.llReadMore.setVisibility(View.GONE);
                    }
                }

                itemBinding.llCard.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });


            }
        }
    }

    public static String encode(String url) {

        try {

            String encodeURL = URLEncoder.encode(url, "UTF-8");

            return encodeURL;

        } catch (UnsupportedEncodingException e) {

            return "Issue while encoding" + e.getMessage();

        }

    }

    private void getDownloadCurricullamDesc(String url) {
        savedChapters.clear();
        mDataManager.getCurricullamChaptersDesc(url,
                new OnResponseCallback<List<CurricullamChaptersModel>>() {
                    @Override
                    public void onSuccess(List<CurricullamChaptersModel> chapters) {
                        for (CurricullamChaptersModel chapter : chapters) {
                            if (chapter.getDownloadStatus() != null) {
                                savedChapters.add(chapter);
                            }
                        }


                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });

    }


    public void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(PDFDownloadModel model) {
        if (model.isDownload()) {
//            progressVisible.set(false);
            curricullamAdapter.notifyDataSetChanged();
        }
    }
}
