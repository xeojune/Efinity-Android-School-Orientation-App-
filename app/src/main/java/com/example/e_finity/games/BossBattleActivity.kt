package com.example.e_finity.games

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.R
import com.example.e_finity.databinding.ActivityBossBattleBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

var m2MediaPlayer: MediaPlayer? = null
var m3MediaPlayer: MediaPlayer? = null
var m4MediaPlayer: MediaPlayer? = null
class BossBattleActivity: AppCompatActivity() {
    private lateinit var binding: ActivityBossBattleBinding
    var playerHp = 0
    var bossHp = 0
    var bossId = 0
    var patt = 0
    var pacc = 0
    var pdef = 0
    var playerHp2 = 0
    var bossHp2 = 0
    var userPoints = 0
    var userName = ""

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBossBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.enemyDmgPts.visibility = View.GONE
        binding.playerDmgPts.visibility = View.GONE
        binding.overcard.visibility = View.GONE

        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide)
        val slideAnimationBoss = AnimationUtils.loadAnimation(this, R.anim.slideboss)

        val playerStat = intent.getIntegerArrayListExtra("stats")
        val userPts = intent.getStringExtra("score")
        val userNm = intent.getStringExtra("fullname")
        val bossPower = intent.getStringExtra("bossPower")
        val bossName = intent.getStringExtra("bossName")
        val bossIdentity = intent.getStringExtra("bossId")

        if (playerStat != null) {
            playerHp = playerStat[1]
            playerHp2 = playerStat[1]
            patt = playerStat[0]
            pdef = playerStat[2]
            pacc = playerStat[3]
        }
        if (userPts != null) {
            userPoints = userPts.toInt()
        }
        if (userNm != null) {
            userName = userNm
        }
        if (bossPower != null) {
            bossHp = bossPower.toInt()
            bossHp2 = bossPower.toInt()
        }
        if (bossIdentity != null) {
            bossId = bossIdentity.toInt()
        }

        binding.bossHpText.text = bossHp.toString() + "/" + bossHp.toString()
        binding.playerHpText.text = playerHp.toString() + "/" + playerHp.toString()
        binding.bossName.text = bossName

        val client = getclient()
        val bucket = client.storage["avatar"]
        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        val url = bucket.publicUrl(sharePreference.getString("SESSION", "").toString() + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
        Glide.with(this).load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.baseline_person).into(binding.playerSprite)
        binding.playerSpriteCard.startAnimation(slideAnimationBoss)
        binding.bossSprite.startAnimation(slideAnimation)

        m2MediaPlayer = MediaPlayer.create(this, R.raw.attackeffect)
        m2MediaPlayer!!.isLooping = false
        m2MediaPlayer!!.setVolume(1.0F, 1.0F)

        m3MediaPlayer = MediaPlayer.create(this, R.raw.loses)
        m3MediaPlayer!!.isLooping = false
        m3MediaPlayer!!.setVolume(1.0F, 1.0F)

        m4MediaPlayer = MediaPlayer.create(this, R.raw.victory)
        m4MediaPlayer!!.isLooping = false
        m4MediaPlayer!!.setVolume(1.0F, 1.0F)

        Handler().postDelayed({battle()}, 2000)
        binding.overcardBtn.setOnClickListener {
            activityEnd()
        }
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun battle() {
        var shouldContinue = true
        MainScope().launch {
            while (shouldContinue) {
                val bossAtt = Math.abs((bossHp2 - 10 until bossHp2 + 10).random())
                var bossAttFin = bossAtt - (pdef/10)
                if (bossAttFin < 0) {
                    bossAttFin = 0
                }
                playerHp = playerHp - bossAttFin
                playerBar((playerHp.toDouble()/playerHp2*100).toInt())
                val bossAttAnim = AnimationUtils.loadAnimation(this@BossBattleActivity, R.anim.bossattack)
                binding.bossSprite.startAnimation(bossAttAnim)
                binding.enemyDmgPts.text = bossAttFin.toString()
                binding.enemyDmgPts.visibility = View.VISIBLE
                val damageAnim = AnimationUtils.loadAnimation(this@BossBattleActivity, R.anim.damagepoint)
                binding.enemyDmgPts.startAnimation(damageAnim)
                delay(500)
                binding.enemyDmgPts.visibility = View.GONE
                m2MediaPlayer!!.start()
                if (playerHp <= 0) {
                    binding.playerHpText.text = "0/" + playerHp2.toString()
                    val faint = AnimationUtils.loadAnimation(this@BossBattleActivity, R.anim.faintanimation)
                    binding.playerSpriteCard.startAnimation(faint)
                    binding.playerSpriteCard.visibility = View.GONE
                    mMediaPlayer!!.stop()
                    delay(500)
                    m3MediaPlayer!!.start()
                    shouldContinue = false
                    battleover("boss")
                }
                else {
                    val fidgetAnim = AnimationUtils.loadAnimation(this@BossBattleActivity, R.anim.fidget)
                    binding.playerSpriteCard.startAnimation(fidgetAnim)
                    binding.playerHpText.text = playerHp.toString() + "/" + playerHp2.toString()
                }
                delay(1800)
                if (shouldContinue) {
                    val chance = pacc.toDouble()/bossHp2
                    var playerAttPts = 0
                    if (Random.nextDouble() < chance) {
                        playerAttPts = Math.abs((patt - 10 until patt + 10).random())
                    }
                    else {
                        playerAttPts = 0
                    }
                    bossHp -= playerAttPts
                    bossBar((bossHp.toDouble()/bossHp2*100).toInt())
                    val playerAtt = AnimationUtils.loadAnimation(this@BossBattleActivity, R.anim.playerattack)
                    binding.playerSpriteCard.startAnimation(playerAtt)
                    binding.playerDmgPts.text = playerAttPts.toString()
                    binding.playerDmgPts.visibility = View.VISIBLE
                    val damageAnim = AnimationUtils.loadAnimation(this@BossBattleActivity, R.anim.damagepoint)
                    binding.playerDmgPts.startAnimation(damageAnim)
                    delay(500)
                    binding.playerDmgPts.visibility = View.GONE
                    m2MediaPlayer!!.start()
                    if (bossHp <= 0) {
                        binding.bossHpText.text = "0/" + bossHp2.toString()
                        val faint = AnimationUtils.loadAnimation(this@BossBattleActivity, R.anim.faintanimation)
                        binding.bossSprite.startAnimation(faint)
                        binding.bossSprite.visibility = View.GONE
                        mMediaPlayer!!.stop()
                        delay(500)
                        m4MediaPlayer!!.start()
                        shouldContinue = false
                        battleover("player")
                    }
                    else {
                        val fidgetAnim = AnimationUtils.loadAnimation(this@BossBattleActivity, R.anim.fidget)
                        binding.bossSprite.startAnimation(fidgetAnim)
                        binding.bossHpText.text = bossHp.toString() + "/" + bossHp2.toString()
                    }
                    delay(1800)
                }
            }
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun battleover(who: String) {
        val client = getclient()
        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        val animator = ValueAnimator.ofFloat(0f, 1f)
        binding.overcard.visibility = View.VISIBLE
        animator.addUpdateListener {
                animation -> val value = animation.animatedValue as Float
            val layoutParams = binding.overcard.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.matchConstraintPercentHeight = value * 0.2f
            binding.overcard.layoutParams = layoutParams
        }
        animator.duration = 1000
        animator.start()
        if (who == "player") {
            binding.overcardLost.visibility = View.GONE
            val newatt = Math.abs(((bossHp2*0.2-10).toInt() until (bossHp2*0.2+10).toInt()).random())
            val newhp = Math.abs(((bossHp2*0.2-10).toInt() until (bossHp2*0.2+10).toInt()).random())
            val newdef = Math.abs(((bossHp2*0.2-10).toInt() until (bossHp2*0.2+10).toInt()).random())
            val newacc = Math.abs(((bossHp2*0.2-10).toInt() until (bossHp2*0.2+10).toInt()).random())
            val pts = Math.abs(((bossHp2*1.2-10).toInt() until (bossHp2*1.2+10).toInt()).random())
            binding.overcardAtt.text = "Att: +" + newatt.toString()
            binding.overcardHp.text = "Hp: +" + newhp.toString()
            binding.overcardDef.text = "Def: +" + newdef.toString()
            binding.overcardAcc.text = "Acc: +" + newacc.toString()
            binding.overcardPoints.text = "Points: +" + pts.toString()
            patt += newatt
            playerHp2 += newhp
            pdef += newdef
            pacc += newacc
            userPoints += pts
            MainScope().launch {
                client.postgrest["stats"].update({
                    set("Attack", patt)
                    set("HP", playerHp2)
                    set("Defence", pdef)
                    set("Accuracy", pacc)
                }) {
                    eq("uniqueID", sharePreference.getString("SESSION", "").toString())
                }
                client.postgrest["user"].update({
                    set("score", userPoints)
                }) {
                    eq("uniqueID", sharePreference.getString("SESSION", "").toString())
                }
                client.postgrest["bosses"].update(
                    {
                        set("defeated", userName)
                    }
                ) {
                    eq("id", bossId)
                }
            }
        }
        else {
            binding.overcardAcc.visibility = View.GONE
            binding.overcardAtt.visibility = View.GONE
            binding.overcardDef.visibility = View.GONE
            binding.overcardHp.visibility = View.GONE
            binding.overcardHeader.visibility = View.GONE
            binding.overcardPoints.visibility = View.GONE
        }
    }

    private fun activityEnd() {
        val intent = Intent(this, BossFightActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun bossBar(hp: Int) {
        ObjectAnimator.ofInt(binding.bossHp, "progress", hp).setDuration(1000).start()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun playerBar(hp: Int) {
        ObjectAnimator.ofInt(binding.playerHp, "progress", hp).setDuration(1000).start()
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