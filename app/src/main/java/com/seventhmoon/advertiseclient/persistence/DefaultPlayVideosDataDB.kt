package com.seventhmoon.advertiseclient.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DefaultPlayVideosData::class], version = 2, exportSchema = true)
abstract class DefaultPlayVideosDataDB : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "DefaultPlayVideosData.db"
    }

    abstract fun defaultPlayVideosDataDao(): DefaultPlayVideosDataDao
}