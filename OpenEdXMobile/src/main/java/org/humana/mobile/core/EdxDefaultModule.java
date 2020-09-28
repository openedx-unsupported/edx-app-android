package org.humana.mobile.core;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import org.humana.mobile.authentication.LoginService;
import org.humana.mobile.base.MainApplication;
import org.humana.mobile.course.CourseService;
import org.humana.mobile.discussion.DiscussionService;
import org.humana.mobile.discussion.DiscussionTextUtils;
import org.humana.mobile.http.Api;
import org.humana.mobile.http.IApi;
import org.humana.mobile.http.OkHttpUtil;
import org.humana.mobile.http.RestApiManager;
import org.humana.mobile.http.provider.RetrofitProvider;
import org.humana.mobile.http.util.CallUtil;
import org.humana.mobile.http.provider.OkHttpClientProvider;
import org.humana.mobile.http.serialization.ISO8601DateTypeAdapter;
import org.humana.mobile.http.serialization.JsonPageDeserializer;
import org.humana.mobile.model.Page;
import org.humana.mobile.model.course.BlockData;
import org.humana.mobile.model.course.BlockList;
import org.humana.mobile.model.course.BlockType;
import org.humana.mobile.module.analytics.ISegment;
import org.humana.mobile.module.analytics.ISegmentEmptyImpl;
import org.humana.mobile.module.analytics.ISegmentImpl;
import org.humana.mobile.module.analytics.ISegmentTracker;
import org.humana.mobile.module.analytics.ISegmentTrackerImpl;
import org.humana.mobile.module.db.IDatabase;
import org.humana.mobile.module.db.impl.IDatabaseImpl;
import org.humana.mobile.module.download.IDownloadManager;
import org.humana.mobile.module.download.IDownloadManagerImpl;
import org.humana.mobile.module.notification.DummyNotificationDelegate;
import org.humana.mobile.module.notification.NotificationDelegate;
import org.humana.mobile.module.storage.IStorage;
import org.humana.mobile.module.storage.Storage;
import org.humana.mobile.tta.analytics.AnalyticsRetrofitProvider;
import org.humana.mobile.tta.data.remote.service.TaService;
import org.humana.mobile.tta.scorm.ScormService;
import org.humana.mobile.user.UserService;
import org.humana.mobile.util.AppStoreUtils;
import org.humana.mobile.util.BrowserUtil;
import org.humana.mobile.util.Config;
import org.humana.mobile.util.MediaConsentUtils;

import de.greenrobot.event.EventBus;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class EdxDefaultModule extends AbstractModule {
    //if your module requires a context, add a constructor that will be passed a context.
    private Context context;

    //with RoboGuice 3.0, the constructor for AbstractModule will use an `Application`, not a `Context`
    public EdxDefaultModule(Context context) {
        this.context = context;
    }

    @Override
    public void configure() {
        Config config = new Config(context);

        bind(IDatabase.class).to(IDatabaseImpl.class);
        bind(IStorage.class).to(Storage.class);
        bind(ISegmentTracker.class).to(ISegmentTrackerImpl.class);
        if (config.getSegmentConfig().isEnabled()) {
            bind(ISegment.class).to(ISegmentImpl.class);
        } else {
            bind(ISegment.class).to(ISegmentEmptyImpl.class);
        }

        bind(IDownloadManager.class).to(IDownloadManagerImpl.class);

        bind(OkHttpClient.class).toInstance(OkHttpUtil.getOAuthBasedClient(context));

        if (MainApplication.RETROFIT_ENABLED) {
            bind(IApi.class).to(RestApiManager.class);
        } else {
            bind(IApi.class).to(Api.class);
        }


        bind(NotificationDelegate.class).to(DummyNotificationDelegate.class);

        bind(IEdxEnvironment.class).to(EdxEnvironment.class);

        bind(LinearLayoutManager.class).toProvider(LinearLayoutManagerProvider.class);

        bind(EventBus.class).toInstance(EventBus.getDefault());

        bind(Gson.class).toInstance(new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapterFactory(ISO8601DateTypeAdapter.FACTORY)
                .registerTypeAdapter(Page.class, new JsonPageDeserializer())
                .registerTypeAdapter(BlockData.class, new BlockData.Deserializer())
                .registerTypeAdapter(BlockType.class, new BlockType.Deserializer())
                .registerTypeAdapter(BlockList.class, new BlockList.Deserializer())
                .serializeNulls()
                .create());

     /*   bind(Gson.class).toInstance(new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapterFactory(ISO8601DateTypeAdapter.FACTORY)
                .registerTypeAdapter(Page.class, new JsonPageDeserializer())
                .serializeNulls()
                .create());*/

        bind(OkHttpClientProvider.class).to(OkHttpClientProvider.Impl.class);
        bind(RetrofitProvider.class).to(RetrofitProvider.Impl.class);
//        bind(OkHttpClient.class).toProvider(OkHttpClientProvider.Impl.class).in(Singleton.class);
        bind(Retrofit.class).toProvider(RetrofitProvider.Impl.class).in(Singleton.class);

        bind(LoginService.class).toProvider(LoginService.Provider.class).in(Singleton.class);
        bind(CourseService.class).toProvider(CourseService.Provider.class).in(Singleton.class);
        bind(DiscussionService.class).toProvider(DiscussionService.Provider.class).in(Singleton.class);
        bind(UserService.class).toProvider(UserService.Provider.class).in(Singleton.class);
        bind(ScormService.class).toProvider(ScormService.Provider.class).in(Singleton.class);
        bind(ScormService.class).toProvider(ScormService.Provider.class).in(Singleton.class);

        bind(IStorage.class).to(Storage.class);
        //Room.databaseBuilder(context, AppDatabase.class, dbName).fallbackToDestructiveMigration()
        //                .build()
       // bind(ILocalDataSource.class).to(LocalDataSource.class);

       // bind(IRemoteDataSource.class).toInstance(RetrofitServiceUtil.create());

        //bind(TADatabase.class).toInstance(Room.databaseBuilder(context, TADatabase.class, "dbasfsfs").fallbackToDestructiveMigration()
          //      .build());

        //bind(AppPref.class).toProvider(AppPref.Provider.class);
        //bind(DataManager.class).toProvider(DataManager.Provider.class);

        bind(TaService.class).toProvider(TaService.TaProvider.class);

        bind(AnalyticsRetrofitProvider.class).to(AnalyticsRetrofitProvider.Impl.class);

        bind(IEdxDataManager.class).to(EdxDataManager.class);
        requestStaticInjection(CallUtil.class, BrowserUtil.class, MediaConsentUtils.class,
                DiscussionTextUtils.class, AppStoreUtils.class);
    }
}
