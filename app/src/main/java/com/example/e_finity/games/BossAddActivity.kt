package com.example.e_finity.games

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_finity.bossesAdd
import com.example.e_finity.bossesClass
import com.example.e_finity.databinding.ActivityBossAddBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BossAddActivity: AppCompatActivity() {
    private lateinit var binding: ActivityBossAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBossAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bossId = intent.getStringExtra("bossId")
        val latitude = intent.getStringExtra("latitude")?.toDouble()
        val longitude = intent.getStringExtra("longitude")?.toDouble()

        if (bossId != null) {
            binding.bossAddBtn.visibility = View.GONE
            MainScope().launch {
                val client = getclient()
                val bossData = client.postgrest["bosses"].select {
                    eq("id", bossId.toInt())
                }.decodeList<bossesClass>()
                binding.bossNameEditText.setText(bossData[0].bossName)
                binding.bossPowerEditText.setText(bossData[0].bossPower.toString())
                binding.bossDescEditText.setText(bossData[0].bossDesc)
            }
        }
        else {
            binding.bossDeleteBtn.visibility = View.GONE
            binding.bossEditBtn.visibility = View.GONE
        }

        binding.latitude.text = "Latitude: " + latitude.toString()
        binding.longitude.text = "Longitude: " + longitude.toString()

        binding.bossAddBtn.setOnClickListener {
            if (binding.bossNameEditText.text.toString() == "" ||
                binding.bossDescEditText.text.toString() == "" ||
                binding.bossPowerEditText.text.toString() == "") {
                Toast.makeText(this, "Incomplete fields", Toast.LENGTH_LONG).show()
            }
            else {
                val client = getclient()
                if (latitude != null && longitude != null) {
                    val boss = bossesAdd(bossName = binding.bossNameEditText.text.toString(),
                        bossPower = binding.bossPowerEditText.text.toString().toInt(),
                        bossDesc = binding.bossDescEditText.text.toString(),
                        lat = latitude, log = longitude)
                    MainScope().launch {
                        kotlin.runCatching {
                            client.postgrest["bosses"].insert(boss, returning = Returning.MINIMAL)
                        }
                    }
                }
                ActivityEnd()
            }
        }

        binding.bossEditBtn.setOnClickListener {
            if (binding.bossNameEditText.text.toString() == "" ||
                binding.bossDescEditText.text.toString() == "" ||
                binding.bossPowerEditText.text.toString() == "") {
                Toast.makeText(this, "Incomplete fields", Toast.LENGTH_LONG).show()
            }
            else {
                val client = getclient()
                MainScope().launch {
                    client.postgrest["bosses"].update(
                        {
                            set("bossName", binding.bossNameEditText.text.toString())
                            set("bossPower", binding.bossPowerEditText.text.toString().toInt())
                            set("bossDesc", binding.bossDescEditText.text.toString())
                        }
                    ) {
                        if (bossId != null) {
                            eq("id", bossId)
                        }
                    }
                }
                ActivityEnd()
            }
        }

        binding.bossDeleteBtn.setOnClickListener {
            val client = getclient()
            MainScope().launch {
                client.postgrest["bosses"].delete {
                    if (bossId != null) {
                        eq("id", bossId)
                    }
                }
            }
            ActivityEnd()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ActivityEnd()
    }

    private fun ActivityEnd() {
        val intent = Intent(this, BossFightActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getclient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://nabbsmcfsskdwjncycnk.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5hYmJzbWNmc3NrZHdqbmN5Y25rIiwicm9sZSI6ImFub24iLCJpYXQiOjE2OTM5MDM3ODksImV4cCI6MjAwOTQ3OTc4OX0.dRVk2u91mLhSMaA1s0FSyIFwnxe2Y3TPdZZ4Shc9mAY"
        ) {
            install(Postgrest)
            install(GoTrue)
            install(Storage)
        }
    }
}