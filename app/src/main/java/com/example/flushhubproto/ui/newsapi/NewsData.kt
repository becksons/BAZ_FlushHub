package com.example.flushhubproto.ui.newsapi


data class NewsResponse (
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)
data class Article (
    val id: String?,
    val sourceName: String,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
)