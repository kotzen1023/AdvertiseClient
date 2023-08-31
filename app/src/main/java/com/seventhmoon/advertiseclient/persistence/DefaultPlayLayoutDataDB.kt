package com.seventhmoon.advertiseclient.persistence

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [DefaultPlayLayoutData::class], version = 2, exportSchema = true)

abstract class DefaultPlayLayoutDataDB : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "defaultPlayLayoutData.db"
    }

    abstract fun defaultPlayLayoutDataDao(): DefaultPlayLayoutDataDao
}