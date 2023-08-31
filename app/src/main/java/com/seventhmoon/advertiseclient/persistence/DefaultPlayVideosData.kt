package com.seventhmoon.advertiseclient.persistence

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = DefaultPlayVideosData.TABLE_NAME)
class DefaultPlayVideosData(fileName: String) {
    companion object {
        const val TABLE_NAME = "DefaultPlayVideosData"
    }

    @NonNull
    @PrimaryKey(autoGenerate = true)
    private var id: Int = 0

    @ColumnInfo(name = "fileName")
    private var fileName: String = ""

    init {
        this.fileName = fileName
    }

    fun getId(): Int {
        return id
    }

    fun setId(id : Int) {
        this.id = id
    }

    fun getFileName(): String {
        return fileName
    }

    fun setFileName(fileName: String) {
        this.fileName = fileName
    }
}