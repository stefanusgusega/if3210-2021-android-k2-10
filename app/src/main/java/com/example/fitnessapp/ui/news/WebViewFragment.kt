package com.example.fitnessapp.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class WebViewFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_webview, container, false)
        val wvNews = rootView.findViewById<WebView>(R.id.webView_news)

        val newsLinkBundle = arguments

        if (newsLinkBundle != null) {
            val newsLink = newsLinkBundle.getString(NewsViewHolder.NEWS_LINK)
            println("news link : ")
            println(newsLink)
            if (newsLink != null) {
                wvNews.loadUrl(newsLink)
            }
            else {
                wvNews.loadUrl("https://en.wikipedia.org/wiki/HTTP_404")
            }
        }

        return rootView
    }
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_webview)
//
//        val newsLink = intent.getStringExtra(NewsViewHolder.NEWS_LINK)
//        val newsWebView = findViewById<WebView>(R.id.webView_news)
//        if (newsLink != null) {
//            newsWebView.loadUrl(newsLink)
//        }
//        else {
//            newsWebView.loadUrl("https://en.wikipedia.org/wiki/HTTP_404")
//        }
//
//        val navView: BottomNavigationView = findViewById(R.id.nav_view)
//
//        val navController = findNavController(R.id.nav_host_fragment)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(setOf(
//                R.id.navigation_news, R.id.navigation_tracker, R.id.navigation_history, R.id.navigation_scheduler))
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
//
//    }

}

