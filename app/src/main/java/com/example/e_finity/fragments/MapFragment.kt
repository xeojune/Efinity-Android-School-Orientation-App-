package com.example.e_finity.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.e_finity.MainActivity
import com.example.e_finity.R

class MapFragment : Fragment() {

    private var activity: MainActivity?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val webView = view.findViewById<WebView>(R.id.webView)
        webView.webViewClient = WebViewClient()

        webView.loadUrl("https://maps.ntu.edu.sg/#/ntu/d386ffa80e4e46f286d17f08/poi/search")
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true)

    }
}