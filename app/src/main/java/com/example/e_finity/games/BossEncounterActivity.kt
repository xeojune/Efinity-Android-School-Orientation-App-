package com.example.e_finity.games

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.R
import com.example.e_finity.bossesClass
import com.example.e_finity.databinding.ActivityBossEncounterBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.bind

var mMediaPlayer: MediaPlayer? = null
class BossEncounterActivity: AppCompatActivity() {
    private lateinit var binding: ActivityBossEncounterBinding
    var bossPower = 0
    var bossName = ""
    private var delayedHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBossEncounterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val playerStat = intent.getIntegerArrayListExtra("stats")
        val bossId = intent.getStringExtra("bossId")
        val userPts = intent.getStringExtra("score")
        val userName = intent.getStringExtra("fullname")

        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide)
        val slideAnimationBoss = AnimationUtils.loadAnimation(this, R.anim.slideboss)

        val client = getclient()
        MainScope().launch {
            val bossData = client.postgrest["bosses"].select {
                if (bossId != null) {
                    eq("id", bossId.toInt())
                }
            }.decodeList<bossesClass>()
            bossPower = bossData[0].bossPower
            bossName = bossData[0].bossName
        }

        val bucket = client.storage["avatar"]
        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        val url = bucket.publicUrl(sharePreference.getString("SESSION", "").toString() + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
        Glide.with(this).load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.baseline_person).into(binding.playerSprite)
        binding.playerSpriteCard.startAnimation(slideAnimation)
        binding.bossSprite.startAnimation(slideAnimationBoss)

        mMediaPlayer = MediaPlayer.create(this, R.raw.battlebgm)
        mMediaPlayer!!.isLooping = true
        mMediaPlayer!!.setVolume(0.2F, 0.2F)
        mMediaPlayer!!.start()

        delayedHandler = Handler()

        delayedHandler?.postDelayed({
            val intent = Intent(this, BossBattleActivity::class.java)
            intent.putExtra("stats", playerStat)
            intent.putExtra("score", userPts)
            intent.putExtra("fullname", userName)
            intent.putExtra("bossPower", bossPower.toString())
            intent.putExtra("bossName", bossName)
            intent.putExtra("bossId", bossId)
            startActivity(intent)
        }, 3500)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer!!.stop()
    }

    override fun onBackPressed() {
//        delayedHandler?.removeCallbacksAndMessages(null)
//        super.onBackPressed()
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