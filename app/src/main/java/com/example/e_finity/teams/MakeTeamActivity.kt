package com.example.e_finity.teams

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.e_finity.GroupAdd
import com.example.e_finity.GroupRead
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
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class MakeTeamActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMaketeamBinding

    @OptIn(SupabaseExperimental::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaketeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val client = getclient()
        val bucket = client.storage["avatar"]
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

        binding.uploadImage.setOnClickListener{
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            changeImage.launch(gallery)
        }

        binding.teamcreateButton.setOnClickListener {
            if (binding.teamnameEditText.text.toString() == "" || binding.colorEditText.text.toString() == "" ) {
                Toast.makeText(this, "Incomplete fields", Toast.LENGTH_SHORT).show()
            }
            else if (imagechanged == 0) {
                Toast.makeText(this, "Upload an Image", Toast.LENGTH_SHORT).show()
            }
            else {
                uploadImg()
                addgrouptable()
                Toast.makeText(this,"Success", Toast.LENGTH_LONG).show()
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
            color = binding.colorEditText.text.toString()
        )
        lifecycleScope.launch {
            kotlin.runCatching {
                client.postgrest["Orientation Group"].insert(group, returning = Returning.MINIMAL)
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
        lifecycleScope.launch {
            bucket.uploadAsFlow(binding.teamnameEditText.text.toString() + ".png", byte, upsert = false).collect {
                when(it) {
                    is UploadStatus.Progress -> println("Progress: ${it.totalBytesSend.toFloat() / it.contentLength * 100}%")
                    is UploadStatus.Success -> println("Success")
                }
            }
        }

    }
}