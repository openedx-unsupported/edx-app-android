package org.edx.mobile

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.edx.mobile.base.http.SynchronousExecutorService
import org.edx.mobile.http.interceptor.OnlyIfCachedStrippingInterceptor
import org.edx.mobile.http.provider.RetrofitProvider
import org.edx.mobile.model.course.BlockData
import org.edx.mobile.model.course.BlockList
import org.edx.mobile.model.course.BlockType
import org.edx.mobile.util.Config
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Injector(config: Config) {

    var synchronousExecutorService: SynchronousExecutorService = SynchronousExecutorService()
    var dispatcher = Dispatcher(synchronousExecutorService)

    private var okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .addInterceptor(OnlyIfCachedStrippingInterceptor())
        .build()

    private var retrofit = Retrofit.Builder()
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
                return retrofit;
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
        return GsonBuilder().registerTypeAdapter(
            BlockList::class.java,
            BlockList.Deserializer()
        ).registerTypeAdapter(BlockType::class.java, BlockType.Deserializer())
            .registerTypeAdapter(BlockData::class.java, BlockData.Deserializer())
            .create()
    }
}
