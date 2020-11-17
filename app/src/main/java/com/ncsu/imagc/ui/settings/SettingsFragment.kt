package com.ncsu.imagc.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ncsu.imagc.MainActivity
import com.ncsu.imagc.R
import com.ncsu.imagc.manager.SharedPreferencesManager
import com.ncsu.imagc.manager.SharedPreferencesManager.SettingsPreferences.USE_AZURE
import com.ncsu.imagc.ui.adapters.SettingAdapter
import kotlinx.android.synthetic.main.settings_fragment.*


class SettingsFragment : Fragment(), CompoundButton.OnCheckedChangeListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sensors = (activity as MainActivity).sensorValues
        val adapter = SettingAdapter(sensors, context!!)
        settingList.adapter = adapter
        settingList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val preferences: SharedPreferences =
            requireContext().getSharedPreferences(SharedPreferencesManager.name, Context.MODE_PRIVATE)
        saveButton.setOnClickListener {
            val editor = preferences.edit()
            editor.putBoolean(USE_AZURE.namePreference, azureRadioButton.isChecked)
            editor.apply()
            requireActivity().finish()
            startActivity(requireActivity().intent)
        }
        azureRadioButton.setOnCheckedChangeListener(this)
        googleDriveRadioButton.setOnCheckedChangeListener(this)
        if(preferences.getBoolean(USE_AZURE.namePreference, true))
            azureRadioButton.isChecked = true
        else
            googleDriveRadioButton.isChecked = true
    }

    override fun onCheckedChanged(radioButton: CompoundButton, isChecked: Boolean) {
        if(isChecked) {
            azureRadioButton.isChecked = radioButton.id == R.id.azureRadioButton
            googleDriveRadioButton.isChecked = radioButton.id == R.id.googleDriveRadioButton
        }
    }


}