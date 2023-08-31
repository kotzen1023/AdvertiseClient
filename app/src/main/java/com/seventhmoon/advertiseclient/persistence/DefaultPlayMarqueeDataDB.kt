package com.seventhmoon.advertiseclient.persistence

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [DefaultPlayMarqueeData::class], version = 2, exportSchema = true)
abstract class DefaultPlayMarqueeDataDB : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "defaultPlayMarqueeData.db"
    }

    abstract fun defaultPlayMarqueeDataDao(): DefaultPlayMarqueeDataDao
}