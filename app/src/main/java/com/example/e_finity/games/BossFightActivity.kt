package com.example.e_finity.games

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.e_finity.R
import com.example.e_finity.bossesClass

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.e_finity.databinding.ActivityBossFightBinding
import com.example.e_finity.fragments.url
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BossFightActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityBossFightBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBossFightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        checkPermission()
    }

    var AccessLocation = 123

    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), AccessLocation)
                return
            }
        }
        getUserLocation()
    }

    fun getUserLocation() {
        Toast.makeText(this, "UserLocation Access On", Toast.LENGTH_SHORT).show()
        var loclic = MyLocationListener()
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3f, loclic)

        var thread = MyThread()
        thread.start()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            AccessLocation -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation()
                }
                else {
                    Toast.makeText(this, "User Not Granted Permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener { marker: Marker ->
            var bossLoc = Location("boss")
            bossLoc.latitude = marker.position.latitude
            bossLoc.longitude = marker.position.longitude
            if (locationUser!!.distanceTo(bossLoc) < 30) {
                Toast.makeText(this, marker.tag.toString(), Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "You need to be closer to the boss to fight it", Toast.LENGTH_LONG).show()
            }
            false
        }
    }

    var locationUser : Location? = null

    inner class MyLocationListener : LocationListener {

        constructor() {
            locationUser = Location("Start")
            locationUser?.latitude = 0.0
            locationUser?.longitude = 0.0
        }
        override fun onLocationChanged(p0: Location) {
            locationUser = p0
        }
    }

    var location : Location? = null
    inner class MyThread : Thread {
        var oldLocation : Location? = null
        constructor() : super() {
            oldLocation = Location("Start")
            oldLocation!!.longitude = 0.0
            oldLocation!!.latitude = 0.0
        }

        override fun run() {
            while (true) {
                try {

                    if (oldLocation!!.distanceTo(locationUser!!) == 0f) {
                        continue
                    }

                    oldLocation = locationUser

                    runOnUiThread {

                        mMap.clear()
                        val client = getclient()
                        val bucket = client.storage["avatar"]
                        val sharePreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)

                        url = bucket.publicUrl(sharePreference.getString("SESSION", "").toString() + ".png") + "?timestamp=" + (System.currentTimeMillis()/(1000*60*3))
//                        val sydney = LatLng(1.34835, 103.68313)
                        val sydney = LatLng(locationUser!!.latitude, locationUser!!.longitude)
//                        mMap.addMarker(MarkerOptions().position(sydney).title("NTU"))
                        Glide.with(applicationContext).asBitmap().load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.avatar).into(
                            object: CustomTarget<Bitmap>(100,100) {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    mMap.addMarker(MarkerOptions().position(sydney).title("NTU").icon(BitmapDescriptorFactory.fromBitmap(resource)))
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    mMap.addMarker(MarkerOptions().position(sydney).title("NTU").icon(BitmapDescriptorFactory.fromResource(R.drawable.avatar)))
                                }
                            }
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,18f))
                        mMap.uiSettings.isZoomControlsEnabled = true

                        loadBosses()
                    }
                    Thread.sleep(1000)
                }
                catch (ex : Exception) {

                }
            }
            super.run()
        }
    }

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

    fun loadBosses() {
        val client = getclient()
        MainScope().launch {
            val bossesData = client.postgrest["bosses"].select() {
            }.decodeList<bossesClass>()

            if (bossesData.size != 0) {
                for (i in 0..bossesData.size-1) {
                    val bossLoc = LatLng(bossesData[i].lat, bossesData[i].log)
                    val url2 = "https://freepngimg.com/download/pokemon/117725-charmander-free-hd-image.png"
                    Glide.with(applicationContext).asBitmap().load(url2).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.avatar).into(
                        object: CustomTarget<Bitmap>(250,250) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                val marker = mMap.addMarker(MarkerOptions().position(bossLoc).title(bossesData[i].bossName).icon(BitmapDescriptorFactory.fromBitmap(resource)))
                                marker!!.tag = bossesData[i].bossPower
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                val marker = mMap.addMarker(MarkerOptions().position(bossLoc).title(bossesData[i].bossName).icon(BitmapDescriptorFactory.fromResource(R.drawable.avatar)))
                            }
                        }
                    )
                }
            }
        }

    }
}