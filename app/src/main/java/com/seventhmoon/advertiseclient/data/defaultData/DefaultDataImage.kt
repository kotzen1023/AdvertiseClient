package com.seventhmoon.advertiseclient.data.defaultData

import androidx.room.ColumnInfo

class DefaultDataImage(fileName: String) {
    private var id: Int = 0
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

    fun setFileName(fileName : String) {
        this.fileName = fileName
    }
}