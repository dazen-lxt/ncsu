package com.ncsu.imagc.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ncsu.imagc.data.dao.PhotoDao
import com.ncsu.imagc.data.entities.*

@Database(entities = arrayOf(PhotoInfo::class, SensorInfo::class, SensorValue::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}