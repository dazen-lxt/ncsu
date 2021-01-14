package com.ncsu.imagc.data.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class PhotoInfo(var photoUrl: String,
                     var name: String,
                     var description: String,
                     var weedsAmount: String,
                     val weather: String,
                     var typeWeed: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var synced: String = ""
    var fileId: String = ""
    var fileInfoId: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
}

data class PhotoInfoWithSensors(
    @Embedded val photo: PhotoInfo,
    @Relation(
        parentColumn = "id",
        entityColumn = "photoId"
    )
    val sensors: List<SensorInfo>
)