package com.example.e_finity.teams

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.e_finity.databinding.ActivityTeamBinding

class TeamActivity: AppCompatActivity() {
    private lateinit var binding: ActivityTeamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.joinTeamButton.setOnClickListener {
            val intent = Intent(this, JoinTeamActivity::class.java)
            startActivity(intent)
        }
        binding.makeTeamButton.setOnClickListener {
            val intent = Intent(this, MakeTeamActivity::class.java)
            startActivity(intent)
        }
    }
}