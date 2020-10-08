package com.ncsu.imagc.data.dao

import android.hardware.Sensor
import androidx.room.*
import com.ncsu.imagc.data.entities.PhotoInfo
import com.ncsu.imagc.data.entities.SensorInfo
import com.ncsu.imagc.data.entities.SensorValue
import com.ncsu.imagc.data.entities.SensorWithValues

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photoinfo")
    fun getPhotos(): List<PhotoInfo>

    @Query("SELECT * FROM sensorinfo WHERE photoId = :photoId")
    fun getSensors(photoId: Long): List<SensorInfo>

    @Query("SELECT * FROM sensorvalue WHERE sensorId = :sensorId")
    fun getSensorValues(sensorId: Long): List<SensorValue>

    @Transaction
    @Query("SELECT * FROM sensorinfo WHERE photoId = :photoId")
    fun getSensorWithValues(photoId: Long): List<SensorWithValues>

    @Insert
    fun insertPhoto(photo: PhotoInfo): Long

    @Insert
    fun insertSensor(sensor: SensorInfo): Long

    @Insert
    fun insertValues(values: List<SensorValue>)

    @Update
    fun updatePhoto(photo: PhotoInfo)
}