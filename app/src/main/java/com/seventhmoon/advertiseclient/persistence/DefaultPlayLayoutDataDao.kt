package com.seventhmoon.advertiseclient.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DefaultPlayLayoutDataDao {
    @Query("SELECT * FROM " + DefaultPlayLayoutData.TABLE_NAME)

    fun getAll(): List<DefaultPlayLayoutData>

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(defaultPlayLayoutData: DefaultPlayLayoutData)

    @Update
    fun update(defaultPlayLayoutData: DefaultPlayLayoutData)

    @Query("DELETE FROM " + DefaultPlayLayoutData.TABLE_NAME + " WHERE 1")
    fun clearTable()
}