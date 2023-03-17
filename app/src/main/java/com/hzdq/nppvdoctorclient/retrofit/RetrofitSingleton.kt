package com.hzdq.nppvdoctorclient.retrofit

import android.content.Context
import com.hzdq.nppvdoctorclient.util.Shp
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitSingleton private constructor(val context: Context){
    private val BASE_URL = "http://test-hx.bajiesleep.com"
//    private val BASE_URL = "https://pap.bajiesleep.com/hx_app"




    private var shp  = Shp(context)
    //单例模式
    //private constructor 外部不可以通过构造器来生成实例
    companion object{
        private var  INSTANCE: RetrofitSingleton? = null

        fun getInstance(context: Context)=
            INSTANCE ?: synchronized(this){
                RetrofitSingleton(context).also {
                    INSTANCE = it

                }

            }
    }

    fun api(): Api {

        val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .client(OkHttpClient.Builder().addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .addHeader("token", shp.getToken())
                        .build()

                    chain.proceed(request)
                }.build())
                .baseUrl(URLCollection.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofit.create(Api::class.java)
    }


}


