package com.example.e_finity.teams

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.e_finity.GroupName
import com.example.e_finity.GroupRead
import com.example.e_finity.MainActivity
import com.example.e_finity.databinding.ActivityJointeamBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class JoinTeamActivity: AppCompatActivity() {
    private lateinit var binding: ActivityJointeamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJointeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val client = getclient()
        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        lifecycleScope.launch {
            val teamDataReponse = client.postgrest["Orientation Group"].select() {
                neq("name", "None")
            }
            val teamData = teamDataReponse.decodeList<GroupRead>()

            val arr = teamData.map { it.name }
            val arrayAdapter = ArrayAdapter(this@JoinTeamActivity, android.R.layout.simple_spinner_dropdown_item, arr)
            binding.teamSpinner.adapter = arrayAdapter
        }

        binding.teamjoinButton.setOnClickListener {
            if (binding.teamSpinner.selectedItem == null) {
                Toast.makeText(this,"There is no team at the moment, go and make one!", Toast.LENGTH_LONG).show()
            }
            else {
                lifecycleScope.launch {
                    client.postgrest["user"].update(
                        {
                            set("group", binding.teamSpinner.selectedItem.toString())
                        }
                    ) {
                        eq("uniqueID", sharePreference.getString("SESSION", "").toString())
                    }
                }
                Toast.makeText(this,"Joined " + binding.teamSpinner.selectedItem.toString() + " !", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

    }

    private fun getclient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://nabbsmcfsskdwjncycnk.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5hYmJzbWNmc3NrZHdqbmN5Y25rIiwicm9sZSI6ImFub24iLCJpYXQiOjE2OTM5MDM3ODksImV4cCI6MjAwOTQ3OTc4OX0.dRVk2u91mLhSMaA1s0FSyIFwnxe2Y3TPdZZ4Shc9mAY"
        ) {
            install(Postgrest)
            install(GoTrue)
        }
    }
}