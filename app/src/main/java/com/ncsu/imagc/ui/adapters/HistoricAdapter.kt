package com.ncsu.imagc.ui.adapters

import android.content.Context
import android.hardware.Sensor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ncsu.imagc.R
import com.ncsu.imagc.data.entities.PhotoInfo
import kotlinx.android.synthetic.main.item_historic.view.*


class HistoricAdapter(var items: List<PhotoInfo>, var context: Context): RecyclerView.Adapter<HistoricAdapter.HistoricViewHolder>() {
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
    }

}