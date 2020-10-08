package com.ncsu.imagc.ui.settings

import android.content.Context
import android.content.SharedPreferences
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
import kotlinx.android.synthetic.main.settings_fragment.view.*


class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.settings_fragment, container, false)
        view.saveButton.setOnClickListener {
            activity!!.finish();
            startActivity(activity!!.intent);
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var sensors = (activity as MainActivity).sensorValues
        var adapter = SettingAdapter(sensors, context!!)
        settingList.adapter = adapter
        settingList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }



}