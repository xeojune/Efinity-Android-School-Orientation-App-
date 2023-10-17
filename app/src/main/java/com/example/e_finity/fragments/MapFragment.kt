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

private lateinit var webView: WebView
private lateinit var webviewstate: Bundle

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

    override fun onPause() {
        super.onPause()
        webviewstate = Bundle()
        webView.saveState(webviewstate)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById<WebView>(R.id.webView)
        webView.webViewClient = WebViewClient()

        if (savedInstanceState == null) {
            webView.loadUrl("https://maps.ntu.edu.sg/#/ntu/d386ffa80e4e46f286d17f08/poi/search")
        }
        else {
            webView.restoreState(webviewstate)
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true)

    }
}