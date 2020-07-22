package com.ncsu.imagc.data.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class SensorInfo(var sensorName: String,
                      var units: String,
                      var photoId: Long) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

@Entity
data class SensorValue(var value: Double,
                      var sensorId: Long) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

data class SensorWithValues(
    @Embedded val sensor: SensorInfo,
    @Relation(
        parentColumn = "id",
        entityColumn = "sensorId"
    )
    val values: List<SensorValue>
)