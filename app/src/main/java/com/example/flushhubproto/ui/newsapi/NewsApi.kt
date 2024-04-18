package com.example.newsapp.newsapi

import com.example.flushhubproto.ui.newsapi.NewsResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query



object RetrofitInstance {

    private const val url = "https://newsapi.org"

    val api: NewsApi by lazy {

        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApi::class.java)
    }

}
interface NewsApi {
    @GET("v2/top-headlines")
    suspend fun fetchHeadlines(
        @Query("apiKey") apiKey: String,
        @Query("country") country: String,
        @Query("category") category: String
    ): Response<NewsResponse>
}