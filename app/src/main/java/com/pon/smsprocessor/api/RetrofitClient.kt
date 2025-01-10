package com.pon.smsprocessor.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pon.smsprocessor.DefaultsRepository
import com.pon.smsprocessor.Logger
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val BASE_URL =  "https://ibronevik.ru/taxi/c/gruzvill/api/v1/"
object RetrofitClient  {
    private val retrofit: Retrofit

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private var loggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }


    private val retryInterceptor: Interceptor = Interceptor { chain ->
        val request = chain.request()
        // try the request
        var response = chain.proceed(request)
        var tryCount = 0
        while (!response.isSuccessful && tryCount < DefaultsRepository.retryCount) {
            Log.d("intercept", "Request is not successful - $tryCount")
            Logger.addToLog("Ответ сервера ${response.code()}, повтор попытки")
            tryCount++
            Thread.sleep(DefaultsRepository.retryTime)
            response.close()
            response = chain.proceed(request)
        }

        // otherwise just pass the original response on
        response
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(retryInterceptor)
        .build()


    init {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api: Api
        get() = retrofit.create(Api::class.java)
}
