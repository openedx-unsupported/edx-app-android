package org.edx.mobile.tta.data;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.core.IEdxDataManager;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.enums.ScormStatus;
import org.edx.mobile.tta.data.local.db.ILocalDataSource;
import org.edx.mobile.tta.data.local.db.LocalDataSource;
import org.edx.mobile.tta.data.local.db.TADatabase;
import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.local.db.table.ContentList;
import org.edx.mobile.tta.data.model.HtmlResponse;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.model.agenda.AgendaItem;
import org.edx.mobile.tta.data.model.agenda.AgendaList;
import org.edx.mobile.tta.data.model.BaseResponse;
import org.edx.mobile.tta.data.model.content.BookmarkResponse;
import org.edx.mobile.tta.data.model.content.TotalLikeResponse;
import org.edx.mobile.tta.data.model.library.CollectionConfigResponse;
import org.edx.mobile.tta.data.model.library.CollectionItemsResponse;
import org.edx.mobile.tta.data.model.library.ConfigModifiedDateResponse;
import org.edx.mobile.tta.data.model.EmptyResponse;
import org.edx.mobile.tta.data.model.search.FilterSection;
import org.edx.mobile.tta.data.model.search.SearchFilter;
import org.edx.mobile.tta.data.pref.AppPref;
import org.edx.mobile.tta.data.remote.IRemoteDataSource;
import org.edx.mobile.tta.data.remote.RetrofitServiceUtil;
import org.edx.mobile.tta.exception.TaException;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.scorm.ScormBlockModel;
import org.edx.mobile.tta.task.agenda.GetMyAgendaContentTask;
import org.edx.mobile.tta.task.agenda.GetMyAgendaCountTask;
import org.edx.mobile.tta.task.agenda.GetStateAgendaContentTask;
import org.edx.mobile.tta.task.agenda.GetStateAgendaCountTask;
import org.edx.mobile.tta.task.authentication.LoginTask;
import org.edx.mobile.tta.task.content.IsContentMyAgendaTask;
import org.edx.mobile.tta.task.content.IsLikeTask;
import org.edx.mobile.tta.task.content.SetBookmarkTask;
import org.edx.mobile.tta.task.content.SetLikeTask;
import org.edx.mobile.tta.task.content.TotalLikeTask;
import org.edx.mobile.tta.task.content.course.GetCourseDataFromPersistableCacheTask;
import org.edx.mobile.tta.task.content.course.UserEnrollmentCourseFromCacheTask;
import org.edx.mobile.tta.task.content.course.UserEnrollmentCourseTask;
import org.edx.mobile.tta.task.library.GetCollectionConfigTask;
import org.edx.mobile.tta.task.library.GetCollectionItemsTask;
import org.edx.mobile.tta.task.library.GetConfigModifiedDateTask;
import org.edx.mobile.tta.task.profile.GetUserAddressTask;
import org.edx.mobile.tta.data.model.profile.UserAddressResponse;
import org.edx.mobile.tta.task.search.GetSearchFilterTask;
import org.edx.mobile.tta.task.search.SearchTask;
import org.edx.mobile.tta.utils.RxUtil;
import org.edx.mobile.tta.wordpress_client.model.Comment;
import org.edx.mobile.tta.wordpress_client.model.CustomComment;
import org.edx.mobile.tta.wordpress_client.model.Post;
import org.edx.mobile.tta.wordpress_client.model.User;
import org.edx.mobile.tta.wordpress_client.model.WPProfileModel;
import org.edx.mobile.tta.wordpress_client.model.WpAuthResponse;
import org.edx.mobile.tta.wordpress_client.rest.HttpServerErrorResponse;
import org.edx.mobile.tta.wordpress_client.rest.WordPressRestResponse;
import org.edx.mobile.tta.wordpress_client.rest.WpClientRetrofit;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.NetworkUtil;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import okhttp3.HttpUrl;
import retrofit2.Call;

import static org.edx.mobile.tta.Constants.TA_DATABASE;

/**
 * Created by Arjun on 2018/9/18.
 */

public class DataManager extends BaseRoboInjector {
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

    @com.google.inject.Inject
    private CourseManager courseManager;

    @com.google.inject.Inject
    private CourseAPI courseApi;

    @com.google.inject.Inject
    private VideoDownloadHelper downloadManager;

    private WpClientRetrofit wpClientRetrofit;

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

    public static DataManager getInstance(Context context) {
        if (mDataManager == null) {
            synchronized (DataManager.class) {
                if (mDataManager == null) {
                    mDataManager = new DataManager(context, RetrofitServiceUtil.create(context, true),
                            new LocalDataSource(Room.databaseBuilder(context, TADatabase.class, TA_DATABASE).fallbackToDestructiveMigration()
                                    .build()));
                }
            }
        }
        mDataManager.wpClientRetrofit = new WpClientRetrofit(true,false);
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

    public void login(String username, String password, OnResponseCallback<AuthResponse> callback) {

        wpClientRetrofit.getAccessToken(username, password, new WordPressRestResponse<WpAuthResponse>() {
            @Override
            public void onSuccess(WpAuthResponse result) {
                loginPrefs.storeWPAuthTokenResponse(result);
                doEdxLogin(username, password, callback);
            }

            @Override
            public void onFailure(HttpServerErrorResponse errorResponse) {
                if (config.isWordpressAuthentication()){
                    callback.onFailure(new TaException(errorResponse.getMessage()));
                } else {
                    doEdxLogin(username, password, callback);
                }
            }
        });

    }

    private void doEdxLogin(String username, String password, OnResponseCallback<AuthResponse> callback){

        new LoginTask(context, username, password){
            @Override
            protected void onSuccess(AuthResponse authResponse) throws Exception {
                super.onSuccess(authResponse);
                callback.onSuccess(authResponse);
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(ex);
            }
        }.execute();

    }

    public void logout() {
        edxEnvironment.getRouter().performManualLogout(
                context,
                mDataManager.getEdxEnvironment().getAnalyticsRegistry(),
                mDataManager.getEdxEnvironment().getNotificationDelegate());

        new Thread() {
            @Override
            public void run() {
                mLocalDataSource.clear();
            }
        }.start();
    }

    public Observable<EmptyResponse> getEmpty() {
        return preEmptyProcess(mRemoteDataSource.getEmpty());
    }

    public void getCollectionConfig(OnResponseCallback<CollectionConfigResponse> callback) {

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
        if (NetworkUtil.isConnected(context)) {
            new GetCollectionConfigTask(context) {
                @Override
                protected void onSuccess(CollectionConfigResponse response) throws Exception {
                    super.onSuccess(response);
                    if (response != null) {
                        new Thread() {
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
            getCollectionConfigFromLocal(callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getCollectionConfigFromLocal(OnResponseCallback<CollectionConfigResponse> callback, Exception ex) {
        new AsyncTask<Void, Void, CollectionConfigResponse>() {

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

    public void getConfigModifiedDate(OnResponseCallback<ConfigModifiedDateResponse> callback) {

        if (NetworkUtil.isConnected(context)) {
            new GetConfigModifiedDateTask(context) {
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
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void getCollectionItems(Long[] listIds, int skip, int take, OnResponseCallback<List<CollectionItemsResponse>> callback) {

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
        if (NetworkUtil.isConnected(context)) {
            Bundle parameters = new Bundle();
            long[] listIds_long = new long[listIds.length];
            for (int i = 0; i < listIds.length; i++) {
                listIds_long[i] = listIds[i];
            }
            parameters.putLongArray(Constants.KEY_LIST_IDS, listIds_long);
            parameters.putInt(Constants.KEY_SKIP, skip);
            parameters.putInt(Constants.KEY_TAKE, take);
            new GetCollectionItemsTask(context, parameters) {
                @Override
                protected void onSuccess(List<CollectionItemsResponse> collectionItemsList) throws Exception {
                    super.onSuccess(collectionItemsList);
                    if (collectionItemsList != null && !collectionItemsList.isEmpty()) {

                        for (CollectionItemsResponse itemsResponse: collectionItemsList){
                            if (itemsResponse.getContent() != null){
                                for (Content content: itemsResponse.getContent()){
                                    if (content.getLists() == null){
                                        List<Long> listIds = new ArrayList<>();
                                        listIds.add(itemsResponse.getId());
                                        content.setLists(listIds);
                                    } else if (!content.getLists().contains(itemsResponse.getId())){
                                        content.getLists().add(itemsResponse.getId());
                                    }
                                }
                            }
                        }

                        new Thread() {
                            @Override
                            public void run() {
                                for (CollectionItemsResponse collectionItemsResponse : collectionItemsList) {
                                    if (collectionItemsResponse.getContent() != null) {
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
            getCollectionItemsFromLocal(listIds, callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    private void getCollectionItemsFromLocal(Long[] listIds, OnResponseCallback<List<CollectionItemsResponse>> callback, Exception ex) {
        new AsyncTask<Void, Void, List<CollectionItemsResponse>>() {

            @Override
            protected List<CollectionItemsResponse> doInBackground(Void... voids) {
                List<Content> contents = mLocalDataSource.getContents();
                List<CollectionItemsResponse> responses = new ArrayList<>();
                if (contents != null) {
                    for (Long listId : listIds) {
                        CollectionItemsResponse response = new CollectionItemsResponse();
                        response.setId(listId);
                        response.setContent(new ArrayList<>());
                        responses.add(response);
                    }
                    List<Long> requiredListIds = Arrays.asList(listIds);
                    for (Content content : contents) {
                        for (long listId : content.getLists()) {
                            if (requiredListIds.contains(listId)) {
                                for (CollectionItemsResponse response : responses) {
                                    if (response.getId() == listId) {
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

                if (responses == null || responses.isEmpty()) {
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

    public void getStateAgendaCount(OnResponseCallback<List<AgendaList>> callback) {

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
        if (NetworkUtil.isConnected(context)) {
            new GetStateAgendaCountTask(context) {
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
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getMyAgendaCount(OnResponseCallback<AgendaList> callback) {

        //Mocking start
        /*AgendaList agendaList = new AgendaList();
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
        callback.onSuccess(agendaList);*/
        //Mocking end

        //Actual code   **Do not delete**
        if (NetworkUtil.isConnected(context)) {
            new GetMyAgendaCountTask(context) {
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
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getDownloadAgendaCount(OnResponseCallback<AgendaList> callback) {

        //Mocking start
        AgendaList agendaList = new AgendaList();
        agendaList.setLevel("Download");
        List<AgendaItem> items = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            AgendaItem item = new AgendaItem();
            item.setContent_count(10 - i);
            item.setSource_id(i);
            switch (i) {
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

        //Actual code **Do not delete**


    }

    public void getBlocks(OnResponseCallback<List<RegistrationOption>> callback, Bundle parameters,
                          @NonNull List<RegistrationOption> blocks) {

        new GetUserAddressTask(context, parameters) {
            @Override
            protected void onSuccess(UserAddressResponse userAddressResponse) throws Exception {
                super.onSuccess(userAddressResponse);
                blocks.clear();
                if (userAddressResponse != null && userAddressResponse.getBlock() != null) {
                    for (Object o : userAddressResponse.getBlock()) {
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

    public void getCourse(String courseId, OnResponseCallback<EnrolledCoursesResponse> callback) {

        if (NetworkUtil.isConnected(context)) {
            new UserEnrollmentCourseTask(context, courseId) {
                @Override
                protected void onSuccess(EnrolledCoursesResponse enrolledCoursesResponse) throws Exception {
                    super.onSuccess(enrolledCoursesResponse);
                    if (enrolledCoursesResponse == null ||
                            enrolledCoursesResponse.getMode() == null ||
                            enrolledCoursesResponse.getMode().equals("")
                    ) {
                        callback.onFailure(new TaException("Invalid Course"));
                    } else {
                        callback.onSuccess(enrolledCoursesResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getCourseFromLocal(courseId, callback, ex);
                }
            }.execute();
        } else {
            getCourseFromLocal(courseId, callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getCourseFromLocal(String courseId, OnResponseCallback<EnrolledCoursesResponse> callback, Exception e) {
        new UserEnrollmentCourseFromCacheTask(context, courseId) {
            @Override
            protected void onSuccess(EnrolledCoursesResponse enrolledCoursesResponse) throws Exception {
                super.onSuccess(enrolledCoursesResponse);
                if (enrolledCoursesResponse == null ||
                        enrolledCoursesResponse.getMode() == null ||
                        enrolledCoursesResponse.getMode().equals("")
                ) {
                    callback.onFailure(new TaException("Invalid Course"));
                } else {
                    callback.onSuccess(enrolledCoursesResponse);
                }
            }

            @Override
            protected void onException(Exception ex) {
                callback.onFailure(e);
            }
        }.execute();
    }

    public void getTotalLikes(long contentId, OnResponseCallback<TotalLikeResponse> callback) {
        if (NetworkUtil.isConnected(context)) {
            new TotalLikeTask(context, contentId) {
                @Override
                protected void onSuccess(TotalLikeResponse totalLikeResponse) throws Exception {
                    super.onSuccess(totalLikeResponse);
                    if (totalLikeResponse == null) {
                        callback.onFailure(new TaException("No response for total likes."));
                    } else {
                        callback.onSuccess(totalLikeResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void isLike(long contentId, OnResponseCallback<StatusResponse> callback) {
        if (NetworkUtil.isConnected(context)) {
            new IsLikeTask(context, contentId) {
                @Override
                protected void onSuccess(StatusResponse statusResponse) throws Exception {
                    super.onSuccess(statusResponse);
                    if (statusResponse == null) {
                        callback.onFailure(new TaException("No response for is like."));
                    } else {
                        callback.onSuccess(statusResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void isContentMyAgenda(long contentId, OnResponseCallback<StatusResponse> callback) {
        if (NetworkUtil.isConnected(context)) {
            new IsContentMyAgendaTask(context, contentId) {
                @Override
                protected void onSuccess(StatusResponse statusResponse) throws Exception {
                    super.onSuccess(statusResponse);
                    if (statusResponse == null) {
                        callback.onFailure(new TaException("No response for is content my agenda."));
                    } else {
                        callback.onSuccess(statusResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void setLike(long contentId, OnResponseCallback<StatusResponse> callback) {
        if (NetworkUtil.isConnected(context)) {
            new SetLikeTask(context, contentId) {
                @Override
                protected void onSuccess(StatusResponse statusResponse) throws Exception {
                    super.onSuccess(statusResponse);
                    if (statusResponse == null) {
                        callback.onFailure(new TaException("Error occured. Couldn't like."));
                    } else {
                        callback.onSuccess(statusResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void setBookmark(long contentId, OnResponseCallback<BookmarkResponse> callback) {
        if (NetworkUtil.isConnected(context)) {
            new SetBookmarkTask(context, contentId) {
                @Override
                protected void onSuccess(BookmarkResponse bookmarkResponse) throws Exception {
                    super.onSuccess(bookmarkResponse);
                    if (bookmarkResponse == null) {
                        callback.onFailure(new TaException("Error occured. Couldn't add to My Agenda."));
                    } else {
                        callback.onSuccess(bookmarkResponse);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void getCourseComponent(String courseId, OnResponseCallback<CourseComponent> callback) {

        CourseComponent courseComponent = courseManager.getComponentByCourseId(courseId);
        if (courseComponent != null) {
            // Course data exist in app session cache
            callback.onSuccess(courseComponent);
            return;
        }

        new GetCourseDataFromPersistableCacheTask(context, courseId) {
            @Override
            protected void onSuccess(CourseComponent courseComponent) throws Exception {
                super.onSuccess(courseComponent);
                courseComponent = courseManager.getComponentByCourseId(courseId);
                if (courseComponent != null) {
                    callback.onSuccess(courseComponent);
                } else {
                    getCourseComponentFromServer(courseId, callback);
                }
            }

            @Override
            protected void onException(Exception ex) {
                getCourseComponentFromServer(courseId, callback);
            }
        }.execute();

    }

    public void getCourseComponentFromServer(String courseId, OnResponseCallback<CourseComponent> callback) {

        if (!NetworkUtil.isConnected(context)) {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
            return;
        }

        Call<CourseStructureV1Model> getHierarchyCall = courseApi.getCourseStructureWithoutStale(courseId);
        getHierarchyCall.enqueue(new CourseAPI.GetCourseStructureCallback(context, courseId,
                null, null, null, null) {
            @Override
            protected void onResponse(@NonNull CourseComponent courseComponent) {
                courseManager.addCourseDataInAppLevelCache(courseId, courseComponent);
                courseComponent = courseManager.getComponentByCourseId(courseId);
                if (courseComponent != null) {
                    callback.onSuccess(courseComponent);
                } else {
                    callback.onFailure(new TaException("Empty course."));
                }
            }

            @Override
            protected void onFailure(@NonNull Throwable error) {
                super.onFailure(error);
                callback.onFailure(new TaException(error.getLocalizedMessage()));
            }
        });
    }

    @NonNull
    private CourseComponent validateCourseComponent(@NonNull CourseComponent courseComponent, String courseId, String courseComponentId) {
        final CourseComponent cached = courseManager.getComponentByIdFromAppLevelCache(
                courseId, courseComponentId);
        courseComponent = cached != null ? cached : courseComponent;
        return courseComponent;
    }

    public void downloadSingle(ScormBlockModel scorm,
                               FragmentActivity activity,
                               VideoDownloadHelper.DownloadManagerCallback callback) {
        DownloadEntry de = scorm.getDownloadEntry(edxEnvironment.getStorage());
        de.url = scorm.getDownloadUrl();
        de.title = scorm.getParent().getDisplayName();
        downloadManager.downloadVideo(de, activity, callback);
    }

    public void downloadMultiple(List<? extends HasDownloadEntry> downloadEntries,
                                 FragmentActivity activity,
                                 VideoDownloadHelper.DownloadManagerCallback callback) {
        downloadManager.downloadVideos(downloadEntries, activity, callback);
    }

    public void getDownloadedStateForVideoId(String videoId, DataCallback<DownloadEntry.DownloadedState> callback) {
        edxEnvironment.getDatabase().getDownloadedStateForVideoId(videoId, callback);
    }

    public boolean scormNotDownloaded(ScormBlockModel scorm) {
        return getScormStatus(scorm).equals(ScormStatus.not_downloaded);
    }

    public ScormStatus getScormStatus(ScormBlockModel scorm) {

        DownloadEntry entry = scorm.getDownloadEntry(edxEnvironment.getStorage());
        return getDownloadStatus(entry);

    }

    private ScormStatus getDownloadStatus(DownloadEntry entry) {
        if (entry == null || entry.downloaded.equals(DownloadEntry.DownloadedState.ONLINE)){
            return ScormStatus.not_downloaded;
        } else if (entry.downloaded.equals(DownloadEntry.DownloadedState.DOWNLOADING)) {
            return ScormStatus.downloading;
        } else if (entry.watched.equals(DownloadEntry.WatchedState.UNWATCHED)) {
            return ScormStatus.downloaded;
        } else if (entry.watched.equals(DownloadEntry.WatchedState.PARTIALLY_WATCHED)) {
            return ScormStatus.watching;
        } else {
            return ScormStatus.watched;
        }
    }

    public void deleteScorm(ScormBlockModel scormBlockModel) {
        DownloadEntry de = scormBlockModel.getDownloadEntry(edxEnvironment.getStorage());
        de.url = scormBlockModel.getDownloadUrl();
        de.title = scormBlockModel.getParent().getDisplayName();
        edxEnvironment.getStorage().removeDownload(de);
    }

    public void getHtmlFromUrl(HttpUrl absoluteUrl, OnResponseCallback<String> callback) {
        if (NetworkUtil.isConnected(context)) {

            new Task<HtmlResponse>(context) {
                @Override
                public HtmlResponse call() throws Exception {
                    IRemoteDataSource source = RetrofitServiceUtil.create(context, false);
                    return source.getHtmlFromUrl(absoluteUrl).execute().body();
                }

                @Override
                protected void onSuccess(HtmlResponse htmlResponse) throws Exception {
                    super.onSuccess(htmlResponse);
                    callback.onSuccess(htmlResponse.getContent());
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

            /*new GetHtmlFromUrlTask(context, absoluteUrl){
                @Override
                protected void onSuccess(Void s) throws Exception {
                    super.onSuccess(s);
                    callback.onSuccess(s.toString());
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();*/
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void getMyAgendaContent(long sourceId, OnResponseCallback<List<Content>> callback) {

        //Mocking start
        /*List<Content> contents = new ArrayList<>();
        for (int i=0;i<20;i++){
            Content content =  new Content();
            content.setName("course");
            content.setIcon("http://theteacherapp.org/asset-v1:Mathematics+M01+201706_Mat_01+type@asset+block@Math_sample2.png");
            Source source =  new Source();
            source.setType("edx");
            source.setTitle("course");
            source.setName("course");
            content.setSource(source);
            contents.add(content);
        }
        callback.onSuccess(contents);*/
        //Mocking end

        //Actual code   **Do not delete**
        if (NetworkUtil.isConnected(context)) {
            new GetMyAgendaContentTask(context, sourceId) {
                @Override
                protected void onSuccess(List<Content> response) throws Exception {
                    super.onSuccess(response);
                    if (response != null && response.size() > 0) {
                        callback.onSuccess(response);
                    } else {
                        callback.onFailure(new TaException("No data found"));
                    }

                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void getStateAgendaContent(long sourceId, long list_id, OnResponseCallback<List<Content>> callback) {

        //Mocking start
        /*List<Content> contents = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Content content = new Content();
            content.setName("course");
            content.setIcon("http://theteacherapp.org/asset-v1:Mathematics+M01+201706_Mat_01+type@asset+block@Math_sample2.png");
            Source source = new Source();
            source.setType("edx");
            source.setTitle("course");
            source.setName("course");
            content.setSource(source);
            contents.add(content);
        }
        callback.onSuccess(contents);*/
        //Mocking end

        //Actual code   **Do not delete**
        if (NetworkUtil.isConnected(context)) {
            new GetStateAgendaContentTask(context, sourceId, list_id) {
                @Override
                protected void onSuccess(List<Content> response) throws Exception {
                    super.onSuccess(response);
                    if (response != null && response.size() > 0) {
                        callback.onSuccess(response);
                    } else {
                        callback.onFailure(new TaException("No data found."));
                    }

                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }
    }

    public void getPostById(long postId, OnResponseCallback<Post> callback){

        if (NetworkUtil.isConnected(context)){

            wpClientRetrofit.getPost(postId, new WordPressRestResponse<Post>() {
                @Override
                public void onSuccess(Post result) {
                    if (result != null){
                        callback.onSuccess(result);
                    } else {
                        callback.onFailure(new TaException("Invalid post"));
                    }
                }

                @Override
                public void onFailure(HttpServerErrorResponse errorResponse) {
                    callback.onFailure(new TaException(errorResponse.getMessage()));
                }
            });

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getCommentsByPost(long postId, OnResponseCallback<List<Comment>> callback){

        if (NetworkUtil.isConnected(context)){

            wpClientRetrofit.getCommentsByPost(postId, new WordPressRestResponse<List<Comment>>() {
                @Override
                public void onSuccess(List<Comment> result) {
                    if (result == null){
                        result = new ArrayList<>();
                    }
                    callback.onSuccess(result);
                }

                @Override
                public void onFailure(HttpServerErrorResponse errorResponse) {
                    callback.onFailure(new TaException(errorResponse.getMessage()));
                }
            });

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void downloadPost(Post post, String category_id,String category_name,
                             FragmentActivity activity,
                             VideoDownloadHelper.DownloadManagerCallback callback){

        DownloadEntry videoData=new DownloadEntry();
        videoData.setDownloadEntryForPost(category_id,category_name,post);
        downloadManager.downloadVideo(videoData, activity, callback);

    }

    public void deletePost(Post post){

        DownloadEntry entry = edxEnvironment.getStorage().getPostVideo(String.valueOf(post.getId()));
        if (entry != null)
            edxEnvironment.getStorage().removeDownload(entry);

    }

    public ScormStatus getPostDownloadStatus(Post post){

        DownloadEntry entry = edxEnvironment.getStorage().getPostVideo(String.valueOf(post.getId()));
        return getDownloadStatus(entry);

    }

    public void addComment(String comment, int commentParentId, long postId, OnResponseCallback<Comment> callback) {
        if(loginPrefs.getWPCurrentUserProfile()==null||loginPrefs.getWPCurrentUserProfile().id==null) {
            callback.onFailure(new TaException("Not authenticated to comment."));
            return;
        }

        String ua=new WebView(context).getSettings().getUserAgentString();
        CustomComment obj=new CustomComment();
        obj.author=loginPrefs.getWPCurrentUserProfile().id;
        //obj.author_ip=ip;
        obj.author_url ="";
        obj.author_user_agent=ua;
        obj.content=comment;
        obj.date= DateUtil.getCurrentDateForServerLocal();
        obj.date_gmt=DateUtil.getCurrentDateForServerGMT();
        obj.parent=commentParentId;
        obj.post=postId;

        addComment(obj, callback);
    }

    private void addComment(CustomComment comment, OnResponseCallback<Comment> callback)
    {
        wpClientRetrofit.createComment(comment, new WordPressRestResponse<Comment>() {
            @Override
            public void onSuccess(Comment result) {
                callback.onSuccess(result);
            }
            @Override
            public void onFailure(HttpServerErrorResponse errorResponse) {
                callback.onFailure(new TaException(errorResponse.getMessage()));
            }
        });
    }

    public void setWpProfileCache()
    {
        wpClientRetrofit.getUserMe(new WordPressRestResponse<User>() {
            @Override
            public void onSuccess(User result) {
                WPProfileModel model = new WPProfileModel();
                model.name = result.getName();
                model.username = result.getUsername();
                model.id = result.getId();
                if (result.getRoles() != null && result.getRoles().size() > 0)
                    model.roles = result.getRoles();
                loginPrefs.setWPCurrentUserProfileInCache(model);
            }

            @Override
            public void onFailure(HttpServerErrorResponse errorResponse) {
                if(!NetworkUtil.isLimitedAcess(errorResponse) && NetworkUtil.isUnauthorize(errorResponse))
                {
                    logout();
                    Toast.makeText(context, "Session expire", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public DownloadEntry getDownloadedVideo(Post post, String categoryId, String categoryName)
    {
        DownloadEntry videoData=new DownloadEntry();
        videoData.setDownloadEntryForPost(categoryId,categoryName,post);

        return edxEnvironment.getStorage().getPostVideo(videoData.videoId,videoData.url);

    }

    public void getSearchFilter(OnResponseCallback<SearchFilter> callback){

        //Mocking start
        /*SearchFilter searchFilter = new SearchFilter();
        List<FilterSection> sections = new ArrayList<>();
        for (int i = 0; i < 5; i++){
            FilterSection section = new FilterSection();
            section.setName("Section " + (i+1));
            List<FilterTag> tags = new ArrayList<>();
            for (int j = 0; j < 5; j++){
                FilterTag tag = new FilterTag();
                tag.setDisplay_name("Tag " + (i+1) + (j+1));
                tags.add(tag);
            }
            section.setTags(tags);
            sections.add(section);
        }
        searchFilter.setResult(sections);
        callback.onSuccess(searchFilter);*/
        //Mocking start

        //Actual code   **Do not delete**
        if (NetworkUtil.isConnected(context)){
            new GetSearchFilterTask(context){
                @Override
                protected void onSuccess(SearchFilter searchFilter) throws Exception {
                    super.onSuccess(searchFilter);
                    if (searchFilter == null){
                        callback.onFailure(new TaException("Cannot fetch filters"));
                    } else {
                        List<FilterSection> tempSections = new ArrayList<>();
                        for (FilterSection section: searchFilter.getResult()){
                            if (section.getTags() == null || section.getTags().isEmpty()){
                                tempSections.add(section);
                            }
                        }
                        for (FilterSection section: tempSections){
                            searchFilter.getResult().remove(section);
                        }
                        Collections.sort(searchFilter.getResult());
                        callback.onSuccess(searchFilter);
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();
        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getCategoryFromLocal(long sourceId, OnResponseCallback<Category> callback){

        new AsyncTask<Void, Void, Category>() {
            @Override
            protected Category doInBackground(Void... voids) {
                return mLocalDataSource.getCategoryBySourceId(sourceId);
            }

            @Override
            protected void onPostExecute(Category category) {
                super.onPostExecute(category);
                if (category == null){
                    callback.onFailure(new TaException("Category not found."));
                } else {
                    callback.onSuccess(category);
                }
            }
        }.execute();

    }

    public void getContentListsFromLocal(long categoryId, String mode, OnResponseCallback<List<ContentList>> callback){

        new AsyncTask<Void, Void, List<ContentList>>() {
            @Override
            protected List<ContentList> doInBackground(Void... voids) {
                return mLocalDataSource.getContentListsByCategoryIdAndMode(categoryId, mode);
            }

            @Override
            protected void onPostExecute(List<ContentList> contentLists) {
                super.onPostExecute(contentLists);
                if (contentLists == null || contentLists.isEmpty()){
                    callback.onFailure(new TaException("Content lists not found."));
                } else {
                    Collections.sort(contentLists);
                    callback.onSuccess(contentLists);
                }
            }
        }.execute();

    }

    public void search(int take, int skip, boolean isPriority, long listId, String searchText, List<FilterSection> sections,
                       OnResponseCallback<List<Content>> callback){

        if (NetworkUtil.isConnected(context)){

            new SearchTask(context, take, skip, isPriority, listId, searchText, sections){
                @Override
                protected void onSuccess(List<Content> contents) throws Exception {
                    super.onSuccess(contents);
                    if (contents == null){
                        contents = new ArrayList<>();
                    }
                    callback.onSuccess(contents);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            callback.onFailure(new TaException(context.getString(R.string.no_connection_exception)));
        }

    }
}

