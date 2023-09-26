package com.example.e_finity.games

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_finity.adapter.treasureAdapter
import com.example.e_finity.databinding.ActivityTreasureBinding
import com.example.e_finity.treasureClass
import com.romellfudi.fudinfc.gear.NfcAct
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class treasureHuntActivity: NfcAct() {
    private lateinit var binding: ActivityTreasureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTreasureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getTreasureData()
    }

    private fun getTreasureData() {
        lifecycleScope.launch {
            val client = getclient()
            val supabaseResponse = client.postgrest["treasureHunt"].select() {
                order("id", Order.ASCENDING)
            }
            val data = supabaseResponse.decodeList<treasureClass>()
            val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
            val adapter = treasureAdapter(sharePreference.getString("SESSION", "").toString(), data)
            binding.treasureRecycler.adapter = adapter
            binding.treasureRecycler.layoutManager = LinearLayoutManager(this@treasureHuntActivity)
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