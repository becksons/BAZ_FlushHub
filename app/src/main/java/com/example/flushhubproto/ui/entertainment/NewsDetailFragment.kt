package com.example.flushhubproto.ui.entertainment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.flushhubproto.ui.entertainment.ArticleAdapter.Companion.author
import com.example.flushhubproto.ui.entertainment.ArticleAdapter.Companion.content
import com.example.flushhubproto.ui.entertainment.ArticleAdapter.Companion.title
import com.example.tomtom.R

class DetailFragment : Fragment() {

    private lateinit var articleViewModel: ArticleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val articleModelFactory = ArticleModelFactory(requireContext())
        articleViewModel = ViewModelProvider(requireActivity(), articleModelFactory)[(ArticleViewModel::class.java)]
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.news_item_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.articleTitle).text = title
        view.findViewById<TextView>(R.id.articleAuthor).text = author
        view.findViewById<TextView>(R.id.articleContent).text = content

        view.findViewById<Button>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }
    }
}