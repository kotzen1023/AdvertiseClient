package com.seventhmoon.advertiseclient.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DefaultPlayAdSettingData::class], version = 2, exportSchema = true)
abstract class DefaultPlayAdSettingDataDB : RoomDatabase()  {
    companion object {
        const val DATABASE_NAME = "DefaultPlayAdSettingData.db"
    }

    abstract fun defaultPlayAdSettingDataDao(): DefaultPlayAdSettingDataDao
}