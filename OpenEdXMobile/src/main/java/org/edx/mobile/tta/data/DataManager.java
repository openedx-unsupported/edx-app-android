package org.edx.mobile.tta.data;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import org.edx.mobile.discussion.CourseTopics;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionTopic;
import org.edx.mobile.discussion.DiscussionTopicDepth;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.impl.DbHelper;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.enums.CertificateStatus;
import org.edx.mobile.tta.data.enums.ScormStatus;
import org.edx.mobile.tta.data.enums.SourceType;
import org.edx.mobile.tta.data.local.db.ILocalDataSource;
import org.edx.mobile.tta.data.local.db.LocalDataSource;
import org.edx.mobile.tta.data.local.db.TADatabase;
import org.edx.mobile.tta.data.local.db.operation.GetCourseContentsOperation;
import org.edx.mobile.tta.data.local.db.operation.GetWPContentsOperation;
import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.Certificate;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.local.db.table.ContentList;
import org.edx.mobile.tta.data.model.HtmlResponse;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.model.agenda.AgendaItem;
import org.edx.mobile.tta.data.model.agenda.AgendaList;
import org.edx.mobile.tta.data.model.BaseResponse;
import org.edx.mobile.tta.data.model.content.BookmarkResponse;
import org.edx.mobile.tta.data.model.content.CertificateStatusResponse;
import org.edx.mobile.tta.data.model.content.MyCertificatesResponse;
import org.edx.mobile.tta.data.model.content.TotalLikeResponse;
import org.edx.mobile.tta.data.model.feed.SuggestedUser;
import org.edx.mobile.tta.data.model.library.CollectionConfigResponse;
import org.edx.mobile.tta.data.model.library.CollectionItemsResponse;
import org.edx.mobile.tta.data.model.library.ConfigModifiedDateResponse;
import org.edx.mobile.tta.data.model.EmptyResponse;
import org.edx.mobile.tta.data.model.profile.ChangePasswordResponse;
import org.edx.mobile.tta.data.model.profile.FeedbackResponse;
import org.edx.mobile.tta.data.model.profile.UpdateMyProfileResponse;
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
import org.edx.mobile.tta.task.content.GetContentTask;
import org.edx.mobile.tta.task.content.IsContentMyAgendaTask;
import org.edx.mobile.tta.task.content.IsLikeTask;
import org.edx.mobile.tta.task.content.SetBookmarkTask;
import org.edx.mobile.tta.task.content.SetLikeTask;
import org.edx.mobile.tta.task.content.TotalLikeTask;
import org.edx.mobile.tta.task.content.course.GetCourseDataFromPersistableCacheTask;
import org.edx.mobile.tta.task.content.course.UserEnrollmentCourseFromCacheTask;
import org.edx.mobile.tta.task.content.course.UserEnrollmentCourseTask;
import org.edx.mobile.tta.task.content.course.certificate.GenerateCertificateTask;
import org.edx.mobile.tta.task.content.course.certificate.GetCertificateStatusTask;
import org.edx.mobile.tta.task.content.course.certificate.GetCertificateTask;
import org.edx.mobile.tta.task.content.course.certificate.GetMyCertificatesTask;
import org.edx.mobile.tta.task.content.course.discussion.GetCommentRepliesTask;
import org.edx.mobile.tta.task.content.course.discussion.GetDiscussionThreadsTask;
import org.edx.mobile.tta.task.content.course.discussion.GetDiscussionTopicsTask;
import org.edx.mobile.tta.task.content.course.discussion.GetThreadCommentsTask;
import org.edx.mobile.tta.task.feed.FollowUserTask;
import org.edx.mobile.tta.task.feed.GetSuggestedUsersTask;
import org.edx.mobile.tta.task.library.GetCollectionConfigTask;
import org.edx.mobile.tta.task.library.GetCollectionItemsTask;
import org.edx.mobile.tta.task.library.GetConfigModifiedDateTask;
import org.edx.mobile.tta.task.profile.ChangePasswordTask;
import org.edx.mobile.tta.task.profile.GetAccountTask;
import org.edx.mobile.tta.task.profile.GetProfileTask;
import org.edx.mobile.tta.task.profile.GetUserAddressTask;
import org.edx.mobile.tta.data.model.profile.UserAddressResponse;
import org.edx.mobile.tta.task.profile.SubmitFeedbackTask;
import org.edx.mobile.tta.task.profile.UpdateMyProfileTask;
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
import org.edx.mobile.user.Account;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.user.SetAccountImageTask;
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

    private DbHelper dbHelper;

    private DataManager(Context context, IRemoteDataSource remoteDataSource, ILocalDataSource localDataSource) {
        super(context);
        this.context = context;
        mRemoteDataSource = remoteDataSource;
        mLocalDataSource = localDataSource;

        mAppPref = new AppPref(context);
        loginPrefs = new LoginPrefs(context);

        dbHelper = new DbHelper(context);
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

                        new Thread() {
                            @Override
                            public void run() {

                                for (CollectionItemsResponse itemsResponse: collectionItemsList){
                                    if (itemsResponse.getContent() != null){
                                        for (Content content: itemsResponse.getContent()){
                                            if (content.getLists() == null){
                                                content.setLists(new ArrayList<>());
                                            }

                                            Content localContent = mLocalDataSource.getContentById(content.getId());
                                            if (localContent != null && localContent.getLists() != null){
                                                content.getLists().addAll(localContent.getLists());
                                            }

                                            if (!content.getLists().contains(itemsResponse.getId())){
                                                content.getLists().add(itemsResponse.getId());
                                            }

                                            mLocalDataSource.insertContent(content);
                                        }
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
        /*AgendaList agendaList = new AgendaList();
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
        callback.onSuccess(agendaList);*/
        //Mocking end

        //Actual code **Do not delete**
        final boolean[] coursesReceived = new boolean[1];
        final boolean[] chatshalaReceived = new boolean[1];
        final boolean[] toolkitReceived = new boolean[1];
        final boolean[] hoisReceived = new boolean[1];

        AgendaList agendaList = new AgendaList();
        agendaList.setLevel("Download");
        agendaList.setResult(new ArrayList<>());

        getdownloadedCourseContents(new OnResponseCallback<List<Content>>() {
            @Override
            public void onSuccess(List<Content> data) {
                addContentsToAgendaList(data, agendaList, SourceType.course);
                coursesReceived[0] = true;
                sendDownloadAgenda(coursesReceived[0], chatshalaReceived[0], toolkitReceived[0], hoisReceived[0],
                        agendaList, callback);
            }

            @Override
            public void onFailure(Exception e) {
                addContentsToAgendaList(null, agendaList, SourceType.course);
                coursesReceived[0] = true;
                sendDownloadAgenda(coursesReceived[0], chatshalaReceived[0], toolkitReceived[0], hoisReceived[0],
                        agendaList, callback);
            }
        });

        getdownloadedWPContents(SourceType.chatshala.name(), new OnResponseCallback<List<Content>>() {
            @Override
            public void onSuccess(List<Content> data) {
                addContentsToAgendaList(data, agendaList, SourceType.chatshala);
                chatshalaReceived[0] = true;
                sendDownloadAgenda(coursesReceived[0], chatshalaReceived[0], toolkitReceived[0], hoisReceived[0],
                        agendaList, callback);
            }

            @Override
            public void onFailure(Exception e) {
                addContentsToAgendaList(null, agendaList, SourceType.chatshala);
                chatshalaReceived[0] = true;
                sendDownloadAgenda(coursesReceived[0], chatshalaReceived[0], toolkitReceived[0], hoisReceived[0],
                        agendaList, callback);
            }
        });

        getdownloadedWPContents(SourceType.toolkit.name(), new OnResponseCallback<List<Content>>() {
            @Override
            public void onSuccess(List<Content> data) {
                addContentsToAgendaList(data, agendaList, SourceType.toolkit);
                toolkitReceived[0] = true;
                sendDownloadAgenda(coursesReceived[0], chatshalaReceived[0], toolkitReceived[0], hoisReceived[0],
                        agendaList, callback);
            }

            @Override
            public void onFailure(Exception e) {
                addContentsToAgendaList(null, agendaList, SourceType.toolkit);
                toolkitReceived[0] = true;
                sendDownloadAgenda(coursesReceived[0], chatshalaReceived[0], toolkitReceived[0], hoisReceived[0],
                        agendaList, callback);
            }
        });

        getdownloadedWPContents(SourceType.hois.name(), new OnResponseCallback<List<Content>>() {
            @Override
            public void onSuccess(List<Content> data) {
                addContentsToAgendaList(data, agendaList, SourceType.hois);
                hoisReceived[0] = true;
                sendDownloadAgenda(coursesReceived[0], chatshalaReceived[0], toolkitReceived[0], hoisReceived[0],
                        agendaList, callback);
            }

            @Override
            public void onFailure(Exception e) {
                addContentsToAgendaList(null, agendaList, SourceType.hois);
                hoisReceived[0] = true;
                sendDownloadAgenda(coursesReceived[0], chatshalaReceived[0], toolkitReceived[0], hoisReceived[0],
                        agendaList, callback);
            }
        });

    }

    private void addContentsToAgendaList(List<Content> contents, AgendaList agendaList, SourceType sourceType){

        AgendaItem item = new AgendaItem();
        item.setContent_count(contents == null ? 0 : contents.size());
        item.setSource_name(sourceType.name());

        String sourceTitle = null;
        switch (sourceType){
            case course:
                sourceTitle = context.getString(R.string.course);
                break;
            case chatshala:
                sourceTitle = context.getString(R.string.chatshala);
                break;
            case toolkit:
                sourceTitle = context.getString(R.string.toolkit);
                break;
            case hois:
                sourceTitle = context.getString(R.string.hois);
                break;
        }
        item.setSource_title(sourceTitle);
        agendaList.getResult().add(item);

    }

    private void sendDownloadAgenda(boolean b1, boolean b2, boolean b3, boolean b4, AgendaList agendaList,
                                    OnResponseCallback<AgendaList> callback){

        if (b1 && b2 && b3 && b4){
            callback.onSuccess(agendaList);
        }

    }

    public void getdownloadedCourseContents(OnResponseCallback<List<Content>> callback){

        new AsyncTask<Void, Void, List<Content>>() {
            @Override
            protected List<Content> doInBackground(Void... voids) {
                List<Long> contentIds = new GetCourseContentsOperation().execute(dbHelper.getDatabase());
                List<Content> contents = new ArrayList<>();
                if (contentIds != null){
                    for (long id: contentIds){
                        Content content = mLocalDataSource.getContentById(id);
                        if (content != null){
                            contents.add(content);
                        }
                    }
                }
                return contents;
            }

            @Override
            protected void onPostExecute(List<Content> contents) {
                super.onPostExecute(contents);
                if (!contents.isEmpty()) {
                    callback.onSuccess(contents);
                } else {
                    callback.onFailure(new TaException("No content downloaded"));
                }
            }
        }.execute();

    }

    public void getdownloadedWPContents(String sourceName, OnResponseCallback<List<Content>> callback){

        new AsyncTask<Void, Void, List<Content>>() {
            @Override
            protected List<Content> doInBackground(Void... voids) {
                List<Long> contentIds = new GetWPContentsOperation(sourceName).execute(dbHelper.getDatabase());
                List<Content> contents = new ArrayList<>();
                if (contentIds != null){
                    for (long id: contentIds){
                        Content content = mLocalDataSource.getContentById(id);
                        if (content != null){
                            contents.add(content);
                        }
                    }
                }
                return contents;
            }

            @Override
            protected void onPostExecute(List<Content> contents) {
                super.onPostExecute(contents);
                if (!contents.isEmpty()) {
                    callback.onSuccess(contents);
                } else {
                    callback.onFailure(new TaException("No content downloaded"));
                }
            }
        }.execute();

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
                               long contentId,
                               FragmentActivity activity,
                               VideoDownloadHelper.DownloadManagerCallback callback) {
        DownloadEntry de = scorm.getDownloadEntry(edxEnvironment.getStorage());
        de.url = scorm.getDownloadUrl();
        de.title = scorm.getParent().getDisplayName();
        de.content_id = contentId;
        downloadManager.downloadVideo(de, activity, callback);
    }

    public void downloadMultiple(List<? extends HasDownloadEntry> downloadEntries,
                                 long contentid,
                                 FragmentActivity activity,
                                 VideoDownloadHelper.DownloadManagerCallback callback) {
        downloadManager.downloadVideos(downloadEntries, contentid, activity, callback);
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

    public void getDownloadedContent(String sourceName, OnResponseCallback<List<Content>> callback) {

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
        if (sourceName.equalsIgnoreCase(SourceType.course.name())){
            getdownloadedCourseContents(callback);
        } else {
            getdownloadedWPContents(sourceName, callback);
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

    public void downloadPost(Post post, long contentId, String category_id,String category_name,
                             FragmentActivity activity,
                             VideoDownloadHelper.DownloadManagerCallback callback){

        DownloadEntry videoData=new DownloadEntry();
        videoData.setDownloadEntryForPost(contentId, category_id,category_name,post);
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

    public DownloadEntry getDownloadedVideo(Post post, long contentId, String categoryId, String categoryName)
    {
        DownloadEntry videoData=new DownloadEntry();
        videoData.setDownloadEntryForPost(contentId, categoryId,categoryName,post);

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

                    if (!contents.isEmpty()){
                        List<Content> finalContents = contents;
                        new Thread(){
                            @Override
                            public void run() {
                                for (Content content: finalContents){
                                    if (content.getLists() == null){
                                        content.setLists(new ArrayList<>());
                                    }

                                    Content localContent = mLocalDataSource.getContentById(content.getId());
                                    if (localContent != null && localContent.getLists() != null){
                                        content.getLists().addAll(localContent.getLists());
                                    }
                                    mLocalDataSource.insertContent(content);
                                }
                            }
                        }.start();
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

    public void submitFeedback(String msg, OnResponseCallback<FeedbackResponse> callback){

        if (NetworkUtil.isConnected(context)){

            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_USERNAME, loginPrefs.getUsername());
            parameters.putString(Constants.KEY_FEEDBACK, msg);
            parameters.putString(Constants.KEY_DEVICE_INFO,
                    "API Level:"+ Build.VERSION.RELEASE+"  Device:"+Build.DEVICE+"  Model no:"+Build.MODEL+"  Product:"+Build.PRODUCT);

            new SubmitFeedbackTask(context, parameters){
                @Override
                protected void onSuccess(FeedbackResponse feedbackResponse) throws Exception {
                    super.onSuccess(feedbackResponse);
                    callback.onSuccess(feedbackResponse);
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

    public void changePassword(String oldPass, String newPass, OnResponseCallback<ChangePasswordResponse> callback){

        if (NetworkUtil.isConnected(context)){

            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_OLD_PASSWORD, oldPass);
            parameters.putString(Constants.KEY_NEW_PASSWORD, newPass);
            parameters.putString(Constants.KEY_USERNAME, loginPrefs.getUsername());

            new ChangePasswordTask(context, parameters){
                @Override
                protected void onSuccess(ChangePasswordResponse changePasswordResponse) throws Exception {
                    super.onSuccess(changePasswordResponse);

                    edxEnvironment.getRouter().resetAuthForChangePassword(context,
                            edxEnvironment.getAnalyticsRegistry(), edxEnvironment.getNotificationDelegate());
                    login(loginPrefs.getUsername(), newPass, new OnResponseCallback<AuthResponse>() {
                        @Override
                        public void onSuccess(AuthResponse data) {
                            callback.onSuccess(changePasswordResponse);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
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

    public void getAccount(OnResponseCallback<Account> callback){

        if (NetworkUtil.isConnected(context)){

            new GetAccountTask(context, loginPrefs.getUsername()){
                @Override
                protected void onSuccess(Account account) throws Exception {
                    super.onSuccess(account);
                    if (account != null) {
                        loginPrefs.setProfileImage(loginPrefs.getUsername(), account.getProfileImage());

                        getProfile(new OnResponseCallback<ProfileModel>() {
                            @Override
                            public void onSuccess(ProfileModel data) {
                                loginPrefs.setCurrentUserProfileInCache(data);
                                callback.onSuccess(account);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onSuccess(account);
                            }
                        });

                    } else {
                        callback.onFailure(new TaException("Invalid account"));
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

    public void updateProfile(Bundle parameters, OnResponseCallback<UpdateMyProfileResponse> callback){

        if (NetworkUtil.isConnected(context)){

            new UpdateMyProfileTask(context, parameters, loginPrefs.getUsername()){
                @Override
                protected void onSuccess(UpdateMyProfileResponse updateMyProfileResponse) throws Exception {
                    super.onSuccess(updateMyProfileResponse);

                    if (updateMyProfileResponse == null){
                        callback.onFailure(new TaException("Your action could not be completed"));
                        return;
                    }

                    //for cache consistency
                    ProfileModel profileModel = new ProfileModel();
                    profileModel.name = updateMyProfileResponse.getName();
                    profileModel.email = updateMyProfileResponse.getEmail();
                    profileModel.gender=updateMyProfileResponse.getGender();

                    profileModel.title=updateMyProfileResponse.getTitle();
                    profileModel.classes_taught=updateMyProfileResponse.getClasses_taught();
                    profileModel.state=updateMyProfileResponse.getState();
                    profileModel.district=updateMyProfileResponse.getDistrict();
                    profileModel.block=updateMyProfileResponse.getBlock();
                    profileModel.pmis_code=updateMyProfileResponse.getPMIS_code();
                    profileModel.diet_code=updateMyProfileResponse.getDIETCode();
                    profileModel.setTagLabel(updateMyProfileResponse.getTagLabel());
                    profileModel.setFollowers(updateMyProfileResponse.getFollowers());
                    profileModel.setFollowing(updateMyProfileResponse.getFollowing());

                    loginPrefs.setCurrentUserProfileInCache(profileModel);
                    loginPrefs.removeMxProfilePageCache();

                    callback.onSuccess(updateMyProfileResponse);
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

    public void updateProfileImage(@NonNull Uri uri, @NonNull Rect cropRect,
                                   OnResponseCallback<ProfileImage> callback){

        if (NetworkUtil.isConnected(context)){

            new SetAccountImageTask(context, loginPrefs.getUsername(), uri, cropRect){
                @Override
                protected void onSuccess(Void response) throws Exception {
                    super.onSuccess(response);

                    getAccount(new OnResponseCallback<Account>() {
                        @Override
                        public void onSuccess(Account data) {
                            loginPrefs.setProfileImage(loginPrefs.getUsername(), data.getProfileImage());
                            callback.onSuccess(data.getProfileImage());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
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

    public void getProfile(OnResponseCallback<ProfileModel> callback){

        if (NetworkUtil.isConnected(context)){

            new GetProfileTask(context){
                @Override
                protected void onSuccess(ProfileModel profileModel) throws Exception {
                    super.onSuccess(profileModel);
                    callback.onSuccess(profileModel);
                }

                @Override
                protected void onException(Exception ex) {
                    callback.onFailure(ex);
                }
            }.execute();

        } else {
            getProfileFromLocal(new TaException(context.getString(R.string.no_connection_exception)),callback);
        }

    }

    public void getProfileFromLocal(Exception e, OnResponseCallback<ProfileModel> callback){
        ProfileModel model = loginPrefs.getCurrentUserProfile();
        if (model != null){
            callback.onSuccess(model);
        } else {
            callback.onFailure(e);
        }
    }

    public void getCertificateStatus(String courseId, OnResponseCallback<CertificateStatusResponse> callback){

        getCertificateFromLocal(courseId, new OnResponseCallback<Certificate>() {
            @Override
            public void onSuccess(Certificate data) {
                CertificateStatusResponse response = new CertificateStatusResponse();
                response.setStatus(CertificateStatus.GENERATED.name());
                callback.onSuccess(response);
            }

            @Override
            public void onFailure(Exception e) {

                if (NetworkUtil.isConnected(context)){

                    new GetCertificateStatusTask(context, courseId){
                        @Override
                        protected void onSuccess(CertificateStatusResponse certificateStatusResponse) throws Exception {
                            super.onSuccess(certificateStatusResponse);
                            if (certificateStatusResponse != null){
                                callback.onSuccess(certificateStatusResponse);
                            } else {
                                callback.onFailure(new TaException("Status of certificate could not be fetched"));
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
        }, null);

    }

    public void getCertificate(String courseId, OnResponseCallback<Certificate> callback){

        if (NetworkUtil.isConnected(context)){

            new GetCertificateTask(context, courseId){
                @Override
                protected void onSuccess(MyCertificatesResponse myCertificatesResponse) throws Exception {
                    super.onSuccess(myCertificatesResponse);
                    if (myCertificatesResponse == null || myCertificatesResponse.getCertificates() == null ||
                            myCertificatesResponse.getCertificates().isEmpty()){
                        getCertificateFromLocal(courseId, callback, new TaException("Certificate not available"));
                    } else {
                        new Thread(){
                            @Override
                            public void run() {
                                Certificate certificate = myCertificatesResponse.getCertificates().get(0);
                                if (certificate.getUsername() == null){
                                    certificate.setUsername(loginPrefs.getUsername());
                                }
                                mLocalDataSource.insertCertificate(certificate);
                            }
                        }.start();

                        callback.onSuccess(myCertificatesResponse.getCertificates().get(0));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getCertificateFromLocal(courseId, callback, ex);
                }
            }.execute();

        } else {
            getCertificateFromLocal(courseId, callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getCertificateFromLocal(String courseId, OnResponseCallback<Certificate> callback, Exception e){

        new AsyncTask<Void, Void, Certificate>() {
            @Override
            protected Certificate doInBackground(Void... voids) {
                return mLocalDataSource.getCertificate(courseId, loginPrefs.getUsername());
            }

            @Override
            protected void onPostExecute(Certificate certificate) {
                if (certificate != null){
                    callback.onSuccess(certificate);
                } else {
                    callback.onFailure(e);
                }
            }
        }.execute();

    }

    public void getMyCertificates(OnResponseCallback<List<Certificate>> callback){

        if (NetworkUtil.isConnected(context)){

            new GetMyCertificatesTask(context){
                @Override
                protected void onSuccess(MyCertificatesResponse myCertificatesResponse) throws Exception {
                    super.onSuccess(myCertificatesResponse);
                    if (myCertificatesResponse == null || myCertificatesResponse.getCertificates() == null){
                        getMyCertificatesFromLocal(callback, new TaException("Certificates not available"));
                    } else {
                        new Thread(){
                            @Override
                            public void run() {
                                List<Certificate> certificates = myCertificatesResponse.getCertificates();
                                for (Certificate certificate: certificates){
                                    if (certificate.getUsername() == null){
                                        certificate.setUsername(loginPrefs.getUsername());
                                    }
                                }
                                mLocalDataSource.insertCertificates(certificates);
                            }
                        }.start();

                        callback.onSuccess(myCertificatesResponse.getCertificates());
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getMyCertificatesFromLocal(callback, new TaException("Certificates not available"));
                }
            }.execute();

        } else {
            getMyCertificatesFromLocal(callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getMyCertificatesFromLocal(OnResponseCallback<List<Certificate>> callback, Exception e){

        new AsyncTask<Void, Void, List<Certificate>>() {
            @Override
            protected List<Certificate> doInBackground(Void... voids) {
                return mLocalDataSource.getAllCertificates(loginPrefs.getUsername());
            }

            @Override
            protected void onPostExecute(List<Certificate> certificates) {
                if (certificates != null){
                    callback.onSuccess(certificates);
                } else {
                    callback.onFailure(e);
                }
            }
        }.execute();

    }

    public void generateCertificate(String courseId, OnResponseCallback<CertificateStatusResponse> callback){

        if (NetworkUtil.isConnected(context)){

            new GenerateCertificateTask(context, courseId){
                @Override
                protected void onSuccess(CertificateStatusResponse certificateStatusResponse) throws Exception {
                    super.onSuccess(certificateStatusResponse);
                    if (certificateStatusResponse != null){
                        callback.onSuccess(certificateStatusResponse);
                    } else {
                        callback.onFailure(new TaException("Certificate could not be generated"));
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

    public void getContent(long contentId, OnResponseCallback<Content> callback){

        if (NetworkUtil.isConnected(context)){

            new GetContentTask(context, contentId){
                @Override
                protected void onSuccess(Content content) throws Exception {
                    super.onSuccess(content);
                    if (content != null && content.getDetail() == null){
                        new Thread(){
                            @Override
                            public void run() {
                                mLocalDataSource.insertContent(content);
                            }
                        }.start();

                        callback.onSuccess(content);
                    } else {
                        getContentFromLocal(contentId, callback, new TaException("Content not found"));
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    getContentFromLocal(contentId, callback, ex);
                }
            }.execute();

        } else {
            getContentFromLocal(contentId, callback, new TaException(context.getString(R.string.no_connection_exception)));
        }

    }

    public void getContentFromLocal(long contentId, OnResponseCallback<Content> callback, Exception e){

        new AsyncTask<Void, Void, Content>() {
            @Override
            protected Content doInBackground(Void... voids) {
                return mLocalDataSource.getContentById(contentId);
            }

            @Override
            protected void onPostExecute(Content content) {
                super.onPostExecute(content);
                if (content != null){
                    callback.onSuccess(content);
                } else {
                    callback.onFailure(e);
                }
            }
        }.execute();

    }

    public void getSuggestedUsers(int take, int skip, OnResponseCallback<List<SuggestedUser>> callback){

        if (NetworkUtil.isConnected(context)){

            new GetSuggestedUsersTask(context, take, skip){
                @Override
                protected void onSuccess(List<SuggestedUser> suggestedUsers) throws Exception {
                    super.onSuccess(suggestedUsers);
                    if (suggestedUsers == null || suggestedUsers.isEmpty()){
                        callback.onFailure(new TaException("No suggested users"));
                    } else {
                        List<SuggestedUser> emptyUsers = new ArrayList<>();
                        for (SuggestedUser user: suggestedUsers){
                            if (user.getUsername() == null){
                                emptyUsers.add(user);
                            }
                        }
                        for (SuggestedUser user: emptyUsers){
                            suggestedUsers.remove(user);
                        }
                        if (!suggestedUsers.isEmpty()) {
                            callback.onSuccess(suggestedUsers);
                        } else {
                            callback.onFailure(new TaException("No suggested users"));
                        }
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

    public void followUnfollowUser(String username, OnResponseCallback<StatusResponse> callback){

        if (NetworkUtil.isConnected(context)){

            new FollowUserTask(context, username){
                @Override
                protected void onSuccess(StatusResponse statusResponse) throws Exception {
                    super.onSuccess(statusResponse);
                    if (statusResponse == null){
                        callback.onFailure(new TaException("Error occured while following"));
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

    public void getDiscussionTopics(String courseId, OnResponseCallback<List<DiscussionTopicDepth>> callback){

        if (NetworkUtil.isConnected(context)){

            new GetDiscussionTopicsTask(context, courseId){
                @Override
                protected void onSuccess(CourseTopics courseTopics) throws Exception {
                    super.onSuccess(courseTopics);
                    if (courseTopics == null ||
                            (courseTopics.getCoursewareTopics() == null && courseTopics.getNonCoursewareTopics() == null)){
                        callback.onFailure(new TaException("No discussion topics available"));
                    } else {
                        List<DiscussionTopic> allTopics = new ArrayList<>();
                        if (courseTopics.getNonCoursewareTopics() != null) {
                            allTopics.addAll(courseTopics.getNonCoursewareTopics());
                        }
                        if (courseTopics.getCoursewareTopics() != null) {
                            allTopics.addAll(courseTopics.getCoursewareTopics());
                        }
                        if (!allTopics.isEmpty()) {
                            List<DiscussionTopicDepth> allTopicsWithDepth =
                                    DiscussionTopicDepth.createFromDiscussionTopics(allTopics);
                            callback.onSuccess(allTopicsWithDepth);
                        } else {
                            callback.onFailure(new TaException("No discussion topics available"));
                        }
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

    public void getDiscussionThreads(String courseId, List<String> topicIds, String view,
                                     String orderBy, int take, int page, List<String> requestedFields,
                                     OnResponseCallback<List<DiscussionThread>> callback){

        if (NetworkUtil.isConnected(context)){

            new GetDiscussionThreadsTask(context, courseId, topicIds, view, orderBy, take, page, requestedFields){
                @Override
                protected void onSuccess(Page<DiscussionThread> discussionThreadPage) throws Exception {
                    super.onSuccess(discussionThreadPage);
                    if (discussionThreadPage == null || discussionThreadPage.getResults().isEmpty()){
                        callback.onFailure(new TaException("No discussion threads available"));
                    } else {
                        callback.onSuccess(discussionThreadPage.getResults());
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

    public void getThreadComments(String threadId, int take, int page, List<String> requestedFields,
                                  OnResponseCallback<List<DiscussionComment>> callback){

        if (NetworkUtil.isConnected(context)){

            new GetThreadCommentsTask(context, threadId, take, page, requestedFields){
                @Override
                protected void onSuccess(Page<DiscussionComment> discussionCommentPage) throws Exception {
                    super.onSuccess(discussionCommentPage);
                    if (discussionCommentPage == null || discussionCommentPage.getResults().isEmpty()){
                        callback.onFailure(new TaException("No comments available"));
                    } else {
                        callback.onSuccess(discussionCommentPage.getResults());
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

    public void getCommentReplies(String commentId, int take, int page, List<String> requestedFields,
                                  OnResponseCallback<List<DiscussionComment>> callback){

        if (NetworkUtil.isConnected(context)){

            new GetCommentRepliesTask(context, commentId, take, page, requestedFields){
                @Override
                protected void onSuccess(Page<DiscussionComment> discussionCommentPage) throws Exception {
                    super.onSuccess(discussionCommentPage);
                    if (discussionCommentPage == null || discussionCommentPage.getResults().isEmpty()){
                        callback.onFailure(new TaException("No replies available"));
                    } else {
                        callback.onSuccess(discussionCommentPage.getResults());
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

}

