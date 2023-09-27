package com.example.e_finity.games

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.e_finity.databinding.ActivityAddtreasureBinding
import com.example.e_finity.treasureClassAdd
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
class AddTreasureActivity: NfcAct() {

    private lateinit var binding: ActivityAddtreasureBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddtreasureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.treasureAddBtn.setOnClickListener {
            if (binding.treasureContentEdit.text.toString() == "" || binding.treasureScoreEdit.text.toString() == "") {
                Toast.makeText(this, "Incomplete fields", Toast.LENGTH_LONG).show()
            }
            else if (binding.nfcTextView.text == "Scanned NFC: ") {
                Toast.makeText(this, "Scan an unregistered NFC", Toast.LENGTH_LONG).show()
            }
            else {
                addTreasureData()
                finish()
            }
        }
    }

    public override fun onNewIntent(paramIntent: Intent) {
        super.onNewIntent(paramIntent)
        val dataMac = getMAC(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) as? Tag)
        binding.nfcTextView.text = "Scanned NFC: " + dataMac
    }

    private fun addTreasureData() {
        val client = getclient()
        val treasureData = treasureClassAdd(
            UID = binding.nfcTextView.text.toString().replace("Scanned NFC: ", ""),
            content = binding.treasureContentEdit.text.toString(),
            points = binding.treasureScoreEdit.text.toString().toInt(),
            completed = ""
        )
        lifecycleScope.launch {
            client.postgrest["treasureHunt"].insert(treasureData, returning = Returning.MINIMAL)
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