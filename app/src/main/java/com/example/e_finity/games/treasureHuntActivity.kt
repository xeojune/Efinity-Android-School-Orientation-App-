package com.example.e_finity.games

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_finity.adapter.treasureAdapter
import com.example.e_finity.databinding.ActivityTreasureBinding
import com.example.e_finity.treasureClass
import com.example.e_finity.treasureClassComplete
import com.example.e_finity.userScore
import com.romellfudi.fudinfc.gear.NfcAct
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import java.math.BigInteger

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

    private fun getTreasureData2() {
        val rvs = binding.treasureRecycler.layoutManager?.onSaveInstanceState()
        lifecycleScope.launch {
            val client = getclient()
            val supabaseResponse = client.postgrest["treasureHunt"].select() {
                order("id", Order.ASCENDING)
            }
            val data = supabaseResponse.decodeList<treasureClass>()
            val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
            val adapter = treasureAdapter(sharePreference.getString("SESSION", "").toString(), data)
            binding.treasureRecycler.adapter = adapter
            binding.treasureRecycler.layoutManager?.onRestoreInstanceState(rvs)
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

    override fun onResume() {
        super.onResume()
        getTreasureData2()
    }

    public override fun onNewIntent(paramIntent: Intent) {
        super.onNewIntent(paramIntent)
        val dataMac = getMAC(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) as? Tag)
        scanTag(dataMac)
    }


    private fun scanTag(dataMac: String) {
        val client = getclient()
        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        lifecycleScope.launch {
            val comp = client.postgrest["treasureHunt"].select(columns = Columns.list("UID", "points", "completed")){
                eq("UID", dataMac)
            }.decodeList<treasureClassComplete>()
            if (comp.toString() == "[]") {
                Toast.makeText(this@treasureHuntActivity, "There is no mission registered with the scanned tag", Toast.LENGTH_LONG).show()
            }
            else if (sharePreference.getString("SESSION", "").toString() in comp[0].completed) {
                Toast.makeText(this@treasureHuntActivity, "You've already completed this mission", Toast.LENGTH_LONG).show()
            }
            else {
                addComplete(dataMac, comp[0].points, comp[0].completed, sharePreference.getString("SESSION", "").toString())
            }
        }
    }

    private fun addComplete(dataMac: String, points: Int, comp: String, user: String) {
        val client = getclient()
        lifecycleScope.launch{
            kotlin.runCatching {
                client.postgrest["treasureHunt"].update(
                    {
                        set("completed", comp + user + ", ")
                    }
                ){
                    eq("UID", dataMac)
                }
            }.onSuccess {
                getTreasureData2()
                addPoints(points, user)
                Toast.makeText(this@treasureHuntActivity, "You have earned " + points.toString() + " points!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addPoints(points: Int, user: String) {
        val client = getclient()
        lifecycleScope.launch {
            val userScore = client.postgrest["user"].select(columns = Columns.list("uniqueID", "score")){
                eq("uniqueID", user)
            }.decodeList<userScore>()
            val total = userScore[0].score + points
            kotlin.runCatching {
                client.postgrest["user"].update(
                    {
                        set("score", total)
                    }
                ){
                    eq("uniqueID", user)
                }
            }.onFailure {
                Toast.makeText(this@treasureHuntActivity, total.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun getMAC(tag: Tag?): String =
        Regex("(.{2})").replace(
            String.format(
                "%0" + ((tag?.id?.size ?: 0) * 2).toString() + "X",
                BigInteger(1, tag?.id ?: byteArrayOf())
            ), "$1-"
        ).dropLast(1)
}