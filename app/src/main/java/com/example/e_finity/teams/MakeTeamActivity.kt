package com.example.e_finity.teams

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.GroupAdd
import com.example.e_finity.GroupRead
import com.example.e_finity.MainActivity
import com.example.e_finity.R
import com.example.e_finity.databinding.ActivityMaketeamBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.updateAsFlow
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream


class MakeTeamActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMaketeamBinding
    var state = "None"

    @OptIn(SupabaseExperimental::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaketeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val client = getclient()
        val bucket = client.storage["avatar"]
        binding.colorWheel.visibility = View.GONE
        binding.colorWheelBtn.visibility = View.GONE
        binding.teamDeleteButton.visibility = View.GONE
        binding.teamEditButton.visibility = View.GONE

        val name = intent.getStringExtra("name")
        val color = intent.getStringExtra("color")
        val timemodi = intent.getStringExtra("timemodi")

        if (name != null) {
            binding.teamDeleteButton.visibility = View.VISIBLE
            binding.teamEditButton.visibility = View.VISIBLE
            binding.teamcreateButton.visibility = View.GONE
            binding.teamnameEditText.setText(name)
            binding.teamnameEditText.isEnabled = false
            binding.colorEditText.setText("#"+color)
            val url = bucket.publicUrl(name + ".png") + "?timestamp=" + timemodi
            Glide.with(this).load(url).fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL).error(
                R.drawable.avatar).into(binding.uploadImage)
            binding.imageStroke.setStrokeColor(Color.parseColor("#"+color))
        }

        var imagechanged = 0
        val changeImage = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                val imgUri = data?.data
                imagechanged = 1
                binding.uploadImage.setImageURI(imgUri)
            }
        }

        binding.colorWheelBtn.setOnClickListener {
            binding.colorWheel.visibility = View.VISIBLE
        }

        binding.uploadImage.setOnClickListener{
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            changeImage.launch(gallery)
        }

        binding.teamDeleteButton.setOnClickListener {
            runBlocking {
                bucket.delete(name+".png")

                client.postgrest["user"].update(
                    {
                        set("group", "None")
                    }
                ) {
                    if (name != null) {
                        eq("group", name)
                    }
                }

                client.postgrest["Orientation Group"].delete {
                    if (name != null) {
                        eq("name", name)
                    }
                }
            }
            val intent = Intent(this@MakeTeamActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        binding.teamEditButton.setOnClickListener {
            try {
                binding.imageStroke.setStrokeColor(Color.parseColor(binding.colorEditText.text.toString()))
                runBlocking {
                    kotlin.runCatching {
                        client.postgrest["Orientation Group"].update(
                            {
                                set("color", binding.colorEditText.text.toString().replace("#", ""))
                                set("timemodi", System.currentTimeMillis()-1698189000000)
                            }) {
                            if (name != null) {
                                eq("name", name)
                            }
                        }
                    }.onSuccess {
                        val bitmap = (binding.uploadImage.getDrawable() as BitmapDrawable).bitmap
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                        val byte = stream.toByteArray()
                        bucket.update(name+".png", byte, upsert = false)
                        val intent = Intent(this@MakeTeamActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                    }.onFailure {
                        Toast.makeText(this@MakeTeamActivity,"There is an existing group with this name",Toast.LENGTH_LONG).show()
                    }
                }
            }
            catch (e: Exception) {
                Toast.makeText(this,"Invalid color",Toast.LENGTH_LONG).show()
            }
        }

        binding.teamcreateButton.setOnClickListener {
            if (binding.teamnameEditText.text.toString() == "" || binding.colorEditText.text.toString() == "" ) {
                Toast.makeText(this, "Incomplete fields", Toast.LENGTH_SHORT).show()
            }
            else if (imagechanged == 0) {
                Toast.makeText(this, "Upload an Image", Toast.LENGTH_SHORT).show()
            }
            else {
                try {
                    binding.imageStroke.setStrokeColor(Color.parseColor(binding.colorEditText.text.toString()))
                    addgrouptable()
                }
                catch (e: Exception) {
                    Toast.makeText(this,"Invalid color",Toast.LENGTH_LONG).show()
                }
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
            install(Storage)
        }
    }

    private fun addgrouptable() {
        val client = getclient()
        val group = GroupAdd(
            name = binding.teamnameEditText.text.toString(),
            color = binding.colorEditText.text.toString().replace("#", "")
        )
        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        runBlocking {
            kotlin.runCatching {
                client.postgrest["Orientation Group"].insert(group, returning = Returning.MINIMAL)
//                client.postgrest["user"].update(
//                    {
//                        set("group", binding.teamnameEditText.text.toString())
//                    }
//                ) {
//                    eq("uniqueID", sharePreference.getString("SESSION", "").toString())
//                }
            }.onSuccess {
                uploadImg()
                Toast.makeText(this@MakeTeamActivity,"Successfully created group", Toast.LENGTH_LONG).show()
                val intent = Intent(this@MakeTeamActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("State", "Added")
                setResult(1000, intent)
                startActivity(intent)
            }.onFailure {
                Toast.makeText(this@MakeTeamActivity,"There is an existing group with this name", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadImg() {
        val client = getclient()
        val bucket = client.storage["avatar"]
        val bitmap = (binding.uploadImage.getDrawable() as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val byte = stream.toByteArray()
        runBlocking {
            bucket.uploadAsFlow(binding.teamnameEditText.text.toString() + ".png", byte, upsert = false).collect {
                when(it) {
                    is UploadStatus.Progress -> println("Progress: ${it.totalBytesSend.toFloat() / it.contentLength * 100}%")
                    is UploadStatus.Success -> println("Success")
                }
            }
        }

    }

    private fun updateUserGroup() {
        val client = getclient()
        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        runBlocking {
            client.postgrest["user"].update(
                {
                    set("group", binding.teamnameEditText.text.toString())
                }
            ) {
                eq("uniqueID", sharePreference.getString("SESSION", "").toString())
            }
        }
    }
}