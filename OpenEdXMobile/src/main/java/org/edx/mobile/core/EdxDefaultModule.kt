package org.edx.mobile.core

import android.app.DownloadManager
import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.edx.mobile.authentication.LoginAPI
import org.edx.mobile.course.CourseAPI
import org.edx.mobile.discussion.DiscussionService
import org.edx.mobile.http.provider.OkHttpClientProvider
import org.edx.mobile.http.provider.RetrofitProvider
import org.edx.mobile.http.serialization.ISO8601DateTypeAdapter
import org.edx.mobile.http.serialization.JsonPageDeserializer
import org.edx.mobile.inapppurchases.InAppPurchasesAPI
import org.edx.mobile.model.Page
import org.edx.mobile.model.api.EnrollmentResponse
import org.edx.mobile.model.authentication.AuthResponse
import org.edx.mobile.model.course.BlockData
import org.edx.mobile.model.course.BlockList
import org.edx.mobile.model.course.BlockType
import org.edx.mobile.module.db.IDatabase
import org.edx.mobile.module.db.impl.IDatabaseImpl
import org.edx.mobile.module.download.IDownloadManager
import org.edx.mobile.module.download.IDownloadManagerImpl
import org.edx.mobile.module.notification.DummyNotificationDelegate
import org.edx.mobile.module.notification.NotificationDelegate
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.module.storage.IStorage
import org.edx.mobile.module.storage.Storage
import org.edx.mobile.player.TranscriptManager
import org.edx.mobile.repository.CourseDatesRepository
import org.edx.mobile.repository.InAppPurchasesRepository
import org.edx.mobile.services.CourseManager
import org.edx.mobile.services.EdxCookieManager
import org.edx.mobile.user.UserAPI
import org.edx.mobile.user.UserService
import org.greenrobot.eventbus.EventBus
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EdxDefaultModule {

    @Binds
    abstract fun bindIEdxEnvironment(edxEnvironment: EdxEnvironment): IEdxEnvironment

    @Binds
    abstract fun bindIStorage(storage: Storage): IStorage

    @Binds
    abstract fun bindIDatabase(database: IDatabaseImpl): IDatabase

    @Binds
    abstract fun bindIDownloadManager(iDownloadManagerImpl: IDownloadManagerImpl): IDownloadManager

    @Binds
    abstract fun bindNotificationDelegate(dummyNotificationDelegate: DummyNotificationDelegate): NotificationDelegate

    @Binds
    abstract fun bindRetrofitProvider(impl: RetrofitProvider.Impl): RetrofitProvider

    @Binds
    abstract fun bindOkHttpClientProvider(impl: OkHttpClientProvider.Impl): OkHttpClientProvider

    /**
     * ref: https://dagger.dev/dev-guide/faq.html#why-cant-binds-and-instance-provides-methods-go-in-the-same-module
     */
    @Module
    @InstallIn(SingletonComponent::class)
    object ProvideModule {

        @Singleton
        @Provides
        fun provideDownloadManager(@ApplicationContext context: Context): DownloadManager {
            return (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
        }

        @Singleton
        @Provides
        fun provideEventBus(): EventBus {
            return EventBus.getDefault()
        }

        @Singleton
        @Provides
        fun provideRetrofit(impl: RetrofitProvider.Impl): Retrofit {
            return impl.get()
        }

        /**
         * The Gson instance for converting the response body to the desired type.
         */
        @Singleton
        @Provides
        fun provideGson(): Gson {
            return GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapterFactory(ISO8601DateTypeAdapter.FACTORY)
                .registerTypeAdapter(Page::class.java, JsonPageDeserializer())
                .registerTypeAdapter(AuthResponse::class.java, AuthResponse.Deserializer())
                .registerTypeAdapter(BlockList::class.java, BlockList.Deserializer())
                .registerTypeAdapter(BlockType::class.java, BlockType.Deserializer())
                .registerTypeAdapter(BlockData::class.java, BlockData.Deserializer())
                .registerTypeAdapter(
                    EnrollmentResponse::class.java,
                    EnrollmentResponse.Deserializer()
                )
                .serializeNulls()
                .create()
        }

        @Singleton
        @Provides
        fun provideCourseDatesRepository(courseAPI: CourseAPI): CourseDatesRepository {
            return CourseDatesRepository(courseAPI)
        }

        @Singleton
        @Provides
        fun provideInAppPurchasesRepository(iapAPI: InAppPurchasesAPI): InAppPurchasesRepository {
            return InAppPurchasesRepository(iapAPI)
        }
    }

    // Inject dependencies in classes not supported by Hilt
    // Ref: https://developer.android.com/training/dependency-injection/hilt-android#not-supported
    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface ProviderEntryPoint {

        fun getEnvironment(): IEdxEnvironment

        fun getLoginAPI(): LoginAPI

        fun getUserAPI(): UserAPI

        fun getLoginPrefs(): LoginPrefs

        fun getEdxCookieManager(): EdxCookieManager

        fun getCourseManager(): CourseManager

        fun getTranscriptManager(): TranscriptManager

        fun getIDatabase(): IDatabase

        fun getOkHttpClientProvider(): OkHttpClientProvider

        fun getDiscussionService(): DiscussionService

        fun getCourseAPI(): CourseAPI

        fun getUserService(): UserService

        fun getGSon(): Gson

        fun getInAppPurchasesAPI(): InAppPurchasesAPI
    }
}
