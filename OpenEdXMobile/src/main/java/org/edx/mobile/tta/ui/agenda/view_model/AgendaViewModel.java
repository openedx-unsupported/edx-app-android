package org.edx.mobile.tta.ui.agenda.view_model;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowAgendaItemBinding;
import org.edx.mobile.tta.data.model.AgendaItem;
import org.edx.mobile.tta.data.model.AgendaList;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.utils.SourceUtil;

public class AgendaViewModel extends BaseViewModel {

    public ObservableField<String> regionListTitle = new ObservableField<>("Region");

    public AgendaListAdapter regionListAdapter, myListAdapter, downloadListAdapter;

    public AgendaViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        regionListAdapter = new AgendaListAdapter(mActivity);
        myListAdapter = new AgendaListAdapter(mActivity);
        downloadListAdapter = new AgendaListAdapter(mActivity);

        getRegionAgenda();
        getMyAgenda();
        getDownloadAgenda();
    }

    private void getRegionAgenda() {
        mActivity.show();

        mDataManager.getStateAgendaCount(new OnResponseCallback<AgendaList>() {
            @Override
            public void onSuccess(AgendaList data) {
                mActivity.hide();
                if (data != null && data.getResult() != null){
                    regionListAdapter.clear();
                    regionListAdapter.addAll(data.getResult());
                }
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hide();
                mActivity.showShortSnack(e.getLocalizedMessage());
            }
        });

    }

    private void getMyAgenda() {
        mActivity.show();

        mDataManager.getMyAgendaCount(new OnResponseCallback<AgendaList>() {
            @Override
            public void onSuccess(AgendaList data) {
                mActivity.hide();
                if (data != null && data.getResult() != null){
                    myListAdapter.clear();
                    myListAdapter.addAll(data.getResult());
                }
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hide();
                mActivity.showShortSnack(e.getLocalizedMessage());
            }
        });
    }

    private void getDownloadAgenda() {

    }

    public class AgendaListAdapter extends MxFiniteAdapter<AgendaItem> {
        public AgendaListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull AgendaItem model, @Nullable OnRecyclerItemClickListener<AgendaItem> listener) {
            if (binding instanceof TRowAgendaItemBinding){
                TRowAgendaItemBinding itemBinding = (TRowAgendaItemBinding) binding;

                if (model.getContent_count() > 0) {
                    itemBinding.getRoot().setBackgroundColor(ContextCompat.getColor(mActivity, SourceUtil.getSourceColor(model.getSource_name())));
                } else {
                    itemBinding.getRoot().setBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_grey_light));
                }
                itemBinding.agendaItemCount.setText(String.valueOf(model.getContent_count()));
                itemBinding.agendaSource.setText(model.getSource_name());
            }
        }
    }
}
