package com.ncsu.imagc.ui.adapters

import android.content.Context
import android.hardware.Sensor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncsu.imagc.R
import kotlinx.android.synthetic.main.item_settings.view.*

class SettingAdapter(var sensors: List<Sensor>, var context: Context): RecyclerView.Adapter<SettingAdapter.SettingViewHolder>() {
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
        var sensor = sensors[position]
        holder.itemView.nameTextView.text = "${sensor.name} (${sensor.stringType})"
    }

}