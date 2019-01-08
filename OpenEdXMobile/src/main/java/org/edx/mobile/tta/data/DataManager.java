package org.edx.mobile.tta.data;

import android.arch.persistence.room.Room;
import android.content.Context;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxDataManager;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.tta.data.enums.ContentListType;
import org.edx.mobile.tta.data.local.db.ILocalDataSource;
import org.edx.mobile.tta.data.local.db.LocalDataSource;
import org.edx.mobile.tta.data.local.db.TADatabase;
import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.ContentList;
import org.edx.mobile.tta.data.local.db.table.Source;
import org.edx.mobile.tta.data.model.AgendaItem;
import org.edx.mobile.tta.data.model.AgendaList;
import org.edx.mobile.tta.data.model.BaseResponse;
import org.edx.mobile.tta.data.model.ConfigurationResponse;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.EmptyResponse;
import org.edx.mobile.tta.data.model.ModificationResponse;
import org.edx.mobile.tta.data.pref.AppPref;
import org.edx.mobile.tta.data.remote.IRemoteDataSource;
import org.edx.mobile.tta.data.remote.RetrofitServiceUtil;
import org.edx.mobile.tta.exception.NoConnectionException;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.task.agenda.GetMyAgendaCountTask;
import org.edx.mobile.tta.task.agenda.GetStateAgendaCountTask;
import org.edx.mobile.tta.task.library.GetModificationTask;
import org.edx.mobile.tta.ui.login.model.LoginRequest;
import org.edx.mobile.tta.ui.login.model.LoginResponse;
import org.edx.mobile.tta.utils.RxUtil;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;


import java.util.ArrayList;
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
    }

    public Observable<EmptyResponse> getEmpty() {
        return preEmptyProcess(mRemoteDataSource.getEmpty());
    }

    public void getConfiguration(OnResponseCallback<ConfigurationResponse> callback){

        //Mocking start
        List<Category> categories = new ArrayList<>();
        List<ContentList> contentLists = new ArrayList<>();
        List<Source> sources = new ArrayList<>();
        for (int i = 0; i < 5; i++){
            Category category = new Category();
            category.setId(i);
            category.setName("Category " + (i+1));
            category.setOrder(i);
            category.setSource(i-1);
            categories.add(category);

            if (i == 0){
                ContentList contentList = new ContentList();
                contentList.setId(i);
                contentList.setCategory(i);
                contentList.setFormat_type(ContentListType.feature.toString());
                contentList.setOrder(i);
                contentList.setName("Content List " + (i+1));
                contentLists.add(contentList);
            }

            for (int j = 1; j < 5; j++) {
                ContentList contentList = new ContentList();
                contentList.setId(j);
                contentList.setCategory(i);
                contentList.setFormat_type(ContentListType.normal.toString());
                contentList.setOrder(j);
                contentList.setName("Content List " + (j+1));
                contentLists.add(contentList);
            }

            if (i < 4){
                Source source = new Source();
                source.setId(i);
                source.setName("Source " + (i+1));
                sources.add(source);
            }
        }
        ConfigurationResponse response = new ConfigurationResponse();
        response.setCategory(categories);
        response.setList(contentLists);
        response.setSource(sources);
        callback.onSuccess(response);
        //Mocking end

        //Actual code   **Do not delete**
        /*if (NetworkUtil.isConnected(context)){
            new GetConfigurationTask(context){
                @Override
                protected void onSuccess(ConfigurationResponse response) throws Exception {
                    super.onSuccess(response);
                    if (response != null){
                        mLocalDataSource.insertConfiguration(response);
                    }
                    callback.onSuccess(response);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onSuccess(mLocalDataSource.getConfiguration());
        }*/

    }

    public void getModification(OnResponseCallback<ModificationResponse> callback){

        if (NetworkUtil.isConnected(context)){
            new GetModificationTask(context){
                @Override
                protected void onSuccess(ModificationResponse modificationResponse) throws Exception {
                    super.onSuccess(modificationResponse);
                    callback.onSuccess(modificationResponse);
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

    public void getContents(OnResponseCallback<List<Content>> callback){

        //Mocking start
        List<Content> contents = new ArrayList<>();
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
            content.setName("Content " + (i+1));
            if (i%10 == 0){
                content.setLists(lists1);
                content.setIcon("https://cdn1.imggmi.com/uploads/2019/1/3/cd6880f554501535b2bb4832870362fd-full.jpg");
            } else {
                content.setLists(lists2);
                content.setIcon("https://cdn1.imggmi.com/uploads/2019/1/3/0880fd66016bae2b8052fbcf3aeaf267-full.jpg");
            }
            content.setSource(i%4);
            contents.add(content);
        }
        callback.onSuccess(contents);
        //Mocking end

        //Actual code   **Do not delete**
        /*if (NetworkUtil.isConnected(context)){
            new GetContentsTask(context){
                @Override
                protected void onSuccess(List<Content> contents) throws Exception {
                    super.onSuccess(contents);
                    if (contents != null){
                        mLocalDataSource.insertContents(contents);
                    }
                    callback.onSuccess(contents);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onSuccess(mLocalDataSource.getContents());
        }*/

    }

    public void getStateAgendaCount(OnResponseCallback<AgendaList> callback){

        //Mocking start
        AgendaList agendaList = new AgendaList();
        agendaList.setList_id(1);
        agendaList.setList_name("State wise Agenda");
        List<AgendaItem> items = new ArrayList<>();
        for (int i = 0; i < 4; i++){
            AgendaItem item = new AgendaItem();
            item.setContent_count(10 - i);
            item.setSource(i);
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
        agendaList.setResult(items);
        callback.onSuccess(agendaList);
        //Mocking end

        //Actual code   **Do not delete**
        /*if (NetworkUtil.isConnected(context)){
            new GetStateAgendaCountTask(context){
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

    public void getMyAgendaCount(OnResponseCallback<AgendaList> callback){

        //Mocking start
        AgendaList agendaList = new AgendaList();
        agendaList.setList_id(1);
        agendaList.setList_name("State wise Agenda");
        List<AgendaItem> items = new ArrayList<>();
        for (int i = 0; i < 4; i++){
            AgendaItem item = new AgendaItem();
            item.setContent_count(10 - i);
            item.setSource(i);
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

}

