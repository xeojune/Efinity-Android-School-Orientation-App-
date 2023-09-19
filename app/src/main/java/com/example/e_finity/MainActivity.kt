package com.example.e_finity


import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.e_finity.databinding.ActivityMainBinding
import com.example.e_finity.login.LogOrSignActivity
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        val sess = sharePreference.getString("SESSION", "").toString()

        if (sess == "") {
            movePage()
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val navView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        navView.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.scoreFragment, R.id.profileFragment)
        )


    }

    private fun movePage() {
        val intent = Intent(this, LogOrSignActivity::class.java)
        startActivity(intent)
        finish()
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