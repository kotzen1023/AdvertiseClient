package com.seventhmoon.advertiseclient


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.ParseException
import androidx.core.text.HtmlCompat
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.seventhmoon.advertiseclient.api.ApiFunc
import com.seventhmoon.advertiseclient.data.Constants
import com.seventhmoon.advertiseclient.model.recv.RecvAdSetting
import com.seventhmoon.advertiseclient.model.recv.RecvLayout
import com.seventhmoon.advertiseclient.model.recv.RecvMarquee
import com.seventhmoon.advertiseclient.persistence.DefaultPlayAdSettingData
import com.seventhmoon.advertiseclient.persistence.DefaultPlayAdSettingDataDB
import com.seventhmoon.advertiseclient.persistence.DefaultPlayImagesData
import com.seventhmoon.advertiseclient.persistence.DefaultPlayImagesDataDB
import com.seventhmoon.advertiseclient.persistence.DefaultPlayLayoutData
import com.seventhmoon.advertiseclient.persistence.DefaultPlayLayoutDataDB
import com.seventhmoon.advertiseclient.persistence.DefaultPlayMarqueeData
import com.seventhmoon.advertiseclient.persistence.DefaultPlayMarqueeDataDB
import com.seventhmoon.advertiseclient.persistence.DefaultPlayVideosData
import com.seventhmoon.advertiseclient.persistence.DefaultPlayVideosDataDB
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private val mTag = MainActivity::class.java.name
    var mContext: Context? = null

    private val requestIdMultiplePermission = 1
    private var pref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private val fileName = "Preference"

    var layoutList: ArrayList<RecvLayout> = ArrayList()

    private var marqueeList : ArrayList<RecvMarquee> = ArrayList() // for text marquee
    private var playMarqueeList : ArrayList<RecvMarquee> = ArrayList() // for text marquee
    private var imageList : ArrayList<String> = ArrayList() // for image
    private var videoList : ArrayList<String> = ArrayList() // for video
    private var adSettingList : ArrayList<RecvAdSetting> = ArrayList()

    private var mReceiver: BroadcastReceiver? = null
    private var isRegister = false
    private var currentTextIndexTop = -1
    private var currentImageIndexTop = -1
    private var currentVideoIndexTop = -1
    private var currentTextIndexCenter = -1
    private var currentImageIndexCenter = -1
    private var currentVideoIndexCenter = -1
    private var currentTextIndexBottom = -1
    private var currentImageIndexBottom = -1
    private var currentVideoIndexBottom = -1

    private var rootView: ViewGroup? = null
    //main Linearlayout
    private var mainLinearLayout : LinearLayout ?= null
    //top
    private var linearLayoutTop : LinearLayout ?= null
    private var textViewTop : TextView ?= null

    private var imageViewTop : ImageView ?= null
    private var imageViewTop2 : ImageView ?= null
    private var videoViewLayoutTop: RelativeLayout ?= null
    private var videoViewTop: VideoView ?= null
    //private var exoPlayerViewTop: PlayerView?= null
    //center
    private var linearLayoutCenter : LinearLayout ?= null
    var textViewCenter : TextView ?=     null
    var imageViewCenter : ImageView ?= null
    var imageViewCenter2 : ImageView ?= null
    private var videoViewLayoutCenter: RelativeLayout ?= null
    private var videoViewCenter: VideoView ?= null
    //private var exoPlayerViewCenter: PlayerView?= null
    //bottom
    private var linearLayoutBottom : LinearLayout ?= null
    var textViewBottom : TextView ?= null
    var imageViewBottom : ImageView ?= null
    var imageViewBottom2 : ImageView ?= null
    private var videoViewLayoutBottom: RelativeLayout ?= null
    private var videoViewBottom: VideoView ?= null
    //private var exoPlayerViewBottom: PlayerView?= null
    private var linearLayoutTriangle : LinearLayout ?= null

    //layout weight
    private var linearLayoutTriangleWeight : Float = 2.0F
    private var linearLayoutTopWeight: Float = 2.0F
    private var linearLayoutCenterWeight: Float = 2.0F
    private var linearLayoutBottomWeight: Float = 2.0F

    private var mediaControllerTop: MediaController? = null
    private var mediaControllerCenter: MediaController? = null
    private var mediaControllerBottom: MediaController? = null

    private var deviceID = ""
    private var deviceName = ""
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    var getFirstPingResponse = false


    private var toastHandle: Toast? = null

    private lateinit var countDownTimerMarquee : CountDownTimer
    var countDownTimerMarqueeRunning : Boolean = false
    private lateinit var countDownTimerImage : CountDownTimer
    var countDownTimerImageRunning : Boolean = false

    private var videoRunningTop : Boolean = false
    private var videoRunningCenter : Boolean = false
    private var videoRunningBottom : Boolean = false

    private var currentOrientation = 0
    companion object {
        @JvmStatic var server_ip_address: String = ""
        @JvmStatic var server_webservice_port: String = ""
        @JvmStatic var base_ip_address_webservice: String = ""

        @JvmStatic var server_images_folder: String = ""
        @JvmStatic var server_videos_folder: String = ""

        @JvmStatic var dest_images_folder: String = ""
        @JvmStatic var dest_videos_folder: String = ""
    }

    private val handler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            // length may be negative because it is based on http header
            val (progress, length) = msg.obj as Pair<*, *>
            //Log.d(mTag, "progress = $progress, length = $length")
        }
    }

    private var defaultPlayLayoutDataDB: DefaultPlayLayoutDataDB? = null
    private var defaultPlayAdSettingDataDB: DefaultPlayAdSettingDataDB? = null
    private var defaultPlayMarqueeDataDB: DefaultPlayMarqueeDataDB? = null
    private var defaultPlayImagesDataDB: DefaultPlayImagesDataDB? = null
    private var defaultPlayVideosDataDB: DefaultPlayVideosDataDB? = null

    private var defaultLayoutPlayList: ArrayList<DefaultPlayLayoutData> ?= ArrayList()
    private var defaultAdSettingPlayList: ArrayList<DefaultPlayAdSettingData> ?= ArrayList()
    private var defaultMarqueePlayList: ArrayList<DefaultPlayMarqueeData> ?= ArrayList()
    private var defaultImagesPlayList: ArrayList<DefaultPlayImagesData> ?= ArrayList()
    private var defaultVideosPlayList: ArrayList<DefaultPlayVideosData> ?= ArrayList()


    private var downloadImageComplete: Int = 0
    private var downloadVideoComplete: Int = 0
    private var downloadImageReadyArray: ArrayList<Boolean> = ArrayList()
    private var downloadVideoReadyArray: ArrayList<Boolean> = ArrayList()

    private var infoRenew = false
    private var isFirstNetworkError = true

    var pingCount: Int = 0
    //for Log
    private var process: Process? = null
    private var debugLog: Boolean = true

    private var planStartTime : String = "--:--"
    private var plan2StartTime : String = "--:--"
    private var plan3StartTime : String = "--:--"
    private var plan4StartTime : String = "--:--"

    private var currentPlanId: Int = 0

    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootView = findViewById<View>(android.R.id.content) as ViewGroup

        mContext = applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions()
        } else {
            //create default folder
            Log.d(mTag, "create default folder directly!!")
            dest_images_folder = Environment.getExternalStorageDirectory().toString() + "/Download/images/"
            dest_videos_folder = Environment.getExternalStorageDirectory().toString() + "/Download/videos/"
            val imagesDir = File(dest_images_folder)
            imagesDir.mkdirs()
            val videoDir = File(dest_videos_folder)
            videoDir.mkdirs()
            if (debugLog) {
                initLog()
            }
        }

        @Suppress("DEPRECATION")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController

            if(controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        pref = getSharedPreferences(fileName, MODE_PRIVATE)
        server_ip_address = pref!!.getString("SERVER_IP_ADDRESS", "") as String
        server_webservice_port = pref!!.getString("SERVER_WEBSERVICE_PORT", "") as String

        Log.d(mTag, "server_ip_address = $server_ip_address")
        Log.d(mTag, "server_webservice_port = $server_webservice_port")

        if (server_ip_address != "" && server_webservice_port != "") {
            server_images_folder = "$server_ip_address:$server_webservice_port/uploads/images"
            server_videos_folder = "$server_ip_address:$server_webservice_port/uploads/videos"
        }

        //get screen width and height
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
        {
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            screenHeight = displayMetrics.heightPixels
            screenWidth = displayMetrics.widthPixels
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            mContext!!.display!!.getRealMetrics(displayMetrics)

            screenHeight = displayMetrics.heightPixels
            screenWidth = displayMetrics.widthPixels
        } else { //Android 11
            //mContext!!.display!!.getMetrics(displayMetrics)
            screenHeight = windowManager.currentWindowMetrics.bounds.height()
            screenWidth = windowManager.currentWindowMetrics.bounds.width()

        }

        currentOrientation = this.resources.configuration.orientation
        Log.d(mTag, "currentOrientation = $currentOrientation")

        deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        deviceName = Build.MODEL

        //Log.d(mTag, "macAddress = $macAddress")
        Log.d(mTag, "deviceID = $deviceID")
        Log.d(mTag, "deviceName = $deviceName")
        Log.d(mTag, "width = $screenWidth, height = $screenHeight")

        //read from db as default
        val migration12 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //database.execSQL("ALTER TABLE '"+History.TABLE_NAME+"' ADD COLUMN 'timeStamp' LONG NOT NULL DEFAULT 0")
            }
        }

        //load layout from DB
        defaultPlayLayoutDataDB = Room.databaseBuilder(mContext as Context, DefaultPlayLayoutDataDB::class.java, DefaultPlayLayoutDataDB.DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(migration12)
            .build()
        defaultLayoutPlayList = defaultPlayLayoutDataDB!!.defaultPlayLayoutDataDao().getAll() as ArrayList<DefaultPlayLayoutData>
        Log.d(mTag, "defaultLayoutPlayList = ${defaultLayoutPlayList!!.size}")

        if (defaultLayoutPlayList!!.size > 0) {
            layoutList.clear()
            val receiveLayout = RecvLayout()
            receiveLayout.layout_id = defaultLayoutPlayList!![0].getLayout_id()
            receiveLayout.screenWidth = defaultLayoutPlayList!![0].getScreenWidth()
            receiveLayout.screenHeight = defaultLayoutPlayList!![0].getScreenHeight()
            receiveLayout.orientation = defaultLayoutPlayList!![0].getOrientation()
            receiveLayout.layout_top = defaultLayoutPlayList!![0].getLayout_top()
            receiveLayout.layout_center = defaultLayoutPlayList!![0].getLayout_center()
            receiveLayout.layout_bottom = defaultLayoutPlayList!![0].getLayout_bottom()
            receiveLayout.layoutOrientation = defaultLayoutPlayList!![0].getLayoutOrientation()
            receiveLayout.plan_id = defaultLayoutPlayList!![0].getPlan_id()
            receiveLayout.plan2_id = defaultLayoutPlayList!![0].getPlan2_id()
            receiveLayout.plan3_id = defaultLayoutPlayList!![0].getPlan3_id()
            receiveLayout.plan4_id = defaultLayoutPlayList!![0].getPlan4_id()
            receiveLayout.plan_start_time = defaultLayoutPlayList!![0].getPlan_start_time()
            receiveLayout.plan2_start_time = defaultLayoutPlayList!![0].getPlan2_start_time()
            receiveLayout.plan3_start_time = defaultLayoutPlayList!![0].getPlan3_start_time()
            receiveLayout.plan4_start_time = defaultLayoutPlayList!![0].getPlan4_start_time()

            planStartTime = layoutList[0].plan_start_time
            plan2StartTime = layoutList[0].plan2_start_time
            plan3StartTime = layoutList[0].plan3_start_time
            plan4StartTime = layoutList[0].plan4_start_time

            layoutList.add(receiveLayout)
        }

        //load AdSetting from DB
        defaultPlayAdSettingDataDB = Room.databaseBuilder(mContext as Context, DefaultPlayAdSettingDataDB::class.java, DefaultPlayAdSettingDataDB.DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(migration12)
            .build()
        defaultAdSettingPlayList = defaultPlayAdSettingDataDB!!.defaultPlayAdSettingDataDao().getAll() as ArrayList<DefaultPlayAdSettingData>
        Log.d(mTag, "defaultAdSettingPlayList = ${defaultAdSettingPlayList!!.size}")

        if (defaultAdSettingPlayList!!.size > 0) {
            adSettingList.clear()
            for (i in defaultAdSettingPlayList!!.indices) {
                val adSetting = RecvAdSetting()
                adSetting.plan_id = defaultAdSettingPlayList!![i].getPlan_id()
                adSetting.plan_name = defaultAdSettingPlayList!![i].getPlan_name()
                adSetting.plan_marquee = defaultAdSettingPlayList!![i].getPlan_marquee()
                adSetting.plan_images = defaultAdSettingPlayList!![i].getPlan_images()
                adSetting.plan_videos = defaultAdSettingPlayList!![i].getPlan_videos()
                adSetting.marquee_mode = defaultAdSettingPlayList!![i].getMarquee_mode()
                adSetting.images_mode = defaultAdSettingPlayList!![i].getImages_mode()
                adSetting.videos_mode = defaultAdSettingPlayList!![i].getVideos_mode()
                adSetting.image_interval = defaultAdSettingPlayList!![i].getImage_interval()

                adSettingList.add(adSetting)
            }

        }

        //load marquee from DB
        defaultPlayMarqueeDataDB = Room.databaseBuilder(mContext as Context, DefaultPlayMarqueeDataDB::class.java, DefaultPlayMarqueeDataDB.DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(migration12)
            .build()
        defaultMarqueePlayList = defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().getAll() as ArrayList<DefaultPlayMarqueeData>
        Log.d(mTag, "defaultMarqueePlayList = ${defaultMarqueePlayList!!.size}")

        if (defaultMarqueePlayList!!.size > 0) {
            playMarqueeList.clear()

            for (i in defaultMarqueePlayList!!.indices) {
                val marquee = RecvMarquee()
                marquee.marqueeId = defaultMarqueePlayList!![i].getMarqueeId()
                marquee.name = defaultMarqueePlayList!![i].getName()
                marquee.content = defaultMarqueePlayList!![i].getContent()
                playMarqueeList.add(marquee)
            }
        }

        //load images from DB
        defaultPlayImagesDataDB = Room.databaseBuilder(mContext as Context, DefaultPlayImagesDataDB::class.java, DefaultPlayImagesDataDB.DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(migration12)
            .build()
        defaultImagesPlayList = defaultPlayImagesDataDB!!.defaultPlayImagesDataDao().getAll() as ArrayList<DefaultPlayImagesData>
        Log.d(mTag, "defaultImagesPlayList = ${defaultImagesPlayList!!.size}")

        if (defaultImagesPlayList!!.size > 0) {
            imageList.clear()
            downloadImageReadyArray.clear()
            for (i in defaultImagesPlayList!!.indices) {
                imageList.add(defaultImagesPlayList!![i].getFileName())

                val destPath = "$dest_images_folder${defaultImagesPlayList!![i].getFileName()}"
                Log.d(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(!file.exists()) {
                    downloadImageReadyArray.add(false)
                } else {
                    downloadImageReadyArray.add(true)
                }
            }
        }

        //load video from DB
        defaultPlayVideosDataDB = Room.databaseBuilder(mContext as Context, DefaultPlayVideosDataDB::class.java, DefaultPlayVideosDataDB.DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(migration12)
            .build()
        defaultVideosPlayList = defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().getAll() as ArrayList<DefaultPlayVideosData>
        Log.d(mTag, "defaultVideosPlayList = ${defaultVideosPlayList!!.size}")

        if (defaultVideosPlayList!!.size > 0) {
            videoList.clear()
            downloadVideoReadyArray.clear()
            for (i in defaultVideosPlayList!!.indices) {
                videoList.add(defaultVideosPlayList!![i].getFileName())

                val destPath = "$dest_videos_folder${defaultVideosPlayList!![i].getFileName()}"
                Log.d(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(!file.exists()) {
                    downloadVideoReadyArray.add(false)
                } else {
                    downloadVideoReadyArray.add(true)
                }


            }
        }

        if (layoutList.size == 1 && adSettingList.size == 1 &&
            (playMarqueeList.size > 0 || imageList.size > 0 || videoList.size > 0 )) {
            //because marquee download is unnecessary, start with image
            //downloadImageComplete = 0
            checkImagesExists()
            clearImagesNotInImageList()
            downloadImages()
            infoRenew = true

        } else {
            Log.d(mTag, "no default setting")
        }

        if (server_ip_address == "" || server_webservice_port == "") {
            showInputServerAddressDialog()
        } else {
            base_ip_address_webservice = "$server_ip_address:$server_webservice_port/"

            pingWeb()
        }

        getTimeStampFromString(planStartTime)

        val filter: IntentFilter
        @SuppressLint("CommitPrefEdits")
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null) {
                    if (intent.action!!.equals(Constants.ACTION.ACTION_GET_ADVERTISES, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_ADVERTISES")

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SHOW_DIALOG_AGAIN, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_SHOW_DIALOG_AGAIN")
                        showInputServerAddressDialog()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_TEST_IP_AND_PORT, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_TEST_IP_AND_PORT")

                        base_ip_address_webservice = "$server_ip_address:$server_webservice_port/"

                        Log.d(mTag, "base_ip_address_webservice = $base_ip_address_webservice")

                        pingWeb()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_PING_WEB, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_PING_WEB")

                        base_ip_address_webservice = "$server_ip_address:$server_webservice_port/"

                        Log.d(mTag, "base_ip_address_webservice = $base_ip_address_webservice")

                        pingWeb()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_PING_WEB_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_PING_WEB_FAILED")

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_PING_WEB_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_PING_WEB_SUCCESS")

                        val getLayoutIntent = Intent()
                        getLayoutIntent.action = Constants.ACTION.ACTION_GET_LAYOUT
                        mContext?.sendBroadcast(getLayoutIntent)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_LAYOUT, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_LAYOUT")

                        getLayout()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_LAYOUT_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_LAYOUT_FAILED")



                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_LAYOUT_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_LAYOUT_SUCCESS")

                        val planId = intent.getIntExtra("PLAN_ID", 0)
                        val plan2Id = intent.getIntExtra("PLAN2_ID", 0)
                        val plan3Id = intent.getIntExtra("PLAN3_ID", 0)
                        val plan4Id = intent.getIntExtra("PLAN4_ID", 0)

                        Log.d(mTag, "planId = $planId")
                        Log.d(mTag, "plan2Id = $plan2Id")
                        Log.d(mTag, "plan3Id = $plan3Id")
                        Log.d(mTag, "plan4Id = $plan4Id")

                        if (planId > 0 || plan2Id > 0 || plan3Id > 0 || plan4Id > 0) {
                            getAdSetting(planId, plan2Id, plan3Id, plan4Id)
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_AD_SETTING_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_AD_SETTING_FAILED")

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_AD_SETTING_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_AD_SETTING_SUCCESS")

                        if (adSettingList.size > 0) {
                            //get Marquee
                            getMarquee()
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_AD_SETTING_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_AD_SETTING_EMPTY")


                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MARQUEE_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MARQUEE_FAILED")
                        /*
                        playMarqueeList.clear()
                        defaultMarqueePlayList!!.clear()
                        //clear sqlite table
                        defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().clearTable()

                        //ignore error, start download images
                        val imagesArray = adSettingList[0].plan_images.split(",")

                        if (imagesArray.isNotEmpty()) {
                            imageList.clear()
                            downloadImageReadyArray.clear()
                            for (i in imagesArray.indices) {
                                imageList.add(imagesArray[i])
                                downloadImageReadyArray.add(false)
                            }

                            Log.d(mTag, "imageList = $imageList, downloadImageReadyArray = $downloadImageReadyArray")
                            downloadImageComplete = 0
                            downloadImages()
                        } else { //no images, start download videos
                            downloadVideoComplete = 0
                            downloadVideos()
                        }
                        */
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MARQUEE_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MARQUEE_EMPTY")
                        playMarqueeList.clear()
                        defaultMarqueePlayList!!.clear()
                        //clear sqlite table
                        defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().clearTable()

                        //no marquee, start download images
                        val imagesArray = adSettingList[0].plan_images.split(",")

                        if (imagesArray.isNotEmpty()) {
                            imageList.clear()
                            downloadImageReadyArray.clear()
                            for (i in imagesArray.indices) {
                                imageList.add(imagesArray[i])
                                downloadImageReadyArray.add(false)
                            }

                            Log.d(mTag, "imageList = $imageList, downloadImageReadyArray = $downloadImageReadyArray")
                            //downloadImageComplete = 0
                            checkImagesExists()
                            clearImagesNotInImageList()
                            downloadImages()
                        } else { //no images, start download videos
                            //downloadVideoComplete = 0
                            checkVideosExists()
                            clearVideosNotInVideoList()
                            downloadVideos()
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MARQUEE_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MARQUEE_SUCCESS")

                        playMarqueeList.clear()
                        defaultMarqueePlayList!!.clear()
                        defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().clearTable()
                        //then find match marquee
                        if (adSettingList.size > 0) {
                            Log.d(mTag, "plan_marquee = ${adSettingList[0].plan_marquee}")

                            if (adSettingList[0].plan_marquee.isNotEmpty()) {
                                val marqueeArray = adSettingList[0].plan_marquee.split(",")
                                Log.d(mTag, "marqueeArray.size = ${marqueeArray.size}")

                                for (i in marqueeArray.indices) {
                                    var found = false
                                    var foundIdx = -1
                                    for (j in marqueeList.indices) {
                                        if (marqueeArray[i] == marqueeList[j].name) {
                                            found = true
                                            foundIdx = j
                                            break
                                        }
                                    }

                                    if (found) {
                                        playMarqueeList.add(marqueeList[foundIdx])
                                    }
                                }

                                Log.d(mTag, "playMarqueeList = $playMarqueeList")
                                //write db

                                //clear before add
                                if (playMarqueeList.size > 0) {
                                    for (i in playMarqueeList.indices) {
                                        val defaultPlayMarqueeData = DefaultPlayMarqueeData(
                                            playMarqueeList[i].marqueeId, playMarqueeList[i].name, playMarqueeList[i].content)
                                        defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().insert(defaultPlayMarqueeData)
                                    }
                                    defaultMarqueePlayList = defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().getAll() as ArrayList<DefaultPlayMarqueeData>
                                    Log.d(mTag, "defaultMarqueePlayList.size = ${defaultMarqueePlayList!!.size}")
                                }
                            }

                            //then download images
                            imageList.clear()
                            downloadImageReadyArray.clear()
                            if (adSettingList[0].plan_images.isNotEmpty()) {
                                val imagesArray = adSettingList[0].plan_images.split(",")

                                if (imagesArray.isNotEmpty()) {

                                    for (i in imagesArray.indices) {
                                        imageList.add(imagesArray[i])
                                        downloadImageReadyArray.add(false)
                                    }

                                    Log.d(mTag, "imageList = $imageList, downloadImageReadyArray = $downloadImageReadyArray")
                                    //downloadImageComplete = 0
                                    checkImagesExists()
                                    clearImagesNotInImageList()
                                    downloadImages()
                                } else { //no images, start download videos
                                    defaultImagesPlayList!!.clear()
                                    defaultPlayImagesDataDB!!.defaultPlayImagesDataDao().clearTable()
                                    //downloadVideoComplete = 0
                                    checkVideosExists()
                                    clearVideosNotInVideoList()
                                    downloadVideos()
                                }
                            } else {
                                Log.d(mTag, "adSettingList[0].plan_images.isEmpty")
                                defaultImagesPlayList!!.clear()
                                defaultPlayImagesDataDB!!.defaultPlayImagesDataDao().clearTable()



                                //then download videos
                                Log.d(mTag, "Download Images complete, then download videos.")
                                Log.d(mTag, "adSettingList[0].plan_videos.length = ${adSettingList[0].plan_videos.length}")
                                if (adSettingList[0].plan_videos.isNotEmpty()) {
                                    val videosArray = adSettingList[0].plan_videos.split(",")

                                    if (videosArray.isNotEmpty()) {
                                        videoList.clear()
                                        downloadVideoReadyArray.clear()
                                        for (i in videosArray.indices) {
                                            videoList.add(videosArray[i])
                                            downloadVideoReadyArray.add(false)
                                        }

                                        Log.d(mTag, "videoList = $videoList, downloadVideoReadyArray = $downloadVideoReadyArray")


                                        //then download videos
                                        //downloadVideoComplete = 0
                                        checkVideosExists()
                                        clearVideosNotInVideoList()
                                        downloadVideos()
                                    } else { //no videos, start ads
                                        defaultVideosPlayList!!.clear()
                                        defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().clearTable()

                                        //start to play
                                        if (infoRenew) {
                                            Log.d(mTag, "start to play!")
                                            playAd()
                                        }
                                    }
                                } else {
                                    Log.d(mTag, "adSettingList[0].plan_videos.isEmpty()")
                                    videoList.clear()
                                    defaultVideosPlayList!!.clear()
                                    defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().clearTable()

                                    //start to play
                                    if (infoRenew) {
                                        Log.d(mTag, "start to play!")
                                        playAd()
                                    }
                                }
                            }

                        } else {
                            Log.d(mTag, "No AdSetting")
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_IMAGES_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_IMAGES_FAILED")
                        if (downloadImageComplete < imageList.size) {
                            downloadImages()
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_IMAGES_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_IMAGES_SUCCESS")

                        if (downloadImageComplete < imageList.size) {
                            downloadImages()
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_IMAGES_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_IMAGES_EMPTY")

                        imageList.clear()
                        defaultImagesPlayList!!.clear()
                        defaultPlayImagesDataDB!!.defaultPlayImagesDataDao().clearTable()

                        //then download videos
                        Log.d(mTag, "Download Images complete, then download videos.")

                        if (adSettingList[0].plan_videos.isNotEmpty()) {
                            val videosArray = adSettingList[0].plan_videos.split(",")

                            if (videosArray.isNotEmpty()) {
                                videoList.clear()
                                downloadVideoReadyArray.clear()
                                for (i in videosArray.indices) {
                                    videoList.add(videosArray[i])
                                    downloadVideoReadyArray.add(false)
                                }

                                Log.d(mTag, "videoList = $videoList, downloadVideoReadyArray = $downloadVideoReadyArray")


                                //then download videos
                                //downloadVideoComplete = 0
                                checkVideosExists()
                                clearVideosNotInVideoList()
                                downloadVideos()
                            } else { //no videos, start ads
                                defaultVideosPlayList!!.clear()
                                defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().clearTable()

                                //start to play
                                if (infoRenew) {
                                    Log.d(mTag, "start to play!")
                                    playAd()
                                }
                            }
                        } else {
                            Log.d(mTag, "adSettingList[0].plan_videos.isEmpty()")
                            videoList.clear()
                            defaultVideosPlayList!!.clear()
                            defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().clearTable()

                            //start to play
                            if (infoRenew) {
                                Log.d(mTag, "start to play!")
                                playAd()
                            }
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_IMAGES_COMPLETE, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_IMAGES_COMPLETE")

                        if (imageList.size > 0) {
                            //clear before add
                            defaultPlayImagesDataDB!!.defaultPlayImagesDataDao().clearTable()
                            for (i in imageList.indices) {
                                val defaultPlayImagesData = DefaultPlayImagesData(imageList[i])
                                defaultPlayImagesDataDB!!.defaultPlayImagesDataDao().insert(defaultPlayImagesData)
                            }
                            defaultImagesPlayList!!.clear()
                            defaultImagesPlayList = defaultPlayImagesDataDB!!.defaultPlayImagesDataDao().getAll() as ArrayList<DefaultPlayImagesData>
                            Log.d(mTag, "defaultImagesPlayList.size = ${defaultImagesPlayList!!.size}")
                        }

                        //then download videos
                        Log.d(mTag, "Download Images complete, then download videos.")
                        if (adSettingList[0].plan_videos.isNotEmpty()) {
                            val videosArray = adSettingList[0].plan_videos.split(",")
                            videoList.clear()
                            downloadVideoReadyArray.clear()
                            for (i in videosArray.indices) {
                                videoList.add(videosArray[i])
                                downloadVideoReadyArray.add(false)
                            }

                            Log.d(mTag, "videoList = $videoList, downloadVideoReadyArray = $downloadVideoReadyArray")


                            //then download videos
                            //downloadVideoComplete = 0
                            checkVideosExists()
                            clearVideosNotInVideoList()
                            downloadVideos()
                        } else {
                            Log.d(mTag, "adSettingList[0].plan_videos.isEmpty()")
                            videoList.clear()
                            defaultVideosPlayList!!.clear()
                            defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().clearTable()

                            //start to play
                            if (infoRenew) {
                                Log.d(mTag, "start to play!")
                                playAd()
                            }
                        }


                    }  else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_VIDEOS_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_VIDEOS_FAILED")

                        if (checkDownloadVideosAll()) {
                            Log.d(mTag, "ok, there might be some files can't download, but fine, just play!")
                            if (infoRenew) {
                                Log.d(mTag, "start to play!")
                                playAd()
                            }
                        }

                        if (downloadVideoComplete < videoList.size) {
                            downloadVideos()
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_VIDEOS_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_VIDEOS_SUCCESS")
                        //val fileName = intent.getStringExtra("fileName")
                        //val idx = intent.getStringExtra("fileName")

                        if (checkDownloadVideosAll()) {
                            Log.d(mTag, "ok, there might be some files can't download, but fine, just play!")
                            if (infoRenew) {
                                Log.d(mTag, "start to play!")
                                playAd()
                            }
                        }

                        /*if (isFirstVideoDownload) {
                            isFirstVideoDownload = false
                            //start to play
                            if (infoRenew) {
                                Log.d(mTag, "start to play!")
                                playAd()
                            }
                        }*/

                        if (downloadVideoComplete < videoList.size) {
                            downloadVideos()
                        }
                        /*


                        Log.d(mTag, "fileName: $fileName download complete!")
                        Log.d(mTag, "downloadVideoReadyArray = $downloadVideoReadyArray")

                        if (downloadVideoComplete == videoList.size) {
                            if (videoList.size > 0) {

                                //clear before add
                                defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().clearTable()
                                for (i in videoList.indices) {
                                    val defaultPlayVideosData = DefaultPlayVideosData(videoList[i])
                                    defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().insert(defaultPlayVideosData)
                                }
                                defaultVideosPlayList!!.clear()
                                defaultVideosPlayList = defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().getAll() as ArrayList<DefaultPlayVideosData>
                                Log.d(mTag, "defaultVideosPlayList.size = ${defaultVideosPlayList!!.size}")
                            }

                            //start to play
                            if (infoRenew) {
                                Log.d(mTag, "start to play!")
                                playAd()
                            }

                        } else {
                            Log.d(mTag, "Not Yet")
                        }
                        */

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_VIDEOS_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_VIDEOS_EMPTY")
                        videoList.clear()
                        defaultVideosPlayList!!.clear()

                        defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().clearTable()

                        //start to play
                        if (infoRenew) {
                            Log.d(mTag, "start to play!")
                            playAd()
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_VIDEOS_COMPLETE, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_VIDEOS_COMPLETE")

                        if (downloadVideoComplete == videoList.size) {
                            if (videoList.size > 0) {

                                //clear before add
                                defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().clearTable()
                                for (i in videoList.indices) {
                                    val defaultPlayVideosData = DefaultPlayVideosData(videoList[i])
                                    defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().insert(defaultPlayVideosData)
                                }
                                defaultVideosPlayList!!.clear()
                                defaultVideosPlayList = defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().getAll() as ArrayList<DefaultPlayVideosData>
                                Log.d(mTag, "defaultVideosPlayList.size = ${defaultVideosPlayList!!.size}")
                            }

                            //start to play
                            if (infoRenew) {
                                Log.d(mTag, "start to play!")
                                playAd()
                            }

                        } else {
                            Log.d(mTag, "Not Yet")
                        }


                    }
                }
            }
        }

        if (!isRegister) {
            filter = IntentFilter()

            filter.addAction(Constants.ACTION.ACTION_GET_ADVERTISES)
            filter.addAction(Constants.ACTION.ACTION_SHOW_DIALOG_AGAIN)
            filter.addAction(Constants.ACTION.ACTION_TEST_IP_AND_PORT)

            filter.addAction(Constants.ACTION.ACTION_PING_WEB)
            filter.addAction(Constants.ACTION.ACTION_PING_WEB_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_PING_WEB_FAILED)

            filter.addAction(Constants.ACTION.ACTION_GET_LAYOUT)
            filter.addAction(Constants.ACTION.ACTION_GET_LAYOUT_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_LAYOUT_FAILED)

            filter.addAction(Constants.ACTION.ACTION_GET_AD_SETTING_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_AD_SETTING_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GET_AD_SETTING_EMPTY)

            filter.addAction(Constants.ACTION.ACTION_GET_MARQUEE_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_MARQUEE_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GET_MARQUEE_EMPTY)

            filter.addAction(Constants.ACTION.ACTION_GET_IMAGES_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_IMAGES_COMPLETE)
            filter.addAction(Constants.ACTION.ACTION_GET_IMAGES_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GET_IMAGES_EMPTY)

            filter.addAction(Constants.ACTION.ACTION_GET_VIDEOS_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_VIDEOS_COMPLETE)
            filter.addAction(Constants.ACTION.ACTION_GET_VIDEOS_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GET_VIDEOS_EMPTY)
            mContext!!.registerReceiver(mReceiver, filter)
            isRegister = true
            Log.d(mTag, "registerReceiver mReceiver")
        }
    }

    override fun onDestroy() {
        Log.i(mTag, "onDestroy")
        if (isRegister && mReceiver != null) {
            try {
                mContext!!.unregisterReceiver(mReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

            isRegister = false
            mReceiver = null
            Log.d(mTag, "unregisterReceiver mReceiver")
        }

        super.onDestroy()
    }

    private fun pingWeb() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("deviceID", deviceID)
            jsonObject.put("deviceName", deviceName)
            jsonObject.put("screenWidth", screenWidth)
            jsonObject.put("screenHeight", screenHeight)
            jsonObject.put("orientation", currentOrientation)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        ApiFunc().getServerPingResponse(jsonObject, getPingCallback)

        val timer = object : CountDownTimer(60000, 60000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(mTag, "60 second passed...")
            }

            override fun onFinish() { //结束后的操作
                pingCount += 1
                Log.d(mTag, "pingCount = $pingCount")
                ApiFunc().getServerPingResponse(jsonObject, getPingCallback)
                this.start()
            }
        }.start()
    }

    private var getPingCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread(netErrRunnable)

        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.d(mTag, "onResponse : "+response.body.toString())
            //val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            val json = JSONObject(response.body!!.string())
            runOnUiThread {
                try {
                    Log.d(mTag, "getPingCallback json = $json")
                    getCurrentTimeString()
                    if (pingCount >= 1440) {
                        Log.d(mTag, "pingCount >= 1440")
                        pingCount = 0
                        getFirstPingResponse = false
                    }

                    if (json["result"] == 0 ) {
                        if (!getFirstPingResponse) {
                            getFirstPingResponse = true
                            infoRenew = true

                            if (server_ip_address != "" && server_webservice_port != "") {
                                server_images_folder = "$server_ip_address:$server_webservice_port/uploads/images"
                                server_videos_folder = "$server_ip_address:$server_webservice_port/uploads/videos"
                            }

                            //get layout first
                            val successIntent = Intent()
                            successIntent.action = Constants.ACTION.ACTION_PING_WEB_SUCCESS
                            mContext?.sendBroadcast(successIntent)
                            //getLayout()
                        }

                    } else if (json["result"] == 1) {
                        Log.d(mTag, "====>Layout changed.")
                        //orientationChanged = false
                        //getLayout()
                        infoRenew = true
                        val successIntent = Intent()
                        successIntent.action = Constants.ACTION.ACTION_PING_WEB_SUCCESS
                        mContext?.sendBroadcast(successIntent)
                    } else if (json["result"] == -1) {
                        Log.d(mTag, "->no deviceID")
                    }

                } catch (ex: Exception) {

                    Log.e(mTag, "server error")
                    val failedIntent = Intent()
                    failedIntent.action = Constants.ACTION.ACTION_PING_WEB_FAILED
                    mContext?.sendBroadcast(failedIntent)
                }
            }

        }//onResponse
    }

    private fun getLayout() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("deviceID", deviceID)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        ApiFunc().getLayout(jsonObject, getLayoutCallback)
    }

    private var getLayoutCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread(netErrRunnable)

        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.d(mTag, "onResponse : "+response.body.toString())

            //val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            val jsonStr = response.body!!.string()
            Log.d(mTag, "jsonStr = $jsonStr")
            runOnUiThread {
                try {
                    val listType = object : TypeToken<ArrayList<RecvLayout>>() {}.type
                    layoutList.clear()
                    layoutList = Gson().fromJson(jsonStr, listType)

                    Log.d(mTag, "layoutList.size = " + layoutList.size)

                    Log.d(mTag, "layoutList[0].plan_id = ${layoutList[0].plan_id}")




                    if (layoutList.size > 0) {
                        val defaultPlayLayoutData = DefaultPlayLayoutData(layoutList[0].layout_id, layoutList[0].screenWidth,
                            layoutList[0].screenHeight, layoutList[0].orientation,
                            layoutList[0].layout_top, layoutList[0].layout_center,
                            layoutList[0].layout_bottom, layoutList[0].layoutOrientation,
                            layoutList[0].plan_id, layoutList[0].plan2_id, layoutList[0].plan3_id, layoutList[0].plan4_id,
                            layoutList[0].plan_start_time, layoutList[0].plan2_start_time, layoutList[0].plan3_start_time, layoutList[0].plan4_start_time)

                        if (defaultLayoutPlayList!!.size == 0) {
                            defaultPlayLayoutDataDB!!.defaultPlayLayoutDataDao().insert(defaultPlayLayoutData)
                        } else {
                            defaultPlayLayoutDataDB!!.defaultPlayLayoutDataDao().update(defaultPlayLayoutData)
                        }

                        planStartTime = layoutList[0].plan_start_time
                        plan2StartTime = layoutList[0].plan2_start_time
                        plan3StartTime = layoutList[0].plan3_start_time
                        plan4StartTime = layoutList[0].plan4_start_time

                        //defaultLayoutPlayList = defaultPlayLayoutDataDB!!.defaultPlayLayoutDataDao().getAll() as ArrayList<DefaultPlayLayoutData>
                        //Log.d(mTag, "defaultLayoutPlayList = ${defaultLayoutPlayList!!.size}")

                        val successIntent = Intent()
                        successIntent.action = Constants.ACTION.ACTION_GET_LAYOUT_SUCCESS
                        successIntent.putExtra("PLAN_ID", layoutList[0].plan_id)
                        successIntent.putExtra("PLAN2_ID", layoutList[0].plan2_id)
                        successIntent.putExtra("PLAN3_ID", layoutList[0].plan3_id)
                        successIntent.putExtra("PLAN4_ID", layoutList[0].plan4_id)
                        mContext?.sendBroadcast(successIntent)
                    } else {
                        val failedIntent = Intent()
                        failedIntent.action = Constants.ACTION.ACTION_GET_LAYOUT_FAILED
                        mContext?.sendBroadcast(failedIntent)
                    }
                } catch (ex: Exception) {

                    Log.e(mTag, "Server error")
                    val failedIntent = Intent()
                    failedIntent.action = Constants.ACTION.ACTION_GET_LAYOUT_FAILED
                    mContext?.sendBroadcast(failedIntent)
                }
            }

        }//onResponse
    }

    private fun getAdSetting(plan_id: Int, plan2_id: Int, plan3_id: Int, plan4_id: Int) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("plan_id", plan_id)
            jsonObject.put("plan2_id", plan2_id)
            jsonObject.put("plan3_id", plan3_id)
            jsonObject.put("plan4_id", plan4_id)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        ApiFunc().getAdSetting(jsonObject, getAdSettingBack)
    }

    private var getAdSettingBack: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread(netErrRunnable)

        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.d(mTag, "onResponse : "+response.body.toString())

            //val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            val jsonStr = response.body!!.string()
            Log.d(mTag, "jsonStr = $jsonStr")
            runOnUiThread {
                try {
                    adSettingList.clear()
                    val listType = object : TypeToken<ArrayList<RecvAdSetting>>() {}.type
                    adSettingList = Gson().fromJson(jsonStr, listType)

                    Log.d(mTag, "adSettingList.size = " + adSettingList.size)

                    Log.d(mTag, "adSettingList[0].plan_name = ${adSettingList[0].plan_name}")
                    //clear before add
                    defaultPlayAdSettingDataDB!!.defaultPlayAdSettingDataDao().clearTable()

                    if (adSettingList.size > 0) {

                        for (i in adSettingList.indices) {
                            val defaultPlayAdSettingData = DefaultPlayAdSettingData(adSettingList[i].plan_id, adSettingList[i].plan_name,
                                adSettingList[i].plan_marquee, adSettingList[i].plan_images,
                                adSettingList[i].plan_videos, adSettingList[i].marquee_mode,
                                adSettingList[i].images_mode, adSettingList[i].videos_mode,
                                adSettingList[i].image_interval)
                            defaultPlayAdSettingDataDB!!.defaultPlayAdSettingDataDao().insert(defaultPlayAdSettingData)
                            /*if (defaultAdSettingPlayList!!.size == 0) {
                                defaultPlayAdSettingDataDB!!.defaultPlayAdSettingDataDao().insert(defaultPlayAdSettingData)
                            } else {
                                defaultPlayAdSettingDataDB!!.defaultPlayAdSettingDataDao().update(defaultPlayAdSettingData)
                            }*/
                        }




                        val successIntent = Intent()
                        successIntent.action = Constants.ACTION.ACTION_GET_AD_SETTING_SUCCESS
                        mContext?.sendBroadcast(successIntent)
                    } else {
                        val emptyIntent = Intent()
                        emptyIntent.action = Constants.ACTION.ACTION_GET_AD_SETTING_EMPTY
                        mContext?.sendBroadcast(emptyIntent)
                    }
                } catch (ex: Exception) {

                    Log.e(mTag, "Server error")
                    val failedIntent = Intent()
                    failedIntent.action = Constants.ACTION.ACTION_GET_AD_SETTING_FAILED
                    mContext?.sendBroadcast(failedIntent)
                }
            }

        }//onResponse
    }

    private fun getMarquee() {
        /*val jsonObject = JSONObject()
        try {
            jsonObject.put("name", name)

        } catch (e: JSONException) {
            e.printStackTrace()
        }*/

        ApiFunc().getMarquee(getMarqueeCallback)
    }

    private var getMarqueeCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread(netErrRunnable)

        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.d(mTag, "onResponse : "+response.body.toString())
            //val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            //val jsonStr = ReceiveTransform.addToJsonArrayStr(response.body!!.string())
            val jsonStr = response.body!!.string()
            Log.d(mTag, "jsonStr = $jsonStr")
            //val json = JSONObject(response.body!!.string())
            runOnUiThread {
                try {
                    marqueeList.clear()
                    val listType = object : TypeToken<ArrayList<RecvMarquee>>() {}.type

                    marqueeList = Gson().fromJson(jsonStr, listType)

                    Log.d(mTag, "marqueeList.size = " + marqueeList.size)

                    Log.d(mTag, "marqueeList = $marqueeList")
                    //for (i in 0 until advertiseList.size) {
                        //Log.d(mTag, "advertiseList[$i] = ${advertiseList.get(i).ad_path}")
                    //}

                    if (marqueeList.size > 0) {
                        val successIntent = Intent()
                        successIntent.action = Constants.ACTION.ACTION_GET_MARQUEE_SUCCESS
                        mContext?.sendBroadcast(successIntent)
                    } else {
                        val emptyIntent = Intent()
                        emptyIntent.action = Constants.ACTION.ACTION_GET_MARQUEE_EMPTY
                        mContext?.sendBroadcast(emptyIntent)
                    }

                } catch (ex: Exception) {

                    Log.e(mTag, "Server error")
                    val failedIntent = Intent()
                    failedIntent.action = Constants.ACTION.ACTION_GET_MARQUEE_FAILED
                    mContext?.sendBroadcast(failedIntent)
                }
            }

        }//onResponse
    }

    internal var netErrRunnable: Runnable = Runnable {
        Log.e(mTag, "->Network Error")
        if (isFirstNetworkError) {
            isFirstNetworkError = false
            if (adSettingList.size == 1) {
                Log.d(mTag, "playMarqueeList.size = ${playMarqueeList.size}")
                Log.d(mTag, "imageList.size = ${imageList.size}")
                Log.d(mTag, "videoList.size = ${videoList.size}")
                playAd()
            }
        }
    }

    fun downloadImages() {
        Log.d(mTag, "=== downloadImages start ===")

        if (imageList.size > 0) {
            var downloadIdx = -1
            for (i in imageList.indices) {
                val srcPath = server_images_folder
                val destPath = "$dest_images_folder${imageList[i]}"
                Log.d(mTag, "srcPath = $srcPath")
                Log.d(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(!file.exists()) {
                    downloadIdx = i
                    break
                }/* else {
                    Log.d(mTag, "download file exist!")
                    downloadImageComplete += 1
                    downloadImageReadyArray[i] = true
                }*/
            }

            Log.d(mTag, "downloadImageComplete = $downloadImageComplete, downloadImageReadyArray = $downloadImageReadyArray")

            if (downloadIdx >= 0 ) {

                val srcPath = server_images_folder
                val destPath = "$dest_images_folder${imageList[downloadIdx]}"
                Log.d(mTag, "start download file : ${imageList[downloadIdx]} to $dest_images_folder")
                Thread {
                    try {
                        val totalSize = download(srcPath, destPath, imageList[downloadIdx]) { progress, length ->
                            // handling the result on main thread
                            handler.sendMessage(handler.obtainMessage(0, progress to length))
                        }

                        Log.d(mTag, "totalSize = $totalSize")

                        downloadImageComplete += 1
                        downloadImageReadyArray[downloadIdx] = true

                        if (downloadImageComplete == imageList.size) {
                            val completeIntent = Intent()
                            completeIntent.action = Constants.ACTION.ACTION_GET_IMAGES_COMPLETE
                            mContext?.sendBroadcast(completeIntent)
                        } else {
                            val successIntent = Intent()
                            successIntent.action = Constants.ACTION.ACTION_GET_IMAGES_SUCCESS
                            successIntent.putExtra("idx", downloadIdx)
                            successIntent.putExtra("fileName", imageList[downloadIdx])
                            mContext?.sendBroadcast(successIntent)
                        }
                    } catch (ex: Exception) {
                        Log.e(mTag, ex.toString())
                        //we can't stuck on download failed, keep try next one
                        downloadImageComplete += 1
                        downloadImageReadyArray[downloadIdx] = false
                        val completeIntent = Intent()
                        completeIntent.action = Constants.ACTION.ACTION_GET_IMAGES_FAILED
                        mContext?.sendBroadcast(completeIntent)

                    }
                }.start()
            } else { //downloadIdx == -1
                if (downloadImageComplete == imageList.size) {
                    val completeIntent = Intent()
                    completeIntent.action = Constants.ACTION.ACTION_GET_IMAGES_COMPLETE
                    mContext?.sendBroadcast(completeIntent)
                }
            }



        } else {
            val emptyIntent = Intent()
            emptyIntent.action = Constants.ACTION.ACTION_GET_IMAGES_EMPTY
            mContext?.sendBroadcast(emptyIntent)
        }
    }

    fun downloadVideos() {
        Log.d(mTag, "downloadVideos")

        if (videoList.size > 0) {
            var downloadIdx = -1
            for (i in videoList.indices) {
                val srcPath = server_videos_folder
                val destPath = "$dest_videos_folder${videoList[i]}"

                Log.d(mTag, "srcPath = $srcPath")
                Log.d(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(!file.exists()) {
                    downloadIdx = i
                    break
                }
                /*else {
                    downloadVideoComplete += 1
                    downloadVideoReadyArray[i] = true
                }*/
            }

            Log.d(mTag, "downloadVideoComplete = $downloadVideoComplete, downloadVideoReadyArray = $downloadVideoReadyArray")

            if (downloadIdx >= 0 ) {
                val srcPath = server_videos_folder
                val destPath = "$dest_videos_folder${videoList[downloadIdx]}"
                Log.d(mTag, "start download file : ${videoList[downloadIdx]} to $dest_videos_folder")
                Thread {
                    try {
                        val totalSize = download(srcPath, destPath, videoList[downloadIdx]) { progress, length ->
                            // handling the result on main thread
                            handler.sendMessage(handler.obtainMessage(0, progress to length))
                        }

                        Log.d(mTag, "totalSize = $totalSize")

                        downloadVideoComplete += 1
                        downloadVideoReadyArray[downloadIdx] = true

                        if (downloadVideoComplete == videoList.size) {
                            val completeIntent = Intent()
                            completeIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_COMPLETE
                            mContext?.sendBroadcast(completeIntent)
                        } else {
                            val successIntent = Intent()
                            successIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_SUCCESS
                            successIntent.putExtra("idx", downloadIdx)
                            successIntent.putExtra("fileName", videoList[downloadIdx])
                            mContext?.sendBroadcast(successIntent)
                        }
                    } catch (ex: Exception) {
                        Log.e(mTag, ex.toString())
                        //we can't stuck on download failed, keep try next one
                        downloadVideoComplete += 1
                        downloadVideoReadyArray[downloadIdx] = false
                        val completeIntent = Intent()
                        completeIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_FAILED
                        mContext?.sendBroadcast(completeIntent)
                    }

                }.start()
            } else { //downloadIdx == -1
                if (downloadVideoComplete == videoList.size) {
                    val completeIntent = Intent()
                    completeIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_COMPLETE
                    mContext?.sendBroadcast(completeIntent)
                }
            }


        } else {
            val emptyIntent = Intent()
            emptyIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_EMPTY
            mContext?.sendBroadcast(emptyIntent)
        }
    }

    private fun download(link: String, path: String, fileName: String, progress: ((Long, Long) -> Unit)? = null): Long {
        Log.d(mTag, "download ->")
        val originalFile = File(fileName)
        val nameWithoutExtension = originalFile.nameWithoutExtension
        //var fileNameArray = fileName.split(".")
        val fileExt = originalFile.extension
        var urlUtf8 = "$link/${URLEncoder.encode(nameWithoutExtension, "UTF-8")}.${fileExt}"
        //var urlUtf8 = "$link/${URLEncoder.encode(nameWithoutExtension, "UTF-8")}.${fileNameArray[1]}"
        if (urlUtf8.contains("+")) {
            urlUtf8 = urlUtf8.replace("+", "%20")
        }

        val url = URL(urlUtf8)
        Log.d(mTag, "url = $url")
        val connection = url.openConnection()
        connection.connect()
        Log.d(mTag, "link = $link")
        Log.d(mTag, "path = $path")
        val file =  File(path)

        val length = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) connection.contentLengthLong else
            connection.contentLength.toLong()
        url.openStream().use { input ->
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                //Log.d(mTag, "-->1")
                var bytesRead = input.read(buffer)
                //Log.d(mTag, "-->2")
                var bytesCopied = 0L
                while (bytesRead >= 0) {
                    output.write(buffer, 0, bytesRead)
                    bytesCopied += bytesRead
                    progress?.invoke(bytesCopied, length)
                    bytesRead = input.read(buffer)
                }
                return bytesCopied
            }
        }
    }

    private fun checkDownloadImagesOnlyOne(): Boolean {
        var ret = false

        var count = 0
        for (i in downloadImageReadyArray.indices) {
            if (downloadImageReadyArray[i]) {
                count += 1
            }
        }

        if (count == 1) {
            ret = true
        }

        return ret
    }

    private fun checkDownloadVideosOnlyOne(): Boolean {
        var ret = false

        var count = 0
        for (i in downloadVideoReadyArray.indices) {
            if (downloadVideoReadyArray[i]) {
                count += 1
            }
        }

        if (count == 1) {
            ret = true
        }

        return ret
    }

    private fun checkDownloadImagesAll(): Boolean {
        var ret = false

        for (i in downloadImageReadyArray.indices) {
            if (downloadImageReadyArray[i]) {
                ret = true
                break
            }
        }

        return ret
    }

    private fun checkDownloadVideosAll(): Boolean {
        var ret = false

        for (i in downloadVideoReadyArray.indices) {
            if (downloadVideoReadyArray[i]) {
                ret = true
                break
            }
        }

        return ret
    }

    fun checkImagesExists() {
        downloadImageComplete = 0
        if (imageList.size > 0) {
            for (i in imageList.indices) {
                val srcPath = server_images_folder
                val destPath = "$dest_images_folder${imageList[i]}"
                Log.d(mTag, "srcPath = $srcPath")
                Log.d(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(file.exists()) {
                    downloadImageComplete += 1
                    downloadImageReadyArray[i] = true

                }
            }
        }
    }

    fun checkVideosExists() {
        downloadVideoComplete = 0
        if (videoList.size > 0) {
            for (i in videoList.indices) {
                val srcPath = server_videos_folder
                val destPath = "$dest_videos_folder${videoList[i]}"
                Log.d(mTag, "srcPath = $srcPath")
                Log.d(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(file.exists()) {
                    downloadVideoComplete += 1
                    downloadVideoReadyArray[i] = true

                }
            }
        }
    }

    fun clearImagesNotInImageList() {
        Log.d(mTag, "clearImagesNotInImageList")
        val directory = File(dest_images_folder)
        val files = directory.listFiles()

        if (directory.isDirectory && files != null) {
            for (i in files.indices) {
                var found = false

                for (j in imageList.indices) {
                    if (files[i].name == imageList[j]) {
                        found = true

                        break
                    }
                }

                if (!found) { //not found in imageList, delete it!
                    val deletePath = "$dest_images_folder${files[i].name}"
                    val deleteFile = File(deletePath)
                    deleteFile.delete()
                    Log.d(mTag, "Delete $deletePath")
                }
            }
        }
    }

    fun clearVideosNotInVideoList() {
        Log.d(mTag, "clearVideosNotInVideoList")
        val directory = File(dest_videos_folder)
        val files = directory.listFiles()

        Log.d(mTag, "files -> $files")

        if (directory.isDirectory && files != null) {

            for (i in files.indices) {
                var found = false

                for (j in videoList.indices) {
                    if (files[i].name == videoList[j]) {
                        found = true

                        break
                    }
                }

                if (!found) { //not found in imageList, delete it!
                    val deletePath = "$dest_videos_folder${files[i].name}"
                    val deleteFile = File(deletePath)
                    deleteFile.delete()
                    Log.d(mTag, "Delete $deletePath")
                }
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun playAd() {
        Log.d(mTag, "playAd Start")

        infoRenew = false

        //if marquee loop is running, stop it
        if (countDownTimerMarqueeRunning) {
            countDownTimerMarquee.cancel()
        }
        //if image loop is running, stop it
        if (countDownTimerImageRunning) {
            countDownTimerImage.cancel()
        }
        //if video view is playing, stop it
        if (videoViewTop != null && videoViewTop!!.isPlaying) {
            videoViewTop!!.stopPlayback()
            //videoViewTop!!.visibility = View.INVISIBLE
        }
        if (videoViewCenter != null && videoViewCenter!!.isPlaying) {
            videoViewCenter!!.stopPlayback()
            //videoViewCenter!!.visibility = View.INVISIBLE
        }
        if (videoViewBottom != null && videoViewBottom!!.isPlaying) {
            videoViewBottom!!.stopPlayback()
            //videoViewBottom!!.visibility = View.INVISIBLE
        }

        //clear layout
        rootView!!.removeAllViews()
        rootView!!.setBackgroundColor(Color.BLACK)
        rootView!!.setBackgroundResource(R.drawable.customborder)
        //init play index
        currentTextIndexTop = -1
        currentImageIndexTop = -1
        currentVideoIndexTop = -1
        currentTextIndexCenter = -1
        currentImageIndexCenter = -1
        currentVideoIndexCenter = -1
        currentTextIndexBottom = -1
        currentImageIndexBottom = -1
        currentVideoIndexBottom = -1

        //init layout
        if (layoutList.size > 0 ) {
            if (layoutList.size == 1) { //only one layout
                if (adSettingList.size > 0) { // must have adSetting
                    val orientation = layoutList[0].orientation
                    val layoutTop = layoutList[0].layout_top
                    val layoutCenter = layoutList[0].layout_center
                    val layoutBottom = layoutList[0].layout_bottom

                    val marqueeMode = adSettingList[0].marquee_mode
                    val imagesMode = adSettingList[0].images_mode
                    val videosMode = adSettingList[0].videos_mode

                    val layoutOrientation = layoutList[0].layoutOrientation

                    val imageInterval = adSettingList[0].image_interval
                    var imagesPlayInterval = 7000
                    when(imageInterval) {
                        0 -> {
                            imagesPlayInterval = 7000
                        }
                        1 -> {
                            imagesPlayInterval = 10000
                        }
                        2 -> {
                            imagesPlayInterval = 15000
                        }
                    }

                    Log.d(mTag, "orientation = $orientation, layoutTop = $layoutTop, layoutCenter = $layoutCenter, layoutBottom = $layoutBottom")
                    //mode = 0 => cycle, mode = 1 => random
                    Log.d(mTag, "marqueeMode = $marqueeMode, imagesMode = $imagesMode, videosMode = $videosMode, layoutOrientation = $layoutOrientation")

                    if (orientation == 2) { //screen landscape
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        currentOrientation = 2
                        Log.d(mTag, "screen: Horizontal")
                    } else { //orientation == 1
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        currentOrientation = 1
                        Log.d(mTag, "screen: Vertical")
                    }
                    //main LinearLayout
                    if (mainLinearLayout == null) {
                        mainLinearLayout = LinearLayout(mContext)
                        mainLinearLayout!!.setPadding(12,12,12,12)

                    }
                    mainLinearLayout!!.removeAllViews()
                    mainLinearLayout!!.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    when (layoutOrientation) {
                        1 -> { //horizontal
                            mainLinearLayout!!.orientation = LinearLayout.HORIZONTAL
                            Log.d(mTag, "mainLinearLayout: HORIZONTAL")
                        }
                        2 -> { //left triangle
                            mainLinearLayout!!.orientation = LinearLayout.HORIZONTAL
                            Log.d(mTag, "mainLinearLayout: HORIZONTAL")
                        }
                        3 -> { //right triangle
                            mainLinearLayout!!.orientation = LinearLayout.HORIZONTAL
                            Log.d(mTag, "mainLinearLayout: HORIZONTAL")
                        }
                        4 -> { //up triangle
                            mainLinearLayout!!.orientation = LinearLayout.VERTICAL
                            Log.d(mTag, "mainLinearLayout: VERTICAL")
                        }
                        5 -> { //down triangle
                            mainLinearLayout!!.orientation = LinearLayout.VERTICAL
                            Log.d(mTag, "mainLinearLayout: VERTICAL")
                        }
                        else -> { //vertical
                            mainLinearLayout!!.orientation = LinearLayout.VERTICAL
                            Log.d(mTag, "mainLinearLayout: VERTICAL")
                        }
                    }
                    rootView!!.addView(mainLinearLayout)

                    //linearLayoutTriangle for new layout
                    if (linearLayoutTriangle == null) {
                        linearLayoutTriangle = LinearLayout(mContext)
                    }
                    linearLayoutTriangle!!.removeAllViews()
                    linearLayoutTriangle!!.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, linearLayoutTriangleWeight)
                    //linearLayoutTriangle!!.weightSum = 4.0F
                    if (layoutOrientation in 2..5) {
                        if (layoutOrientation == 2 || layoutOrientation == 3) { //left and right triangle
                            linearLayoutTriangle!!.orientation = LinearLayout.VERTICAL
                            Log.d(mTag, "linearLayoutTriangle: VERTICAL")
                        } else { //4, 5 up and down triangle
                            linearLayoutTriangle!!.orientation = LinearLayout.HORIZONTAL
                            Log.d(mTag, "linearLayoutTriangle: HORIZONTAL")
                        }
                    }

                    //linearLayoutTop
                    if (linearLayoutTop == null) {
                        linearLayoutTop = LinearLayout(mContext)
                    }
                    linearLayoutTop!!.removeAllViews()
                    linearLayoutTop!!.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, linearLayoutTopWeight)
                    linearLayoutTop!!.orientation = LinearLayout.VERTICAL
                    //linearLayoutTop!!.weightSum = 2.0F

                    //textViewTop
                    if (textViewTop == null) {
                        textViewTop = TextView(mContext)
                    }
                    textViewTop!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    textViewTop!!.setBackgroundColor(Color.BLACK)
                    textViewTop!!.textSize = 40.0F
                    textViewTop!!.setTextColor(Color.WHITE)
                    textViewTop!!.ellipsize = TextUtils.TruncateAt.MARQUEE
                    textViewTop!!.isSingleLine = true
                    textViewTop!!.freezesText = true
                    textViewTop!!.gravity = Gravity.CENTER_VERTICAL
                    textViewTop!!.marqueeRepeatLimit = -1
                    textViewTop!!.visibility = View.GONE
                    linearLayoutTop!!.addView(textViewTop)
                    //imageViewTop
                    if (imageViewTop == null) {
                        imageViewTop = ImageView(mContext)
                    }
                    imageViewTop!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    imageViewTop!!.visibility = View.GONE
                    linearLayoutTop!!.addView(imageViewTop)
                    //imageViewTop2
                    if (imageViewTop2 == null) {
                        imageViewTop2 = ImageView(mContext)
                    }
                    imageViewTop2!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    imageViewTop2!!.visibility = View.GONE
                    linearLayoutTop!!.addView(imageViewTop2)
                    //videoViewLayoutTop
                    if (videoViewLayoutTop == null) {
                        videoViewLayoutTop = RelativeLayout(mContext)
                    }
                    videoViewLayoutTop!!.removeAllViews()
                    videoViewLayoutTop!!.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                    videoViewLayoutTop!!.gravity = Gravity.CENTER
                    videoViewLayoutTop!!.visibility = View.GONE
                    linearLayoutTop!!.addView(videoViewLayoutTop)
                    //videoViewTop
                    if (videoViewTop == null) {
                        videoViewTop = VideoView(mContext)
                    }
                    videoViewTop!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    videoViewLayoutTop!!.addView(videoViewTop)

                    //top pack
                    /*
                    when(layoutOrientation) {
                        1 -> { //horizontal
                            mainLinearLayout!!.addView(linearLayoutTop)
                        }
                        2 -> { //left triangle
                            Log.d(mTag, "left triangle")
                            mainLinearLayout!!.addView(linearLayoutTop)
                            Log.d(mTag, "linearLayoutTop add to mainLinearLayout")
                            mainLinearLayout!!.addView(linearLayoutTriangle)
                            Log.d(mTag, "linearLayoutTriangle add to mainLinearLayout")
                        }
                        3 -> { //right triangle
                            Log.d(mTag, "right triangle")
                            linearLayoutTriangle!!.addView(linearLayoutTop)
                            mainLinearLayout!!.addView(linearLayoutTriangle)
                        }
                        4 -> { //top triangle
                            Log.d(mTag, "top triangle")
                            mainLinearLayout!!.addView(linearLayoutTop)
                            mainLinearLayout!!.addView(linearLayoutTriangle)
                        }
                        5 -> { //down triangle
                            Log.d(mTag, "down triangle")
                            linearLayoutTriangle!!.addView(linearLayoutTop)
                            mainLinearLayout!!.addView(linearLayoutTriangle)
                        }
                        else -> { //0 vertical
                            mainLinearLayout!!.addView(linearLayoutTop)
                        }
                    }*/

                    //LinearLayoutCenter
                    if (linearLayoutCenter == null) {
                        linearLayoutCenter = LinearLayout(mContext)
                    }
                    linearLayoutCenter!!.removeAllViews()
                    linearLayoutCenter!!.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, linearLayoutCenterWeight)
                    linearLayoutCenter!!.orientation = LinearLayout.VERTICAL
                    //linearLayoutCenter!!.weightSum = 2.0F

                    //textViewCenter
                    if (textViewCenter == null) {
                        textViewCenter = TextView(mContext)
                    }
                    textViewCenter!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    textViewCenter!!.setBackgroundColor(Color.BLACK)
                    textViewCenter!!.textSize = 40.0F
                    textViewCenter!!.setTextColor(Color.WHITE)
                    textViewCenter!!.ellipsize = TextUtils.TruncateAt.MARQUEE
                    textViewCenter!!.isSingleLine = true
                    textViewCenter!!.freezesText = true
                    textViewCenter!!.gravity = Gravity.CENTER_VERTICAL
                    textViewCenter!!.marqueeRepeatLimit = -1
                    textViewCenter!!.visibility = View.GONE
                    linearLayoutCenter!!.addView(textViewCenter)
                    //imageViewCenter
                    if (imageViewCenter == null) {
                        imageViewCenter = ImageView(mContext)
                    }
                    imageViewCenter!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    imageViewCenter!!.visibility = View.GONE
                    linearLayoutCenter!!.addView(imageViewCenter)
                    //imageViewCenter2
                    if (imageViewCenter2 == null) {
                        imageViewCenter2 = ImageView(mContext)
                    }
                    imageViewCenter2!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    imageViewCenter2!!.visibility = View.GONE
                    linearLayoutCenter!!.addView(imageViewCenter2)
                    //videoViewLayoutCenter
                    if (videoViewLayoutCenter == null) {
                        videoViewLayoutCenter = RelativeLayout(mContext)
                    }
                    videoViewLayoutCenter!!.removeAllViews()
                    videoViewLayoutCenter!!.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                    videoViewLayoutCenter!!.gravity = Gravity.CENTER
                    videoViewLayoutCenter!!.visibility = View.GONE
                    linearLayoutCenter!!.addView(videoViewLayoutCenter)
                    //videoViewCenter
                    if (videoViewCenter == null) {
                        videoViewCenter = VideoView(mContext)
                    }
                    videoViewCenter!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    videoViewLayoutCenter!!.addView(videoViewCenter)

                    //center pack
                    /*
                    when(layoutOrientation) {
                        1 -> { //horizontal
                            mainLinearLayout!!.addView(linearLayoutCenter)
                        }
                        2 -> { //left triangle
                            linearLayoutTriangle!!.addView(linearLayoutCenter)
                            Log.d(mTag, "linearLayoutCenter add to linearLayoutTriangle")
                        }
                        3 -> { //left triangle
                            linearLayoutTriangle!!.addView(linearLayoutCenter)
                        }
                        4 -> { //left triangle
                            linearLayoutTriangle!!.addView(linearLayoutCenter)
                        }
                        5 -> { //left triangle
                            linearLayoutTriangle!!.addView(linearLayoutCenter)
                        }
                        else -> { //0 vertical
                            mainLinearLayout!!.addView(linearLayoutCenter)
                        }
                    }*/

                    //LinearLayoutBottom
                    if (linearLayoutBottom == null) {
                        linearLayoutBottom = LinearLayout(mContext)
                    }
                    linearLayoutBottom!!.removeAllViews()
                    linearLayoutBottom!!.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, linearLayoutBottomWeight)
                    linearLayoutBottom!!.orientation = LinearLayout.VERTICAL
                    //linearLayoutBottom!!.weightSum = 2.0F

                    //textViewBottom
                    if (textViewBottom == null) {
                        textViewBottom = TextView(mContext)
                    }
                    textViewBottom!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    textViewBottom!!.setBackgroundColor(Color.BLACK)
                    textViewBottom!!.textSize = 40.0F
                    textViewBottom!!.setTextColor(Color.WHITE)
                    textViewBottom!!.ellipsize = TextUtils.TruncateAt.MARQUEE
                    textViewBottom!!.isSingleLine = true
                    textViewBottom!!.freezesText = true
                    textViewBottom!!.gravity = Gravity.CENTER_VERTICAL
                    textViewBottom!!.marqueeRepeatLimit = -1
                    textViewBottom!!.visibility = View.GONE
                    linearLayoutBottom!!.addView(textViewBottom)
                    //imageViewBottom
                    if (imageViewBottom == null) {
                        imageViewBottom = ImageView(mContext)
                    }
                    imageViewBottom!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    imageViewBottom!!.visibility = View.GONE
                    linearLayoutBottom!!.addView(imageViewBottom)
                    //imageViewBottom2
                    if (imageViewBottom2 == null) {
                        imageViewBottom2 = ImageView(mContext)
                    }
                    imageViewBottom2!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    imageViewBottom2!!.visibility = View.GONE
                    linearLayoutBottom!!.addView(imageViewBottom2)
                    //videoViewLayoutBottom
                    if (videoViewLayoutBottom == null) {
                        videoViewLayoutBottom = RelativeLayout(mContext)
                    }
                    videoViewLayoutBottom!!.removeAllViews()
                    videoViewLayoutBottom!!.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                    videoViewLayoutBottom!!.gravity = Gravity.CENTER
                    videoViewLayoutBottom!!.visibility = View.GONE
                    linearLayoutBottom!!.addView(videoViewLayoutBottom)
                    //videoViewBottom
                    if (videoViewBottom == null) {
                        videoViewBottom = VideoView(mContext)
                    }
                    videoViewBottom!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    videoViewLayoutBottom!!.addView(videoViewBottom)
                    //bottom pack
                    /*
                    when(layoutOrientation) {
                        1 -> {
                            mainLinearLayout!!.addView(linearLayoutBottom)
                        }
                        2 -> {
                            linearLayoutTriangle!!.addView(linearLayoutBottom)
                            Log.d(mTag, "linearLayoutBottom add to linearLayoutTriangle")
                        }
                        3 -> {
                            mainLinearLayout!!.addView(linearLayoutBottom)
                        }
                        4 -> {
                            linearLayoutTriangle!!.addView(linearLayoutBottom)
                        }
                        5 -> {
                            mainLinearLayout!!.addView(linearLayoutBottom)
                        }
                        else -> {
                            mainLinearLayout!!.addView(linearLayoutBottom)
                        }
                    }*/
                    when(layoutOrientation) {
                        1 -> { //horizontal
                            mainLinearLayout!!.addView(linearLayoutTop)
                            mainLinearLayout!!.addView(linearLayoutCenter)
                            mainLinearLayout!!.addView(linearLayoutBottom)
                        }
                        2 -> { //left triangle
                            Log.d(mTag, "left triangle")
                            Log.d(mTag, "linearLayoutTop add to mainLinearLayout")
                            mainLinearLayout!!.addView(linearLayoutTop)
                            linearLayoutTriangle!!.addView(linearLayoutCenter)
                            linearLayoutTriangle!!.addView(linearLayoutBottom)
                            mainLinearLayout!!.addView(linearLayoutTriangle)
                            Log.d(mTag, "linearLayoutTriangle add to mainLinearLayout")
                        }
                        3 -> { //right triangle
                            Log.d(mTag, "right triangle")
                            linearLayoutTriangle!!.addView(linearLayoutTop)
                            linearLayoutTriangle!!.addView(linearLayoutCenter)
                            mainLinearLayout!!.addView(linearLayoutTriangle)
                            mainLinearLayout!!.addView(linearLayoutBottom)
                        }
                        4 -> { //top triangle
                            Log.d(mTag, "top triangle")
                            mainLinearLayout!!.addView(linearLayoutTop)
                            linearLayoutTriangle!!.addView(linearLayoutCenter)
                            linearLayoutTriangle!!.addView(linearLayoutBottom)
                            mainLinearLayout!!.addView(linearLayoutTriangle)
                        }
                        5 -> { //down triangle
                            Log.d(mTag, "down triangle")
                            linearLayoutTriangle!!.addView(linearLayoutTop)
                            linearLayoutTriangle!!.addView(linearLayoutCenter)
                            mainLinearLayout!!.addView(linearLayoutTriangle)
                            mainLinearLayout!!.addView(linearLayoutBottom)
                        }
                        else -> { //0 vertical
                            mainLinearLayout!!.addView(linearLayoutTop)
                            mainLinearLayout!!.addView(linearLayoutCenter)
                            mainLinearLayout!!.addView(linearLayoutBottom)
                        }
                    }

                    //for marquee
                    textViewTop!!.isSelected = true
                    textViewCenter!!.isSelected = true
                    textViewBottom!!.isSelected = true

                    if (mediaControllerTop == null) {
                        mediaControllerTop = MediaController(mContext)
                        // anchor view for the videoView
                        mediaControllerTop!!.setAnchorView(videoViewTop)
                        // sets the media player to the videoView
                        mediaControllerTop!!.setMediaPlayer(videoViewTop)
                        // sets the media controller to the videoView
                        videoViewTop!!.setMediaController(mediaControllerTop)
                    }
                    if (mediaControllerCenter == null) {
                        mediaControllerCenter = MediaController(mContext)
                        // anchor view for the videoView
                        mediaControllerCenter!!.setAnchorView(videoViewCenter)
                        // sets the media player to the videoView
                        mediaControllerCenter!!.setMediaPlayer(videoViewCenter)
                        // sets the media controller to the videoView
                        videoViewCenter!!.setMediaController(mediaControllerCenter)
                    }
                    if (mediaControllerBottom == null) {
                        mediaControllerBottom = MediaController(mContext)
                        // anchor view for the videoView
                        mediaControllerBottom!!.setAnchorView(videoViewBottom)
                        // sets the media player to the videoView
                        mediaControllerBottom!!.setMediaPlayer(videoViewBottom)
                        // sets the media controller to the videoView
                        videoViewBottom!!.setMediaController(mediaControllerBottom)
                    }

                    //disable controller
                    mediaControllerTop!!.visibility = View.GONE
                    mediaControllerCenter!!.visibility = View.GONE
                    mediaControllerBottom!!.visibility = View.GONE

                    linearLayoutTop!!.visibility = View.GONE
                    linearLayoutCenter!!.visibility = View.GONE
                    linearLayoutBottom!!.visibility = View.GONE
                    textViewTop!!.visibility = View.GONE
                    textViewCenter!!.visibility = View.GONE
                    textViewBottom!!.visibility = View.GONE
                    imageViewTop!!.visibility = View.GONE
                    imageViewTop2!!.visibility = View.GONE
                    imageViewTop!!.clearAnimation()
                    imageViewTop2!!.clearAnimation()
                    imageViewCenter!!.visibility = View.GONE
                    imageViewCenter2!!.visibility = View.GONE
                    imageViewCenter!!.clearAnimation()
                    imageViewCenter2!!.clearAnimation()
                    imageViewBottom!!.visibility = View.GONE
                    imageViewBottom2!!.visibility = View.GONE
                    imageViewBottom!!.clearAnimation()
                    imageViewBottom2!!.clearAnimation()

                    videoViewLayoutTop!!.visibility = View.GONE
                    videoViewLayoutCenter!!.visibility = View.GONE
                    videoViewLayoutBottom!!.visibility = View.GONE

                    //top
                    when (layoutTop) {
                        0-> { //no
                            Log.d(mTag, "layoutTop => no")
                        }
                        1 -> { //text
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                3.0f
                            )
                            linearLayoutTop!!.layoutParams = param
                            linearLayoutTop!!.visibility = View.VISIBLE
                            textViewTop!!.visibility = View.VISIBLE
                        }
                        2 -> { //image
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.0f
                            )
                            linearLayoutTop!!.layoutParams = param
                            linearLayoutTop!!.visibility = View.VISIBLE
                            imageViewTop!!.visibility = View.VISIBLE
                        }
                        3 -> { //video
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.0f
                            )
                            linearLayoutTop!!.layoutParams = param
                            linearLayoutTop!!.visibility = View.VISIBLE
                            videoViewLayoutTop!!.visibility = View.VISIBLE
                            videoViewTop!!.visibility = View.VISIBLE
                        }
                    }

                    //center
                    when (layoutCenter) {
                        0-> { //no
                            Log.d(mTag, "layoutCenter => no")
                        }
                        1 -> { //text
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                3.0f
                            )
                            linearLayoutCenter!!.layoutParams = param
                            linearLayoutCenter!!.visibility = View.VISIBLE
                            textViewCenter!!.visibility = View.VISIBLE
                        }
                        2 -> { //image
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.0f
                            )
                            linearLayoutCenter!!.layoutParams = param
                            linearLayoutCenter!!.visibility = View.VISIBLE
                            imageViewCenter!!.visibility = View.VISIBLE
                        }
                        3 -> { //video
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.0f
                            )
                            linearLayoutCenter!!.layoutParams = param
                            linearLayoutCenter!!.visibility = View.VISIBLE
                            videoViewLayoutCenter!!.visibility = View.VISIBLE
                            videoViewCenter!!.visibility = View.VISIBLE
                            //exoPlayerViewCenter!!.visibility = View.VISIBLE
                        }
                    }

                    //bottom
                    when (layoutBottom) {
                        0-> { //no
                            Log.d(mTag, "layoutBottom => no")
                        }
                        1 -> { //text
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                3.0f
                            )
                            linearLayoutBottom!!.layoutParams = param

                            linearLayoutBottom!!.visibility = View.VISIBLE
                            textViewBottom!!.visibility = View.VISIBLE
                        }
                        2 -> { //image
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.0f
                            )
                            linearLayoutBottom!!.layoutParams = param
                            linearLayoutBottom!!.visibility = View.VISIBLE
                            imageViewBottom!!.visibility = View.VISIBLE
                        }
                        3 -> { //video
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.0f
                            )
                            linearLayoutBottom!!.layoutParams = param
                            linearLayoutBottom!!.visibility = View.VISIBLE
                            videoViewLayoutBottom!!.visibility = View.VISIBLE
                            videoViewBottom!!.visibility = View.VISIBLE
                        }
                    }

                    // marquee play timer
                    if (layoutTop == 1 || layoutCenter == 1 || layoutBottom == 1) { //Text

                        //init top start
                        if (layoutTop == 1) {
                            if (playMarqueeList.size > 0) {
                                if (marqueeMode == 1) { //random
                                    var nextTop: Int
                                    do {
                                        nextTop = Random.nextInt(playMarqueeList.size)
                                    } while (nextTop == currentTextIndexTop && playMarqueeList.size > 1)
                                    currentTextIndexTop = nextTop
                                } else { //circle
                                    currentTextIndexTop = 0
                                }
                                textViewTop!!.text = playMarqueeList[currentTextIndexTop].content
                            } else {
                                textViewTop!!.text = "請設定文字跑馬燈"
                            }

                        }
                        //init center start
                        if (layoutCenter == 1) {
                            if (playMarqueeList.size > 0) {
                                if (marqueeMode == 1) { //random
                                    var nextCenter: Int
                                    do {
                                        nextCenter = Random.nextInt(playMarqueeList.size )
                                    } while (nextCenter == currentTextIndexCenter && playMarqueeList.size > 1)
                                    currentTextIndexCenter = nextCenter
                                } else { //circle
                                    currentTextIndexCenter = 0
                                }
                                textViewCenter!!.text = playMarqueeList[currentTextIndexCenter].content
                            } else {
                                textViewCenter!!.text = "請設定文字跑馬燈"
                            }
                        }
                        //init bottom start
                        if (layoutBottom == 1) {
                            if (playMarqueeList.size > 0) {
                                if (marqueeMode == 1) { //random
                                    var nextBottom: Int
                                    do {
                                        nextBottom = Random.nextInt(playMarqueeList.size)
                                    } while (nextBottom == currentTextIndexBottom && playMarqueeList.size > 1)
                                    currentTextIndexBottom = nextBottom
                                } else { //circle
                                    currentTextIndexBottom = 0
                                }

                                textViewBottom!!.text = playMarqueeList[currentTextIndexBottom].content
                            } else {
                                textViewBottom!!.text = "請設定文字跑馬燈"
                            }
                        }

                        //if playMarqueeList.size > 0, it would play in loop
                        if (playMarqueeList.size > 0) {
                            countDownTimerMarqueeRunning = true
                            countDownTimerMarquee = object : CountDownTimer(40000, 40000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    Log.d(mTag, "countDownTimerMarquee onTick = $millisUntilFinished")
                                }
                                override fun onFinish() { //结束後的操作
                                    Log.d(mTag, "countDownTimerMarquee finish")
                                    countDownTimerMarqueeRunning = false
                                    //top
                                    if (layoutTop == 1) {
                                        if (marqueeMode == 1) { //random
                                            var nextTop: Int
                                            do {
                                                nextTop = Random.nextInt(playMarqueeList.size)
                                            } while (nextTop == currentTextIndexTop && playMarqueeList.size > 1)
                                            currentTextIndexTop = nextTop
                                        } else { //circle
                                            currentTextIndexTop += 1
                                            if (currentTextIndexTop >= playMarqueeList.size) {
                                                currentTextIndexTop = 0
                                            }
                                        }
                                        textViewTop!!.text = playMarqueeList[currentTextIndexTop].content
                                    }

                                    if (layoutCenter == 1) {
                                        if (marqueeMode == 1) { //random
                                            var nextCenter: Int
                                            do {
                                                nextCenter = Random.nextInt(playMarqueeList.size)
                                            } while (nextCenter == currentTextIndexCenter && playMarqueeList.size > 1)
                                            currentTextIndexCenter = nextCenter
                                        } else { //circle
                                            currentTextIndexCenter += 1
                                            if (currentTextIndexCenter >= playMarqueeList.size) {
                                                currentTextIndexCenter = 0
                                            }
                                        }
                                        textViewCenter!!.text = playMarqueeList[currentTextIndexCenter].content
                                    }

                                    if (layoutBottom == 1) {
                                        if (marqueeMode == 1) { //random
                                            var nextBottom: Int
                                            do {
                                                nextBottom = Random.nextInt(playMarqueeList.size)
                                            } while (nextBottom == currentTextIndexBottom && playMarqueeList.size > 1)
                                            currentTextIndexBottom = nextBottom
                                        } else { //circle
                                            currentTextIndexBottom += 1
                                            if (currentTextIndexBottom >= playMarqueeList.size) {
                                                currentTextIndexBottom = 0
                                            }
                                        }
                                        textViewBottom!!.text = playMarqueeList[currentTextIndexBottom].content
                                    }

                                    this.start()
                                    countDownTimerMarqueeRunning = true
                                }
                            }.start()
                        } else {
                            Log.d(mTag, "playMarqueeList.size == 0")
                        }
                    } else { //all is not text
                        if (countDownTimerMarqueeRunning) {
                            countDownTimerMarquee.cancel()
                            countDownTimerMarqueeRunning = false
                        }
                    }

                    // image play time
                    if (layoutTop == 2 || layoutCenter == 2 || layoutBottom == 2) {

                        //image anime
                        val animMoveFromLeft = AnimationUtils.loadAnimation(mContext, R.anim.move_from_left)
                        val animMoveToRight = AnimationUtils.loadAnimation(mContext, R.anim.move_to_right)
                        val animMoveFromRight = AnimationUtils.loadAnimation(mContext, R.anim.move_from_right)
                        val animMoveToLeft = AnimationUtils.loadAnimation(mContext, R.anim.move_to_left)
                        //init image start
                        if (layoutTop == 2) {
                            if (imageList.size > 0 && checkDownloadImagesAll()) { //at least one image cant play
                                if (imagesMode == 1) { //random
                                    var nextTop: Int
                                    do {
                                        nextTop = Random.nextInt(imageList.size)
                                    } while ((nextTop == currentImageIndexTop && imageList.size > 1) || !downloadImageReadyArray[nextTop])
                                    currentImageIndexTop = nextTop
                                } else { //circle
                                    do { //if next downloadImageReadyArray is false, next one
                                        currentImageIndexTop += 1
                                        if (currentImageIndexTop >= imageList.size) {
                                            currentImageIndexTop = 0
                                        }
                                    } while (!downloadImageReadyArray[currentImageIndexTop])

                                }
                                //top
                                imageViewTop!!.visibility = View.VISIBLE
                                //Picasso.with(mContext).load(imageUrlTop).into(imageViewTop)
                                val srcPath = "$dest_images_folder/${imageList[currentImageIndexTop]}"
                                val file = File(srcPath)
                                if (file.exists()) {
                                    imageViewTop!!.setImageURI(Uri.fromFile(file))
                                    imageViewTop!!.startAnimation(animMoveFromLeft)
                                }
                            }

                        } else {
                            imageViewTop!!.visibility = View.GONE
                            imageViewTop2!!.visibility = View.GONE
                        }
                        //init center start
                        if (layoutCenter == 2) {
                            if (imageList.size > 0 && checkDownloadImagesAll()) {
                                if (imagesMode == 1) { //random
                                    var nextCenter: Int
                                    do {
                                        nextCenter = Random.nextInt(imageList.size)
                                    } while ((nextCenter == currentImageIndexCenter && imageList.size > 1 && !checkDownloadImagesOnlyOne()) || !downloadImageReadyArray[nextCenter])
                                    currentImageIndexCenter = nextCenter
                                } else { //circle
                                    do {
                                        currentImageIndexCenter += 1
                                        if (currentImageIndexCenter >= imageList.size) {
                                            currentImageIndexCenter = 0
                                        }
                                    } while (!downloadImageReadyArray[currentImageIndexCenter])
                                }
                                //center
                                imageViewCenter!!.visibility = View.VISIBLE
                                //Picasso.with(mContext).load(imageUrlCenter).into(imageViewCenter)
                                val srcPath = "$dest_images_folder/${imageList[currentImageIndexCenter]}"
                                val file = File(srcPath)
                                if (file.exists()) {
                                    imageViewCenter!!.setImageURI(Uri.fromFile(file))
                                    imageViewCenter!!.startAnimation(animMoveFromLeft)
                                }
                            }
                        } else {
                            imageViewCenter!!.visibility = View.GONE
                            imageViewCenter2!!.visibility = View.GONE
                        }
                        //init bottom start
                        if (layoutBottom == 2) {
                            if (imageList.size > 0 && checkDownloadImagesAll()) {
                                if (imagesMode == 1) { //random
                                    var nextBottom: Int
                                    do {
                                        nextBottom = Random.nextInt(imageList.size)
                                    } while ((nextBottom == currentImageIndexBottom && imageList.size > 1) || !downloadImageReadyArray[nextBottom])
                                    currentImageIndexBottom = nextBottom
                                } else { //circle
                                    do {
                                        currentImageIndexBottom += 1
                                        if (currentImageIndexBottom >= imageList.size) {
                                            currentImageIndexBottom = 0
                                        }
                                    } while (!downloadImageReadyArray[currentImageIndexBottom])
                                }
                                //bottom
                                imageViewBottom!!.visibility = View.VISIBLE
                                //Picasso.with(mContext).load(imageUrlBottom).into(imageViewBottom)
                                val srcPath = "$dest_images_folder/${imageList[currentImageIndexBottom]}"
                                val file = File(srcPath)
                                if (file.exists()) {
                                    imageViewBottom!!.setImageURI(Uri.fromFile(file))
                                    imageViewBottom!!.startAnimation(animMoveFromLeft)
                                }
                            }
                        } else {
                            imageViewBottom!!.visibility = View.GONE
                            imageViewBottom2!!.visibility = View.GONE
                        }

                        //if imageList.size > 0, then play in loop
                        if (imageList.size > 0 && checkDownloadImagesAll()) {
                            countDownTimerImageRunning = true
                            countDownTimerImage = object : CountDownTimer(imagesPlayInterval.toLong(), imagesPlayInterval.toLong()) {
                                override fun onTick(millisUntilFinished: Long) {
                                    Log.d(mTag, "countDownTimerImage millisUntilFinished = $millisUntilFinished")
                                }
                                override fun onFinish() { //结束后的操作
                                    Log.d(mTag, "countDownTimerImage finish")
                                    if (imageList.size > 0 && checkDownloadImagesAll()) {
                                        countDownTimerImageRunning = false
                                        if (layoutTop == 2) {
                                            if (imagesMode == 1) { //random
                                                var nextTop: Int
                                                do {
                                                    nextTop = Random.nextInt(imageList.size)
                                                } while ((nextTop == currentImageIndexTop && imageList.size > 1) || !downloadImageReadyArray[nextTop])
                                                currentImageIndexTop = nextTop
                                            } else { //circle
                                                do {
                                                    currentImageIndexTop += 1
                                                    if (currentImageIndexTop >= imageList.size) {
                                                        currentImageIndexTop = 0
                                                    }
                                                } while (!downloadVideoReadyArray[currentImageIndexTop])
                                            }

                                            //top
                                            val srcPath = "$dest_images_folder/${imageList[currentImageIndexTop]}"
                                            val file = File(srcPath)
                                            if (file.exists()) {
                                                if (imageViewTop!!.visibility == View.VISIBLE) {
                                                    imageViewTop!!.startAnimation(animMoveToRight)
                                                    imageViewTop!!.visibility = View.GONE
                                                    imageViewTop2!!.setImageURI(Uri.fromFile(file))
                                                    imageViewTop2!!.visibility = View.VISIBLE
                                                    //Picasso.with(mContext).load(imageUrlTop).into(imageViewTop2)
                                                    imageViewTop2!!.startAnimation(animMoveFromLeft)
                                                } else {
                                                    imageViewTop2!!.startAnimation(animMoveToRight)
                                                    imageViewTop2!!.visibility = View.GONE
                                                    imageViewTop!!.setImageURI(Uri.fromFile(file))
                                                    imageViewTop!!.visibility = View.VISIBLE
                                                    //Picasso.with(mContext).load(imageUrlTop).into(imageViewTop)
                                                    imageViewTop!!.startAnimation(animMoveFromLeft)
                                                }
                                            }
                                        }

                                        if (layoutCenter == 2) {
                                            if (imagesMode == 1) { //random
                                                var nextCenter: Int
                                                do {
                                                    nextCenter = Random.nextInt(imageList.size)
                                                } while ((nextCenter == currentImageIndexCenter && imageList.size > 1) || !downloadImageReadyArray[nextCenter])
                                                currentImageIndexCenter = nextCenter
                                            } else { //circle
                                                do {
                                                    currentImageIndexCenter += 1
                                                    if (currentImageIndexCenter >= imageList.size) {
                                                        currentImageIndexCenter = 0
                                                    }
                                                } while (!downloadImageReadyArray[currentImageIndexCenter])
                                            }

                                            //center
                                            val srcPath = "$dest_images_folder/${imageList[currentImageIndexCenter]}"
                                            val file = File(srcPath)
                                            if (file.exists()) {
                                                if (imageViewCenter!!.visibility == View.VISIBLE) {
                                                    imageViewCenter!!.startAnimation(animMoveToLeft)
                                                    imageViewCenter!!.visibility = View.GONE
                                                    imageViewCenter2!!.setImageURI(Uri.fromFile(file))
                                                    imageViewCenter2!!.visibility = View.VISIBLE
                                                    //Picasso.with(mContext).load(imageUrlCenter).into(imageViewCenter2)
                                                    imageViewCenter2!!.startAnimation(animMoveFromRight)
                                                } else { //imageViewCenter!!.visibility == View.GONE
                                                    imageViewCenter2!!.startAnimation(animMoveToLeft)
                                                    imageViewCenter2!!.visibility = View.GONE
                                                    imageViewCenter!!.setImageURI(Uri.fromFile(file))
                                                    imageViewCenter!!.visibility = View.VISIBLE
                                                    //Picasso.with(mContext).load(imageUrlCenter).into(imageViewCenter)
                                                    imageViewCenter!!.startAnimation(animMoveFromRight)
                                                }
                                            }
                                        }

                                        if (layoutBottom == 2) {
                                            if (imagesMode == 1) { //random
                                                var nextBottom: Int
                                                do {
                                                    nextBottom = Random.nextInt(imageList.size)
                                                } while ((nextBottom == currentImageIndexBottom && imageList.size > 1) || !downloadImageReadyArray[nextBottom])
                                                currentImageIndexBottom = nextBottom
                                            } else { //circle
                                                do {
                                                    currentImageIndexBottom += 1
                                                    if (currentImageIndexBottom >= imageList.size) {
                                                        currentImageIndexBottom = 0
                                                    }
                                                } while (!downloadImageReadyArray[currentImageIndexBottom])
                                            }

                                            //bottom
                                            val srcPath = "$dest_images_folder/${imageList[currentImageIndexBottom]}"
                                            val file = File(srcPath)
                                            if (file.exists()) {
                                                if (imageViewBottom!!.visibility == View.VISIBLE) {
                                                    imageViewBottom!!.startAnimation(animMoveToRight)
                                                    imageViewBottom!!.visibility = View.GONE
                                                    imageViewBottom2!!.setImageURI(Uri.fromFile(file))
                                                    imageViewBottom2!!.visibility = View.VISIBLE
                                                    //Picasso.with(mContext).load(imageUrlBottom).into(imageViewBottom2)
                                                    imageViewBottom2!!.startAnimation(animMoveFromLeft)
                                                } else {
                                                    imageViewBottom2!!.startAnimation(animMoveToRight)
                                                    imageViewBottom2!!.visibility = View.GONE
                                                    imageViewBottom!!.setImageURI(Uri.fromFile(file))
                                                    imageViewBottom!!.visibility = View.VISIBLE
                                                    //Picasso.with(mContext).load(imageUrlBottom).into(imageViewBottom)
                                                    imageViewBottom!!.startAnimation(animMoveFromLeft)
                                                }
                                            }
                                        }

                                        this.start()
                                        countDownTimerImageRunning = true
                                    }
                                }
                            }.start()
                        }
                    } else { //all is not image
                        if (countDownTimerImageRunning) {
                            countDownTimerImage.cancel()
                            countDownTimerImageRunning = false
                        }

                    }

                    //video
                    if (layoutTop == 3 || layoutCenter == 3 || layoutBottom == 3) { //video
                        Log.d(mTag, "video play time")
                        //top
                        if (layoutTop == 3) {
                            if (videoList.size > 0 && checkDownloadVideosAll()) { //at least one video can play
                                if (videosMode == 1) { //random

                                    var nextTop: Int
                                    do {
                                        nextTop = Random.nextInt(videoList.size)

                                    } while ((nextTop == currentVideoIndexTop && videoList.size > 1) || !downloadVideoReadyArray[nextTop])
                                    currentVideoIndexTop = nextTop
                                    Log.d(mTag, "downloadVideoReadyArray[nextTop] = ${downloadVideoReadyArray[nextTop]}")
                                    Log.d(mTag, "top videosMode == 1 (random), currentVideoIndexTop = $currentVideoIndexTop")
                                } else { //circle
                                    do {
                                        currentVideoIndexTop += 1
                                        if (currentVideoIndexTop >= videoList.size) {
                                            currentVideoIndexTop = 0
                                        }
                                    } while (!downloadVideoReadyArray[currentVideoIndexTop])

                                }

                                //top
                                val filePath = "$dest_videos_folder${videoList[currentVideoIndexTop]}"
                                Log.d(mTag, "start play -> $filePath")
                                val file = File(filePath)
                                if (file.exists()) {
                                    val uriTop = Uri.fromFile(file)

                                    videoViewTop!!.setVideoURI(uriTop)
                                    //videoViewTop!!.start()
                                    //videoRunningTop = true

                                    videoViewTop!!.setOnPreparedListener { mp->
                                        Log.d(mTag, "videoViewTop prepared")
                                        //mp.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK)
                                        mp.seekTo(0)
                                        mp.start()
                                        videoRunningTop = true
                                    }

                                    videoViewTop!!.setOnErrorListener { _, _, _ ->
                                        Log.d("video", "setOnErrorListener ")
                                        true
                                    }
                                    videoViewTop!!.setOnCompletionListener { mp->
                                        //mp.stop()
                                        mp.reset()
                                        videoRunningTop = false

                                        if (videoList.size > 0 && checkDownloadVideosAll()) {
                                            if (videosMode == 1) { //random
                                                var nextTop: Int
                                                do {
                                                    nextTop = Random.nextInt(videoList.size)
                                                } while ((nextTop == currentVideoIndexTop && videoList.size > 1 && !checkDownloadVideosOnlyOne()) || !downloadVideoReadyArray[nextTop])
                                                currentVideoIndexTop = nextTop
                                            } else { //circle
                                                do {
                                                    currentVideoIndexTop += 1
                                                    if (currentVideoIndexTop >= videoList.size) {
                                                        currentVideoIndexTop = 0
                                                    }
                                                } while (!downloadVideoReadyArray[currentVideoIndexTop])
                                            }
                                            val srcPath = "$dest_videos_folder${videoList[currentVideoIndexTop]}"
                                            val fileVideo = File(srcPath)
                                            if (fileVideo.exists()) {
                                                val uriTopVideo = Uri.fromFile(fileVideo)
                                                videoViewTop!!.setVideoURI(uriTopVideo)
                                            }

                                        } else {
                                            Log.d(mTag, "videoList.size == 0")
                                            videoViewTop!!.visibility = View.GONE
                                        }
                                    }
                                } else {
                                    Log.d(mTag, "video top: play file not exist")
                                }
                            }
                        } else {
                            videoViewTop!!.stopPlayback()
                            //exoPlayerViewTop!!.player!!.stop()
                            videoRunningTop = false
                        }

                        //center
                        if (layoutCenter == 3) {
                            if (videoList.size > 0 && checkDownloadVideosAll()) {
                                if (videosMode == 1) { //random
                                    var nextCenter: Int
                                    do {
                                        nextCenter = Random.nextInt(videoList.size)
                                    } while ((nextCenter == currentVideoIndexCenter && videoList.size > 1) || !downloadVideoReadyArray[nextCenter])
                                    currentVideoIndexCenter = nextCenter
                                } else { //circle
                                    do {
                                        currentVideoIndexCenter += 1
                                        if (currentVideoIndexCenter >= videoList.size) {
                                            currentVideoIndexCenter = 0
                                        }
                                    } while (!downloadVideoReadyArray[currentVideoIndexCenter])
                                }

                                //top
                                val filePath = "$dest_videos_folder${videoList[currentVideoIndexCenter]}"
                                val file = File(filePath)
                                val uriCenter = Uri.fromFile(file)
                                if (file.exists()) {
                                    //center
                                    //val uriCenter = Uri.parse(videoList[currentVideoIndexCenter])


                                    videoViewCenter!!.setVideoURI(uriCenter)
                                    //videoViewCenter!!.start()
                                    //videoRunningCenter = true

                                    videoViewCenter!!.setOnPreparedListener { mp->
                                        Log.d(mTag, "videoViewCenter prepared")
                                        //mp.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK)
                                        mp.start()
                                        mp.seekTo(0)
                                        videoRunningCenter = true
                                    }
                                    videoViewCenter!!.setOnErrorListener { _, _, _ ->
                                        Log.d("video", "setOnErrorListener ")
                                        true
                                    }

                                    videoViewCenter!!.setOnCompletionListener { mp->
                                        Log.d(mTag, "videoViewCenter play complete")
                                        mp.stop()
                                        mp.reset()
                                        if (videoList.size > 0 && checkDownloadVideosAll()) {
                                            videoRunningCenter = false
                                            if (videosMode == 1) { //random
                                                var nextCenter: Int
                                                do {
                                                    nextCenter = Random.nextInt(videoList.size)
                                                } while ((nextCenter == currentVideoIndexCenter && videoList.size > 1 && !checkDownloadVideosOnlyOne()) || !downloadVideoReadyArray[nextCenter])
                                                currentVideoIndexCenter = nextCenter
                                            } else { //circle
                                                do {
                                                    currentVideoIndexCenter += 1
                                                    if (currentVideoIndexCenter >= videoList.size) {
                                                        currentVideoIndexCenter = 0
                                                    }
                                                } while (!downloadVideoReadyArray[currentVideoIndexCenter])

                                            }
                                            val srcPath = "$dest_videos_folder${videoList[currentVideoIndexCenter]}"
                                            val fileVideo = File(srcPath)
                                            if (fileVideo.exists()) {
                                                val uriCenterVideo = Uri.fromFile(fileVideo)

                                                //val uri = Uri.parse(videoList[currentVideoIndexCenter])
                                                videoViewCenter!!.setVideoURI(uriCenterVideo)
                                                //videoViewCenter!!.start()
                                                //videoRunningCenter = true
                                            }
                                        } else {
                                            Log.d(mTag, "videoList.size == 0")
                                            videoViewCenter!!.visibility = View.GONE
                                        }

                                    }
                                } else {
                                    Log.d(mTag, "video center: play file not exist")
                                }
                            }
                        } else {
                            videoViewCenter!!.stopPlayback()
                            //exoPlayerViewCenter!!.player!!.stop()
                            videoRunningCenter = false
                        }

                        //bottom
                        if (layoutBottom == 3) {
                            if (videoList.size > 0 && checkDownloadVideosAll()) {
                                if (videosMode == 1) { //random
                                    var nextBottom: Int
                                    do {
                                        nextBottom = Random.nextInt(videoList.size)
                                    } while ((nextBottom == currentVideoIndexBottom && videoList.size > 1) || !downloadVideoReadyArray[nextBottom])
                                    currentVideoIndexBottom = nextBottom
                                } else { //circle
                                    do {
                                        currentVideoIndexBottom += 1
                                        if (currentVideoIndexBottom >= videoList.size) {
                                            currentVideoIndexBottom = 0
                                        }
                                    } while (!downloadVideoReadyArray[currentVideoIndexBottom])
                                }

                                Log.d(mTag, "start => currentVideoIndexBottom = $currentVideoIndexBottom")


                                val filePath = "$dest_videos_folder${videoList[currentVideoIndexBottom]}"
                                Log.d(mTag, "=> filePath = $filePath")
                                val file = File(filePath)
                                if (file.exists()) {
                                    val uriBottom = Uri.fromFile(file)
                                    videoViewBottom!!.setVideoURI(uriBottom)
                                    //videoViewBottom!!.start()
                                    //videoRunningBottom = true

                                    videoViewBottom!!.setOnPreparedListener { mp->
                                        Log.d(mTag, "videoViewBottom prepared, path = $filePath")
                                        //mp.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK)
                                        mp.start()
                                        mp.seekTo(0)
                                        videoRunningBottom = true
                                    }

                                    //videoViewBottom!!.start()
                                    //videoRunningBottom = true
                                    videoViewBottom!!.setOnErrorListener { _, _, _ ->
                                        Log.d("video", "setOnErrorListener ")
                                        true
                                    }
                                    videoViewBottom!!.setOnCompletionListener { mp->
                                        Log.d(mTag, "videoViewBottom play complete")
                                        mp.stop()
                                        mp.reset()
                                        if (videoList.size > 0 && checkDownloadVideosAll()) {
                                            videoRunningBottom = false
                                            if (videosMode == 1) { //random
                                                var nextBottom: Int
                                                do {
                                                    nextBottom = Random.nextInt(videoList.size)
                                                    Log.d(mTag, "nextBottom = $nextBottom")
                                                } while ((nextBottom == currentVideoIndexBottom && videoList.size > 1 && !checkDownloadVideosOnlyOne()) || !downloadVideoReadyArray[nextBottom])
                                                currentVideoIndexBottom = nextBottom
                                            } else { //circle
                                                do {
                                                    currentVideoIndexBottom += 1
                                                    if (currentVideoIndexBottom >= videoList.size) {
                                                        currentVideoIndexBottom = 0
                                                    }
                                                } while (!downloadVideoReadyArray[currentVideoIndexBottom])

                                            }
                                            Log.d(mTag, "videoList.size = ${videoList.size}, currentVideoIndexBottom = $currentVideoIndexBottom")
                                            val srcPath = "$dest_videos_folder${videoList[currentVideoIndexBottom]}"
                                            Log.d(mTag, "==>srcPath = $srcPath")

                                            val fileVideo = File(srcPath)
                                            if (fileVideo.exists()) {
                                                val uriBottomVideo = Uri.fromFile(fileVideo)
                                                videoViewBottom!!.setVideoURI(uriBottomVideo)
                                                //videoViewBottom!!.start()
                                                //videoRunningBottom = true
                                            }
                                        } else {
                                            Log.d(mTag, "videoList.size == 0")
                                            videoViewBottom!!.visibility = View.GONE
                                        }
                                    }
                                } else {
                                    Log.d(mTag, "video bottom: play file not exist")
                                }
                            }
                        } else {
                            videoViewBottom!!.stopPlayback()
                            //exoPlayerViewBottom!!.player!!.stop()
                            videoRunningBottom = false
                        }
                    } else { //all are not video
                        if (videoRunningTop) {
                            videoViewTop!!.stopPlayback()
                            //exoPlayerViewTop!!.player!!.stop()
                            videoRunningTop = false
                        }

                        if (videoRunningCenter) {
                            videoViewCenter!!.stopPlayback()
                            //exoPlayerViewCenter!!.player!!.stop()
                            videoRunningCenter = false
                        }

                        if (videoRunningBottom) {
                            videoViewBottom!!.stopPlayback()
                            //exoPlayerViewBottom!!.player!!.stop()
                            videoRunningBottom = false
                        }
                    }
                } else {
                    Log.d(mTag, "No AdSetting!")
                }


            }
        }
    }

    private fun checkAndRequestPermissions() {

        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val networkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)

        val accessNetworkStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)

        val accessWiFiStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)

        val changeWifiStatePermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)

        val coarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        val listPermissionsNeeded = ArrayList<String>()

        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (networkPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET)
        }

        if (accessNetworkStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE)
        }

        if (accessWiFiStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE)
        }

        if (changeWifiStatePermissions != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CHANGE_WIFI_STATE)
        }

        if (coarsePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                requestIdMultiplePermission
            )
            //return false;
        } else {
            Log.d(mTag, "All permission are granted")
            //create local folder
            dest_images_folder = Environment.getExternalStorageDirectory().toString() + "/Download/images/"
            dest_videos_folder = Environment.getExternalStorageDirectory().toString() + "/Download/videos/"
            val imagesDir = File(dest_images_folder)
            imagesDir.mkdirs()
            val videoDir = File(dest_videos_folder)
            videoDir.mkdirs()
            if (debugLog) {
                initLog()
            }
        }
        //return true;
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d(mTag, "Permission callback called------- permissions.size = ${permissions.size}")
        when (requestCode) {
            requestIdMultiplePermission -> {

                val perms: HashMap<String, Int> = HashMap()

                // Initialize the map with both permissions
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.INTERNET] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.ACCESS_NETWORK_STATE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.ACCESS_WIFI_STATE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.CHANGE_WIFI_STATE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] = PackageManager.PERMISSION_GRANTED
                //perms.put(Manifest.permission.ACCESS_WIFI_STATE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                //if (grantResults.size > 0) {
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices) {
                        perms[permissions[i]] = grantResults[i]
                        Log.d(mTag, "perms[permissions[$i]] = ${permissions[i]}")

                    }
                    // Check for both permissions
                    if (perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                        && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                        && perms[Manifest.permission.INTERNET] == PackageManager.PERMISSION_GRANTED
                        && perms[Manifest.permission.ACCESS_NETWORK_STATE] == PackageManager.PERMISSION_GRANTED
                        && perms[Manifest.permission.ACCESS_WIFI_STATE] == PackageManager.PERMISSION_GRANTED
                        && perms[Manifest.permission.CHANGE_WIFI_STATE] == PackageManager.PERMISSION_GRANTED
                        && perms[Manifest.permission.ACCESS_COARSE_LOCATION] == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(mTag, "permission granted")
                        //create local folder
                        dest_images_folder = Environment.getExternalStorageDirectory().toString() + "/Download/images/"
                        dest_videos_folder = Environment.getExternalStorageDirectory().toString() + "/Download/videos/"
                        val imagesDir = File(dest_images_folder)
                        imagesDir.mkdirs()
                        val videoDir = File(dest_videos_folder)
                        videoDir.mkdirs()
                        if (debugLog) {
                            initLog()
                        }
                    } else {
                        Log.d(mTag, "Some permissions are not granted ask again ")

                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                            || ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                            ||ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.INTERNET
                            )
                            || ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.ACCESS_NETWORK_STATE
                            )
                            || ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.ACCESS_WIFI_STATE
                            )
                            || ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.CHANGE_WIFI_STATE
                            )
                            || ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        ) {
                            showDialogOK { _, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                    DialogInterface.BUTTON_NEGATIVE ->
                                        // proceed with logic by disabling the related features or quit the app.
                                        finish()
                                }
                            }
                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                .show()
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }//|| ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE )
                        //|| ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_WIFI_STATE )
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }//&& perms.get(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                    //perms.get(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                }
            }
        }

    }

    private fun showDialogOK(okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage("Warning")
            .setPositiveButton("Ok", okListener)
            .setNegativeButton("Cancel", okListener)
            .create()
            .show()
    }

    private fun toast(message: String) {

        if (toastHandle != null) {
            toastHandle!!.cancel()
        }

        toastHandle = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            val toast = Toast.makeText(this, HtmlCompat.fromHtml("<h1>$message</h1>", HtmlCompat.FROM_HTML_MODE_COMPACT), Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
            toast.show()

            toast
        } else { //Android 11
            val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
            toast.show()

            toast
        }
        /*val group = toast.view as ViewGroup
        val textView = group.getChildAt(0) as TextView
        textView.textSize = 30.0f*/



    }

    private fun showInputServerAddressDialog() {
        val promptView = View.inflate(this@MainActivity, R.layout.dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this@MainActivity).create()
        alertDialogBuilder.setView(promptView)

        //final EditText editFileName = (EditText) promptView.findViewById(R.id.editFileName);
        val editTextDialogServerIP = promptView.findViewById<EditText>(R.id.editTextDialogServerIP)
        val editTextDialogServerPort = promptView.findViewById<EditText>(R.id.editTextDialogServerPort)
        val btnClear = promptView.findViewById<Button>(R.id.btnDialogClear)
        val btnConfirm = promptView.findViewById<Button>(R.id.btnDialogConfirm)

        //editTextDialogServerIP.text = "http://"
        editTextDialogServerIP.setText("http://")
        editTextDialogServerPort.setText("")

        alertDialogBuilder.setCancelable(false)
        btnClear!!.setOnClickListener {
            editTextDialogServerIP.setText("http://")
            //editTextDialogServerIP.text = "http://"
            editTextDialogServerPort.setText("")
        }
        btnConfirm!!.setOnClickListener {

            if (editTextDialogServerIP.text.toString() != "" &&
                editTextDialogServerPort.text.toString() != "") {

                Log.d(mTag,"IP = ${ editTextDialogServerIP.text}")
                Log.d(mTag,"Port = ${ editTextDialogServerPort.text}")

                server_ip_address = editTextDialogServerIP.text.toString()
                editTextDialogServerIP.setSelection(editTextDialogServerIP.length())
                server_webservice_port = editTextDialogServerPort.text.toString()

                editor = pref!!.edit()
                editor!!.putString("SERVER_IP_ADDRESS", editTextDialogServerIP.text.toString())
                editor!!.putString("SERVER_WEBSERVICE_PORT", editTextDialogServerPort.text.toString())
                editor!!.apply()

                val testServerIPAndPortIntent = Intent()
                testServerIPAndPortIntent.action = Constants.ACTION.ACTION_TEST_IP_AND_PORT
                mContext?.sendBroadcast(testServerIPAndPortIntent)
            } else {
                toast(getString(R.string.server_ip_input_empty))
                val showAgainIntent = Intent()
                showAgainIntent.action = Constants.ACTION.ACTION_SHOW_DIALOG_AGAIN
                mContext?.sendBroadcast(showAgainIntent)
            }



            alertDialogBuilder.dismiss()
        }
        alertDialogBuilder.show()
    }

    private fun initLog() {
        Log.d(mTag, "=== start log ===")
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentDateAndTime = sdf.format(Date())
        val logFilename = "logcat_$currentDateAndTime.txt"
        //val outputFile = File(getExternalCacheDir(), logFilename)
        val outputFile = File(externalCacheDir, logFilename)
        try {
            //process = Runtime.getRuntime().exec("logcat -d -f " + outputFile.getAbsolutePath());
            process = Runtime.getRuntime().exec("logcat -c")
            process = Runtime.getRuntime().exec("logcat -f $outputFile")

        } catch (e: IOException) {
            e.printStackTrace()
        }




    }

    private fun getCurrentTimeString(): String {

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        Log.d(mTag, "currentTime = $currentTime")
        return currentTime
    }

    private fun getTimeStampFromString(startTime: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        Log.d(mTag, "Today = $today")
        val combineString = "$today $startTime"
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        try {
            val date: Date = format.parse(combineString) as Date
            Log.d(mTag, "date = $date")
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
}