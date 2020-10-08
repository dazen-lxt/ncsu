package com.ncsu.imagc.ui.map

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ncsu.imagc.MainActivity
import com.ncsu.imagc.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MapFragment : SupportMapFragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener  {

    lateinit var mMap: GoogleMap

    override fun onActivityCreated(p0: Bundle?) {
        super.onActivityCreated(p0)
        getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        try {
            doAsync {
                var db = (activity as MainActivity).db
                var photos = db.photoDao().getPhotos()

                uiThread {
                    for(photo in photos) {
                        val location = LatLng(photo.latitude, photo.longitude)
                        mMap.addMarker(MarkerOptions().position(location).title(photo.name))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
                    }
                }
            }
        } catch (e: Exception) {
            Log.v("LXT", "KLXR")
        }

    }

    override fun onMapClick(p0: LatLng?) {


    }


}