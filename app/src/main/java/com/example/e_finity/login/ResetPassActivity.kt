package com.example.e_finity.login

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.e_finity.databinding.ActivityResetpassBinding
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.FlowType
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.gotrue
import kotlinx.coroutines.launch

class ResetPassActivity: AppCompatActivity() {
    private lateinit var binding: ActivityResetpassBinding
    @OptIn(SupabaseExperimental::class)
    val client = createSupabaseClient("https://nabbsmcfsskdwjncycnk.supabase.co", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5hYmJzbWNmc3NrZHdqbmN5Y25rIiwicm9sZSI6ImFub24iLCJpYXQiOjE2OTM5MDM3ODksImV4cCI6MjAwOTQ3OTc4OX0.dRVk2u91mLhSMaA1s0FSyIFwnxe2Y3TPdZZ4Shc9mAY") {
        install(GoTrue) {
            flowType = FlowType.PKCE
            scheme = "http"
            host = "example.com"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetpassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data: Uri? = intent?.data
        val path = data?.path
        val email = data?.getQueryParameter("email").toString()
        val token = data?.getQueryParameter("Token").toString()

        binding.confirmResetBtn.setOnClickListener {
            if (binding.newPass.text.toString() == "" || binding.newPass2.text.toString() == "") {
                Toast.makeText(this, "Incomplete fields", Toast.LENGTH_SHORT).show()
            }
            else if (binding.newPass.text.toString() != binding.newPass2.text.toString()) {
                Toast.makeText(this, "The passwords do not match", Toast.LENGTH_SHORT).show()
            }
            else if (binding.newPass.text.toString().length < 6) {
                Toast.makeText(this@ResetPassActivity, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
            else {
                checkToken(email, token)
            }
        }
    }

    private fun checkToken(mail: String, code: String) {
        lifecycleScope.launch {
            kotlin.runCatching {
                client.gotrue.verifyEmailOtp(OtpType.Email.RECOVERY, email = mail, token = code)
            }.onFailure { Toast.makeText(this@ResetPassActivity, "Invalid or expired token", Toast.LENGTH_SHORT).show()
            }.onSuccess {
                changePassword()
            }
        }
    }

    private fun changePassword() {
        lifecycleScope.launch {
            kotlin.runCatching {
                client.gotrue.modifyUser {
                    password = binding.newPass.text.toString()
                }
            }.onSuccess {
                Toast.makeText(this@ResetPassActivity, "Successfully changed password", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}