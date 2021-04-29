package com.example.fitnessapp.ui.news

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.squareup.picasso.Picasso

class NewsAdapter(private val responses: NewsResponse) : RecyclerView.Adapter<NewsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return responses.articles.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = responses.articles.get(position)
        holder.itemView.findViewById<TextView>(R.id.news_title).text = news.title
        holder.itemView.findViewById<TextView>(R.id.news_author).text = news.author
        holder.itemView.findViewById<TextView>(R.id.news_desc).text = news.description

        // thumbnail image
        val thumbnail = holder.itemView.findViewById<ImageView>(R.id.imageView_thumbnail)
        Picasso.with(holder.itemView.context).load(news.urlToImage).into(thumbnail)

        // webview
        holder.news = news
    }
}