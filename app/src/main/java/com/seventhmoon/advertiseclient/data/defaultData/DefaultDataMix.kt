package com.seventhmoon.advertiseclient.data.defaultData

class DefaultDataMix(fileName: String) {
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
}