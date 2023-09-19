package com.example.e_finity.teams

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.e_finity.GroupRead
import com.example.e_finity.databinding.ActivityMaketeamBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class MakeTeamActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMaketeamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaketeamBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}