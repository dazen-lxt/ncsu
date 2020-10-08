package com.ncsu.imagc.ui.adapters

import android.content.Context
import android.hardware.Sensor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.ncsu.imagc.R
import com.ncsu.imagc.data.AppDatabase
import com.ncsu.imagc.data.entities.PhotoInfo
import kotlinx.android.synthetic.main.dialog_photo.view.*
import kotlinx.android.synthetic.main.item_historic.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class HistoricAdapter(var items: List<PhotoInfo>, var context: Context, var db: AppDatabase): RecyclerView.Adapter<HistoricAdapter.HistoricViewHolder>() {
    class HistoricViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    class HistoricViewModel(var fileName: String)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historic, parent, false)
        return HistoricViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: HistoricViewHolder, position: Int) {
        var item = items[position]

        holder.itemView.nameTextView.text = "${item.name}"
        if(!item.synced)
            holder.itemView.syncImageView.visibility = View.GONE
        holder.itemView.detailButton.setOnClickListener {
            doAsync {
                var sensors = db.photoDao().getSensorWithValues(item.id)
                var sensorInfo = "Name: ${item.name}\n" +
                        "Description: ${item.description}\n" +
                        "Sensors:\n"
                for(sensor in sensors) {
                    sensorInfo += "${sensor.sensor.sensorName}: ${sensor.values.map { value -> "${value.value}${sensor.sensor.units}" }}\n"
                }
                uiThread {
                    val builder = AlertDialog.Builder(context!!)
                    builder.setTitle(item.name)
                    builder.setMessage(sensorInfo)
                    builder.setPositiveButton("Close", null)
                    builder.create()
                    builder.show()
                }
            }
        }
    }

}