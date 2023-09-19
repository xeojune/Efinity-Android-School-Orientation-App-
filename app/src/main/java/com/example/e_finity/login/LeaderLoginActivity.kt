package com.example.e_finity.login

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.e_finity.MainActivity
import com.example.e_finity.UserRead
import com.example.e_finity.databinding.ActivityLeaderloginBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class LeaderLoginActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLeaderloginBinding
    private val client = getclient()

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderloginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.signinButton.setOnClickListener {
            login()
        }
        binding.signupTextView.setOnClickListener {
            signupPage()
        }
    }


    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun login() {
        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        lifecycleScope.launch {
            kotlin.runCatching {
                client.gotrue.loginWith(Email) {
                    email = binding.emailEditText.text.toString()
                    password = binding.passwordEditText.text.toString()
                }
            }.onFailure {
                if (binding.emailEditText.text.toString() == "" || binding.passwordEditText.text.toString() == "") {
                    Toast.makeText(this@LeaderLoginActivity, "Fill in all the fields", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@LeaderLoginActivity, "Incorrect email or password", Toast.LENGTH_LONG).show()
                }
            }.onSuccess {
                val editor = sharePreference.edit()
                editor.putString("SESSION", binding.emailEditText.text.toString())
                val userinforesponse = client.postgrest["user"].select{
                    eq("uniqueID", binding.emailEditText.text.toString())
                }
                val userinfo = userinforesponse.decodeList<UserRead>()
                editor.putBoolean("AVATAR", userinfo[0].avatar)
                editor.apply()
                movePage()
            }
        }
    }

    private fun signupPage() {
        val intent = Intent(this, leaderSignupActivity::class.java)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun movePage() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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
}

