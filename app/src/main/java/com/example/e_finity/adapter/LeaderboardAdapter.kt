package com.example.e_finity.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.R
import com.example.e_finity.leaderBoard
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class LeaderboardAdapter(var data: List<leaderBoard>): RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>()  {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.teamscoredisplay, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            val groupScorePos = findViewById<TextView>(R.id.groupScorePosition)
            val groupScoreAva = findViewById<ImageView>(R.id.groupScoreAvatar)
            val groupScoreName = findViewById<TextView>(R.id.groupScoreName)
            val groupScorePoints = findViewById<TextView>(R.id.groupScorePoints)
            val client = getclient()
            groupScorePos.text = (position+1).toString()
            groupScoreName.text = data[position].full_name
            groupScorePoints.text = data[position].score.toString() + " pts"
            val bucket = client.storage["avatar"]
            val url = bucket.publicUrl(data[position].uniqueID + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
            Glide.with(context).load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.baseline_person).into(groupScoreAva)
            holder.itemView.background.setColorFilter(Color.parseColor("#"+data[position].group.color), PorterDuff.Mode.SRC)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
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