package com.example.e_finity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.e_finity.login.LogOrSignActivity
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        val sess = sharePreference.getString("SESSION", "").toString()

        if (sess == "") {
            movePage()
        }

        val logoutTextView = findViewById<TextView>(R.id.logoutTextView)
        logoutTextView.setOnClickListener {
            val editor = sharePreference.edit()
            editor.clear()
            editor.apply()
            movePage()
        }
    }

    private fun movePage() {
        val intent = Intent(this, LogOrSignActivity::class.java)
        startActivity(intent)
        finish()
    }
}