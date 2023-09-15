package com.example.e_finity.login

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.e_finity.databinding.ActivityLeadersignupBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.lang.Exception

class leaderSignupActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLeadersignupBinding

    val roles = arrayOf("Leader", "Senior")
    val client = getclient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeadersignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,roles)
        binding.roleSpinner.adapter = arrayAdapter

        binding.signupButton.setOnClickListener {
            if (binding.fullnameEditText.text.toString() == "" ||
                binding.phoneEditText.text.toString() == "" ||
                binding.emailEditText.text.toString() == "" ||
                binding.passwordEditText.text.toString() == "" ||
                binding.codeEditText.text.toString() == "") {
                Toast.makeText(this, "Incomplete fields", Toast.LENGTH_SHORT).show()
            }
            else {
                if (binding.roleSpinner.selectedItem.toString() == "Leader") {
                    if (binding.codeEditText.text.toString() == "bLds98") {
                        lifecycleScope.launch {
                            signUp()
                        }
                    }
                    else {
                        Toast.makeText(this, "Incorrect Code. Please check with your organiser for the correct code.", Toast.LENGTH_LONG).show()
                    }
                }
                else {
                    if (binding.codeEditText.text.toString() == "Slki20") {
                        lifecycleScope.launch {
                            signUp()
                        }
                    }
                    else {
                        Toast.makeText(this, "Incorrect Code. Please check with your organiser for the correct code.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    suspend fun signUp() {
        return try {
            client.gotrue.signUpWith(Email) {
                this.email = binding.emailEditText.text.toString()
                this.password = binding.passwordEditText.text.toString()
            }
            updateTable()
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            if ("confirmation_sent_at" in e.toString()) {
                updateTable()
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            }
            else if ("User already registered" in e.toString()) {
                Toast.makeText(this, "There is an existing user with this email", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(this, "Password should be at least 6 characters long", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun updateTable() {
        val user = User(email = binding.emailEditText.text.toString(),
            full_name = binding.fullnameEditText.text.toString(),
            phone_num = binding.phoneEditText.text.toString(),
            role = binding.roleSpinner.selectedItem.toString())
        lifecycleScope.launch {
            kotlin.runCatching {
                client.postgrest["user"].insert(user, returning = Returning.MINIMAL)
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

}

@Serializable
data class User(
    val email: String,
    val full_name: String,
    val phone_num: String,
    val role: String
)