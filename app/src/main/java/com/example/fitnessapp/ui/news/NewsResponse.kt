package com.example.fitnessapp.ui.news

data class NewsResponse (
    val status: String,
    val totalResults: Int,
    val articles: ArrayList<News>
)
