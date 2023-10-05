package com.example.e_finity.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.e_finity.MainActivity
import com.example.e_finity.R
import com.example.e_finity.UserRead
import com.example.e_finity.login.LogOrSignActivity
import com.example.e_finity.teams.TeamActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class HomeFragment : Fragment() {

    private var activity: MainActivity?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext().applicationContext
        val sharePreference = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)

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
        val manageTeamBtn = view.findViewById<Button>(R.id.manageteamButton)
        val pBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val headerTitle = view.findViewById<TextView>(R.id.headerTitle)
        val themeTitle = view.findViewById<TextView>(R.id.themeTitle)
        val bannerImage = view.findViewById<ImageView>(R.id.bannerImage)
        val descriptionHeader = view.findViewById<TextView>(R.id.descriptionHeader)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)
        headerTitle.visibility = View.GONE
        themeTitle.visibility = View.GONE
        bannerImage.visibility = View.GONE
        descriptionHeader.visibility = View.GONE
        descriptionTextView.visibility = View.GONE
        lifecycleScope.launch {
            kotlin.runCatching {
                val userRight = client.postgrest["user"].select {
                    eq("uniqueID", sharePreference.getString("SESSION", "").toString())
                }.decodeList<UserRead>()
                val editor = sharePreference.edit()
                editor.putString("ROLE", userRight[0].role)
                editor.apply()
                if (userRight[0].role == "Freshman") {
                    pBar.visibility = View.GONE
                    headerTitle.visibility = View.VISIBLE
                    themeTitle.visibility = View.VISIBLE
                    bannerImage.visibility = View.VISIBLE
                    descriptionHeader.visibility = View.VISIBLE
                    descriptionTextView.visibility = View.VISIBLE
                }
                else {
                    pBar.visibility = View.GONE
                    headerTitle.setText("Welcome Back \nLeader!")
                    headerTitle.visibility = View.VISIBLE
                    themeTitle.setText("REGISTER YOUR MEMBERS BY SUB GROUP")
                    themeTitle.visibility = View.VISIBLE
                }
            }.onFailure {
                Toast.makeText(context, "There is no internet access / Server is down (App may crash)", Toast.LENGTH_LONG).show()
            }
        }


//        pBar.visibility = View.GONE
//        manageTeamBtn.setOnClickListener {
//            lifecycleScope.launch {
//                pBar.visibility = View.VISIBLE
//                val userteamResponse = client.postgrest["user"].select {
//                    eq("uniqueID", sharePreference.getString("SESSION", "").toString())
//                }
//                val userteam = userteamResponse.decodeList<UserRead>()
//                if (userteam[0].group == "None") {
//                    pBar.visibility = View.GONE
//                    requireActivity().run {
//                        startActivity(Intent(this, TeamActivity::class.java))
//                    }
//                }
//            }
//        }
    }

}