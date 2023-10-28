package com.example.e_finity.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.GroupAdd
import com.example.e_finity.GroupRead
import com.example.e_finity.R
import com.example.e_finity.fragments.url
import com.example.e_finity.games.AddScatterActivity
import com.example.e_finity.games.AddTreasureActivity
import com.example.e_finity.scatterClass
import com.example.e_finity.teams.JoinTeamActivity
import com.example.e_finity.teams.MakeTeamActivity
import com.google.android.material.card.MaterialCardView
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class GroupAdapter(var data: List<GroupRead>): RecyclerView.Adapter<GroupAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.groupdisplay, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            val groupAvaBorder = findViewById<MaterialCardView>(R.id.groupAvaBorder)
            val groupAvaImageView = findViewById<ImageView>(R.id.groupAvaImageView)
            val groupAvaName = findViewById<TextView>(R.id.groupAvaName)
            val groupAdd = findViewById<ImageView>(R.id.groupAdd)
            val sharePreference = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
            val role = sharePreference.getString("ROLE", "").toString()
            groupAdd.visibility = View.GONE
            if (position == data.size) {
                if (role == "Senior") {
                    groupAdd.visibility = View.VISIBLE
                    groupAvaBorder.visibility = View.GONE
                    groupAvaImageView.visibility = View.GONE
                    groupAvaName.visibility = View.GONE
                }
                else{
                    groupAvaBorder.visibility = View.GONE
                    groupAvaImageView.visibility = View.GONE
                    groupAvaName.visibility = View.GONE
                }
            }
            else {
                val client = getclient()
                val bucket = client.storage["avatar"]
                groupAvaName.text = data[position].name
                groupAvaBorder.setStrokeColor(Color.parseColor("#"+data[position].color))
                val url = bucket.publicUrl(data[position].name + ".png") + "?timestamp=" + data[position].timemodi//(System.currentTimeMillis()/(1000*60*3))
                Glide.with(context).load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.avatar).into(groupAvaImageView)
//                if (data[position].name == "None") {
//                    groupLayout.visibility = View.GONE
//                    groupLayout.layoutParams = ViewGroup.LayoutParams(0,0)
//                }



            }
            groupAdd.setOnClickListener{
                val intent = Intent(context, MakeTeamActivity::class.java)
                (context as Activity).startActivityForResult(intent, 1000)
            }
            holder.itemView.setOnClickListener {
                if (position != data.size) {
                    if (role == "Senior") {
                        val intent = Intent(context, MakeTeamActivity::class.java)
                        intent.putExtra("name", data[position].name)
                        intent.putExtra("color", data[position].color)
                        intent.putExtra("timemodi", data[position].timemodi.toString())
                        context.startActivity(intent)
                    }
                    else {
                        val intent = Intent(context, JoinTeamActivity::class.java)
                        intent.putExtra("name", data[position].name)
                        intent.putExtra("color", data[position].color)
                        intent.putExtra("timemodi", data[position].timemodi.toString())
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size+1
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