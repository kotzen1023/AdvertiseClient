package com.seventhmoon.advertiseclient.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
@Dao
interface DefaultPlayMixDataDao {
    @Query("SELECT * FROM " + DefaultPlayMixData.TABLE_NAME)

    fun getAll(): List<DefaultPlayMixData>

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(defaultPlayMixData: DefaultPlayMixData)

    @Update
    fun update(defaultPlayMixData: DefaultPlayMixData)

    @Query("DELETE FROM " + DefaultPlayMixData.TABLE_NAME + " WHERE 1")
    fun clearTable()
}