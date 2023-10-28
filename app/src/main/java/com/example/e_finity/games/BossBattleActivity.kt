package com.example.e_finity.games

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.R
import com.example.e_finity.databinding.ActivityBossBattleBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class BossBattleActivity: AppCompatActivity() {
    private lateinit var binding: ActivityBossBattleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBossBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide)
        val slideAnimationBoss = AnimationUtils.loadAnimation(this, R.anim.slideboss)

        val client = getclient()
        val bucket = client.storage["avatar"]
        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        val url = bucket.publicUrl(sharePreference.getString("SESSION", "").toString() + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
        Glide.with(this).load(url).circleCrop().fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.avatar).into(binding.playerSprite)
        binding.playerSpriteCard.startAnimation(slideAnimation)
        binding.bossSprite.startAnimation(slideAnimationBoss)

        Handler().postDelayed({binding.bossHp.progress = 80}, 2000)
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
}