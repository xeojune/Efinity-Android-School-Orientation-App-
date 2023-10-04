package com.example.e_finity.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.e_finity.R
import com.example.e_finity.games.AddTreasureActivity
import com.example.e_finity.games.treasureHuntActivity
import com.example.e_finity.treasureClass
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.utilities.Score

class treasureAdapter(val user: String, var data: List<treasureClass>) : RecyclerView.Adapter<treasureAdapter.ViewHolder>() {
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
            val addImage = findViewById<ImageView>(R.id.addImage)
            val editImage = findViewById<ImageView>(R.id.editImage)
            addImage.visibility = View.GONE
            addImage.setOnClickListener {
                val intent = Intent(context, AddTreasureActivity::class.java)
                context.startActivity(intent)
            }
            if (position == data.size) {
                addImage.visibility = View.VISIBLE
                editImage.visibility = View.GONE
                treasureContentText.visibility = View.GONE
                treasurePointBorder.visibility = View.GONE
                treasureScoreText.visibility = View.GONE
                treasureMissionText.visibility = View.GONE
            }
            else {
                if (user in data[position].completed) {
                    treasurePointBorder.setCardBackgroundColor(Color.parseColor("#008000"))
                }
                treasureScoreText.text = data[position].points.toString() + " points"
                treasureContentText.text = data[position].content
                treasureMissionText.text = "Mission "  + (position+1).toString()
            }
            editImage.setOnClickListener {
                val intent = Intent(context, AddTreasureActivity::class.java)
                intent.putExtra("content", treasureContentText.text.toString())
                intent.putExtra("point", data[position].points.toString())
                intent.putExtra("nfc", data[position].UID)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size+1
    }
//
//    override fun getItemId(position: Int): Long {
//        return super.getItemId(position)
//    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}