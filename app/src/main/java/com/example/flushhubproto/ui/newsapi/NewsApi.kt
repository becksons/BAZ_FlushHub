package com.example.flushhubproto.ui.newsapi


import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException


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
    fun fetchMemes(subreddit: String = "memes") {
        val url = "https://meme-api.com/gimme/$subreddit/2"  // Adjust the number of memes and subreddit as needed
        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Handle the error
            }



            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let {

                        // Update your UI here with the list of memes
                    }
                } else {
                    // Handle the response error
                }
            }
        })
    }
}
