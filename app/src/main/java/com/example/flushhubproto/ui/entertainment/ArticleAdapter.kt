package com.example.flushhubproto.ui.entertainment

import com.example.flushhubproto.ui.newsapi.Article
import com.example.tomtom.databinding.NewsItemBinding




import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.example.flushhubproto.ui.entertainment.ArticleAdapter.Companion.author
import com.example.flushhubproto.ui.entertainment.ArticleAdapter.Companion.content
import com.example.flushhubproto.ui.entertainment.ArticleAdapter.Companion.title
import com.example.tomtom.R


class ArticleAdapter(
    private var articles: List<Article> = emptyList(),
    private var fragment: com.example.flushhubproto.ui.entertainment.EntertainmentFragment
) : RecyclerView.Adapter<ArticleHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArticleHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NewsItemBinding.inflate(inflater, parent, false)
        return ArticleHolder(binding, fragment)
    }
    companion object {

        var title: String = ""
        var author: String = ""
        var content: String = ""

    }
    fun updateData(articleList: List<Article>) {
        notifyDataSetChanged()

        articles = articleList
    }

    override fun onBindViewHolder(holder: ArticleHolder, position: Int) {
        val article = articles[position]
        holder.bind(article)
    }

    override fun getItemCount() = articles.size
}
class ArticleHolder(
    private val binding: NewsItemBinding,
    private val fragment: com.example.flushhubproto.ui.entertainment.EntertainmentFragment
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(article: Article) {
        binding.titleText.text = article.title
        binding.authorText.text = article.author

        binding.root.setOnClickListener {

            title = article.title
            author = article.author ?: "Author"
            content = article.content ?: "Content"

            newsNavigation()
        }
    }
    private fun newsNavigation() {
        findNavController(fragment).navigate(R.id.list_to_detail)

    }
}
