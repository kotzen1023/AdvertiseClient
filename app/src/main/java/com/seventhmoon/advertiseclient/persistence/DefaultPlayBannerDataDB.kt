package com.seventhmoon.advertiseclient.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DefaultPlayBannerData::class], version = 2, exportSchema = true)
abstract class DefaultPlayBannerDataDB : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "DefaultPlayBannerData.db"
    }

    abstract fun defaultPlayBannerDataDao(): DefaultPlayBannerDataDao
}