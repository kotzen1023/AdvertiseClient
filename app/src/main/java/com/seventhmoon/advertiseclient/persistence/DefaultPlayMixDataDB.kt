package com.seventhmoon.advertiseclient.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DefaultPlayMixData::class], version = 2, exportSchema = true)
abstract class DefaultPlayMixDataDB : RoomDatabase()  {
    companion object {
        const val DATABASE_NAME = "DefaultPlayMixData.db"
    }

    abstract fun defaultPlayMixDataDao(): DefaultPlayMixDataDao
}