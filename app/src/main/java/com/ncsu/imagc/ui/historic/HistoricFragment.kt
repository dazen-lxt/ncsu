package com.ncsu.imagc.ui.historic

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ncsu.imagc.MainActivity
import com.ncsu.imagc.R
import com.ncsu.imagc.ui.adapters.HistoricAdapter
import kotlinx.android.synthetic.main.fragment_historic.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class HistoricFragment : Fragment() {

    private lateinit var adapter: HistoricAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?  = inflater.inflate(R.layout.fragment_historic, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = HistoricAdapter(listOf())
        historicList.adapter = adapter
        historicList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        listFiles()
    }

    private fun listFiles() {
        try {
            doAsync {
                val db = (activity as MainActivity).db
                val photos = db.photoDao().getPhotos()
                uiThread {
                    historicList.isVisible = true
                    adapter.items = photos
                    adapter.notifyDataSetChanged()
                }
            }
        } catch (e: Exception) {
            Log.v("LXT", "KLXR")
        }
    }
}