package com.example.e_finity.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.e_finity.R
import com.example.e_finity.treasureClass
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.utilities.Score

class treasureAdapter(private val user: String, var data: List<treasureClass>) : RecyclerView.Adapter<treasureAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.treasuredisplay, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            val treasurePointBorder = findViewById<MaterialCardView>(R.id.treasurePointBorder)
            val treasureScoreText = findViewById<TextView>(R.id.treasureScoreText)
            val treasureMissionText = findViewById<TextView>(R.id.treasureMissionText)
            val treasureContentText = findViewById<TextView>(R.id.treasureContentText)
            if (user in data[position].completed) {
                treasurePointBorder.setCardBackgroundColor(Color.parseColor("#008000"))
            }
            treasureScoreText.text = data[position].points.toString() + " points"
            treasureContentText.text = data[position].content
            treasureMissionText.text = "Mission "  + (position+1).toString()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}