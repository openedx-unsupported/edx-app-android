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
import org.edx.mobile.tta.data.model.agenda.AgendaItem;
import org.edx.mobile.tta.data.model.agenda.AgendaList;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.utils.ContentSourceUtil;

import java.util.ArrayList;
import java.util.List;

public class AgendaViewModel extends BaseViewModel {

    public ObservableField<String> regionListTitle = new ObservableField<>("Region");

    public AgendaListAdapter stateListAdapter, myListAdapter, downloadListAdapter;

    public boolean regionListRecieved, myListRecieved, downloadListRecieved;

    public AgendaViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        stateListAdapter = new AgendaListAdapter(mActivity);
        myListAdapter = new AgendaListAdapter(mActivity);
        downloadListAdapter = new AgendaListAdapter(mActivity);

    }

    public void getAgenda(){
        getRegionAgenda();
        getMyAgenda();
        getDownloadAgenda();
    }

    private void getRegionAgenda() {
        mActivity.showLoading();
        regionListRecieved = false;

        mDataManager.getStateAgendaCount(new OnResponseCallback<List<AgendaList>>() {
            @Override
            public void onSuccess(List<AgendaList> data) {
                regionListRecieved = true;
                hideLoader();
                if (data != null && !data.isEmpty()){

                    AgendaList list = data.get(0);
                    if (list == null || list.getResult() == null || list.getResult().isEmpty()){
                        showEmptyAgendaList(stateListAdapter);
                    } else {
                        if (list.getResult().size() != 4){
                            AgendaItem item = new AgendaItem();
                            item.setSource_name("course");
                            if (!list.getResult().contains(item)){
                                item.setSource_title("कोर्स");
                                item.setContent_count(0);
                                list.getResult().add(0, item);
                            }

                            item = new AgendaItem();
                            item.setSource_name("chatshala");
                            if (!list.getResult().contains(item)){
                                item.setSource_title("Chatशाला");
                                item.setContent_count(0);
                                list.getResult().add(1, item);
                            }

                            item = new AgendaItem();
                            item.setSource_name("toolkit");
                            if (!list.getResult().contains(item)){
                                item.setSource_title("शिक्षण सामग्री");
                                item.setContent_count(0);
                                list.getResult().add(2, item);
                            }

                            item = new AgendaItem();
                            item.setSource_name("hois");
                            if (!list.getResult().contains(item)){
                                item.setSource_title("प्रेरणा स्त्रोत");
                                item.setContent_count(0);
                                list.getResult().add(3, item);
                            }
                        }
                        stateListAdapter.setItems(list.getResult());
                    }

                } else {
                    showEmptyAgendaList(stateListAdapter);
                }
            }

            @Override
            public void onFailure(Exception e) {
                regionListRecieved = true;
                hideLoader();
                showEmptyAgendaList(stateListAdapter);
                mActivity.showShortSnack(e.getLocalizedMessage());
            }
        });

    }

    private void showEmptyAgendaList(AgendaListAdapter adapter){
        List<AgendaItem> items = new ArrayList<>();

        AgendaItem item = new AgendaItem();
        item.setSource_title("कोर्स");
        item.setSource_name("course");
        item.setContent_count(0);
        items.add(item);

        item = new AgendaItem();
        item.setSource_title("Chatशाला");
        item.setSource_name("chatshala");
        item.setContent_count(0);
        items.add(item);

        item = new AgendaItem();
        item.setSource_title("शिक्षण सामग्री");
        item.setSource_name("toolkit");
        item.setContent_count(0);
        items.add(item);

        item = new AgendaItem();
        item.setSource_title("प्रेरणा स्त्रोत");
        item.setSource_name("hois");
        item.setContent_count(0);
        items.add(item);

        adapter.setItems(items);
    }

    private void getMyAgenda() {
        mActivity.showLoading();
        myListRecieved = false;

        mDataManager.getMyAgendaCount(new OnResponseCallback<AgendaList>() {
            @Override
            public void onSuccess(AgendaList data) {
                myListRecieved = true;
                hideLoader();
                if (data != null && data.getResult() != null && !data.getResult().isEmpty()){
                    if (data.getResult().size() != 4){
                        AgendaItem item = new AgendaItem();
                        item.setSource_name("course");
                        if (!data.getResult().contains(item)){
                            item.setSource_title("कोर्स");
                            item.setContent_count(0);
                            data.getResult().add(0, item);
                        }

                        item = new AgendaItem();
                        item.setSource_name("chatshala");
                        if (!data.getResult().contains(item)){
                            item.setSource_title("Chatशाला");
                            item.setContent_count(0);
                            data.getResult().add(1, item);
                        }

                        item = new AgendaItem();
                        item.setSource_name("toolkit");
                        if (!data.getResult().contains(item)){
                            item.setSource_title("शिक्षण सामग्री");
                            item.setContent_count(0);
                            data.getResult().add(2, item);
                        }

                        item = new AgendaItem();
                        item.setSource_name("hois");
                        if (!data.getResult().contains(item)){
                            item.setSource_title("प्रेरणा स्त्रोत");
                            item.setContent_count(0);
                            data.getResult().add(3, item);
                        }
                    }
                    myListAdapter.setItems(data.getResult());
                } else {
                    showEmptyAgendaList(myListAdapter);
                }
            }

            @Override
            public void onFailure(Exception e) {
                myListRecieved = true;
                hideLoader();
                showEmptyAgendaList(myListAdapter);
                mActivity.showShortSnack(e.getLocalizedMessage());
            }
        });
    }

    private void getDownloadAgenda() {
        mActivity.showLoading();
        downloadListRecieved = false;

        mDataManager.getDownloadAgendaCount(new OnResponseCallback<AgendaList>() {
            @Override
            public void onSuccess(AgendaList data) {
                downloadListRecieved = true;
                hideLoader();
                if (data != null && data.getResult() != null && !data.getResult().isEmpty()){
                    if (data.getResult().size() != 4){
                        AgendaItem item = new AgendaItem();
                        item.setSource_name("course");
                        if (!data.getResult().contains(item)){
                            item.setSource_title("कोर्स");
                            item.setContent_count(0);
                            data.getResult().add(0, item);
                        }

                        item = new AgendaItem();
                        item.setSource_name("chatshala");
                        if (!data.getResult().contains(item)){
                            item.setSource_title("Chatशाला");
                            item.setContent_count(0);
                            data.getResult().add(1, item);
                        }

                        item = new AgendaItem();
                        item.setSource_name("toolkit");
                        if (!data.getResult().contains(item)){
                            item.setSource_title("शिक्षण सामग्री");
                            item.setContent_count(0);
                            data.getResult().add(2, item);
                        }

                        item = new AgendaItem();
                        item.setSource_name("hois");
                        if (!data.getResult().contains(item)){
                            item.setSource_title("प्रेरणा स्त्रोत");
                            item.setContent_count(0);
                            data.getResult().add(3, item);
                        }
                    }
                    downloadListAdapter.setItems(data.getResult());
                } else {
                    showEmptyAgendaList(downloadListAdapter);
                }
            }

            @Override
            public void onFailure(Exception e) {
                downloadListRecieved = true;
                hideLoader();
                showEmptyAgendaList(downloadListAdapter);
                mActivity.showShortSnack(e.getLocalizedMessage());
            }
        });
    }

    private void hideLoader(){
        if (regionListRecieved && myListRecieved && downloadListRecieved){
            mActivity.hideLoading();
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
                    itemBinding.agendaCard.setCardBackgroundColor(ContextCompat.getColor(mActivity, ContentSourceUtil.getSourceColor(model.getSource_name())));
                } else {
                    itemBinding.agendaCard.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_grey_light));
                }
                itemBinding.agendaItemCount.setText(String.valueOf(model.getContent_count()));
                itemBinding.agendaSource.setText(model.getSource_title());
                itemBinding.agendaSource.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContentSourceUtil.getSourceDrawable_15x15(model.getSource_name()),
                        0,0,0
                );
            }
        }
    }
}
