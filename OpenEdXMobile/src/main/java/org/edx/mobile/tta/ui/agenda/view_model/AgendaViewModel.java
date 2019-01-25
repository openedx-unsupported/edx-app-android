package org.edx.mobile.tta.ui.agenda.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;
import com.maurya.mx.mxlib.view.MxFiniteRecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowAgendaItemBinding;
import org.edx.mobile.tta.data.model.AgendaItem;
import org.edx.mobile.tta.data.model.AgendaList;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.agenda.AgendaFragment;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.utils.SourceUtil;

import java.util.List;

public class AgendaViewModel extends BaseViewModel {

    public ObservableField<String> regionListTitle = new ObservableField<>("Region");

    public AgendaListAdapter regionListAdapter, myListAdapter, downloadListAdapter;

    public boolean regionListRecieved, myListRecieved, downloadListRecieved;

    public ObservableBoolean myAgendaVisible = new ObservableBoolean();
    public ObservableBoolean downloadAgendaVisible = new ObservableBoolean();

    public AgendaViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        regionListAdapter = new AgendaListAdapter(mActivity);
        myListAdapter = new AgendaListAdapter(mActivity);
        downloadListAdapter = new AgendaListAdapter(mActivity);

    }

    public void getAgenda(){
        getRegionAgenda();
        getMyAgenda();
        getDownloadAgenda();
    }

    private void getRegionAgenda() {
        mActivity.show();
        regionListRecieved = false;

        mDataManager.getStateAgendaCount(new OnResponseCallback<List<AgendaList>>() {
            @Override
            public void onSuccess(List<AgendaList> data) {
                regionListRecieved = true;
                hideLoader();
                if (data != null && !data.isEmpty()){

                    for (AgendaList agendaList: data){
                        if (agendaList != null && agendaList.getResult() != null && !agendaList.getResult().isEmpty()){
                            MxFiniteRecyclerView view = ((AgendaFragment) mFragment).addRegionList();
                            view.setTitleText(agendaList.getLevel() + " wise list");
                            AgendaListAdapter adapter = new AgendaListAdapter(mActivity);
                            adapter.addAll(agendaList.getResult());
                            view.setAdapter(adapter);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Exception e) {
                regionListRecieved = true;
                hideLoader();
                mActivity.showShortSnack(e.getLocalizedMessage());
            }
        });

    }

    private void getMyAgenda() {
        mActivity.show();
        myListRecieved = false;

        mDataManager.getMyAgendaCount(new OnResponseCallback<AgendaList>() {
            @Override
            public void onSuccess(AgendaList data) {
                myListRecieved = true;
                hideLoader();
                if (data != null && data.getResult() != null){
                    myListAdapter.clear();
                    myListAdapter.addAll(data.getResult());
                    if (myListAdapter.getItemCount() > 0) {
                        myAgendaVisible.set(true);
                    } else {
                        myAgendaVisible.set(false);
                    }
                } else {
                    myAgendaVisible.set(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                myListRecieved = true;
                hideLoader();
                myAgendaVisible.set(false);
                mActivity.showShortSnack(e.getLocalizedMessage());
            }
        });
    }

    private void getDownloadAgenda() {
        mActivity.show();
        downloadListRecieved = false;

        mDataManager.getDownloadAgendaCount(new OnResponseCallback<AgendaList>() {
            @Override
            public void onSuccess(AgendaList data) {
                downloadListRecieved = true;
                hideLoader();
                if (data != null && data.getResult() != null){
                    //downloadListAdapter.clear();
                    downloadListAdapter.setItems(data.getResult());
                    if (downloadListAdapter.getItemCount() > 0) {
                        downloadAgendaVisible.set(true);
                    } else {
                        downloadAgendaVisible.set(false);
                    }
                } else {
                    downloadAgendaVisible.set(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                downloadListRecieved = true;
                hideLoader();
                downloadAgendaVisible.set(false);
                mActivity.showShortSnack(e.getLocalizedMessage());
            }
        });
    }

    private void hideLoader(){
        if (regionListRecieved && myListRecieved && downloadListRecieved){
            mActivity.hide();
        }
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
                    itemBinding.agendaCard.setCardBackgroundColor(ContextCompat.getColor(mActivity, SourceUtil.getSourceColor(model.getSource_name())));
                } else {
                    itemBinding.agendaCard.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_grey_light));
                }
                itemBinding.agendaItemCount.setText(String.valueOf(model.getContent_count()));
                itemBinding.agendaSource.setText(model.getSource_name());
            }
        }
    }
}
