package com.example.fitnessapp.ui.news

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.replace
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.MainActivity
import com.example.fitnessapp.R

class NewsViewHolder(itemView: View, var news: News? = null): RecyclerView.ViewHolder(itemView) {
    companion object {
        val NEWS_LINK = "news_link"
    }
    init {
        itemView.setOnClickListener {
//            val intent = Intent(itemView.context, WebViewFragment::class.java)
//
//            intent.putExtra(NEWS_LINK, news?.url)
//            itemView.context.startActivity(intent)

            val bundle = Bundle()
            bundle.putString(NEWS_LINK, news?.url)

            val newsFragment = NewsFragment()
            val webViewFragment = WebViewFragment()
            val activity = MainActivity()

            webViewFragment.arguments = bundle
            println("Activity")
            println(activity)
            activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.nav_host_fragment, webViewFragment)
                    ?.commit()
            println("Udah abis cliclknya")

        }
    }
}