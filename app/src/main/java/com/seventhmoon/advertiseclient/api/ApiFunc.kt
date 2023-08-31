package com.seventhmoon.advertiseclient.api

import android.util.Log
import com.seventhmoon.advertiseclient.MainActivity.Companion.base_ip_address_webservice
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ApiFunc {

    private val mTAG = ApiFunc::class.java.name
    //private val baseIP = "http://192.168.0.253:3000/"
    private val baseIP = base_ip_address_webservice

    private val apiPing = baseIP + "ping"

    private val apiAdtest = baseIP + "adtest"

    private val apiGetLayout = baseIP + "getLayout"

    private val apiGetAdSetting = baseIP + "getAdSetting"

    private val apiGetMarquee = baseIP + "getMarquee"

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private object ContentType {

        const val title = "Content-Type"
        const val xxxForm = "application/x-www-form-urlencoded"
        const val json = "application/json"
    }//ContentType

    fun getServerPingResponse(jsonObject: JSONObject, callback: Callback) {
        Log.e(mTAG, "ApiFunc->getServerPingResponse")
        postWithParaPJsonStr(apiPing, jsonObject, callback)

    }

    fun getServerAdvertise(jsonObject: JSONObject, callback: Callback) {
        Log.e(mTAG, "ApiFunc->getServerAdvertise")
        postWithParaPJsonStr(apiAdtest, jsonObject, callback)

    }

    fun getLayout(jsonObject: JSONObject, callback: Callback) {
        Log.e(mTAG, "ApiFunc->getLayout")
        postWithParaPJsonStr(apiGetLayout, jsonObject, callback)

    }

    fun getAdSetting(jsonObject: JSONObject, callback: Callback) {
        Log.e(mTAG, "ApiFunc->getAdSetting")
        postWithParaPJsonStr(apiGetAdSetting, jsonObject, callback)

    }

    fun getMarquee(callback: Callback) {
        Log.e(mTAG, "ApiFunc->getMarquee")
        getWithoutPJsonStr(apiGetMarquee, callback)

    }

    private fun getWithoutPJsonStr(url: String, callback: Callback) {
        Log.e(mTAG, "->getWithoutPJsonStr")

        val client = OkHttpClient().newBuilder()
            .retryOnConnectionFailure(false)
            .build()


        val request: Request = Request.Builder()
            .url(url)
            .build()

        try {
            val response = client.newCall(request).enqueue(callback)
            Log.d("getWithoutPJsonStr", "response = $response")

            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            client.cache?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun postWithParaPJsonStr(url: String, jsonObject: JSONObject, callback: Callback) {
        Log.e(mTAG, "->postWithParaPJsonStr")
        Log.e(mTAG, "send jsonObject = $jsonObject")

        val client = OkHttpClient().newBuilder()
            .retryOnConnectionFailure(false)
            .build()
        val body = jsonObject.toString().toRequestBody(mediaType)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        /*var response: Response? = null
        try {
            response = client.newCall(request).execute()
            val resStr = response.body!!.string()
            Log.e(mTAG, "resStr = $resStr")
        } catch (e: IOException) {
            e.printStackTrace()
        }*/
        try {
            val response = client.newCall(request).enqueue(callback)
            Log.d("postWithParaPJsonStr", "response = $response")

            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            client.cache?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }





        /*val body = FormBody.Builder()
            .add("p_json", jsonStr)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader(ContentType.title, ContentType.json)
            .build()

        val client = OkHttpClient().newBuilder()
            .retryOnConnectionFailure(false)
            .build()

        try {
            val response = client.newCall(request).enqueue(callback)
            Log.d("postWithParaPJsonStr", "response = $response")

            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            client.cache?.close()

        } catch (e: IOException) {



            e.printStackTrace()
        }
        */

    }
}