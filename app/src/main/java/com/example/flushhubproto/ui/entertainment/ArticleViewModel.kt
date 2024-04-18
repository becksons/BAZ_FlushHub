package com.example.flushhubproto.ui.entertainment



import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.newsapp.newsapi.RetrofitInstance
import kotlinx.coroutines.launch
import java.lang.Exception
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.flushhubproto.ui.newsapi.Article


class ArticleViewModel : ViewModel() {
    private val _articles = MutableLiveData<List<Article>>()
    val articles : LiveData<List<Article>> = _articles

    fun fetchArticles(category: String) {
        viewModelScope.launch {


            try {
                val response =
                    RetrofitInstance.api.fetchHeadlines(
                        apiKey = "b095d35964cc4c02ad0c7212c51215dc",
                        country = "us",
                        category=category
                    )
                if (response.isSuccessful) {
                    _articles.postValue(response.body()!!.articles)

                }

            } catch (e: Exception) {
                Log.e("ArticleViewModel", "Error: $e")

            }
        }
    }
}

class ArticleModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticleViewModel() as T
        }
        throw IllegalArgumentException("ERR")
    }
}