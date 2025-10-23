package com.seventhmoon.advertiseclient.data.defaultData

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

class DefaultDataMarquee(marqueeId: Int, name: String, content: String) {

    private var marqueeId : Int = 0
    private var name: String = ""
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