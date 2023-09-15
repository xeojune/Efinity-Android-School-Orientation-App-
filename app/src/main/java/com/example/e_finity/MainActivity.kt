package com.example.e_finity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        val sess = sharePreference.getString("SESSION", "").toString()

        if (sess == "") {
            movePage()
        }
    }

    private fun movePage() {
        val intent = Intent(this, LogOrSignActivity::class.java)
        startActivity(intent)
        finish()
    }
}