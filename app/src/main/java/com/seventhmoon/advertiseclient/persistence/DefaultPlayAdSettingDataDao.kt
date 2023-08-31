package com.seventhmoon.advertiseclient.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DefaultPlayAdSettingDataDao {
    @Query("SELECT * FROM " + DefaultPlayAdSettingData.TABLE_NAME)

    fun getAll(): List<DefaultPlayAdSettingData>

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(defaultPlayAdSettingData: DefaultPlayAdSettingData)

    @Update
    fun update(defaultPlayAdSettingData: DefaultPlayAdSettingData)

    @Query("DELETE FROM " + DefaultPlayAdSettingData.TABLE_NAME + " WHERE 1")
    fun clearTable()
}