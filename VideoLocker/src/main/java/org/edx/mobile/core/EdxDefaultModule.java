package org.edx.mobile.core;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;

import org.edx.mobile.authentication.LoginService;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.course.CourseService;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.http.Api;
import org.edx.mobile.http.IApi;
import org.edx.mobile.http.OkHttpUtil;
import org.edx.mobile.http.RestApiManager;
import org.edx.mobile.http.serialization.ISO8601DateTypeAdapter;
import org.edx.mobile.http.serialization.JsonPageDeserializer;
import org.edx.mobile.model.Page;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.ISegmentEmptyImpl;
import org.edx.mobile.module.analytics.ISegmentImpl;
import org.edx.mobile.module.analytics.ISegmentTracker;
import org.edx.mobile.module.analytics.ISegmentTrackerImpl;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.IDatabaseImpl;
import org.edx.mobile.module.download.IDownloadManager;
import org.edx.mobile.module.download.IDownloadManagerImpl;
import org.edx.mobile.module.notification.DummyNotificationDelegate;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.util.AppUpdateUtils;
import org.edx.mobile.user.UserService;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.MediaConsentUtils;

import de.greenrobot.event.EventBus;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
                .serializeNulls()
                .create());

        bind(Retrofit.class).toProvider(RetrofitProvider.class);

        bind(LoginService.class).toProvider(LoginService.Provider.class);
        bind(CourseService.class).toProvider(CourseService.Provider.class);
        bind(DiscussionService.class).toProvider(DiscussionService.Provider.class);
        bind(UserService.class).toProvider(UserService.Provider.class);

        requestStaticInjection(BrowserUtil.class, MediaConsentUtils.class,
                DiscussionTextUtils.class, AppUpdateUtils.class);
    }
}
