package com.seventhmoon.advertiseclient.model.recv

class ReceiveTransform {
    inner class RecvAdvertiseList {
        var dataList = ArrayList<RecvAdvertise>()
    }

    companion object {
        //private val mTAG = ReceiveTransform::class.java.name
        //const val arrField : String = "dataList"
        fun addToJsonArrayStr(str: String): String {

            return "{\"dataList\":$str}"
        }
    }
}