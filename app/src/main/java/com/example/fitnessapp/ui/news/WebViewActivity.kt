package com.example.fitnessapp.ui.news

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessapp.R

class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val newsLink = intent.getStringExtra(NewsViewHolder.NEWS_LINK)
        val newsWebView = findViewById<WebView>(R.id.webView_news)
        if (newsLink != null) {
            newsWebView.loadUrl(newsLink)
        }

        else {
            newsWebView.loadUrl("https://en.wikipedia.org/wiki/HTTP_404")
        }

    }
}

