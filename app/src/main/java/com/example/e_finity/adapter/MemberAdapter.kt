package com.example.e_finity.adapter

import android.content.Intent
import android.media.Image
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.R
import com.example.e_finity.UserRead
import com.example.e_finity.teams.JoinTeamActivity
import com.example.e_finity.teams.MemberAddActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class MemberAdapter(var data: List<UserRead>, var groupName: String?): RecyclerView.Adapter<MemberAdapter.ViewHolder>()  {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.memberdisplay, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            val memberAva = findViewById<ImageView>(R.id.memberAvaImage)
            val memberName = findViewById<TextView>(R.id.memberName)
            val memberEdit = findViewById<ImageView>(R.id.memberEdit)
            val memberAdd = findViewById<ImageView>(R.id.memberAdd)
            memberAdd.visibility = View.GONE
            val client = getclient()
            val bucket = client.storage["avatar"]
            if (position == data.size) {
                memberName.visibility = View.GONE
                memberAva.visibility = View.GONE
                memberEdit.visibility = View.GONE
                memberAdd.visibility = View.VISIBLE
            }
            else {
                if (data[position].role != "Freshman") {
                    memberEdit.visibility = View.GONE
                }
                memberName.text = data[position].full_name
                val url = bucket.publicUrl(data[position].uniqueID + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
                Glide.with(context).load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.avatar).into(memberAva)
            }
            holder.itemView.setOnClickListener {
                if (position == data.size) {
                    val intent = Intent(context, MemberAddActivity::class.java)
                    intent.putExtra("name", groupName)
                    context.startActivity(intent)
                }
            }
            memberEdit.setOnClickListener {
                val intent = Intent(context, MemberAddActivity::class.java)
                intent.putExtra("fullName", data[position].full_name)
                intent.putExtra("phoneNo", data[position].phone_num)
                intent.putExtra("NFC", data[position].uniqueID)
                context.startActivity(intent)
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