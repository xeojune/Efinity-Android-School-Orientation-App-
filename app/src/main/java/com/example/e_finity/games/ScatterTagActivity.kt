package com.example.e_finity.games

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_finity.adapter.ScatterAdapter
import com.example.e_finity.databinding.ActivityScatterBinding
import com.example.e_finity.scatterClass
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
class ScatterTagActivity: NfcAct() {
    private lateinit var binding: ActivityScatterBinding
    var state: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScatterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getScatterData()
    }

//    override fun onPause() {
//        super.onPause()
//        state = true
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val addState = data?.getStringExtra("State")
        if (addState == "Added" || addState == "Updated" || addState == "Deleted") {
            refreshScatterData()
        }
    }

//    override fun onResume() {
//        super.onResume()
//        if (state) {
//            refreshScatterData()
//        }
//    }

    public override fun onNewIntent(paramIntent: Intent) {
        super.onNewIntent(paramIntent)
        state = false
        val dataMac = getMAC(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) as? Tag)
        scanTag(dataMac)
    }

    private fun getScatterData() {
        val client = getclient()
        lifecycleScope.launch {
            val scatterDataResponse = client.postgrest["scatterTag"].select {
                order("id", Order.ASCENDING)
            }
            val scatterData = scatterDataResponse.decodeList<scatterClass>()
            val adapter = ScatterAdapter(scatterData)
            Log.e("supa", scatterData.toString())
            binding.scatterRecycler.adapter = adapter
            binding.scatterRecycler.layoutManager = GridLayoutManager(this@ScatterTagActivity, 5)
            binding.scatterPBar.visibility = View.GONE
        }
    }

    private fun refreshScatterData() {
        val client = getclient()
        val rvs = binding.scatterRecycler.layoutManager?.onSaveInstanceState()
        lifecycleScope.launch {
            val scatterDataResponse = client.postgrest["scatterTag"].select {
                order("id", Order.ASCENDING)
            }
            val scatterData = scatterDataResponse.decodeList<scatterClass>()
            val adapter = ScatterAdapter(scatterData)
            binding.scatterRecycler.adapter = adapter
            binding.scatterRecycler.layoutManager?.onRestoreInstanceState(rvs)
        }
    }

    private fun scanTag(dataMac: String) {
        val client = getclient()
        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        lifecycleScope.launch {
            val comp = client.postgrest["scatterTag"].select(columns = Columns.list("UID", "points", "completed")){
                eq("UID", dataMac)
            }.decodeList<treasureClassComplete>()
            if (comp.toString() == "[]") {
                Toast.makeText(this@ScatterTagActivity, "This tag is not registered", Toast.LENGTH_LONG).show()
            }
            else if (sharePreference.getString("SESSION", "").toString() == comp[0].completed) {
                Toast.makeText(this@ScatterTagActivity, "You've already scanned this tag", Toast.LENGTH_LONG).show()
            }
            else if (comp[0].completed != "") {
                Toast.makeText(this@ScatterTagActivity, "Someone else has already scanned this tag", Toast.LENGTH_LONG).show()
            }
            else {
                addComplete(dataMac, comp[0].points, sharePreference.getString("SESSION", "").toString())
            }
        }
    }

    private fun addComplete(dataMac: String, points: Int, user: String) {
        val client = getclient()
        lifecycleScope.launch{
            kotlin.runCatching {
                client.postgrest["scatterTag"].update(
                    {
                        set("completed", user)
                    }
                ){
                    eq("UID", dataMac)
                }
            }.onSuccess {
                refreshScatterData()
                addPoints(points, user)
                Toast.makeText(this@ScatterTagActivity, "You have earned " + points.toString() + " points!", Toast.LENGTH_LONG).show()
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
                Toast.makeText(this@ScatterTagActivity, total.toString(), Toast.LENGTH_LONG).show()
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

    private fun getMAC(tag: Tag?): String =
        Regex("(.{2})").replace(
            String.format(
                "%0" + ((tag?.id?.size ?: 0) * 2).toString() + "X",
                BigInteger(1, tag?.id ?: byteArrayOf())
            ), "$1-"
        ).dropLast(1)
}