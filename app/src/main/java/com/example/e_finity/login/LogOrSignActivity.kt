package com.example.e_finity.login

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.e_finity.MainActivity
import com.example.e_finity.R

class LogOrSignActivity: AppCompatActivity() {


    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
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



    }

}