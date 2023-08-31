package com.seventhmoon.advertiseclient.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DefaultPlayImagesDataDao {
    @Query("SELECT * FROM " + DefaultPlayImagesData.TABLE_NAME)

    fun getAll(): List<DefaultPlayImagesData>

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(defaultPlayImagesData: DefaultPlayImagesData)

    @Update
    fun update(defaultPlayImagesData: DefaultPlayImagesData)

    @Query("DELETE FROM " + DefaultPlayImagesData.TABLE_NAME + " WHERE 1")
    fun clearTable()
}