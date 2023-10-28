package com.example.e_finity.teams

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.e_finity.Stats
import com.example.e_finity.User
import com.example.e_finity.databinding.ActivityAddmemberBinding
import com.example.e_finity.memberEdit
import com.romellfudi.fudinfc.gear.NfcAct
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.bind
import java.math.BigInteger

@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class MemberAddActivity: NfcAct() {
    private lateinit var binding: ActivityAddmemberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddmemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.memberDeleteBtn.visibility = View.GONE
        binding.memberEditBtn.visibility = View.GONE

        val name = intent.getStringExtra("name")
        val fullName = intent.getStringExtra("fullName")
        val phoneNo = intent.getStringExtra("phoneNo")
        val passedNfc = intent.getStringExtra("NFC")

        val client = getclient()
        val bucket = client.storage["avatar"]

        if (fullName != null) {
            binding.memberFull.setText(fullName)
            binding.memberPhone.setText(phoneNo)
            binding.memberNFC.text = "Scanned NFC: " + passedNfc
            binding.memberAddBtn.visibility = View.GONE
            binding.memberDeleteBtn.visibility = View.VISIBLE
            binding.memberEditBtn.visibility = View.VISIBLE
        }

        binding.memberAddBtn.setOnClickListener {
            if (binding.memberFull.text.toString() == "" || binding.memberPhone.text.toString() == "") {
                Toast.makeText(this,"Incomplete fields", Toast.LENGTH_LONG).show()
            }
            else if (binding.memberNFC.text.toString() == "Scanned NFC: ") {
                Toast.makeText(this,"Scan an unregistered NFC", Toast.LENGTH_LONG).show()
            }
            else {
                if (name != null) {
                    addMember(name)
                }
            }
        }

        binding.memberDeleteBtn.setOnClickListener {
            runBlocking {
                kotlin.runCatching {
                    client.postgrest["stats"].delete {
                        if (passedNfc != null) {
                            eq("uniqueID", passedNfc)
                        }
                    }
//                    client.postgrest["stats"].update({
//                        set("uniqueID", "DELETE")
//                    }) {
//                        if (passedNfc != null) {
//                            eq("uniqueID", passedNfc)
//                        }
//                    }
                    client.postgrest["user"].delete {
                        if (passedNfc != null) {
                            eq("uniqueID", passedNfc)
                        }
                    }
                    bucket.delete(passedNfc + ".png")
                }.onSuccess {
                    Toast.makeText(this@MemberAddActivity, "Successfully deleted the member data", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure {
                    Toast.makeText(this@MemberAddActivity, "Successfully deleted the member data", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }

        binding.memberEditBtn.setOnClickListener {
            runBlocking {
                kotlin.runCatching {
                    client.postgrest["stats"].update({
                        set("uniqueID", "MODIFY")
                    }){
                        if (passedNfc != null) {
                            eq("uniqueID", passedNfc)
                        }
                    }
                    client.postgrest["user"].update(
                        {
                            set("full_name", binding.memberFull.text.toString())
                            set("phone_num", binding.memberPhone.text.toString())
                            set("uniqueID", binding.memberNFC.text.toString().replace("Scanned NFC: ", ""))
                        }
                    ) {
                        if (passedNfc != null) {
                            eq("uniqueID", passedNfc)
                        }
                    }
                    client.postgrest["stats"].update(
                        {
                            set("uniqueID", passedNfc)
                        }
                    ){
                        eq("uniqueID", "MODIFY")
                    }
                }.onFailure {
                    Toast.makeText(this@MemberAddActivity, "There is an existing user with the scanned NFC", Toast.LENGTH_LONG).show()
                }.onSuccess {
                    Toast.makeText(this@MemberAddActivity, "Successfully edited the member", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    public override fun onNewIntent(paramIntent: Intent) {
        super.onNewIntent(paramIntent)
        val dataMac = getMAC(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) as? Tag)
        binding.memberNFC.text = "Scanned NFC: " + dataMac
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

    private fun addMember(name: String) {
        val client = getclient()
        val user = User(uniqueID = binding.memberNFC.text.toString().replace("Scanned NFC: ", ""),
            full_name = binding.memberFull.text.toString(),
            phone_num = binding.memberPhone.text.toString(),
            role = "Freshman",
            avatar = false,
            group = name,
            score = 0)
        val userStats = Stats(uniqueID = binding.memberNFC.text.toString().replace("Scanned NFC: ", ""),
            Attack = 10, HP = 10, Defence = 10, Accuracy = 10)
        runBlocking {
            kotlin.runCatching {
                client.postgrest["user"].insert(user, returning = Returning.MINIMAL)
                client.postgrest["stats"].insert(userStats, returning = Returning.MINIMAL)
            }.onFailure {
                Toast.makeText(this@MemberAddActivity,"There is an existing user with the scanned NFC", Toast.LENGTH_LONG).show()
            }.onSuccess {
                Toast.makeText(this@MemberAddActivity,"Member added", Toast.LENGTH_LONG).show()
                finish()
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