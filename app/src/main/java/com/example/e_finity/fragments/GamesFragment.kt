package com.example.e_finity.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.example.e_finity.MainActivity
import com.example.e_finity.R
import com.example.e_finity.games.ScatterTagActivity
import com.example.e_finity.games.treasureHuntActivity
import com.example.e_finity.teams.TeamActivity
import com.github.antonpopoff.colorwheel.ColorWheel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class GamesFragment : Fragment() {

    private var activity: MainActivity?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_games, container, false)
        return view
    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val treasurePlayBtn = view.findViewById<AppCompatButton>(R.id.treasurePlayBtn)
        treasurePlayBtn.setOnClickListener{
            requireActivity().run {
                startActivity(Intent(this, treasureHuntActivity::class.java))
            }
        }
        val scatterPlayBtn = view.findViewById<AppCompatButton>(R.id.scatterPlayBtn)
        scatterPlayBtn.setOnClickListener{
            requireActivity().run {
                startActivity(Intent(this, ScatterTagActivity::class.java))
            }
        }
    }

}