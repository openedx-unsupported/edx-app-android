package org.edx.mobile.core;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import org.edx.mobile.authentication.LoginService;
import org.edx.mobile.course.CourseService;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.http.provider.RetrofitProvider;
import org.edx.mobile.http.util.CallUtil;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.http.serialization.ISO8601DateTypeAdapter;
import org.edx.mobile.http.serialization.JsonPageDeserializer;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.course.BlockData;
import org.edx.mobile.model.course.BlockList;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.IDatabaseImpl;
import org.edx.mobile.module.download.IDownloadManager;
import org.edx.mobile.module.download.IDownloadManagerImpl;
import org.edx.mobile.module.notification.DummyNotificationDelegate;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.user.UserService;
import org.edx.mobile.util.AppStoreUtils;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.MediaConsentUtils;

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
        bind(IDownloadManager.class).to(IDownloadManagerImpl.class);

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

        bind(OkHttpClientProvider.class).to(OkHttpClientProvider.Impl.class);
        bind(RetrofitProvider.class).to(RetrofitProvider.Impl.class);
        bind(OkHttpClient.class).toProvider(OkHttpClientProvider.Impl.class).in(Singleton.class);
        bind(Retrofit.class).toProvider(RetrofitProvider.Impl.class).in(Singleton.class);

        bind(LoginService.class).toProvider(LoginService.Provider.class).in(Singleton.class);
        bind(CourseService.class).toProvider(CourseService.Provider.class).in(Singleton.class);
        bind(DiscussionService.class).toProvider(DiscussionService.Provider.class).in(Singleton.class);
        bind(UserService.class).toProvider(UserService.Provider.class).in(Singleton.class);

        bind(IStorage.class).to(Storage.class);

        requestStaticInjection(CallUtil.class, BrowserUtil.class, MediaConsentUtils.class,
                DiscussionTextUtils.class, AppStoreUtils.class);
    }
}
