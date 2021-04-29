package com.example.fitnessapp.ui.news

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class NewsViewHolder(itemView: View, var news: News? = null): RecyclerView.ViewHolder(itemView) {
    companion object {
        val NEWS_LINK = "news_link"
    }
    init {
        itemView.setOnClickListener {
            val intent = Intent(itemView.context, WebViewActivity::class.java)

            intent.putExtra(NEWS_LINK, news?.url)
            itemView.context.startActivity(intent)
        }
    }
}