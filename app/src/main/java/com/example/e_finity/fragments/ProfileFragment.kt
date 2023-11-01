package com.example.e_finity.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.MainActivity
import com.example.e_finity.R
import com.example.e_finity.UserRead
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

var loaded: Boolean = false
var url: String = ""
lateinit var userData: List<uRead>

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
            loaded = false
            url = ""
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

        //For Visibility
        val avatarRelative = view.findViewById<RelativeLayout>(R.id.avatarRelative)
        val infoRelative = view.findViewById<RelativeLayout>(R.id.infoRelative)
        val attackRelative = view.findViewById<RelativeLayout>(R.id.attack)
        val heartRelative = view.findViewById<RelativeLayout>(R.id.heart)
        val shieldRelative = view.findViewById<RelativeLayout>(R.id.shield)
        val dartRelative = view.findViewById<RelativeLayout>(R.id.dart)
        val profileProgressBar = view.findViewById<ProgressBar>(R.id.profileProgressBar)

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
                            }
                        }
                    }
                }
            }
        avatarimageView.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            changeImage.launch(gallery)
            url = bucket.publicUrl(sharePreference.getString("SESSION", "").toString() + ".png") + "?timestamp=" + System.currentTimeMillis()
        }
        MainScope().launch {
            userData =client.postgrest["user"].select(columns = Columns.list("""uniqueID!inner(Attack, HP, Defence, Accuracy), full_name, phone_num, role, score, group!inner(name, color)""")) {
                eq("uniqueID", sharePreference.getString("SESSION", "").toString())
            }.decodeList<uRead>()
        }
        if (loaded == false) {
            avatarRelative.visibility = View.GONE
            infoRelative.visibility = View.GONE
            attackRelative.visibility = View.GONE
            heartRelative.visibility = View.GONE
            shieldRelative.visibility = View.GONE
            dartRelative.visibility = View.GONE
            MainScope().launch{
                kotlin.runCatching {
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
                    attackRelative.setBackgroundColor(Color.parseColor("#CC"+userinfo[0].group.color))
                    heartRelative.setBackgroundColor(Color.parseColor("#99"+userinfo[0].group.color))
                    shieldRelative.setBackgroundColor(Color.parseColor("#66"+userinfo[0].group.color))
                    dartRelative.setBackgroundColor(Color.parseColor("#33"+userinfo[0].group.color))

                    attpts.text = userinfo[0].uniqueID[0].Attack.toString()
                    hp.text = userinfo[0].uniqueID[0].HP.toString()
                    defpts.text = userinfo[0].uniqueID[0].Defence.toString()
                    accpts.text = userinfo[0].uniqueID[0].Accuracy.toString()

                    profileProgressBar.visibility = View.GONE
                    avatarRelative.visibility = View.VISIBLE
                    infoRelative.visibility = View.VISIBLE
                    attackRelative.visibility = View.VISIBLE
                    heartRelative.visibility = View.VISIBLE
                    shieldRelative.visibility = View.VISIBLE
                    dartRelative.visibility = View.VISIBLE

                    url = bucket.publicUrl(sharePreference.getString("SESSION", "").toString() + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
                    Glide.with(view).load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.baseline_person).into(avatarimageView)

                    userData = userinfo
                    loaded = true
                }
            }
        }
        else {
            Glide.with(view).load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.baseline_person).into(avatarimageView)
            avatarBorder.setStrokeColor(Color.parseColor("#"+userData[0].group.color))
            name.text = userData[0].full_name
            name.setTextColor(Color.parseColor("#"+userData[0].group.color))
            oGroup.text = userData[0].group.name
            oGroup.setTextColor(Color.parseColor("#"+userData[0].group.color))
            contactNumber.text = userData[0].phone_num
            contactNumber.setTextColor(Color.parseColor("#"+userData[0].group.color))
            role.text = userData[0].role
            role.setTextColor(Color.parseColor("#"+userData[0].group.color))
            points.text = userData[0].score.toString()
            points.setTextColor(Color.parseColor("#"+userData[0].group.color))

            //For User Stats
            attackRelative.setBackgroundColor(Color.parseColor("#CC"+userData[0].group.color))
            heartRelative.setBackgroundColor(Color.parseColor("#99"+userData[0].group.color))
            shieldRelative.setBackgroundColor(Color.parseColor("#66"+userData[0].group.color))
            dartRelative.setBackgroundColor(Color.parseColor("#33"+userData[0].group.color))

            attpts.text = userData[0].uniqueID[0].Attack.toString()
            hp.text = userData[0].uniqueID[0].HP.toString()
            defpts.text = userData[0].uniqueID[0].Defence.toString()
            accpts.text = userData[0].uniqueID[0].Accuracy.toString()

            profileProgressBar.visibility = View.GONE
            avatarRelative.visibility = View.VISIBLE
            infoRelative.visibility = View.VISIBLE
            attackRelative.visibility = View.VISIBLE
            heartRelative.visibility = View.VISIBLE
            shieldRelative.visibility = View.VISIBLE
            dartRelative.visibility = View.VISIBLE

            Handler().postDelayed({
                avatarBorder.setStrokeColor(Color.parseColor("#"+userData[0].group.color))
                name.text = userData[0].full_name
                name.setTextColor(Color.parseColor("#"+userData[0].group.color))
                oGroup.text = userData[0].group.name
                oGroup.setTextColor(Color.parseColor("#"+userData[0].group.color))
                contactNumber.text = userData[0].phone_num
                contactNumber.setTextColor(Color.parseColor("#"+userData[0].group.color))
                role.text = userData[0].role
                role.setTextColor(Color.parseColor("#"+userData[0].group.color))
                points.text = userData[0].score.toString()
                points.setTextColor(Color.parseColor("#"+userData[0].group.color))

                //For User Stats
                attackRelative.setBackgroundColor(Color.parseColor("#CC"+userData[0].group.color))
                heartRelative.setBackgroundColor(Color.parseColor("#99"+userData[0].group.color))
                shieldRelative.setBackgroundColor(Color.parseColor("#66"+userData[0].group.color))
                dartRelative.setBackgroundColor(Color.parseColor("#33"+userData[0].group.color))

                attpts.text = userData[0].uniqueID[0].Attack.toString()
                hp.text = userData[0].uniqueID[0].HP.toString()
                defpts.text = userData[0].uniqueID[0].Defence.toString()
                accpts.text = userData[0].uniqueID[0].Accuracy.toString()
            }, 1000)
        }

    }



}
