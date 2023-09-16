package com.example.e_finity.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.MainActivity
import com.example.e_finity.R
import com.example.e_finity.login.LogOrSignActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var activity: MainActivity?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = activity
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext().applicationContext
        val sharePreference = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        val sess = sharePreference.getString("SESSION", "").toString()

        val logoutTextView = view.findViewById<TextView>(R.id.logoutTextView)
        logoutTextView.setOnClickListener {
            val editor = sharePreference.edit()
            editor.clear()
            editor.apply()
            requireActivity().run {
                startActivity(Intent(this, LogOrSignActivity::class.java))
                finish()
            }
        }

        fun getclient(): SupabaseClient {
            return createSupabaseClient(
                supabaseUrl = "https://nabbsmcfsskdwjncycnk.supabase.co",
                supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5hYmJzbWNmc3NrZHdqbmN5Y25rIiwicm9sZSI6ImFub24iLCJpYXQiOjE2OTM5MDM3ODksImV4cCI6MjAwOTQ3OTc4OX0.dRVk2u91mLhSMaA1s0FSyIFwnxe2Y3TPdZZ4Shc9mAY"
            ) {
                install(Postgrest)
                install(GoTrue)
                install(Storage)
            }
        }

        val client = getclient()
        val bucket = client.storage["avatar"]
        val imageView = view.findViewById<ImageView>(R.id.imageView)

        lifecycleScope.launch {
            kotlin.runCatching {
                val bytes = bucket.downloadPublic("1581942.png")
                Glide.with(view).load(bytes).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(imageView)
            }
        }
    }

}