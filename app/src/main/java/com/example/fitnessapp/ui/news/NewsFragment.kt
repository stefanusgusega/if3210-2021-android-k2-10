package com.example.fitnessapp.ui.news

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsFragment : Fragment() {

    private val country: String = "id"
    private val category: String = "sport"
    private val apiKey: String = "e6b211efcb8c4930a09fa5361c9bb072"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_news, container, false)
        val rvNews = rootView.findViewById<RecyclerView>(R.id.news_recycler)
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rvNews.apply {
                layoutManager = GridLayoutManager(this.context, 2)
            }
        }
        else {
            rvNews.layoutManager = LinearLayoutManager(this.context)
        }

//        rvNews.layoutManager = LinearLayoutManager(this.context)

        RetrofitClient.instance.getNews(country, category, apiKey).enqueue(object:
            Callback<NewsResponse> {
            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                println("Failed to fetch")
            }

            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                println(response.body().toString())
                rvNews.adapter = NewsAdapter(response.body()!!)
            }

        })
        return rootView
    }
}