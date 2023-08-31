package com.seventhmoon.advertiseclient.persistence

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = DefaultPlayMarqueeData.TABLE_NAME)
class DefaultPlayMarqueeData(marqueeId: Int, name: String, content: String) {
    companion object {
        const val TABLE_NAME = "DefaultPlayMarqueeData"
    }

    @NonNull
    @PrimaryKey(autoGenerate = false)
    private var marqueeId : Int = 0

    @ColumnInfo(name = "name")
    private var name: String = ""

    @ColumnInfo(name = "content")
    private var content: String = ""

    init {
        this.marqueeId = marqueeId
        this.name = name
        this.content = content
    }

    fun getMarqueeId(): Int {
        return marqueeId
    }

    fun setMarqueeId(marqueeId : Int) {
        this.marqueeId = marqueeId
    }

    fun getName(): String {
        return name
    }

    fun setName(name : String) {
        this.name = name
    }

    fun getContent(): String {
        return content
    }

    fun setContent(content : String) {
        this.content = content
    }
}