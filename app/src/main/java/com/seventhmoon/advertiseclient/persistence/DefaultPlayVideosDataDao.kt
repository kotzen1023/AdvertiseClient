package com.seventhmoon.advertiseclient.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DefaultPlayVideosDataDao {
    @Query("SELECT * FROM " + DefaultPlayVideosData.TABLE_NAME)

    fun getAll(): List<DefaultPlayVideosData>

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(defaultPlayVideosData: DefaultPlayVideosData)

    @Update
    fun update(defaultPlayVideosData: DefaultPlayVideosData)

    @Query("DELETE FROM " + DefaultPlayVideosData.TABLE_NAME + " WHERE 1")
    fun clearTable()
}