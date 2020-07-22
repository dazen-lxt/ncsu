package com.ncsu.imagc.ui.settings

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ncsu.imagc.MainActivity
import com.ncsu.imagc.R
import com.ncsu.imagc.ui.adapters.SettingAdapter
import kotlinx.android.synthetic.main.settings_fragment.*


class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var sensors = (activity as MainActivity).sensors.map { item -> item.sensor }
        var adapter = SettingAdapter(sensors, context!!)
        settingList.adapter = adapter
        settingList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

}