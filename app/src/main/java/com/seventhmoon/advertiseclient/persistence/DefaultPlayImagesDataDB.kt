package com.seventhmoon.advertiseclient.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DefaultPlayImagesData::class], version = 2, exportSchema = true)
abstract class DefaultPlayImagesDataDB : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "DefaultPlayImagesData.db"
    }

    abstract fun defaultPlayImagesDataDao(): DefaultPlayImagesDataDao
}