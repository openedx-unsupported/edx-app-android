package org.edx.mobile.base

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.edx.mobile.base.http.SynchronousExecutorService
import org.edx.mobile.http.interceptor.OnlyIfCachedStrippingInterceptor
import org.edx.mobile.http.provider.RetrofitProvider
import org.edx.mobile.http.serialization.ISO8601DateTypeAdapter
import org.edx.mobile.http.serialization.JsonPageDeserializer
import org.edx.mobile.model.Page
import org.edx.mobile.model.api.EnrollmentResponse
import org.edx.mobile.model.authentication.AuthResponse
import org.edx.mobile.model.course.BlockData
import org.edx.mobile.model.course.BlockList
import org.edx.mobile.model.course.BlockType
import org.edx.mobile.util.Config
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Injector(config: Config) {

    private val synchronousExecutorService: SynchronousExecutorService =
        SynchronousExecutorService()
    private val dispatcher = Dispatcher(synchronousExecutorService)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .addInterceptor(OnlyIfCachedStrippingInterceptor())
        .build()

    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(config.apiHostURL)
        .addConverterFactory(GsonConverterFactory.create(getGson()))
        .build()

    fun <T> getInstance(service: Class<T>): T {
        return retrofit.create(service)
    }

    fun getRetrofitProvider(): RetrofitProvider {
        return object : RetrofitProvider {
            override fun get(): Retrofit {
                return retrofit
            }

            override fun getWithOfflineCache(): Retrofit {
                return get()
            }

            override fun getNonOAuthBased(): Retrofit {
                return get()
            }

            override fun getIAPAuth(): Retrofit {
                return get()
            }

        }
    }

    fun getGson(): Gson {
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
}
