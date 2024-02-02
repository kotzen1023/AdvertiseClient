package com.seventhmoon.advertiseclient.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DefaultPlayBannerDataDao {
    @Query("SELECT * FROM " + DefaultPlayBannerData.TABLE_NAME)

    fun getAll(): List<DefaultPlayBannerData>

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(defaultPlayBannerData: DefaultPlayBannerData)

    @Update
    fun update(defaultPlayBannerData: DefaultPlayBannerData)

    @Query("DELETE FROM " + DefaultPlayBannerData.TABLE_NAME + " WHERE 1")
    fun clearTable()
}