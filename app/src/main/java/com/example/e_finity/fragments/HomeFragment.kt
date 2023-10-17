package com.example.e_finity.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.e_finity.GroupAdd
import com.example.e_finity.GroupRead
import com.example.e_finity.MainActivity
import com.example.e_finity.R
import com.example.e_finity.UserRead
import com.example.e_finity.adapter.GroupAdapter
import com.example.e_finity.adapter.ScatterAdapter
import com.example.e_finity.login.LogOrSignActivity
import com.example.e_finity.scatterClass
import com.example.e_finity.teams.TeamActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.w3c.dom.Text

class HomeFragment : Fragment() {

    private var activity: MainActivity?= null
    var loaded: Boolean = false
    lateinit var userData: List<UserRead>

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

        fun getGroupData() {
            val client = getclient()
            lifecycleScope.launch {
                val groupDataResponse = client.postgrest["Orientation Group"].select {
                    order("id", Order.ASCENDING)
                }
                val groupData = groupDataResponse.decodeList<GroupRead>()
                val filteredgroupData = groupData.filter { it.name != "None" }
                val adapter = GroupAdapter(filteredgroupData)
                val groupRecycler = view.findViewById<RecyclerView>(R.id.groupRecycler)
                groupRecycler.adapter = adapter
                groupRecycler.layoutManager = GridLayoutManager(context, 2)
            }
        }


        getGroupData()

        val client = getclient()
        val headerTitle = view.findViewById<TextView>(R.id.headerTitle)
        val themeTitle = view.findViewById<TextView>(R.id.themeTitle)
        val bannerImage = view.findViewById<ImageView>(R.id.bannerImage)
        val descriptionHeader = view.findViewById<TextView>(R.id.descriptionHeader)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)
        val houseHeader = view.findViewById<TextView>(R.id.housesHeader)
        val houseTextView = view.findViewById<TextView>(R.id.housesTextView)
        val groupRecycler = view.findViewById<RecyclerView>(R.id.groupRecycler)
        headerTitle.visibility = View.GONE
        themeTitle.visibility = View.GONE
        bannerImage.visibility = View.GONE
        descriptionHeader.visibility = View.GONE
        descriptionTextView.visibility = View.GONE
        houseHeader.visibility = View.GONE
        houseTextView.visibility = View.GONE
        groupRecycler.visibility = View.GONE
        if (loaded == false) {
            runBlocking {
                kotlin.runCatching {
                    val userRight = client.postgrest["user"].select {
                        eq("uniqueID", sharePreference.getString("SESSION", "").toString())
                    }.decodeList<UserRead>()
                    val editor = sharePreference.edit()
                    editor.putString("ROLE", userRight[0].role)
                    editor.apply()
                    if (userRight[0].role == "Freshman") {
                        headerTitle.visibility = View.VISIBLE
                        themeTitle.visibility = View.VISIBLE
                        bannerImage.visibility = View.VISIBLE
                        descriptionHeader.visibility = View.VISIBLE
                        descriptionTextView.visibility = View.VISIBLE
                        houseTextView.visibility = View.VISIBLE
                    }
                    else {
                        headerTitle.setText("Welcome Back \nLeader!")
                        headerTitle.visibility = View.VISIBLE
                        themeTitle.setText("REGISTER YOUR MEMBERS BY SUB GROUP")
                        themeTitle.visibility = View.VISIBLE
                        groupRecycler.visibility = View.VISIBLE
                    }
                    loaded = true
                    userData = userRight
                }.onFailure {
                    Toast.makeText(context, "There is no internet access / Server is down (App may crash)", Toast.LENGTH_LONG).show()
                }
            }
        }
        else {
            if (userData[0].role == "Freshman") {
                headerTitle.visibility = View.VISIBLE
                themeTitle.visibility = View.VISIBLE
                bannerImage.visibility = View.VISIBLE
                descriptionHeader.visibility = View.VISIBLE
                descriptionTextView.visibility = View.VISIBLE
                houseHeader.visibility = View.VISIBLE
                houseTextView.visibility = View.VISIBLE
            }
            else {
                headerTitle.setText("Welcome Back \nLeader!")
                headerTitle.visibility = View.VISIBLE
                themeTitle.setText("REGISTER YOUR MEMBERS BY SUB GROUP")
                themeTitle.visibility = View.VISIBLE
                groupRecycler.visibility = View.VISIBLE
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