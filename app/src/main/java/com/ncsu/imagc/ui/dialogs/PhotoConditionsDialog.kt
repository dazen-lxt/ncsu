package com.ncsu.imagc.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.core.widget.addTextChangedListener
import com.ncsu.imagc.R
import kotlinx.android.synthetic.main.dialog_photo_conditions.*

class PhotoConditionsDialog(context: Context, private val weather: WeatherType, private val typeWeed: String, private val listener: PhotoConditionsDialogListener) : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen),
    CompoundButton.OnCheckedChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_photo_conditions)

        continueButton.setOnClickListener {
            dismiss()
            val weatherType = when {
                sunnyRadioButton.isChecked -> WeatherType.SUNNY
                cloudyRadioButton.isChecked -> WeatherType.CLOUDY
                else -> WeatherType.PARTIAL
            }
            listener.onContinueButton(weatherType, typeWeedTextView.text.toString())
        }
        sunnyRadioButton.setOnCheckedChangeListener(this)
        cloudyRadioButton.setOnCheckedChangeListener(this)
        partialCloudRadioButton.setOnCheckedChangeListener(this)

        val countries: Array<out String> = context.resources.getStringArray(R.array.weed_types)
        ArrayAdapter<String>(context
            , android.R.layout.simple_list_item_1, countries).also { adapter ->
            typeWeedTextView.setAdapter(adapter)
        }

        continueButton.isEnabled = typeWeed.isNotEmpty()
        typeWeedTextView.setText(typeWeed)
        typeWeedTextView.addTextChangedListener {
            continueButton.isEnabled = it?.isNotEmpty() ?: false
        }
        when(weather) {
            WeatherType.SUNNY -> sunnyRadioButton.isChecked = true
            WeatherType.CLOUDY -> cloudyRadioButton.isChecked = true
            WeatherType.PARTIAL -> partialCloudRadioButton.isChecked = true
        }
    }

    interface PhotoConditionsDialogListener {
        fun onContinueButton(weather: WeatherType, weedType: String)
    }

    override fun onCheckedChanged(radioButton: CompoundButton, isChecked: Boolean) {
        if(isChecked) {
            sunnyRadioButton.isChecked = radioButton.id == R.id.sunnyRadioButton
            partialCloudRadioButton.isChecked = radioButton.id == R.id.partialCloudRadioButton
            cloudyRadioButton.isChecked = radioButton.id == R.id.cloudyRadioButton
        }
    }

    enum class WeatherType {
        SUNNY, CLOUDY, PARTIAL
    }

}