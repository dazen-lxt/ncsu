package com.ncsu.imagc.data.dao

import android.hardware.Sensor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ncsu.imagc.data.entities.PhotoInfo
import com.ncsu.imagc.data.entities.SensorInfo
import com.ncsu.imagc.data.entities.SensorValue

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photoinfo")
    fun getPhotos(): List<PhotoInfo>

    @Query("SELECT * FROM sensorinfo WHERE photoId = :photoId")
    fun getSensors(photoId: Long): List<SensorInfo>

    @Query("SELECT * FROM sensorvalue WHERE sensorId = :sensorId")
    fun getSensorValues(sensorId: Long): List<SensorValue>

    @Insert
    fun insertPhoto(photo: PhotoInfo): Long

    @Insert
    fun insertSensor(sensor: SensorInfo): Long

    @Insert
    fun insertValues(values: List<SensorValue>)

    @Update
    fun updatePhoto(photo: PhotoInfo)
}