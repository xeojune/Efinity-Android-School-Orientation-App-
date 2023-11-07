package com.example.e_finity.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.e_finity.MainActivity
import com.example.e_finity.R
import com.example.e_finity.adapter.LeaderboardAdapter
import com.example.e_finity.leaderBoard
import com.example.e_finity.leaderScore
import com.github.antonpopoff.colorwheel.ColorWheel
import com.google.android.material.card.MaterialCardView
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ScoreFragment : Fragment() {

    private var activity: MainActivity?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_score, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fun getclient(): SupabaseClient {
            return createSupabaseClient(
                supabaseUrl = "https://nabbsmcfsskdwjncycnk.supabase.co",
                supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5hYmJzbWNmc3NrZHdqbmN5Y25rIiwicm9sZSI6ImFub24iLCJpYXQiOjE2OTM5MDM3ODksImV4cCI6MjAwOTQ3OTc4OX0.dRVk2u91mLhSMaA1s0FSyIFwnxe2Y3TPdZZ4Shc9mAY"
            ) {
                install(Postgrest)
                install(GoTrue)
                install(Storage)
            }
        }
        val client = getclient()
        val groupRecycler = view.findViewById<RecyclerView>(R.id.groupScoreRecycler)
        lifecycleScope.launch {
            val leaderBoard = client.postgrest["user"].select(columns = Columns.list("""uniqueID, full_name, role, group!inner(name, color, timemodi), score""")) {
                order("score", Order.DESCENDING)
            }.decodeList<leaderBoard>()
            val filteredLeaderBoard = leaderBoard.filter { it.full_name != "DELETE" && it.full_name != "MODIFY" && it.role != "Senior" && it.group.name != "None"}
            val adapter = LeaderboardAdapter(filteredLeaderBoard)
            groupRecycler.adapter = adapter
            groupRecycler.layoutManager = LinearLayoutManager(context)
            val bucket = client.storage["avatar"]
            val firstPlaceAva = view.findViewById<ImageView>(R.id.firstPlace)
            val firstPlaceName = view.findViewById<TextView>(R.id.firstName)
            val firstCard = view.findViewById<MaterialCardView>(R.id.firstCard)
            val firstText = view.findViewById<TextView>(R.id.firstText)
            val secondPlaceAva = view.findViewById<ImageView>(R.id.secondPlace)
            val secondPlaceName = view.findViewById<TextView>(R.id.secondName)
            val secondCard = view.findViewById<MaterialCardView>(R.id.secondCard)
            val secondText = view.findViewById<TextView>(R.id.secondText)
            val thirdPlaceAva = view.findViewById<ImageView>(R.id.thirdPlace)
            val thirdPlaceName = view.findViewById<TextView>(R.id.thirdName)
            val thirdCard = view.findViewById<MaterialCardView>(R.id.thirdCard)
            val thirdText = view.findViewById<TextView>(R.id.thirdText)
            if (filteredLeaderBoard.size == 1) {
                val url = bucket.publicUrl(filteredLeaderBoard[0].uniqueID + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
                Glide.with(this@ScoreFragment).load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.baseline_person).into(firstPlaceAva)
                firstPlaceName.text = filteredLeaderBoard[0].full_name
                secondPlaceAva.visibility = View.GONE
                secondPlaceName.visibility = View.GONE
                secondCard.visibility = View.GONE
                secondText.visibility = View.GONE
                thirdPlaceAva.visibility = View.GONE
                thirdPlaceName.visibility = View.GONE
                thirdCard.visibility = View.GONE
                thirdText.visibility = View.GONE
            }
            else if (filteredLeaderBoard.size == 2) {
                val url = bucket.publicUrl(filteredLeaderBoard[0].uniqueID + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
                Glide.with(this@ScoreFragment).load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.baseline_person).into(firstPlaceAva)
                firstPlaceName.text = filteredLeaderBoard[0].full_name
                val url2 = bucket.publicUrl(filteredLeaderBoard[1].uniqueID + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
                Glide.with(this@ScoreFragment).load(url2).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.baseline_person).into(secondPlaceAva)
                secondPlaceName.text = filteredLeaderBoard[1].full_name
                thirdPlaceAva.visibility = View.GONE
                thirdPlaceName.visibility = View.GONE
                thirdCard.visibility = View.GONE
                thirdText.visibility = View.GONE
            }
            else if (filteredLeaderBoard.size >= 3) {
                val url = bucket.publicUrl(filteredLeaderBoard[0].uniqueID + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
                Glide.with(this@ScoreFragment).load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.baseline_person).into(firstPlaceAva)
                firstPlaceName.text = filteredLeaderBoard[0].full_name
                val url2 = bucket.publicUrl(filteredLeaderBoard[1].uniqueID + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
                Glide.with(this@ScoreFragment).load(url2).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.baseline_person).into(secondPlaceAva)
                secondPlaceName.text = filteredLeaderBoard[1].full_name
                val url3 = bucket.publicUrl(filteredLeaderBoard[2].uniqueID + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
                Glide.with(this@ScoreFragment).load(url3).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.baseline_person).into(thirdPlaceAva)
                thirdPlaceName.text = filteredLeaderBoard[2].full_name
            }
            else {
                firstPlaceAva.visibility = View.GONE
                firstPlaceName.visibility = View.GONE
                firstCard.visibility = View.GONE
                firstText.visibility = View.GONE
                secondPlaceAva.visibility = View.GONE
                secondPlaceName.visibility = View.GONE
                secondCard.visibility = View.GONE
                secondText.visibility = View.GONE
                thirdPlaceAva.visibility = View.GONE
                thirdPlaceName.visibility = View.GONE
                thirdCard.visibility = View.GONE
                thirdText.visibility = View.GONE
                val crown = view.findViewById<ImageView>(R.id.crown)
                crown.visibility = View.GONE
            }
        }

    }

}