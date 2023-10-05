package com.example.e_finity.games

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.e_finity.databinding.ActivityAddscatterBinding
import com.example.e_finity.scatterClassAdd
import com.romellfudi.fudinfc.gear.NfcAct
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning
import kotlinx.coroutines.launch
import java.math.BigInteger

@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class AddScatterActivity: NfcAct() {

    private lateinit var binding: ActivityAddscatterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddscatterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val point = intent.getStringExtra("Points")
        val UID = intent.getStringExtra("UID")
        binding.scatterDelBtn.visibility = View.GONE
        if (point != null) {
            binding.scatterDelBtn.visibility = View.VISIBLE
            binding.scatterPointEdit.setText(point)
            binding.scatterNfcTextView.text = "Scanned NFC: " + UID
            binding.scatterAddBtn.text = "Edit"
        }
        binding.scatterAddBtn.setOnClickListener {
            if (binding.scatterPointEdit.text.toString() == "") {
                Toast.makeText(this, "Incomplete fields", Toast.LENGTH_LONG).show()
            }
            else if (binding.scatterNfcTextView.text == "Scanned NFC: ") {
                Toast.makeText(this, "Scan an unregistered NFC", Toast.LENGTH_LONG).show()
            }
            else {
                if (binding.scatterAddBtn.text == "Edit") {
                    if (UID != null) {
                        updateScatterData(UID)
                    }
                }
                else {
                    addScatterData()
                }
            }
        }
        binding.scatterDelBtn.setOnClickListener {
            if (UID != null) {
                deleteScatter(UID)
            }
        }
    }

    public override fun onNewIntent(paramIntent: Intent) {
        super.onNewIntent(paramIntent)
        val dataMac = getMAC(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) as? Tag)
        binding.scatterNfcTextView.text = "Scanned NFC: " + dataMac
    }

    private fun addScatterData() {
        val client = getclient()
        val scatterData = scatterClassAdd(
            UID = binding.scatterNfcTextView.text.toString().replace("Scanned NFC: ", ""),
            points = binding.scatterPointEdit.text.toString().toInt())
        lifecycleScope.launch {
            kotlin.runCatching {
                client.postgrest["scatterTag"].insert(scatterData, returning = Returning.MINIMAL)
            }.onFailure {
                Toast.makeText(this@AddScatterActivity, "There is already a mission that is registered with the scanned tag", Toast.LENGTH_LONG).show()
            }.onSuccess {
                val intent = Intent(applicationContext, ScatterTagActivity::class.java)
                intent.putExtra("State", "Added")
                setResult(1000, intent)
                finish()
            }
        }
    }

    private fun updateScatterData(onfc: String) {
        val client = getclient()
        lifecycleScope.launch {
            kotlin.runCatching {
                client.postgrest["scatterTag"].update(
                    {
                        set("UID", binding.scatterNfcTextView.text.toString().replace("Scanned NFC: ", ""))
                        set("points", binding.scatterPointEdit.text.toString().toInt())
                    }
                ) {
                    eq("UID", onfc)
                }
            }.onFailure {
                Toast.makeText(this@AddScatterActivity, "This tag is already registered", Toast.LENGTH_LONG).show()
            }.onSuccess {
                val intent = Intent(applicationContext, ScatterTagActivity::class.java)
                intent.putExtra("State", "Updated")
                setResult(1000, intent)
                finish()
            }
        }
    }

    private fun deleteScatter(nfc: String) {
        val client = getclient()
        lifecycleScope.launch {
            client.postgrest["scatterTag"].delete {
                eq("UID", nfc)
            }
        }
        val intent = Intent(applicationContext, ScatterTagActivity::class.java)
        intent.putExtra("State", "Deleted")
        setResult(1000, intent)
        finish()
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