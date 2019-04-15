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
import org.edx.mobile.tta.data.local.db.table.Source;
import org.edx.mobile.tta.data.model.agenda.AgendaItem;
import org.edx.mobile.tta.data.model.agenda.AgendaList;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.agenda_items.AgendaListFragment;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.utils.ContentSourceUtil;

import java.util.ArrayList;
import java.util.List;

public class AgendaViewModel extends BaseViewModel {

    public List<Source> sources;

    public ObservableField<String> regionListTitle = new ObservableField<>("Region");

    public AgendaListAdapter stateListAdapter, myListAdapter, downloadListAdapter;

    public boolean regionListRecieved, myListRecieved, downloadListRecieved;

    public AgendaViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
        stateListAdapter = new AgendaListAdapter(mActivity,mActivity.getString(R.string.state_wise_list));
        myListAdapter = new AgendaListAdapter(mActivity,mActivity.getString(R.string.my_agenda));
        downloadListAdapter = new AgendaListAdapter(mActivity,mActivity.getString(R.string.download));

    }

    public void getAgenda(){
        mActivity.showLoading();

        mDataManager.getSources(new OnResponseCallback<List<Source>>() {
            @Override
            public void onSuccess(List<Source> data) {
                sources = data;
                getRegionAgenda();
                getMyAgenda();
                getDownloadAgenda();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    private void getRegionAgenda() {
        regionListRecieved = false;
        mDataManager.getStateAgendaCount(new OnResponseCallback<List<AgendaList>>() {
            @Override
            public void onSuccess(List<AgendaList> data) {
                regionListRecieved = true;
                hideLoader();
                if (data != null && !data.isEmpty()){

                    AgendaList list = data.get(0);
                    stateListAdapter.setAgendaList(list);
                    if (list == null || list.getResult() == null || list.getResult().isEmpty()){
                        showEmptyAgendaList(stateListAdapter);
                    } else {
                        List<AgendaItem> items = list.getResult();
                        if (items.size() != sources.size()){

                            for (Source source: sources){
                                AgendaItem item = new AgendaItem();
                                item.setSource_name(source.getName());
                                if (!items.contains(item)) {
                                    item.setSource_title(source.getTitle());
                                    item.setContent_count(0);
                                    items.add(item);
                                }
                            }

                            /*AgendaItem item = new AgendaItem();
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
                            }*/
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
            }
        });

    }

    private void showEmptyAgendaList(AgendaListAdapter adapter){
        List<AgendaItem> items = new ArrayList<>();

        for (Source source: sources){
            AgendaItem item = new AgendaItem();
            item.setSource_title(source.getTitle());
            item.setSource_name(source.getName());
            item.setContent_count(0);
            items.add(item);
        }

        adapter.setItems(items);
    }

    private void getMyAgenda() {
        myListRecieved = false;
        mDataManager.getMyAgendaCount(new OnResponseCallback<AgendaList>() {
            @Override
            public void onSuccess(AgendaList data) {
                myListRecieved = true;
                hideLoader();
                if (data != null && data.getResult() != null && !data.getResult().isEmpty()){
                    List<AgendaItem> items = data.getResult();
                    if (items.size() != sources.size()){

                        for (Source source: sources){
                            AgendaItem item = new AgendaItem();
                            item.setSource_name(source.getName());
                            if (!items.contains(item)) {
                                item.setSource_title(source.getTitle());
                                item.setContent_count(0);
                                items.add(item);
                            }
                        }

                        /*AgendaItem item = new AgendaItem();
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
                        }*/
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
            }
        });
    }

    private void getDownloadAgenda() {
        mActivity.showLoading();
        downloadListRecieved = false;

        mDataManager.getDownloadAgendaCount(sources, new OnResponseCallback<AgendaList>() {
            @Override
            public void onSuccess(AgendaList data) {
                downloadListRecieved = true;
                hideLoader();
                if (data != null && data.getResult() != null && !data.getResult().isEmpty()){
                    List<AgendaItem> items = data.getResult();
                    if (items.size() != sources.size()){

                        for (Source source: sources){
                            AgendaItem item = new AgendaItem();
                            item.setSource_name(source.getName());
                            if (!items.contains(item)) {
                                item.setSource_title(source.getTitle());
                                item.setContent_count(0);
                                items.add(item);
                            }
                        }

                        /*AgendaItem item = new AgendaItem();
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
                        }*/
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
            }
        });
    }

    private void hideLoader(){
        if (regionListRecieved && myListRecieved && downloadListRecieved){
            mActivity.hideLoading();
        }
    }


    public class AgendaListAdapter extends MxFiniteAdapter<AgendaItem> {
        private String agendaListName;
        private AgendaList agendaList;

        public AgendaListAdapter(Context context, String string) {
            super(context);
            agendaListName =string;
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
                itemBinding.agendaCard.setOnClickListener(v -> {
                    ActivityUtil.replaceFragmentInActivity(
                            mActivity.getSupportFragmentManager(),
                            AgendaListFragment.newInstance(agendaListName, getItems(),model, agendaList),
                            R.id.dashboard_fragment,
                            AgendaListFragment.TAG,
                            true,
                            null
                    );
                });
            }
        }

        public void setAgendaList(AgendaList agendaList) {
            this.agendaList = agendaList;
        }
    }
}
