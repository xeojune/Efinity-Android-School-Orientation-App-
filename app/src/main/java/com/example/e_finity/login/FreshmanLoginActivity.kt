package com.example.e_finity.login

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
import com.example.e_finity.MainActivity
import com.example.e_finity.UserRead
import com.example.e_finity.databinding.ActivityFreshmanloginBinding
import com.romellfudi.fudinfc.gear.NfcAct
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.math.BigInteger

@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class FreshmanLoginActivity: NfcAct() {

    private lateinit var binding: ActivityFreshmanloginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFreshmanloginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.flogTextView.text = String(Character.toChars(0x1F33E))
    }

    public override fun onNewIntent(paramIntent: Intent) {
        super.onNewIntent(paramIntent)
        val dataMac = getMAC(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) as? Tag)
        freshLogin(dataMac)
    }

    private fun freshLogin(dataMac: String) {
        val client = getclient()
        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        val editor = sharePreference.edit()

        lifecycleScope.launch {
            val userinforesponse = client.postgrest["user"].select{
                eq("uniqueID", dataMac)
            }
            val userinfo = userinforesponse.decodeList<UserRead>()
            Log.e("dskdasdaskd", userinfo.toString())
            if (userinfo.toString() == "[]") {
                Toast.makeText(this@FreshmanLoginActivity, "This NFC is not registered as a Freshman account", Toast.LENGTH_LONG).show()
            }
            else {
                editor.putString("SESSION", dataMac)
                editor.putBoolean("AVATAR", userinfo[0].avatar)
                editor.apply()
                movePage()
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

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun movePage() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(org.koin.android.R.anim.abc_fade_in, androidx.transition.R.anim.abc_fade_out)
    }

    private fun getMAC(tag: Tag?): String =
        Regex("(.{2})").replace(
            String.format(
                "%0" + ((tag?.id?.size ?: 0) * 2).toString() + "X",
                BigInteger(1, tag?.id ?: byteArrayOf())
            ), "$1-"
        ).dropLast(1)
}