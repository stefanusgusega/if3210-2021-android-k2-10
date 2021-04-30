package com.example.fitnessapp.ui.news

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    @GET("top-headlines")
    fun getNews(@Query("country") country: String,
                @Query("category") category: String,
                @Query("apiKey") apiKey: String): Call<NewsResponse>
}