package org.edx.mobile.tta.data;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxDataManager;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.ILocalDataSource;
import org.edx.mobile.tta.data.local.db.LocalDataSource;
import org.edx.mobile.tta.data.local.db.TADatabase;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.AgendaItem;
import org.edx.mobile.tta.data.model.AgendaList;
import org.edx.mobile.tta.data.model.BaseResponse;
import org.edx.mobile.tta.data.model.CollectionConfigResponse;
import org.edx.mobile.tta.data.model.CollectionItemsResponse;
import org.edx.mobile.tta.data.model.ConfigModifiedDateResponse;
import org.edx.mobile.tta.data.model.EmptyResponse;
import org.edx.mobile.tta.data.pref.AppPref;
import org.edx.mobile.tta.data.remote.IRemoteDataSource;
import org.edx.mobile.tta.data.remote.RetrofitServiceUtil;
import org.edx.mobile.tta.exception.NoConnectionException;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.task.agenda.GetMyAgendaCountTask;
import org.edx.mobile.tta.task.agenda.GetStateAgendaCountTask;
import org.edx.mobile.tta.task.library.GetCollectionConfigTask;
import org.edx.mobile.tta.task.library.GetCollectionItemsTask;
import org.edx.mobile.tta.task.library.GetConfigModifiedDateTask;
import org.edx.mobile.tta.task.profile.GetUserAddressTask;
import org.edx.mobile.tta.ui.logistration.model.LoginRequest;
import org.edx.mobile.tta.ui.logistration.model.LoginResponse;
import org.edx.mobile.tta.ui.logistration.model.UserAddressResponse;
import org.edx.mobile.tta.utils.RxUtil;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

import static org.edx.mobile.tta.Constants.TA_DATABASE;

/**
 * Created by Arjun on 2018/9/18.
 */

public class DataManager extends  BaseRoboInjector {
    private Context context;
    private static DataManager mDataManager;
    private IRemoteDataSource mRemoteDataSource;
    private ILocalDataSource mLocalDataSource;
    @Inject
    public IEdxDataManager edxDataManager;

    @com.google.inject.Inject
    private IEdxEnvironment edxEnvironment;

    @com.google.inject.Inject
    private Config config;

    private AppPref mAppPref;
    private LoginPrefs loginPrefs;

    private DataManager(Context context, IRemoteDataSource remoteDataSource, ILocalDataSource localDataSource) {
        super(context);
        this.context = context;
        mRemoteDataSource = remoteDataSource;
        mLocalDataSource = localDataSource;

        mAppPref = new AppPref(context);
        loginPrefs = new LoginPrefs(context);
    }

    public static DataManager getInstance( Context context) {
        if (mDataManager == null) {
            synchronized (DataManager.class) {
                if (mDataManager == null) {
                    mDataManager = new DataManager(context, RetrofitServiceUtil.create(),
                            new LocalDataSource(Room.databaseBuilder(context, TADatabase.class, TA_DATABASE).fallbackToDestructiveMigration()
                                    .build()));
                }
            }
        }
        return mDataManager;
    }

    public IEdxEnvironment getEdxEnvironment() {
        return edxEnvironment;
    }

    public Config getConfig() {
        return config;
    }

    private <T> Observable<T> preProcess(Observable<BaseResponse<T>> observable) {
        return observable.compose(RxUtil.applyScheduler())
                .map(RxUtil.unwrapResponse(null));
    }

    private <T> Observable<T> preProcess(Observable<BaseResponse<T>> observable, Class<T> cls) {
        return observable.compose(RxUtil.applyScheduler())
                .map(RxUtil.unwrapResponse(cls));
    }

    private Observable<EmptyResponse> preEmptyProcess(Observable<BaseResponse<EmptyResponse>> observable) {
        return preProcess(observable, EmptyResponse.class);
    }

    public AppPref getAppPref() {
        return mAppPref;
    }

    public LoginPrefs getLoginPrefs() {
        return loginPrefs;
    }

    public Observable<LoginResponse> login(LoginRequest loginRequest) {
        return preProcess(mRemoteDataSource.login(loginRequest));
    }

    public void logout(){
        edxEnvironment.getRouter().performManualLogout(
                context,
                mDataManager.getEdxEnvironment().getAnalyticsRegistry(),
                mDataManager.getEdxEnvironment().getNotificationDelegate());

        new Thread(){
            @Override
            public void run() {
                mLocalDataSource.clear();
            }
        }.start();
    }

    public Observable<EmptyResponse> getEmpty() {
        return preEmptyProcess(mRemoteDataSource.getEmpty());
    }

    public void getCollectionConfig(OnResponseCallback<CollectionConfigResponse> callback){

        //Mocking start
        /*List<Category> categories = new ArrayList<>();
        List<ContentList> contentLists = new ArrayList<>();
        List<Source> sources = new ArrayList<>();
        for (int i = 0; i < 5; i++){
            Category category = new Category();
            category.setId(i);
            switch (i){
                case 0:
                    category.setName(context.getString(R.string.all));
                    break;
                case 1:
                    category.setName(context.getString(R.string.course));
                    break;
                case 2:
                    category.setName(context.getString(R.string.chatshala));
                    break;
                case 3:
                    category.setName(context.getString(R.string.hois));
                    break;
                case 4:
                    category.setName(context.getString(R.string.toolkit));
                    break;
            }
            category.setOrder(i);
            category.setSource_id(i-1);
            categories.add(category);

            if (i == 0){
                ContentList contentList = new ContentList();
                contentList.setId(i);
                contentList.setCategory_id(i);
                contentList.setFormat_type(ContentListType.feature.toString());
                contentList.setOrder(i);
                contentList.setName("Featured");
                contentLists.add(contentList);
            }

            for (int j = 1; j < 5; j++) {
                ContentList contentList = new ContentList();
                contentList.setId(j);
                contentList.setCategory_id(i);
                contentList.setFormat_type(ContentListType.normal.toString());
                contentList.setOrder(j);
                switch (j){
                    case 1:
                        contentList.setName("Must See");
                        break;
                    case 2:
                        contentList.setName("Continue Watching");
                        break;
                    case 3:
                        contentList.setName("Recently Added");
                        break;
                    case 4:
                        contentList.setName("Favourites");
                        break;
                }
                contentLists.add(contentList);
            }

            if (i < 4){
                Source source = new Source();
                source.setId(i);
                switch (i){
                    case 0:
                        source.setName(context.getString(R.string.course));
                        break;
                    case 1:
                        source.setName(context.getString(R.string.chatshala));
                        break;
                    case 2:
                        source.setName(context.getString(R.string.hois));
                        break;
                    case 3:
                        source.setName(context.getString(R.string.toolkit));
                        break;
                }
                sources.add(source);
            }
        }
        CollectionConfigResponse response = new CollectionConfigResponse();
        response.setCategory(categories);
        response.setContent_list(contentLists);
        response.setSource_id(sources);
        callback.onSuccess(response);*/
        //Mocking end

        //Actual code   **Do not delete**
        if (NetworkUtil.isConnected(context)){
            new GetCollectionConfigTask(context){
                @Override
                protected void onSuccess(CollectionConfigResponse response) throws Exception {
                    super.onSuccess(response);
                    if (response != null){
                        new Thread(){
                            @Override
                            public void run() {
                                mLocalDataSource.insertConfiguration(response);
                            }
                        }.start();
                    }
                    callback.onSuccess(response);
                }

                @Override
                protected void onException(Exception ex) {
                    getCollectionConfigFromLocal(callback, ex);
                }
            }.execute();
        } else {
            getCollectionConfigFromLocal(callback, new NoConnectionException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getCollectionConfigFromLocal(OnResponseCallback<CollectionConfigResponse> callback, Exception ex) {
        new AsyncTask<Void, Void, CollectionConfigResponse>(){

            @Override
            protected CollectionConfigResponse doInBackground(Void... voids) {
                return mLocalDataSource.getConfiguration();
            }

            @Override
            protected void onPostExecute(CollectionConfigResponse collectionConfigResponse) {
                super.onPostExecute(collectionConfigResponse);
                if (collectionConfigResponse == null ||
                        collectionConfigResponse.getCategory() == null ||
                        collectionConfigResponse.getCategory().isEmpty()) {
                    callback.onFailure(ex);
                } else {
                    callback.onSuccess(collectionConfigResponse);
                }
            }
        }.execute();
        /*new Thread(){
            @Override
            public void run() {
                CollectionConfigResponse response = mLocalDataSource.getConfiguration();
                if (response == null || response.getCategory() == null || response.getCategory().isEmpty()) {
                    callback.onFailure(ex);
                } else {
                    callback.onSuccess(response);
                }
            }
        }.start();*/
    }

    public void getConfigModifiedDate(OnResponseCallback<ConfigModifiedDateResponse> callback){

        if (NetworkUtil.isConnected(context)){
            new GetConfigModifiedDateTask(context){
                @Override
                protected void onSuccess(ConfigModifiedDateResponse configModifiedDateResponse) throws Exception {
                    super.onSuccess(configModifiedDateResponse);
                    callback.onSuccess(configModifiedDateResponse);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new NoConnectionException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void getCollectionItems(Long[] listIds, int skip, int take, OnResponseCallback<List<CollectionItemsResponse>> callback){

        //Mocking start
        /*List<Content> contents = new ArrayList<>();
        List<Long> lists1 = new ArrayList<>();
        List<Long> lists2 = new ArrayList<>();
        lists1.add(0L);
        lists1.add(2L);
        lists1.add(3L);
        lists2.add(1L);
        lists2.add(3L);
        lists2.add(4L);
        for (int i = 0; i < 50; i++){
            Content content = new Content();
            content.setId(i);
            switch (i%4){
                case 0:
                    content.setName("संख्या की शुरूआती समझ");
                    break;
                case 1:
                    content.setName("भिन्न - एक परिचय");
                    break;
                case 2:
                    content.setName("बीजगणित की सोच बनाना");
                    break;
                case 3:
                    content.setName("स्थानीय मान की समझ");
                    break;
            }
            if (i%10 == 0){
                content.setLists(lists1);
                content.setIcon("http://theteacherapp.org/asset-v1:Mathematics+M01+201706_Mat_01+type@asset+block@Math_sample2.png");
            } else {
                content.setLists(lists2);
                content.setIcon("http://theteacherapp.org/asset-v1:Language+01+201706_Lan_01+type@asset+block@Emergent_Literacy_ICON_93_kb.png");
            }
            content.setSource_id(i%4);
            contents.add(content);
        }
        CollectionItemsResponse contentResponse = new CollectionItemsResponse();
        contentResponse.setContent(contents);
        callback.onSuccess(contentResponse);*/
        //Mocking end

        //Actual code   **Do not delete**
        if (NetworkUtil.isConnected(context)){
            Bundle parameters = new Bundle();
            long[] listIds_long = new long[listIds.length];
            for (int i = 0; i < listIds.length; i++){
                listIds_long[i] = listIds[i];
            }
            parameters.putLongArray(Constants.KEY_LIST_IDS, listIds_long);
            parameters.putInt(Constants.KEY_SKIP, skip);
            parameters.putInt(Constants.KEY_TAKE, take);
            new GetCollectionItemsTask(context, parameters){
                @Override
                protected void onSuccess(List<CollectionItemsResponse> collectionItemsList) throws Exception {
                    super.onSuccess(collectionItemsList);
                    if (collectionItemsList != null && !collectionItemsList.isEmpty()){
                        new Thread(){
                            @Override
                            public void run() {
                                for (CollectionItemsResponse collectionItemsResponse: collectionItemsList){
                                    if (collectionItemsResponse.getContent() != null){
                                        mLocalDataSource.insertContents(collectionItemsResponse.getContent());
                                    }
                                }
                            }
                        }.start();
                    }
                    callback.onSuccess(collectionItemsList);
                }

                @Override
                protected void onException(Exception ex) {
                    getCollectionItemsFromLocal(listIds, callback, ex);
                }
            }.execute();
        } else {
            getCollectionItemsFromLocal(listIds, callback, new NoConnectionException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getCollectionItemsFromLocal(Long[] listIds, OnResponseCallback<List<CollectionItemsResponse>> callback, Exception ex) {
        new AsyncTask<Void, Void, List<CollectionItemsResponse>>(){

            @Override
            protected List<CollectionItemsResponse> doInBackground(Void... voids) {
                List<Content> contents = mLocalDataSource.getContents();
                List<CollectionItemsResponse> responses = new ArrayList<>();
                if (contents != null){
                    for (Long listId: listIds){
                        CollectionItemsResponse response = new CollectionItemsResponse();
                        response.setId(listId);
                        response.setContent(new ArrayList<>());
                        responses.add(response);
                    }
                    List<Long> requiredListIds = Arrays.asList(listIds);
                    for (Content content: contents){
                        for (long listId: content.getLists()){
                            if (requiredListIds.contains(listId)){
                                for (CollectionItemsResponse response: responses){
                                    if (response.getId() == listId){
                                        response.getContent().add(content);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                return responses;
            }

            @Override
            protected void onPostExecute(List<CollectionItemsResponse> responses) {
                super.onPostExecute(responses);

                if (responses == null || responses.isEmpty()){
                    callback.onFailure(ex);
                } else {
                    callback.onSuccess(responses);
                }
            }
        }.execute();

        /*new Thread(){
            @Override
            public void run() {
                List<Content> contents = mLocalDataSource.getContents();
                List<CollectionItemsResponse> responses = new ArrayList<>();
                if (contents != null){
                    List<Long> requiredListIds = Arrays.asList(listIds);
                    List<Long> recordedListIds = new ArrayList<>();
                    for (Content content: contents){
                        for (long listId: content.getLists()){
                            if (requiredListIds.contains(listId)){
                                if (recordedListIds.contains(listId)){
                                    for (CollectionItemsResponse response: responses){
                                        if (response.getId() == listId){
                                            response.getContent().add(content);
                                            break;
                                        }
                                    }
                                } else {
                                    recordedListIds.add(listId);
                                    CollectionItemsResponse response = new CollectionItemsResponse();
                                    response.setId(listId);
                                    List<Content> temp = new ArrayList<>();
                                    temp.add(content);
                                    response.setContent(temp);
                                    responses.add(response);
                                }
                            }
                        }
                    }
                }

                if (responses.isEmpty()){
                    callback.onFailure(ex);
                } else {
                    callback.onSuccess(responses);
                }
            }
        }.start();*/
    }

    public void getStateAgendaCount(OnResponseCallback<List<AgendaList>> callback){

        //Mocking start
        /*AgendaList agendaList1 = new AgendaList();
        AgendaList agendaList2 = new AgendaList();
        AgendaList agendaList3 = new AgendaList();
        agendaList1.setLevel("State");
        agendaList2.setLevel("District");
        agendaList3.setLevel("Block");
        List<AgendaItem> items = new ArrayList<>();
        for (int i = 0; i < 4; i++){
            AgendaItem item = new AgendaItem();
            item.setContent_count(10 - i);
            item.setSource_id(i);
            switch (i){
                case 0:
                    item.setSource_name("Course");
                    break;
                case 1:
                    item.setSource_name("Chatshala");
                    break;
                case 2:
                    item.setSource_name("HOIS");
                    break;
                default:
                    item.setSource_name("Toolkit");
                    break;
            }
            items.add(item);
        }
        agendaList1.setResult(items);
        agendaList2.setResult(items);
        agendaList3.setResult(items);

        List<AgendaList> agendaLists = new ArrayList<>();
        agendaLists.add(agendaList1);
        agendaLists.add(agendaList2);
        agendaLists.add(agendaList3);
        callback.onSuccess(agendaLists);*/
        //Mocking end

        //Actual code   **Do not delete**
        if (NetworkUtil.isConnected(context)){
            new GetStateAgendaCountTask(context){
                @Override
                protected void onSuccess(List<AgendaList> agendaLists) throws Exception {
                    super.onSuccess(agendaLists);
                    callback.onSuccess(agendaLists);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new NoConnectionException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getMyAgendaCount(OnResponseCallback<AgendaList> callback){

        //Mocking start
        AgendaList agendaList = new AgendaList();
        agendaList.setLevel("My");
        List<AgendaItem> items = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            AgendaItem item = new AgendaItem();
            item.setContent_count(10 - i);
            item.setSource_id(i);
            switch (i){
                case 0:
                    item.setSource_title("कोर्स");
                    item.setSource_name("course");
                    break;
                case 1:
                    item.setSource_title("शिक्षण सामग्री");
                    item.setSource_name("toolkit");
                    break;
                default:
                    item.setSource_title("प्रेरणा स्त्रोत");
                    item.setSource_name("hois");
                    break;
            }
            items.add(item);
        }
        agendaList.setResult(items);
        callback.onSuccess(agendaList);
        //Mocking end

        //Actual code   **Do not delete**
        /*if (NetworkUtil.isConnected(context)){
            new GetMyAgendaCountTask(context){
                @Override
                protected void onSuccess(AgendaList agendaList) throws Exception {
                    super.onSuccess(agendaList);
                    callback.onSuccess(agendaList);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new NoConnectionException(context.getString(R.string.no_connection_exception)));
        }*/

    }

    public void getDownloadAgendaCount(OnResponseCallback<AgendaList> callback){

        //Mocking start
        AgendaList agendaList = new AgendaList();
        agendaList.setLevel("Download");
        List<AgendaItem> items = new ArrayList<>();
        for (int i = 0; i < 4; i++){
            AgendaItem item = new AgendaItem();
            item.setContent_count(10 - i);
            item.setSource_id(i);
            switch (i){
                case 0:
                    item.setSource_title("कोर्स");
                    item.setSource_name("course");
                    break;
                case 1:
                    item.setSource_title("Chatशाला");
                    item.setSource_name("chatshala");
                    break;
                case 2:
                    item.setSource_title("शिक्षण सामग्री");
                    item.setSource_name("toolkit");
                    break;
                default:
                    item.setSource_title("प्रेरणा स्त्रोत");
                    item.setSource_name("hois");
                    break;
            }
            items.add(item);
        }
        agendaList.setResult(items);
        callback.onSuccess(agendaList);
        //Mocking end

    }

    public void getBlocks(OnResponseCallback<List<RegistrationOption>> callback, Bundle parameters,
                          @NonNull List<RegistrationOption> blocks){

        new GetUserAddressTask(context, parameters){
            @Override
            protected void onSuccess(UserAddressResponse userAddressResponse) throws Exception {
                super.onSuccess(userAddressResponse);
                blocks.clear();
                if (userAddressResponse != null && userAddressResponse.getBlock() != null){
                    for (Object o: userAddressResponse.getBlock()){
                        blocks.add(new RegistrationOption(o.toString(), o.toString()));
                    }
                    callback.onSuccess(blocks);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(ex);
            }
        }.execute();

    }

}

