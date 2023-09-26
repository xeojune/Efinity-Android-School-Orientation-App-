package com.example.e_finity.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.MainActivity
import com.example.e_finity.R
import com.example.e_finity.login.LogOrSignActivity
import com.example.e_finity.uRead
import com.google.android.material.card.MaterialCardView
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.updateAsFlow
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ProfileFragment : Fragment() {

    private var activity: MainActivity?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = activity
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        return view
    }

    @OptIn(SupabaseExperimental::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext().applicationContext
        val sharePreference = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)

        val logoutBtn = view.findViewById<ImageView>(R.id.logoutButton)
        logoutBtn.setOnClickListener {
            val editor = sharePreference.edit()
            editor.clear()
            editor.apply()
            requireActivity().run {
                startActivity(Intent(this, LogOrSignActivity::class.java))
                finish()
            }
        }

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




        val client = getclient()
        val bucket = client.storage["avatar"]

        val avatarimageView = view.findViewById<ImageView>(R.id.avatarImageView)
        val name = view.findViewById<TextView>(R.id.name)
        val oGroup = view.findViewById<TextView>(R.id.oGroup)
        val contactNumber = view.findViewById<TextView>(R.id.contactNumber)
        val role = view.findViewById<TextView>(R.id.roleTextView)
        val points = view.findViewById<TextView>(R.id.pointsTextView)
        val avatarBorder = view.findViewById<MaterialCardView>(R.id.pictureBorder)
        val attpts = view.findViewById<TextView>(R.id.attpts)
        val hp = view.findViewById<TextView>(R.id.hp)
        val defpts = view.findViewById<TextView>(R.id.defpts)
        val accpts = view.findViewById<TextView>(R.id.accpts)

        val haveAvatar = sharePreference.getBoolean("AVATAR", false)
        val avaModified = sharePreference.getString("AVAMODI", "").toString()

        if (haveAvatar == true) {
            val url = bucket.publicUrl(sharePreference.getString("SESSION", "").toString() + ".png") + "?timestamp=" + avaModified
            Glide.with(view).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).into(avatarimageView)
        }
        runBlocking{

            val userinfo =client.postgrest["user"].select(columns = Columns.list("""uniqueID!inner(Attack, HP, Defence, Accuracy), full_name, phone_num, role, score, group!inner(name, color)""")) {
                eq("uniqueID", sharePreference.getString("SESSION", "").toString())
            }.decodeList<uRead>()

            //For User Information
            avatarBorder.setStrokeColor(Color.parseColor("#"+userinfo[0].group.color))
            name.text = userinfo[0].full_name
            name.setTextColor(Color.parseColor("#"+userinfo[0].group.color))
            oGroup.text = userinfo[0].group.name
            oGroup.setTextColor(Color.parseColor("#"+userinfo[0].group.color))
            contactNumber.text = userinfo[0].phone_num
            contactNumber.setTextColor(Color.parseColor("#"+userinfo[0].group.color))
            role.text = userinfo[0].role
            role.setTextColor(Color.parseColor("#"+userinfo[0].group.color))
            points.text = userinfo[0].score.toString()
            points.setTextColor(Color.parseColor("#"+userinfo[0].group.color))

            //For User Stats
            attpts.text = userinfo[0].uniqueID[0].Attack.toString()
            hp.text = userinfo[0].uniqueID[0].HP.toString()
            defpts.text = userinfo[0].uniqueID[0].Defence.toString()
            accpts.text = userinfo[0].uniqueID[0].Accuracy.toString()
        }


        val changeImage =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val data = it.data
                    val imgUri = data?.data
                    avatarimageView.setImageURI(imgUri)
                    GlobalScope.launch {
                        kotlin.runCatching {
                            if (imgUri != null) {
                                bucket.uploadAsFlow(sharePreference.getString("SESSION", "").toString() + ".png", imgUri, upsert = false).collect {
                                    when(it) {
                                        is UploadStatus.Progress -> println("Progress: ${it.totalBytesSend.toFloat() / it.contentLength * 100}%")
                                        is UploadStatus.Success -> println("Success")
                                    }
                                }
                                val editor = sharePreference.edit()
                                editor.putString("AVAMODI", System.currentTimeMillis().toString())
                                editor.putBoolean("AVATAR", true)
                                editor.apply()
                                client.postgrest["user"].update(
                                    {
                                        set("avatar", true)
                                    }
                                ) {
                                    eq("uniqueID", sharePreference.getString("SESSION", "").toString())
                                }
                            }
                        }.onFailure {
                            if (imgUri != null) {
                                bucket.updateAsFlow(sharePreference.getString("SESSION", "").toString() + ".png", imgUri, upsert = false).collect {
                                    when(it) {
                                        is UploadStatus.Progress -> println("Progress: ${it.totalBytesSend.toFloat() / it.contentLength * 100}%")
                                        is UploadStatus.Success -> println("Success")
                                    }
                                }
                                val editor = sharePreference.edit()
                                editor.remove("AVAMODI")
                                editor.putString("AVAMODI", System.currentTimeMillis().toString())
                                editor.apply()
                            }
                        }
                    }
                }
            }
        avatarimageView.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            changeImage.launch(gallery)
        }

    }

}
