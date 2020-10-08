package com.ncsu.imagc.ui.adapters

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncsu.imagc.MainActivity
import com.ncsu.imagc.R
import kotlinx.android.synthetic.main.item_settings.view.*

class SettingAdapter(var sensors: List<MainActivity.SensorValues>, var context: Context): RecyclerView.Adapter<SettingAdapter.SettingViewHolder>() {
    class SettingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_settings, parent, false)
        return SettingViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sensors.size
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        val preferences: SharedPreferences =
            context!!.getSharedPreferences("NCSUPreferences", Context.MODE_PRIVATE)
        var sensor = sensors[position]
        holder.itemView.nameTextView.text = "${sensor.sensor.name} (${sensor.type})"
        holder.itemView.settingSwitch.isChecked = sensor.actived
        holder.itemView.settingSwitch.setOnCheckedChangeListener { _, b ->
            val editor = preferences.edit()
            editor.putBoolean(sensor.sensor.type.toString(), b)
            editor.commit()
        }
    }

}