package com.example.flushhubproto.ui.entertainment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.tomtom.R
import com.example.tomtom.databinding.RandomAnimalFragmentBinding
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class RandomAnimalFragment : Fragment() {

    private lateinit var imageViewMeme: ImageView
    private lateinit var imageViewDog: ImageView
    private lateinit var imageViewFox: ImageView
    private var _binding: RandomAnimalFragmentBinding? = null
    private val binding get() = _binding!!

    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = RandomAnimalFragmentBinding.inflate(inflater, container, false)
        imageViewMeme = binding.catImageView
        imageViewDog = binding.dogImageView
        imageViewFox = binding.foxImageView
        loadImage("https://cataas.com/cat", imageViewMeme)
        fetchJson("https://dog.ceo/api/breeds/image/random", imageViewDog)
        fetchJson("https://randomfox.ca/floof/", imageViewFox)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.catButton.setOnClickListener {
            loadImage("https://cataas.com/cat", imageViewMeme)
        }

        binding.dogButton.setOnClickListener {
            fetchJson("https://dog.ceo/api/breeds/image/random", imageViewDog)
        }

        binding.foxButton.setOnClickListener {
            fetchJson("https://randomfox.ca/floof/", imageViewFox)
        }
    }

    private fun fetchJson(url: String, imageView: ImageView) {
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    activity?.runOnUiThread {
                        jsonResponse?.let {
                            val url = parseJson(it, imageView)
                            url?.let { imgUrl ->
                                loadImage(imgUrl, imageView)
                            }
                        }
                    }
                }else{
                    Glide.with(requireActivity())
                        .load(R.drawable.default_animal)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(imageView)

                }
            }
        })
    }

    private fun parseJson(jsonResponse: String, imageView: ImageView): String? {
        return if (imageView == imageViewDog) {
            Gson().fromJson(jsonResponse, DogApiResponse::class.java).message
        } else if (imageView == imageViewFox) {
            Gson().fromJson(jsonResponse, FoxApiResponse::class.java).image
        } else {
            null
        }
    }

    private fun loadImage(url: String, imageView: ImageView) {
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE) //Preventing caching so a new image is loaded every time
            .skipMemoryCache(true)
            .into(imageView)
    }

    data class DogApiResponse(val message: String, val status: String)
    data class FoxApiResponse(val image: String, val link: String)
}


// Image loading logic referenced from
// https://medium.com/@SmdNayan/the-best-way-to-load-the-image-using-glide-and-cache-strategy-in-android-3032e6ef0c78
