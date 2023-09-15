package com.example.e_finity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView

class SplashScreenActivity : AppCompatActivity() {

    private var SPLASH_SCREEN_TIME: Long = 1500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.myLooper()!!).postDelayed({

            startActivity(Intent(this, MainActivity::class.java))
            finish()

        }, SPLASH_SCREEN_TIME)

//        val backgroundImg : ImageView = findViewById(R.id.iv_logo)
//        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide)
//        backgroundImg.startAnimation(slideAnimation)
//
//        Handler().postDelayed({
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        },5000
//        )


    }
}