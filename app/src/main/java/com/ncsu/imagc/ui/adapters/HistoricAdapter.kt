package com.ncsu.imagc.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ncsu.imagc.R
import com.ncsu.imagc.data.entities.PhotoInfo
import com.ncsu.imagc.extensions.toString
import com.ncsu.imagc.ui.dialogs.PhotoConditionsDialog
import kotlinx.android.synthetic.main.item_historic.view.*
import java.io.File

class HistoricAdapter(var items: List<PhotoInfo>): RecyclerView.Adapter<HistoricAdapter.HistoricViewHolder>() {

    class HistoricViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(photoInfo: PhotoInfo) {
            itemView.titleTextView.text = photoInfo.name
            itemView.descriptionTextView.text = photoInfo.description
            itemView.photoImageView.setImageURI(Uri.fromFile(File(photoInfo.photoUrl)))
            itemView.weedTypeTextView.text = photoInfo.typeWeed.plus(" (").plus(photoInfo.weedsAmount).plus(")")
            itemView.locationTextView.text = "Lat: ${photoInfo.latitude.toString(2)} Lng: ${photoInfo.longitude.toString(2)}"
            itemView.weatherImageView.setImageDrawable(itemView.context.resources.getDrawable(
                when(photoInfo.weather) {
                    PhotoConditionsDialog.WeatherType.SUNNY.name -> R.drawable.ic_sun
                    PhotoConditionsDialog.WeatherType.CLOUDY.name -> R.drawable.ic_cloud
                    PhotoConditionsDialog.WeatherType.PARTIAL.name -> R.drawable.ic_clouds_and_sun
                    else -> R.drawable.ic_clouds_and_sun
                }, null))
            itemView.syncedTextView.text = itemView.context.getString(R.string.no_synced)
            if(photoInfo.synced.isNotEmpty()) itemView.syncedTextView.text = photoInfo.synced
            itemView.weatherTextView.text = photoInfo.weather
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historic, parent, false)
        return HistoricViewHolder(
            view
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: HistoricViewHolder, position: Int) {
        holder.bind(items[position])
    }
}