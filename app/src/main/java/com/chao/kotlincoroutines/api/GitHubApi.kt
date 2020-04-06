package com.chao.kotlincoroutines.api

import com.chao.kotlincoroutines.model.Repo
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

internal interface GitHubApi {

    @GET("users/{user}/repos")
    fun getListRepos(@Path("user") user: String): Call<List<Repo>>

    //改操作用于下载文件,url传入下载的全路径,Streaming在大文件下载时必须添加,ResponseBody封装下载的流
    @Streaming
    @GET
    fun downloadFile(@Url url: String): Call<ResponseBody>


    //伴生对象,伴生对象里的方法和属性类似于java的static
    companion object {
        //Volatile 注解同java中的 volatile关键字,表示属性更新后在其他线程立即可见
        @Volatile
        private var instance: GitHubApi? = null

        /*这里的 "?:" 是Elvis表达式,意思是前面的不为null,直接返回前面的,
        如果前面的为null,执行后面的操作,这里使用了两次,就等价于java单例中的double-check*/
        fun getInstance(): GitHubApi = instance ?: synchronized(GitHubApi::class.java) {
            instance ?: Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GitHubApi::class.java)
                .also { instance = it }
        }
    }
}