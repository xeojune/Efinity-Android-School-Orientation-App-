package com.example.e_finity.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.compose.ui.input.key.Key.Companion.G
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.R
import com.example.e_finity.games.AddScatterActivity
import com.example.e_finity.games.AddTreasureActivity
import com.example.e_finity.scatterClass
import com.example.e_finity.uReadShort
import com.google.android.material.card.MaterialCardView
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.runBlocking

class ScatterAdapter(var data: List<scatterClass>): RecyclerView.Adapter<ScatterAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scatterdisplay, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            val scatterAvaName = findViewById<TextView>(R.id.scatterAvaName)
            val scatterAvaPoints = findViewById<TextView>(R.id.scatterAvaPoints)
            val scatterAvaBorder = findViewById<MaterialCardView>(R.id.scatterAvaBorder)
            val scatterAvaImageView = findViewById<ImageView>(R.id.scatterAvaImageView)
            val scatterAdd = findViewById<ImageView>(R.id.scatterAdd)
            val scatterLayout = findViewById<RelativeLayout>(R.id.scatterLayout)
            scatterAdd.visibility = View.GONE
            scatterAvaBorder.strokeWidth = 0
            val sharePreference = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
            val role = sharePreference.getString("ROLE", "").toString()
            if (position == data.size) {
                if (role == "Senior"){
                    scatterAdd.visibility = View.VISIBLE
                }
                scatterAvaName.visibility = View.GONE
                scatterAvaPoints.visibility = View.GONE
                scatterAvaBorder.visibility = View.GONE
                scatterAvaImageView.visibility = View.GONE
            }
            else {
                if (data[position].completed == "") {
                    scatterAvaName.text = "Unclaimed"
                }
                else{
                    val client = getclient()
                    runBlocking {
                        val userinfo = client.postgrest["user"].select(columns = Columns.list("""full_name, group!inner(name, color)""")) {
                            eq("uniqueID", data[position].completed)
                        }.decodeList<uReadShort>()
                        scatterAvaName.text = userinfo[0].full_name
                        if (userinfo[0].group.name != "None"){
                            scatterAvaBorder.strokeWidth = 10
                            scatterAvaBorder.setStrokeColor(Color.parseColor("#"+userinfo[0].group.color))
                        }
                        val bucket = client.storage["avatar"]
                        val url = bucket.publicUrl(data[position].completed + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
                        Log.e("URL", url)
                        Glide.with(context).load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.baseline_person).into(scatterAvaImageView)
                    }
                }
                scatterAvaPoints.text = data[position].points.toString() + " points"
            }
            scatterAdd.setOnClickListener {
                val intent = Intent(context, AddScatterActivity::class.java)
                (context as Activity).startActivityForResult(intent, 1000)
            }
            if (role == "Senior") {
                scatterLayout.setOnClickListener {
                    val intent = Intent(context, AddScatterActivity::class.java)
                    intent.putExtra("Points", data[position].points.toString())
                    intent.putExtra("UID", data[position].UID)
                    (context as Activity).startActivityForResult(intent, 1000)
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