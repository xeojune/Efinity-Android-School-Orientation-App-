package com.example.e_finity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.GroupAdd
import com.example.e_finity.GroupRead
import com.example.e_finity.R
import com.example.e_finity.scatterClass
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
            val groupLayout = findViewById<RelativeLayout>(R.id.groupLayout)
            groupAdd.visibility = View.GONE
            if (position == data.size) {
                groupAdd.visibility = View.VISIBLE
                groupAvaBorder.visibility = View.GONE
                groupAvaImageView.visibility = View.GONE
                groupAvaName.visibility = View.GONE
            }
            else {
                val client = getclient()
                val bucket = client.storage["avatar"]
                groupAvaName.text = data[position].name
                val url = bucket.publicUrl(data[position].name + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
                Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.avatar).into(groupAvaImageView)
//                if (data[position].name == "None") {
//                    groupLayout.visibility = View.GONE
//                    groupLayout.layoutParams = ViewGroup.LayoutParams(0,0)
//                }



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