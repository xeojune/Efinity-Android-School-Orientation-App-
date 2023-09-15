package com.example.e_finity.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.e_finity.MainActivity
import com.example.e_finity.R

class LogOrSignActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logorsign)

        val freshButton = findViewById<ImageButton>(R.id.freshButton)
        freshButton.setOnClickListener{
            val intent = Intent(this, FreshmanLoginActivity::class.java)
            startActivity(intent)
        }

        val leaderButton = findViewById<ImageButton>(R.id.leaderButton)
        leaderButton.setOnClickListener {
            val intent = Intent(this, LeaderLoginActivity::class.java)
            startActivity(intent)
        }

//        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
//        val sess = sharePreference.getString("SESSION", "").toString()
//
//        if (sess != "") {
//            movePage()
//        }

    }

//    private fun movePage() {
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
//        finish()
//    }

}