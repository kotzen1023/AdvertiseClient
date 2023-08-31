package com.seventhmoon.advertiseclient.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DefaultPlayMarqueeDataDao {
    @Query("SELECT * FROM " + DefaultPlayMarqueeData.TABLE_NAME)

    fun getAll(): List<DefaultPlayMarqueeData>

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(defaultPlayMarqueeData: DefaultPlayMarqueeData)

    @Update
    fun update(defaultPlayMarqueeData: DefaultPlayMarqueeData)

    @Query("DELETE FROM " + DefaultPlayMarqueeData.TABLE_NAME + " WHERE 1")
    fun clearTable()
}