package com.example.e_finity.login

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.e_finity.databinding.ActivityLeaderforgotBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.launch
import android.content.Intent

class LeaderForgotActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLeaderforgotBinding
    private val client = getclient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderforgotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val action: String? = intent?.action
        val data: Uri? = intent?.data


        binding.resetButton.setOnClickListener {
            if (binding.emailEditText.text.toString() == "") {
                Toast.makeText(this, "You have not enter any email", Toast.LENGTH_LONG).show()
            }
            else{
                lifecycleScope.launch {
                    kotlin.runCatching {
                        client.gotrue.sendRecoveryEmail(email = binding.emailEditText.text.toString())
                    }.onFailure {
                        Toast.makeText(this@LeaderForgotActivity, "Invalid email or User does not exists", Toast.LENGTH_LONG).show()
                    }.onSuccess {
                        Toast.makeText(this@LeaderForgotActivity, "A reset link has been sent to your email", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        // ATTENTION: This was auto-generated to handle app links.
        val appLinkIntent: Intent = intent
        val appLinkAction: String? = appLinkIntent.action
        val appLinkData: Uri? = appLinkIntent.data
    }

    private fun getclient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://nabbsmcfsskdwjncycnk.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5hYmJzbWNmc3NrZHdqbmN5Y25rIiwicm9sZSI6ImFub24iLCJpYXQiOjE2OTM5MDM3ODksImV4cCI6MjAwOTQ3OTc4OX0.dRVk2u91mLhSMaA1s0FSyIFwnxe2Y3TPdZZ4Shc9mAY"
        ) {
            install(Postgrest)
            install(GoTrue) {
                host = "www.efinity99.com"
                scheme = "http"
            }
        }
    }
}