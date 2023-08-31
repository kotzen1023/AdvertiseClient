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
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
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
import androidx.core.text.HtmlCompat
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.seventhmoon.advertiseclient.api.ApiFunc
import com.seventhmoon.advertiseclient.data.Constants
import com.seventhmoon.advertiseclient.model.recv.RecvAdSetting
import com.seventhmoon.advertiseclient.model.recv.RecvAdvertise
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
import okhttp3.internal.notify
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private val mTag = MainActivity::class.java.name
    var mContext: Context? = null

    private val requestIdMultiplePermission = 1
    var pref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private val fileName = "Preference"

    var layoutList: ArrayList<RecvLayout> = ArrayList()

    private var marqueeList : ArrayList<RecvMarquee> = ArrayList() // for text marquee
    private var playMarqueeList : ArrayList<RecvMarquee> = ArrayList() // for text marquee
    var advertiseList : ArrayList<RecvAdvertise> = ArrayList() // for image
    private var imageList : ArrayList<String> = ArrayList() // for image
    private var videoList : ArrayList<String> = ArrayList() // for video
    private var adSettingList : ArrayList<RecvAdSetting> = ArrayList()

    private var mReceiver: BroadcastReceiver? = null
    private var isRegister = false
    private var current_text_index_top = -1
    private var current_play_index_top = -1
    private var current_video_index_top = -1
    private var current_text_index_center = -1
    private var current_play_index_center = -1
    private var current_video_index_center = -1
    private var current_text_index_bottom = -1
    private var current_play_index_bottom = -1
    private var current_video_index_bottom = -1

    private var linearLayoutTop : LinearLayout ?= null
    private var textViewTop : TextView ?= null
    private var imageViewTop : ImageView ?= null
    private var imageViewTop2 : ImageView ?= null
    private var videoViewLayoutTop: RelativeLayout ?= null
    private var videoViewTop: VideoView ?= null

    private var linearLayoutCenter : LinearLayout ?= null
    var textViewCenter : TextView ?= null
    var imageViewCenter : ImageView ?= null
    var imageViewCenter2 : ImageView ?= null
    var videoViewLayoutCenter: RelativeLayout ?= null
    var videoViewCenter: VideoView ?= null

    private var linearLayoutBottom : LinearLayout ?= null
    var textViewBottom : TextView ?= null
    var imageViewBottom : ImageView ?= null
    var imageViewBottom2 : ImageView ?= null
    var videoViewLayoutBottom: RelativeLayout ?= null
    var videoViewBottom: VideoView ?= null


    private var deviceID = ""
    private var deviceName = ""
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    var getfirstPingResonse = false



    private var toastHandle: Toast? = null

    lateinit var countDownTimerTop : CountDownTimer
    lateinit var countDownTimerCenter : CountDownTimer
    lateinit var countDownTimerBottom : CountDownTimer

    lateinit var countDownTimerMarquee : CountDownTimer
    var countDownTimerMarqueeRunning : Boolean = false
    lateinit var countDownTimerImage : CountDownTimer
    var countDownTimerImageRunning : Boolean = false
    //lateinit var countDownTimerVideo : CountDownTimer

    var countDownTimerVideoRunningTop : Boolean = false
    var countDownTimerVideoRunningCenter : Boolean = false
    var countDownTimerVideoRunningBottom : Boolean = false



    var orientationChanged = false
    var isPortrait = true
    var currentOrientation = 0
    companion object {
        @JvmStatic var server_ip_address: String = ""
        @JvmStatic var server_webservice_port: String = ""
        @JvmStatic var base_ip_address_webservice: String = ""

        @JvmStatic var server_images_folder: String = ""
        @JvmStatic var server_videos_folder: String = ""

        @JvmStatic var dest_images_folder: String = ""
        @JvmStatic var dest_videos_folder: String = ""
    }

    val handler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            // length may be negative because it is based on http header
            val (progress, length) = msg.obj as Pair<Long, Long>
            //Log.e(mTag, "progress = $progress")
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

    //private lateinit var mediaControllerTop: MediaController
    //private lateinit var mediaControllerCenter: MediaController
    //private lateinit var mediaControllerBottom: MediaController
    private var downloadVideoComplete: Int = 0
    private var downloadVideoReadyArray: ArrayList<Boolean> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions()
        } else {
            //create default folder
            Log.e(mTag, "create default folder directly!!")
            dest_images_folder = Environment.getExternalStorageDirectory().toString() + "/Download/images/"
            dest_videos_folder = Environment.getExternalStorageDirectory().toString() + "/Download/videos/"
            val imagesDir = File(dest_images_folder)
            imagesDir.mkdirs()
            val videoDir = File(dest_videos_folder)
            videoDir.mkdirs()
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

        Log.e(mTag, "server_ip_address = $server_ip_address")
        Log.e(mTag, "server_webservice_port = $server_webservice_port")

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
        Log.e(mTag, "currentOrientation = $currentOrientation")

        //var macAddress = getWIFIMAC()
        linearLayoutTop = findViewById(R.id.linearLayoutTop)
        textViewTop = findViewById(R.id.textViewTop)
        imageViewTop = findViewById(R.id.imageViewTop)
        imageViewTop2 = findViewById(R.id.imageViewTop2)
        videoViewLayoutTop = findViewById(R.id.videoViewLayoutTop)
        videoViewTop = findViewById(R.id.videoViewTop)

        linearLayoutCenter = findViewById(R.id.linearLayoutCenter)
        textViewCenter = findViewById(R.id.textViewCenter)
        imageViewCenter = findViewById(R.id.imageViewCenter)
        imageViewCenter2 = findViewById(R.id.imageViewCenter2)
        videoViewLayoutCenter = findViewById(R.id.videoViewLayoutCenter)
        videoViewCenter = findViewById(R.id.videoViewCenter)

        linearLayoutBottom = findViewById(R.id.linearLayoutBottom)
        textViewBottom = findViewById(R.id.textViewBottom)
        imageViewBottom = findViewById(R.id.imageViewBottom)
        imageViewBottom2 = findViewById(R.id.imageViewBottom2)
        videoViewLayoutBottom = findViewById(R.id.videoViewLayoutBottom)
        videoViewBottom = findViewById(R.id.videoViewBottom)

        //for marquee
        textViewTop!!.isSelected = true
        textViewCenter!!.isSelected = true
        textViewBottom!!.isSelected = true

        val mediaControllerTop = MediaController(this)
        val mediaControllerCenter = MediaController(this)
        val mediaControllerBottom = MediaController(this)
        // sets the anchor view
        // anchor view for the videoView
        mediaControllerTop.setAnchorView(videoViewTop)
        mediaControllerCenter.setAnchorView(videoViewCenter)
        mediaControllerBottom.setAnchorView(videoViewBottom)
        // sets the media player to the videoView
        mediaControllerTop.setMediaPlayer(videoViewTop)
        mediaControllerCenter.setMediaPlayer(videoViewCenter)
        mediaControllerBottom.setMediaPlayer(videoViewBottom)

        //disable controller
        mediaControllerTop.visibility = View.GONE
        mediaControllerCenter.visibility = View.GONE
        mediaControllerBottom.visibility = View.GONE

        // sets the media controller to the videoView
        videoViewTop!!.setMediaController(mediaControllerTop)
        videoViewCenter!!.setMediaController(mediaControllerCenter)
        videoViewBottom!!.setMediaController(mediaControllerBottom)

        deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        deviceName = Build.MODEL

        //Log.d(mTag, "macAddress = $macAddress")
        Log.d(mTag, "deviceID = $deviceID")
        Log.d(mTag, "deviceName = $deviceName")
        Log.e(mTag, "width = $screenWidth, height = $screenHeight")





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
        Log.e(mTag, "defaultLayoutPlayList = ${defaultLayoutPlayList!!.size}")

        if (defaultLayoutPlayList!!.size > 0) {
            layoutList.clear()
            val recvLayout = RecvLayout()
            recvLayout.layout_id = defaultLayoutPlayList!![0].getLayout_id()
            recvLayout.screenWidth = defaultLayoutPlayList!![0].getScreenWidth()
            recvLayout.screenHeight = defaultLayoutPlayList!![0].getScreenHeight()
            recvLayout.layoutOrientation = defaultLayoutPlayList!![0].getOrientation()
            recvLayout.layout_top = defaultLayoutPlayList!![0].getLayout_top()
            recvLayout.layout_center = defaultLayoutPlayList!![0].getLayout_center()
            recvLayout.layout_bottom = defaultLayoutPlayList!![0].getLayout_bottom()
            recvLayout.layoutOrientation = defaultLayoutPlayList!![0].getLayoutOrientation()
            recvLayout.plan_id = defaultLayoutPlayList!![0].getPlan_id()

            layoutList.add(recvLayout)
        }

        //load AdSetting from DB
        defaultPlayAdSettingDataDB = Room.databaseBuilder(mContext as Context, DefaultPlayAdSettingDataDB::class.java, DefaultPlayAdSettingDataDB.DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(migration12)
            .build()
        defaultAdSettingPlayList = defaultPlayAdSettingDataDB!!.defaultPlayAdSettingDataDao().getAll() as ArrayList<DefaultPlayAdSettingData>
        Log.e(mTag, "defaultAdSettingPlayList = ${defaultAdSettingPlayList!!.size}")

        if (defaultAdSettingPlayList!!.size > 0) {
            adSettingList.clear()
            val adSetting = RecvAdSetting()
            adSetting.plan_id = defaultAdSettingPlayList!![0].getPlan_id()
            adSetting.plan_name = defaultAdSettingPlayList!![0].getPlan_name()
            adSetting.plan_marquee = defaultAdSettingPlayList!![0].getPlan_marquee()
            adSetting.plan_images = defaultAdSettingPlayList!![0].getPlan_images()
            adSetting.plan_videos = defaultAdSettingPlayList!![0].getPlan_videos()
            adSetting.marquee_mode = defaultAdSettingPlayList!![0].getMarquee_mode()
            adSetting.images_mode = defaultAdSettingPlayList!![0].getImages_mode()
            adSetting.videos_mode = defaultAdSettingPlayList!![0].getVideos_mode()

            adSettingList.add(adSetting)
        }

        //load marquee from DB
        defaultPlayMarqueeDataDB = Room.databaseBuilder(mContext as Context, DefaultPlayMarqueeDataDB::class.java, DefaultPlayMarqueeDataDB.DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(migration12)
            .build()
        defaultMarqueePlayList = defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().getAll() as ArrayList<DefaultPlayMarqueeData>
        Log.e(mTag, "defaultMarqueePlayList = ${defaultMarqueePlayList!!.size}")

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
        Log.e(mTag, "defaultImagesPlayList = ${defaultImagesPlayList!!.size}")

        if (defaultImagesPlayList!!.size > 0) {
            imageList.clear()
            for (i in defaultImagesPlayList!!.indices) {
                imageList.add(defaultImagesPlayList!![i].getFileName())
            }
        }

        //load video from DB
        defaultPlayVideosDataDB = Room.databaseBuilder(mContext as Context, DefaultPlayVideosDataDB::class.java, DefaultPlayVideosDataDB.DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(migration12)
            .build()
        defaultVideosPlayList = defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().getAll() as ArrayList<DefaultPlayVideosData>
        Log.e(mTag, "defaultVideosPlayList = ${defaultVideosPlayList!!.size}")

        if (defaultVideosPlayList!!.size > 0) {
            videoList.clear()
            downloadVideoReadyArray.clear()
            for (i in defaultVideosPlayList!!.indices) {
                videoList.add(defaultVideosPlayList!![i].getFileName())

                val destPath = "$dest_images_folder${defaultVideosPlayList!![i].getFileName()}"
                Log.e(mTag, "destPath = $destPath")

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
            playAd()
        }

        if (server_ip_address == "" || server_webservice_port == "") {
            showInputServerAddressDialog()
        } else {
            base_ip_address_webservice = "$server_ip_address:$server_webservice_port/"

            pingWeb()
        }

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

                        Log.e(mTag, "base_ip_address_webservice = $base_ip_address_webservice")

                        pingWeb()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_PING_WEB, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_PING_WEB")

                        base_ip_address_webservice = "$server_ip_address:$server_webservice_port/"

                        Log.e(mTag, "base_ip_address_webservice = $base_ip_address_webservice")

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

                        val plan_id = intent.getIntExtra("PLAN_ID", 0)

                        Log.e(mTag, "plan_id = $plan_id")

                        if (plan_id > 0) {
                            getAdSetting(plan_id)
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_AD_SETTING_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_AD_SETTING_FAILED")

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_AD_SETTING_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_AD_SETTING_SUCCESS")

                        if (adSettingList.size > 0) {
                            //get Marquee
                            getMarquee()
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MARQUEE_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MARQUEE_FAILED")

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MARQUEE_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MARQUEE_SUCCESS")

                        //then find match marquee
                        if (adSettingList.size > 0) {
                            Log.d(mTag, "plan_marquee = ${adSettingList[0].plan_marquee}")

                            val marqueeArray = adSettingList[0].plan_marquee.split(",")
                            Log.d(mTag, "marqueeArray.size = ${marqueeArray.size}")

                            playMarqueeList.clear()

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



                            Log.e(mTag, "playMarqueeList = $playMarqueeList")
                            //write db

                            if (playMarqueeList.size > 0) {
                                //clear before delete
                                defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().clearTable()
                                for (i in playMarqueeList.indices) {
                                    val defaultPlayMarqueeData = DefaultPlayMarqueeData(
                                        playMarqueeList[i].marqueeId, playMarqueeList[i].name, playMarqueeList[i].content)
                                    defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().insert(defaultPlayMarqueeData)
                                }
                                defaultMarqueePlayList = defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().getAll() as ArrayList<DefaultPlayMarqueeData>
                                Log.e(mTag, "defaultMarqueePlayList.size = ${defaultMarqueePlayList!!.size}")
                            }


                            //then download images

                            val imagesArray = adSettingList[0].plan_images.split(",")
                            imageList.clear()
                            for (i in imagesArray.indices) {
                                imageList.add(imagesArray[i])
                            }

                            Log.e(mTag, "imageList = $imageList")

                            downloadImages()
                        } else {
                            Log.e(mTag, "No AdSetting")
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MARQUEE_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MARQUEE_EMPTY")
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_IMAGES_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_IMAGES_FAILED")

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_IMAGES_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_IMAGES_SUCCESS")

                        if (imageList.size > 0) {
                            //clear before add
                            defaultPlayImagesDataDB!!.defaultPlayImagesDataDao().clearTable()
                            for (i in imageList.indices) {
                                val defaultPlayImagesData = DefaultPlayImagesData(imageList[i])
                                defaultPlayImagesDataDB!!.defaultPlayImagesDataDao().insert(defaultPlayImagesData)
                            }
                            defaultImagesPlayList!!.clear()
                            defaultImagesPlayList = defaultPlayImagesDataDB!!.defaultPlayImagesDataDao().getAll() as ArrayList<DefaultPlayImagesData>
                            Log.e(mTag, "defaultImagesPlayList.size = ${defaultImagesPlayList!!.size}")
                        }

                        //then download videos

                        val videosArray = adSettingList[0].plan_videos.split(",")
                        videoList.clear()
                        downloadVideoReadyArray.clear()
                        for (i in videosArray.indices) {
                            videoList.add(videosArray[i])
                            downloadVideoReadyArray.add(false)
                        }

                        Log.e(mTag, "videoList = $videoList")


                        //then download videos
                        downloadVideos()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_IMAGES_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_IMAGES_EMPTY")
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_VIDEOS_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_VIDEOS_FAILED")

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_VIDEOS_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_VIDEOS_SUCCESS")

                        val fileName = intent.getStringExtra("fileName")
                        val idx = intent.getStringExtra("fileName")

                        Log.e(mTag, "fileName: $fileName download complete!")

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
                                Log.e(mTag, "defaultVideosPlayList.size = ${defaultVideosPlayList!!.size}")
                            }

                            //start to play
                            Log.d(mTag, "start to play!")
                            playAd()
                        } else {
                            Log.e(mTag, "Not Yet")
                        }


                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_VIDEOS_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_VIDEOS_EMPTY")
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

            filter.addAction(Constants.ACTION.ACTION_GET_MARQUEE_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_MARQUEE_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GET_MARQUEE_EMPTY)

            filter.addAction(Constants.ACTION.ACTION_GET_IMAGES_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_IMAGES_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GET_IMAGES_EMPTY)

            filter.addAction(Constants.ACTION.ACTION_GET_VIDEOS_SUCCESS)
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
                Log.e(mTag, "60 second passed...")
            }

            override fun onFinish() { //结束后的操作
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
            Log.e(mTag, "onResponse : "+response.body.toString())
            //val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            val json = JSONObject(response.body!!.string())
            runOnUiThread {
                try {
                    Log.e(mTag, "getPingCallback json = $json")

                    if (json["result"] == 0 ) {
                        if (!getfirstPingResonse) {
                            getfirstPingResonse = true

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
                        Log.e(mTag, "====>Layout changed.")
                        //orientationChanged = false
                        //getLayout()
                        //layout changed, getlayout
                        val successIntent = Intent()
                        successIntent.action = Constants.ACTION.ACTION_PING_WEB_SUCCESS
                        mContext?.sendBroadcast(successIntent)
                    } else if (json["result"] == -1) {
                        Log.e(mTag, "->no deviceID")
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
            Log.e(mTag, "onResponse : "+response.body.toString())

            //val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            val jsonStr = response.body!!.string()
            Log.e(mTag, "jsonStr = $jsonStr")
            runOnUiThread {
                try {
                    val listType = object : TypeToken<ArrayList<RecvLayout>>() {}.type
                    layoutList.clear()
                    layoutList = Gson().fromJson(jsonStr, listType)

                    Log.e(mTag, "layoutList.size = " + layoutList.size)

                    Log.e(mTag, "layoutList[0].plan_id = ${layoutList[0].plan_id}")




                    if (layoutList.size > 0) {
                        val defaultPlayLayoutData = DefaultPlayLayoutData(layoutList[0].layout_id, layoutList[0].screenWidth,
                            layoutList[0].screenHeight, layoutList[0].orientation,
                            layoutList[0].layout_top, layoutList[0].layout_center,
                            layoutList[0].layout_bottom, layoutList[0].layoutOrientation, layoutList[0].plan_id)

                        if (defaultLayoutPlayList!!.size == 0) {
                            defaultPlayLayoutDataDB!!.defaultPlayLayoutDataDao().insert(defaultPlayLayoutData)
                        } else {
                            defaultPlayLayoutDataDB!!.defaultPlayLayoutDataDao().update(defaultPlayLayoutData)
                        }



                        //defaultLayoutPlayList = defaultPlayLayoutDataDB!!.defaultPlayLayoutDataDao().getAll() as ArrayList<DefaultPlayLayoutData>
                        //Log.e(mTag, "defaultLayoutPlayList = ${defaultLayoutPlayList!!.size}")

                        val successIntent = Intent()
                        successIntent.action = Constants.ACTION.ACTION_GET_LAYOUT_SUCCESS
                        successIntent.putExtra("PLAN_ID", layoutList[0].plan_id)
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

    private fun getAdSetting(plan_id: Int) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("plan_id", plan_id)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        ApiFunc().getAdSetting(jsonObject, getAdSettingback)
    }

    private var getAdSettingback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread(netErrRunnable)

        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.e(mTag, "onResponse : "+response.body.toString())

            //val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            val jsonStr = response.body!!.string()
            Log.e(mTag, "jsonStr = $jsonStr")
            runOnUiThread {
                try {
                    adSettingList.clear()
                    val listType = object : TypeToken<ArrayList<RecvAdSetting>>() {}.type
                    adSettingList = Gson().fromJson(jsonStr, listType)

                    Log.e(mTag, "adSettingList.size = " + adSettingList.size)

                    Log.e(mTag, "adSettingList[0].plan_name = ${adSettingList[0].plan_name}")

                    if (adSettingList.size > 0) {

                        val defaultPlayAdSettingData = DefaultPlayAdSettingData(adSettingList[0].plan_id, adSettingList[0].plan_name,
                            adSettingList[0].plan_marquee, adSettingList[0].plan_images,
                            adSettingList[0].plan_videos, adSettingList[0].marquee_mode,
                            adSettingList[0].images_mode, adSettingList[0].videos_mode)

                        if (defaultAdSettingPlayList!!.size == 0) {
                            defaultPlayAdSettingDataDB!!.defaultPlayAdSettingDataDao().insert(defaultPlayAdSettingData)
                        } else {
                            defaultPlayAdSettingDataDB!!.defaultPlayAdSettingDataDao().update(defaultPlayAdSettingData)
                        }


                        val successIntent = Intent()
                        successIntent.action = Constants.ACTION.ACTION_GET_AD_SETTING_SUCCESS
                        mContext?.sendBroadcast(successIntent)
                    } else {
                        val failedIntent = Intent()
                        failedIntent.action = Constants.ACTION.ACTION_GET_AD_SETTING_FAILED
                        mContext?.sendBroadcast(failedIntent)
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

    //private fun getMarquee(name: String) {
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
            Log.e(mTag, "onResponse : "+response.body.toString())
            //val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            //val jsonStr = ReceiveTransform.addToJsonArrayStr(response.body!!.string())
            val jsonStr = response.body!!.string()
            Log.e(mTag, "jsonStr = $jsonStr")
            //val json = JSONObject(response.body!!.string())
            runOnUiThread {
                try {
                    marqueeList.clear()
                    val listType = object : TypeToken<ArrayList<RecvMarquee>>() {}.type

                    marqueeList = Gson().fromJson(jsonStr, listType)

                    Log.e(mTag, "marqueeList.size = " + marqueeList.size)

                    Log.e(mTag, "marqueeList = $marqueeList")
                    //for (i in 0 until advertiseList.size) {
                        //Log.e(mTag, "advertiseList[$i] = ${advertiseList.get(i).ad_path}")
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
    }

    fun downloadImages() {
        Log.e(mTag, "downloadImages")
        if (imageList.size > 0) {
            for (i in imageList.indices) {
                val srcPath = "$server_images_folder/${imageList[i]}"
                val destPath = "$dest_images_folder${imageList[i]}"
                Log.e(mTag, "srcPath = $srcPath")
                Log.e(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(!file.exists()) {
                    Thread {
                        try {
                            val totalSize = download(srcPath, destPath) { progress, length ->
                                // handling the result on main thread
                                handler.sendMessage(handler.obtainMessage(0, progress to length))
                            }
                        } catch (ex: Exception) {

                            Log.e(mTag, ex.toString())

                        }
                    }.start()
                } else {
                    Log.e(mTag, "file: ${imageList[i]} exist. ")
                }


            }

            val successIntent = Intent()
            successIntent.action = Constants.ACTION.ACTION_GET_IMAGES_SUCCESS
            mContext?.sendBroadcast(successIntent)
        } else {
            val emptyIntent = Intent()
            emptyIntent.action = Constants.ACTION.ACTION_GET_IMAGES_EMPTY
            mContext?.sendBroadcast(emptyIntent)
        }
    }

    fun downloadVideos() {
        downloadVideoComplete = 0

        if (videoList.size > 0) {

            for (i in videoList.indices) {
                val srcPath = "$server_videos_folder/${videoList[i]}"
                val destPath = "$dest_videos_folder${videoList[i]}"
                Log.e(mTag, "srcPath = $srcPath")
                Log.e(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(!file.exists()) {
                    Thread {
                        try {
                            val totalSize = download(srcPath, destPath) { progress, length ->
                                // handling the result on main thread
                                handler.sendMessage(handler.obtainMessage(0, progress to length))
                            }
                            downloadVideoComplete += 1
                            downloadVideoReadyArray[i] = true

                            val successIntent = Intent()
                            successIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_SUCCESS
                            successIntent.putExtra("idx", i)
                            successIntent.putExtra("fileName", videoList[i])
                            mContext?.sendBroadcast(successIntent)

                        } catch (ex: Exception) {

                            Log.e(mTag, ex.toString())

                        }

                    }.start()

                } else {
                    Log.e(mTag, "file: ${videoList[i]} exist. ")
                    downloadVideoComplete += 1
                    downloadVideoReadyArray[i] = true

                    val successIntent = Intent()
                    successIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_SUCCESS
                    successIntent.putExtra("idx", i)
                    successIntent.putExtra("fileName", videoList[i])
                    mContext?.sendBroadcast(successIntent)
                }
            }


        } else {
            val emptyIntent = Intent()
            emptyIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_EMPTY
            mContext?.sendBroadcast(emptyIntent)
        }
    }

    fun download(link: String, path: String, progress: ((Long, Long) -> Unit)? = null): Long {
        val url = URL(link)
        val connection = url.openConnection()
        connection.connect()
        val length = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) connection.contentLengthLong else
            connection.contentLength.toLong()
        url.openStream().use { input ->
            FileOutputStream(File(path)).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesRead = input.read(buffer)
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

    fun playAd() {
        Log.e(mTag, "playAd Start")
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
        if (videoViewTop!!.isPlaying) {
            videoViewTop!!.stopPlayback()
        }
        if (videoViewCenter!!.isPlaying) {
            videoViewCenter!!.stopPlayback()
        }
        if (videoViewBottom!!.isPlaying) {
            videoViewBottom!!.stopPlayback()
        }

        videoViewLayoutTop!!.visibility = View.GONE
        videoViewLayoutCenter!!.visibility = View.GONE
        videoViewLayoutBottom!!.visibility = View.GONE

        /*
        current_text_index_top = -1
        current_play_index_top = -1
        current_video_index_top = -1
        current_text_index_center = -1
        current_play_index_center = -1
        current_video_index_center = -1
        current_text_index_bottom = -1
        current_play_index_bottom = -1
        current_video_index_bottom = -1

        */
        current_video_index_center = -1

        if (countDownTimerMarqueeRunning) {
            countDownTimerMarquee.cancel()
        }

        if (countDownTimerImageRunning) {
            countDownTimerImage.cancel()
        }

        if (layoutList.size > 0 ) {
            if (layoutList.size == 1) { //only one layout
                if (adSettingList.size == 1) { // must have adSetting
                    val orientation = layoutList[0].orientation
                    val layout_top = layoutList[0].layout_top
                    val layout_center = layoutList[0].layout_center
                    val layout_bottom = layoutList[0].layout_bottom

                    val marquee_mode = adSettingList[0].marquee_mode
                    val images_mode = adSettingList[0].images_mode
                    val videos_mode = adSettingList[0].videos_mode

                    Log.e(mTag, "orientation = $orientation, layout_top = $layout_top, layout_center = $layout_center, layout_bottom = $layout_bottom")
                    //mode = 0 => cycle, mode = 1 => random
                    Log.e(mTag, "marquee_mode = $marquee_mode, images_mode = $images_mode, videos_mode = $videos_mode")

                    if (orientation == 2) { //landscape
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        currentOrientation = 2
                    } else { //orientation == 1
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        currentOrientation = 1
                    }

                    //top
                    when (layout_top) {
                        0-> { //no
                            Log.d(mTag, "layout_top => no")

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
                            //imageViewTop!!.visibility = View.GONE
                            //imageViewTop2!!.visibility = View.GONE
                            //videoViewLayoutTop!!.visibility = View.GONE
                            //videoViewTop!!.stopPlayback()
                        }
                        2 -> { //image
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.0f
                            )
                            linearLayoutTop!!.layoutParams = param
                            linearLayoutTop!!.visibility = View.VISIBLE
                            //textViewTop!!.visibility = View.GONE
                            imageViewTop!!.visibility = View.VISIBLE
                            //imageViewTop2!!.visibility = View.GONE
                            //videoViewLayoutTop!!.visibility = View.GONE
                            //videoViewTop!!.stopPlayback()
                        }
                        3 -> { //video
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.0f
                            )
                            linearLayoutTop!!.layoutParams = param
                            linearLayoutTop!!.visibility = View.VISIBLE
                            //textViewTop!!.visibility = View.GONE
                            //imageViewTop!!.visibility = View.GONE
                            //imageViewTop2!!.visibility = View.GONE
                            videoViewLayoutTop!!.visibility = View.VISIBLE
                            /*
                            current_video_index_top = 0
                            val uri = Uri.parse(videoList[0])
                            videoViewTop!!.setVideoURI(uri)

                            videoViewTop!!.start()

                            videoViewTop!!.setOnCompletionListener {
                                if (current_video_index_top == videoList.size - 1) {
                                    current_video_index_top = 0
                                } else {
                                    current_video_index_top += 1
                                }

                                val uri = Uri.parse(videoList[current_video_index_top])
                                videoViewTop!!.setVideoURI(uri)
                                videoViewTop!!.start()
                            }
                            */
                        }

                    }


                    //center
                    when (layout_center) {
                        0-> { //no
                            Log.d(mTag, "layout_center => no")
                        }
                        1 -> { //text

                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                3.0f
                            )
                            linearLayoutCenter!!.layoutParams = param

                            linearLayoutCenter!!.visibility = View.VISIBLE

                            /*
                            current_text_index_center = 0
                            textViewCenter!!.text = marqueeList[current_text_index_center]
                            val timer = object : CountDownTimer(40000, 40000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    Log.e(mTag, "textViewCenter = $millisUntilFinished")
                                }

                                override fun onFinish() { //结束后的操作

                                    if (current_text_index_center == (marqueeList.size - 1)) {
                                        current_text_index_center = 0
                                    } else {
                                        current_text_index_center += 1
                                    }
                                    textViewCenter!!.text = marqueeList[current_text_index_center]
                                    this.start()
                                }
                            }.start()
                            */
                        }
                        2 -> { //image
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.0f
                            )
                            linearLayoutCenter!!.layoutParams = param
                            linearLayoutCenter!!.visibility = View.VISIBLE
                            //textViewCenter!!.visibility = View.GONE
                            imageViewCenter!!.visibility = View.VISIBLE
                            //imageViewCenter2!!.visibility = View.GONE
                            //videoViewLayoutCenter!!.visibility = View.GONE
                            //videoViewCenter!!.stopPlayback()
                            /*
                            val animSlideLeft = AnimationUtils.loadAnimation(mContext, R.anim.slide_left)
                            val animSlideRight = AnimationUtils.loadAnimation(mContext, R.anim.slide_right)
                            val animMoveFromLeft = AnimationUtils.loadAnimation(mContext, R.anim.move_from_left)
                            val animMoveToRight = AnimationUtils.loadAnimation(mContext, R.anim.move_to_right)
                            val animMoveFromRight = AnimationUtils.loadAnimation(mContext, R.anim.move_from_right)
                            val animMoveToLeft = AnimationUtils.loadAnimation(mContext, R.anim.move_to_left)

                            //current_play_index_center = 0
                            current_play_index_center = Random.nextInt(advertiseList.size)

                            val imageUrl = advertiseList[current_play_index_center].ad_path
                            Picasso.with(mContext).load(imageUrl).into(imageViewCenter)
                            imageViewCenter!!.startAnimation(animMoveFromRight)

                            countDownTimerCenterRunning = true
                            countDownTimerCenter = object : CountDownTimer(10000, 10000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    Log.e(mTag, "imageview center millisUntilFinished = $millisUntilFinished")

                                }

                                override fun onFinish() { //结束后的操作
                                    countDownTimerCenterRunning = false
                                    Log.e(mTag, "imageview center finish")
                                    var next: Int
                                    do {
                                        next = Random.nextInt(advertiseList.size)
                                    } while (next == current_play_index_center)
                                    current_play_index_center = next
                                    //current_play_index_center = Random.nextInt(advertiseList.size)
                                    val imageUrl = advertiseList[current_play_index_center].ad_path
                                    if (imageViewCenter!!.visibility == View.VISIBLE) {
                                        imageViewCenter!!.startAnimation(animMoveToLeft)
                                        imageViewCenter!!.visibility = View.GONE

                                        imageViewCenter2!!.visibility = View.VISIBLE
                                        Picasso.with(mContext).load(imageUrl).into(imageViewCenter2)
                                        imageViewCenter2!!.startAnimation(animMoveFromRight)
                                    } else { //imageViewCenter!!.visibility == View.GONE
                                        imageViewCenter2!!.startAnimation(animMoveToLeft)
                                        imageViewCenter2!!.visibility = View.GONE

                                        imageViewCenter!!.visibility = View.VISIBLE
                                        Picasso.with(mContext).load(imageUrl).into(imageViewCenter)
                                        imageViewCenter!!.startAnimation(animMoveFromRight)
                                    }

                                    this.start()
                                    countDownTimerCenterRunning = true
                                }
                            }.start()
                            */
                        }
                        3 -> { //video
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.0f
                            )
                            linearLayoutCenter!!.layoutParams = param
                            linearLayoutCenter!!.visibility = View.VISIBLE
                            //textViewCenter!!.visibility = View.GONE
                            //imageViewCenter!!.visibility = View.GONE
                            //imageViewCenter2!!.visibility = View.GONE
                            videoViewLayoutCenter!!.visibility = View.VISIBLE
                            /*
                            current_video_index_center = 0
                            val uri = Uri.parse(videoList[0])
                            videoViewCenter!!.setVideoURI(uri)

                            videoViewCenter!!.start()

                            videoViewCenter!!.setOnCompletionListener {
                                if (current_video_index_center == videoList.size - 1) {
                                    current_video_index_center = 0
                                } else {
                                    current_video_index_center += 1
                                }

                                val uri = Uri.parse(videoList[current_video_index_center])
                                videoViewCenter!!.setVideoURI(uri)
                                videoViewCenter!!.start()
                            }
                            */
                        }

                    }


                    //bottom
                    when (layout_bottom) {
                        0-> { //no
                            Log.d(mTag, "layout_bottom => no")

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
                            //imageViewBottom!!.visibility = View.GONE
                            //imageViewBottom2!!.visibility = View.GONE
                            //videoViewLayoutBottom!!.visibility = View.GONE
                            //videoViewBottom!!.stopPlayback()
                            /*
                            current_text_index_center = 0
                            textViewBottom!!.text = marqueeList[current_text_index_center]
                            val timer = object : CountDownTimer(40000, 40000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    Log.e(mTag, "textViewBottom = $millisUntilFinished")
                                }

                                override fun onFinish() { //结束后的操作

                                    if (current_text_index_bottom == (marqueeList.size - 1)) {
                                        current_text_index_bottom = 0
                                    } else {
                                        current_text_index_bottom += 1
                                    }
                                    textViewTop!!.text = marqueeList[current_text_index_bottom]
                                    this.start()
                                }
                            }.start()
                            */
                        }
                        2 -> { //image
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.0f
                            )
                            linearLayoutBottom!!.layoutParams = param
                            linearLayoutBottom!!.visibility = View.VISIBLE
                            //textViewBottom!!.visibility = View.GONE
                            imageViewBottom!!.visibility = View.VISIBLE
                            //imageViewBottom2!!.visibility = View.GONE
                            //videoViewLayoutBottom!!.visibility = View.GONE
                            //videoViewBottom!!.stopPlayback()
                            /*
                            //current_play_index_bottom = 0
                            current_play_index_bottom = Random.nextInt(advertiseList.size)

                            val animMoveFromLeft = AnimationUtils.loadAnimation(mContext, R.anim.move_from_left)
                            val animMoveToRight = AnimationUtils.loadAnimation(mContext, R.anim.move_to_right)

                            val imageUrl = advertiseList[current_play_index_bottom].ad_path
                            Picasso.with(mContext).load(imageUrl).into(imageViewBottom)
                            imageViewBottom!!.startAnimation(animMoveFromLeft)

                            countDownTimerBottomRunning = true
                            countDownTimerBottom = object : CountDownTimer(10000, 10000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    Log.e(mTag, "imageview bottom millisUntilFinished = $millisUntilFinished")

                                }

                                override fun onFinish() { //结束后的操作
                                    Log.e(mTag, "imageview bottom finish")
                                    countDownTimerBottomRunning = false
                                    var next: Int
                                    do {
                                        next = Random.nextInt(advertiseList.size)
                                    } while (next == current_play_index_bottom)
                                    current_play_index_bottom = next
                                    //current_play_index_bottom = Random.nextInt(advertiseList.size)
                                    val imageUrl = advertiseList[current_play_index_bottom].ad_path
                                    if (imageViewBottom!!.visibility == View.VISIBLE) {
                                        imageViewBottom!!.startAnimation(animMoveToRight)
                                        imageViewBottom!!.visibility = View.GONE

                                        imageViewBottom2!!.visibility = View.VISIBLE
                                        Picasso.with(mContext).load(imageUrl).into(imageViewBottom2)
                                        imageViewBottom2!!.startAnimation(animMoveFromLeft)
                                    } else {
                                        imageViewBottom2!!.startAnimation(animMoveToRight)
                                        imageViewBottom2!!.visibility = View.GONE

                                        imageViewBottom!!.visibility = View.VISIBLE
                                        Picasso.with(mContext).load(imageUrl).into(imageViewBottom)
                                        imageViewBottom!!.startAnimation(animMoveFromLeft)
                                    }

                                    this.start()
                                    countDownTimerBottomRunning = true
                                }
                            }.start()
                            */
                        }
                        3 -> { //video
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2.0f
                            )
                            linearLayoutBottom!!.layoutParams = param
                            linearLayoutBottom!!.visibility = View.VISIBLE
                            //textViewBottom!!.visibility = View.GONE
                            //imageViewBottom!!.visibility = View.GONE
                            //imageViewBottom2!!.visibility = View.GONE
                            videoViewLayoutBottom!!.visibility = View.VISIBLE
                            /*
                            current_video_index_bottom = 0
                            val uri = Uri.parse(videoList[0])
                            videoViewBottom!!.setVideoURI(uri)

                            videoViewBottom!!.start()

                            videoViewBottom!!.setOnCompletionListener {
                                if (current_video_index_bottom == videoList.size - 1) {
                                    current_video_index_bottom = 0
                                } else {
                                    current_video_index_bottom += 1
                                }

                                val uri = Uri.parse(videoList[current_video_index_bottom])
                                videoViewBottom!!.setVideoURI(uri)
                                videoViewBottom!!.start()
                            }
                            */
                        }

                    }


                    // text play timer
                    if (layout_top == 1 || layout_center == 1 || layout_bottom == 1) { //Text
                        countDownTimerMarqueeRunning = true
                        if (layout_top == 1) {
                            if (marquee_mode == 1) { //random
                                var nextTop: Int
                                do {
                                    nextTop = Random.nextInt(playMarqueeList.size)
                                } while (nextTop == current_text_index_top)
                                current_text_index_top = nextTop
                            } else { //circle
                                current_text_index_top = 0
                            }
                            textViewTop!!.text = playMarqueeList[current_text_index_top].content
                        }

                        if (layout_center == 1) {
                            if (marquee_mode == 1) { //random
                                var nextCenter: Int
                                do {
                                    nextCenter = Random.nextInt(playMarqueeList.size)
                                } while (nextCenter == current_text_index_center)
                                current_text_index_center = nextCenter
                            } else { //circle
                                current_text_index_center = 0
                            }
                            textViewCenter!!.text = playMarqueeList[current_text_index_center].content
                        }

                        if (layout_bottom == 1) {
                            if (marquee_mode == 1) { //random
                                var nextBottom: Int
                                do {
                                    nextBottom = Random.nextInt(playMarqueeList.size)
                                } while (nextBottom == current_text_index_bottom)
                                current_text_index_bottom = nextBottom
                            } else { //circle
                                current_text_index_bottom = 0
                            }

                            textViewBottom!!.text = playMarqueeList[current_text_index_bottom].content
                        }


                        countDownTimerMarquee = object : CountDownTimer(40000, 40000) {
                            override fun onTick(millisUntilFinished: Long) {
                                Log.e(mTag, "countDownTimerMarquee = $millisUntilFinished")
                            }

                            override fun onFinish() { //结束后的操作
                                countDownTimerMarqueeRunning = false
                                if (layout_top == 1) {
                                    if (marquee_mode == 1) { //random
                                        var nextTop: Int
                                        do {
                                            nextTop = Random.nextInt(playMarqueeList.size)
                                        } while (nextTop == current_text_index_top)
                                        current_text_index_top = nextTop
                                    } else { //circle
                                        if (current_text_index_top == playMarqueeList.size - 1) {
                                            current_text_index_top = 0
                                        } else {
                                            current_text_index_top += 1
                                        }
                                    }
                                    textViewTop!!.text = playMarqueeList[current_text_index_top].content
                                }

                                if (layout_center == 1) {
                                    if (marquee_mode == 1) { //random
                                        var nextCenter: Int
                                        do {
                                            nextCenter = Random.nextInt(playMarqueeList.size)
                                        } while (nextCenter == current_text_index_center)
                                        current_text_index_center = nextCenter
                                    } else { //circle
                                        if (current_text_index_center == playMarqueeList.size - 1) {
                                            current_text_index_center = 0
                                        } else {
                                            current_text_index_center += 1
                                        }
                                    }
                                    textViewCenter!!.text = playMarqueeList[current_text_index_center].content
                                }

                                if (layout_bottom == 1) {
                                    if (marquee_mode == 1) { //random
                                        var nextBottom: Int
                                        do {
                                            nextBottom = Random.nextInt(playMarqueeList.size)
                                        } while (nextBottom == current_text_index_bottom)
                                        current_text_index_bottom = nextBottom
                                    } else { //circle
                                        if (current_text_index_bottom == playMarqueeList.size - 1) {
                                            current_text_index_bottom = 0
                                        } else {
                                            current_text_index_bottom += 1
                                        }
                                    }
                                    textViewBottom!!.text = playMarqueeList[current_text_index_bottom].content
                                }

                                this.start()
                                countDownTimerMarqueeRunning = true
                            }
                        }.start()
                    } else { //all is not text
                        if (countDownTimerMarqueeRunning) {
                            countDownTimerMarquee.cancel()
                            countDownTimerMarqueeRunning = false
                        }
                    }

                    // image play time
                    if (layout_top == 2 || layout_center == 2 || layout_bottom == 2) {
                        countDownTimerImageRunning = true
                        val animMoveFromLeft = AnimationUtils.loadAnimation(mContext, R.anim.move_from_left)
                        val animMoveToRight = AnimationUtils.loadAnimation(mContext, R.anim.move_to_right)
                        val animMoveFromRight = AnimationUtils.loadAnimation(mContext, R.anim.move_from_right)
                        val animMoveToLeft = AnimationUtils.loadAnimation(mContext, R.anim.move_to_left)

                        if (layout_top == 2) {
                            if (images_mode == 1) { //random
                                var nextTop: Int
                                do {
                                    nextTop = Random.nextInt(imageList.size)
                                } while (nextTop == current_play_index_top)
                                current_play_index_top = nextTop
                            } else { //circle
                                if (current_play_index_top == imageList.size - 1) {
                                    current_play_index_top = 0
                                } else {
                                    current_play_index_top += 1
                                }
                            }
                            //top
                            //val imageUrlTop = advertiseList[current_play_index_top].ad_path
                            imageViewTop!!.visibility = View.VISIBLE
                            //Picasso.with(mContext).load(imageUrlTop).into(imageViewTop)
                            val srcPath = "$dest_images_folder/${imageList[current_play_index_top]}"
                            val file = File(srcPath)
                            imageViewTop!!.setImageURI(Uri.fromFile(file))

                            imageViewTop!!.startAnimation(animMoveFromLeft)
                        } else {
                            imageViewTop!!.visibility = View.GONE
                            imageViewTop2!!.visibility = View.GONE
                        }

                        if (layout_center == 2) {
                            if (images_mode == 1) { //random
                                var nextCenter: Int
                                do {
                                    nextCenter = Random.nextInt(imageList.size)
                                } while (nextCenter == current_play_index_center)
                                current_play_index_center = nextCenter
                            } else { //circle
                                if (current_play_index_center == imageList.size - 1) {
                                    current_play_index_center = 0
                                } else {
                                    current_play_index_center += 1
                                }
                            }


                            //center
                            //val imageUrlCenter = advertiseList[current_play_index_center].ad_path
                            imageViewCenter!!.visibility = View.VISIBLE
                            //Picasso.with(mContext).load(imageUrlCenter).into(imageViewCenter)
                            val srcPath = "$dest_images_folder/${imageList[current_play_index_center]}"
                            val file = File(srcPath)
                            imageViewCenter!!.setImageURI(Uri.fromFile(file))
                            imageViewCenter!!.startAnimation(animMoveFromLeft)
                        } else {
                            imageViewCenter!!.visibility = View.GONE
                            imageViewCenter2!!.visibility = View.GONE
                        }

                        if (layout_bottom == 2) {
                            if (images_mode == 1) { //random
                                var nextBottom: Int
                                do {
                                    nextBottom = Random.nextInt(imageList.size)
                                } while (nextBottom == current_play_index_bottom)
                                current_play_index_bottom = nextBottom
                            } else { //circle
                                if (current_play_index_bottom == imageList.size - 1) {
                                    current_play_index_bottom = 0
                                } else {
                                    current_play_index_bottom += 1
                                }
                            }


                            //center
                            //val imageUrlBottom = advertiseList[current_play_index_bottom].ad_path
                            imageViewBottom!!.visibility = View.VISIBLE
                            //Picasso.with(mContext).load(imageUrlBottom).into(imageViewBottom)
                            val srcPath = "$dest_images_folder/${imageList[current_play_index_bottom]}"
                            val file = File(srcPath)
                            imageViewBottom!!.setImageURI(Uri.fromFile(file))
                            imageViewBottom!!.startAnimation(animMoveFromLeft)
                        } else {
                            imageViewBottom!!.visibility = View.GONE
                            imageViewBottom2!!.visibility = View.GONE
                        }


                        countDownTimerImage = object : CountDownTimer(10000, 10000) {
                            override fun onTick(millisUntilFinished: Long) {
                                Log.e(mTag, "countDownTimerImage millisUntilFinished = $millisUntilFinished")

                            }

                            override fun onFinish() { //结束后的操作
                                Log.e(mTag, "countDownTimerImage finish")
                                countDownTimerImageRunning = false
                                if (layout_top == 2) {
                                    if (images_mode == 1) { //random
                                        var nextTop: Int
                                        do {
                                            nextTop = Random.nextInt(imageList.size)
                                        } while (nextTop == current_play_index_top)
                                        current_play_index_top = nextTop
                                    } else { //circle
                                        if (current_play_index_top == imageList.size - 1) {
                                            current_play_index_top = 0
                                        } else {
                                            current_play_index_top += 1
                                        }
                                    }


                                    //top
                                    //val imageUrlTop = advertiseList[current_play_index_top].ad_path
                                    val srcPath = "$dest_images_folder/${imageList[current_play_index_top]}"
                                    val file = File(srcPath)

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

                                if (layout_center == 2) {
                                    if (images_mode == 1) { //random
                                        var nextCenter: Int
                                        do {
                                            nextCenter = Random.nextInt(imageList.size)
                                        } while (nextCenter == current_play_index_center)
                                        current_play_index_center = nextCenter
                                    } else { //circle
                                        if (current_play_index_center == imageList.size - 1) {
                                            current_play_index_center = 0
                                        } else {
                                            current_play_index_center += 1
                                        }
                                    }

                                    //center
                                    //val imageUrlCenter = advertiseList[current_play_index_center].ad_path
                                    val srcPath = "$dest_images_folder/${imageList[current_play_index_center]}"
                                    val file = File(srcPath)
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

                                if (layout_bottom == 2) {
                                    if (images_mode == 1) { //random
                                        var nextBottom: Int
                                        do {
                                            nextBottom = Random.nextInt(imageList.size)
                                        } while (nextBottom == current_play_index_bottom)
                                        current_play_index_bottom = nextBottom
                                    } else { //circle
                                        if (current_play_index_bottom == imageList.size - 1) {
                                            current_play_index_bottom = 0
                                        } else {
                                            current_play_index_bottom += 1
                                        }
                                    }


                                    //bottom
                                    //val imageUrlBottom = advertiseList[current_play_index_bottom].ad_path
                                    val srcPath = "$dest_images_folder/${imageList[current_play_index_bottom]}"
                                    val file = File(srcPath)
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

                                this.start()
                                countDownTimerImageRunning = true
                            }
                        }.start()
                    } else { //all is not image
                        if (countDownTimerImageRunning) {
                            countDownTimerImage.cancel()
                            countDownTimerImageRunning = false
                        }

                    }

                    //video
                    if (layout_top == 3 || layout_center == 3 || layout_bottom == 3) { //video

                        //top
                        if (layout_top == 3) {
                            if (videos_mode == 1) { //random
                                var nextTop: Int
                                do {
                                    nextTop = Random.nextInt(videoList.size)
                                } while (nextTop == current_video_index_top)
                                current_video_index_top = nextTop
                            } else { //circle
                                if (current_video_index_top == videoList.size - 1) {
                                    current_video_index_top = 0
                                } else {
                                    current_video_index_top += 1
                                }
                            }


                            //top
                            val filePath = "$dest_videos_folder${videoList[current_video_index_top]}"
                            val file = File(filePath)
                            val uriTop = Uri.fromFile(file)
                            //val uriTop = Uri.parse(videoList[current_video_index_top])
                            videoViewTop!!.setVideoURI(uriTop)
                            videoViewTop!!.start()
                            countDownTimerVideoRunningTop = true
                            /*
                            videoViewTop!!.setOnPreparedListener { mp->
                                Log.d(mTag, "videoViewTop prepared")
                                //mp.start()
                                //countDownTimerVideoRunningTop = true
                            }*/

                            videoViewTop!!.setOnErrorListener { mp, what, extra ->
                                Log.d("video", "setOnErrorListener ")
                                true
                            }
                            videoViewTop!!.setOnCompletionListener {
                                countDownTimerVideoRunningTop = false
                                if (videos_mode == 1) { //random
                                    var next: Int
                                    do {
                                        next = Random.nextInt(videoList.size)
                                    } while (next == current_video_index_top)
                                    current_video_index_top = next
                                } else { //circle
                                    if (current_video_index_top == videoList.size - 1) {
                                        current_video_index_top = 0
                                    } else {
                                        current_video_index_top += 1
                                    }
                                }
                                val srcPath = "$dest_videos_folder${videoList[current_video_index_top]}"
                                val fileVideo = File(srcPath)
                                val uriTopVideo = Uri.fromFile(fileVideo)

                                //val uri = Uri.parse(videoList[current_video_index_top])
                                videoViewTop!!.setVideoURI(uriTopVideo)
                                videoViewTop!!.start()
                                countDownTimerVideoRunningTop = true
                            }
                        } else {
                            videoViewTop!!.stopPlayback()
                            countDownTimerVideoRunningTop = false
                        }

                        //center
                        if (layout_center == 3) {
                            if (videos_mode == 1) { //random
                                var nextTop: Int
                                do {
                                    nextTop = Random.nextInt(videoList.size)
                                } while (nextTop == current_video_index_center)
                                current_video_index_center = nextTop
                            } else { //circle
                                current_video_index_center += 1
                                if (current_video_index_center >= videoList.size) {
                                    current_video_index_center = 0
                                }
                            }

                            //top
                            val filePath = "$dest_videos_folder${videoList[current_video_index_center]}"
                            val file = File(filePath)
                            val uriCenter = Uri.fromFile(file)


                            //center
                            //val uriCenter = Uri.parse(videoList[current_video_index_center])
                            videoViewCenter!!.setVideoURI(uriCenter)
                            //videoViewCenter!!.start()
                            //countDownTimerVideoRunningCenter = true

                            videoViewCenter!!.setOnPreparedListener { mp->
                                Log.d(mTag, "videoViewCenter prepared")
                                mp.start()
                                countDownTimerVideoRunningCenter = true
                            }
                            videoViewCenter!!.setOnErrorListener { mp, what, extra ->
                                Log.d("video", "setOnErrorListener ")
                                true
                            }

                            videoViewCenter!!.setOnCompletionListener { mp->
                                Log.e(mTag, "videoViewCenter play complete")
                                //mp.reset()
                                countDownTimerVideoRunningCenter = false
                                if (videos_mode == 1) { //random
                                    var next: Int
                                    do {
                                        next = Random.nextInt(videoList.size)
                                    } while (next == current_video_index_center)
                                    current_video_index_center = next
                                } else { //circle
                                    current_video_index_center += 1
                                    if (current_video_index_center >= videoList.size) {
                                        current_video_index_center = 0
                                    }
                                }
                                val srcPath = "$dest_videos_folder${videoList[current_video_index_center]}"
                                val fileVideo = File(srcPath)
                                val uriCenterVideo = Uri.fromFile(fileVideo)

                                //val uri = Uri.parse(videoList[current_video_index_center])
                                videoViewCenter!!.setVideoURI(uriCenterVideo)
                                //videoViewCenter!!.start()
                                //countDownTimerVideoRunningCenter = true
                            }
                        } else {
                            videoViewCenter!!.stopPlayback()
                            countDownTimerVideoRunningCenter = false
                        }

                        //bottom
                        if (layout_bottom == 3) {
                            if (videos_mode == 1) { //random
                                var nextBottom: Int
                                do {
                                    nextBottom = Random.nextInt(videoList.size)
                                } while (nextBottom == current_video_index_bottom)
                                current_video_index_bottom = nextBottom
                            } else { //circle
                                current_video_index_bottom += 1
                                if (current_video_index_bottom >= videoList.size) {
                                    current_video_index_bottom = 0
                                }
                            }

                            Log.e(mTag, "start => current_video_index_bottom = $current_video_index_bottom")


                            val filePath = "$dest_videos_folder${videoList[current_video_index_bottom]}"
                            Log.e(mTag, "=> filePath = $filePath")
                            val file = File(filePath)
                            val uriBottom = Uri.fromFile(file)

                            //val uriBottom = Uri.parse(videoList[current_video_index_bottom])
                            videoViewBottom!!.setVideoURI(uriBottom)
                            videoViewBottom!!.start()
                            countDownTimerVideoRunningBottom = true
                            /*
                            videoViewBottom!!.setOnPreparedListener { mp->

                                Log.d(mTag, "videoViewBottom prepared")
                                //mp.start()
                                //countDownTimerVideoRunningBottom = true
                            }
                            */
                            //videoViewBottom!!.start()
                            //countDownTimerVideoRunningBottom = true
                            videoViewBottom!!.setOnErrorListener { mp, what, extra ->
                                Log.d("video", "setOnErrorListener ")
                                true
                            }
                            videoViewBottom!!.setOnCompletionListener { mp->
                                Log.e(mTag, "videoViewBottom play complete")
                                //mp.reset()
                                countDownTimerVideoRunningBottom = false
                                if (videos_mode == 1) { //random
                                    var nextBottom: Int
                                    do {
                                        nextBottom = Random.nextInt(videoList.size)
                                    } while (nextBottom == current_video_index_bottom)
                                    current_video_index_bottom = nextBottom
                                } else { //circle
                                    current_video_index_bottom += 1
                                    if (current_video_index_bottom >= videoList.size) {
                                        current_video_index_bottom = 0
                                    }
                                }
                                Log.e(mTag, "videoList.size = ${videoList.size}, current_video_index_bottom = $current_video_index_bottom")
                                val srcPath = "$dest_videos_folder${videoList[current_video_index_bottom]}"
                                Log.e(mTag, "==>srcPath = $srcPath")

                                val fileVideo = File(srcPath)
                                val uriBottomVideo = Uri.fromFile(fileVideo)

                                //val uri = Uri.parse(videoList[current_video_index_bottom])
                                videoViewBottom!!.setVideoURI(uriBottomVideo)
                                videoViewBottom!!.start()
                                countDownTimerVideoRunningBottom = true
                            }
                        } else {
                            videoViewBottom!!.stopPlayback()
                            countDownTimerVideoRunningBottom = false
                        }
                    } else { //all are not video
                        if (countDownTimerVideoRunningTop) {
                            videoViewTop!!.stopPlayback()
                            countDownTimerVideoRunningTop = false
                        }

                        if (countDownTimerVideoRunningCenter) {
                            videoViewCenter!!.stopPlayback()
                            countDownTimerVideoRunningCenter = false
                        }

                        if (countDownTimerVideoRunningBottom) {
                            videoViewBottom!!.stopPlayback()
                            countDownTimerVideoRunningBottom = false
                        }
                    }
                } else {
                    Log.e(mTag, "No AdSetting!")
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
            Log.e(mTag, "All permission are granted")
            //create local folder
            dest_images_folder = Environment.getExternalStorageDirectory().toString() + "/Download/images/"
            dest_videos_folder = Environment.getExternalStorageDirectory().toString() + "/Download/videos/"
            val imagesDir = File(dest_images_folder)
            imagesDir.mkdirs()
            val videoDir = File(dest_videos_folder)
            videoDir.mkdirs()
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
                        Log.e(mTag, "perms[permissions[$i]] = ${permissions[i]}")

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

                    } else {
                        Log.d(mTag, "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
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

    private fun toastLong(message: String) {

        if (toastHandle != null)
            toastHandle!!.cancel()

        toastHandle = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            val toast = Toast.makeText(this, HtmlCompat.fromHtml("<h1>$message</h1>", HtmlCompat.FROM_HTML_MODE_COMPACT), Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
            toast.show()

            toast
        } else { //Android 11
            val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
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
        val editTextDialogServerIP = promptView.findViewById<TextView>(R.id.editTextDialogServerIP)
        val editTextDialogServerPort = promptView.findViewById<TextView>(R.id.editTextDialogServerPort)
        val btnClear = promptView.findViewById<Button>(R.id.btnDialogClear)
        val btnConfirm = promptView.findViewById<Button>(R.id.btnDialogConfirm)

        editTextDialogServerIP.text = "http://"

        alertDialogBuilder.setCancelable(false)
        btnClear!!.setOnClickListener {
            editTextDialogServerIP.text = "http://"
            editTextDialogServerPort.text = ""
        }
        btnConfirm!!.setOnClickListener {

            if (editTextDialogServerIP.text.toString() != "" &&
                editTextDialogServerPort.text.toString() != "") {

                Log.e(mTag,"IP = ${ editTextDialogServerIP.text}")
                Log.e(mTag,"Port = ${ editTextDialogServerPort.text}")

                server_ip_address = editTextDialogServerIP.text.toString()
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
}