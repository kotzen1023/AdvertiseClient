package com.seventhmoon.advertiseclient


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import android.text.InputType
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.Animation
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
import com.seventhmoon.advertiseclient.data.ScrollTextView
import com.seventhmoon.advertiseclient.model.recv.RecvAdSetting
import com.seventhmoon.advertiseclient.model.recv.RecvLayout
import com.seventhmoon.advertiseclient.model.recv.RecvMarquee
import com.seventhmoon.advertiseclient.persistence.DefaultPlayAdSettingData
import com.seventhmoon.advertiseclient.persistence.DefaultPlayAdSettingDataDB
import com.seventhmoon.advertiseclient.persistence.DefaultPlayBannerData
import com.seventhmoon.advertiseclient.persistence.DefaultPlayBannerDataDB
import com.seventhmoon.advertiseclient.persistence.DefaultPlayImagesData
import com.seventhmoon.advertiseclient.persistence.DefaultPlayImagesDataDB
import com.seventhmoon.advertiseclient.persistence.DefaultPlayLayoutData
import com.seventhmoon.advertiseclient.persistence.DefaultPlayLayoutDataDB
import com.seventhmoon.advertiseclient.persistence.DefaultPlayMarqueeData
import com.seventhmoon.advertiseclient.persistence.DefaultPlayMarqueeDataDB
import com.seventhmoon.advertiseclient.persistence.DefaultPlayMixData
import com.seventhmoon.advertiseclient.persistence.DefaultPlayMixDataDB
import com.seventhmoon.advertiseclient.persistence.DefaultPlayVideosData
import com.seventhmoon.advertiseclient.persistence.DefaultPlayVideosDataDB
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.random.Random
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private val mTag = MainActivity::class.java.name
    //var this@MainActivity: Context? = null

    private val requestIdMultiplePermission = 1
    private var pref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private val fileName = "Preference"

    var layoutList: ArrayList<RecvLayout> = ArrayList()

    private var bannerList : ArrayList<String> = ArrayList() // for banner
    private var marqueeList : ArrayList<RecvMarquee> = ArrayList() // for text marquee
    private var playMarqueeList : ArrayList<RecvMarquee> = ArrayList() // for text marquee
    private var imageList : ArrayList<String> = ArrayList() // for image
    private var videoList : ArrayList<String> = ArrayList() // for video
    private var mixList: ArrayList<String> = ArrayList()
    private var adSettingList : ArrayList<RecvAdSetting> = ArrayList()


    private var mReceiver: BroadcastReceiver? = null
    private var isRegister = false
    private var currentTextIndexTop = -1
    private var currentImageIndexTop = -1
    private var currentVideoIndexTop = -1
    private var currentMixIndexTop = -1
    private var currentTextIndexCenter = -1
    private var currentImageIndexCenter = -1
    private var currentVideoIndexCenter = -1
    private var currentMixIndexCenter = -1
    private var currentTextIndexBottom = -1
    private var currentImageIndexBottom = -1
    private var currentVideoIndexBottom = -1
    private var currentMixIndexBottom = -1

    private var rootView: ViewGroup? = null
    //main Linearlayout
    private var mainLinearLayout : LinearLayout ?= null
    private var textViewShowState : TextView ?= null
    private var textViewShowInitSuccess : TextView ?= null
    private var textViewProgress: TextView ?= null
    //top
    private var linearLayoutTop : LinearLayout ?= null
    private var textViewTop : ScrollTextView ?= null
    private var imageViewTop : ImageView ?= null
    private var imageViewTop2 : ImageView ?= null
    private var videoViewLayoutTop: RelativeLayout ?= null
    private var videoViewTop: VideoView ?= null
    //private var imageViewBannerTop: ImageView?= null
    //center
    private var linearLayoutCenter : LinearLayout ?= null
    private var textViewCenter : ScrollTextView ?=     null
    private var imageViewCenter : ImageView ?= null
    private var imageViewCenter2 : ImageView ?= null
    private var videoViewLayoutCenter: RelativeLayout ?= null
    private var videoViewCenter: VideoView ?= null
    //private var imageViewBannerCenter: ImageView?= null
    //bottom
    private var linearLayoutBottom : LinearLayout ?= null
    //var textViewBottom : TextView ?= null
    var textViewBottom : ScrollTextView ?= null
    var imageViewBottom : ImageView ?= null
    var imageViewBottom2 : ImageView ?= null
    private var videoViewLayoutBottom: RelativeLayout ?= null
    private var videoViewBottom: VideoView ?= null
    //private var messageViewBottom: TextView ?= null
    //private var imageViewBannerBottom: ImageView?= null

    private var linearLayoutTriangle : LinearLayout ?= null

    //layout weight
    //private var linearLayoutTriangleWeight : Float = 2.0F

    private var mediaControllerTop: MediaController? = null
    private var mediaControllerCenter: MediaController? = null
    private var mediaControllerBottom: MediaController? = null

    private var deviceID = ""
    private var deviceName = ""
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    var getFirstPingResponse = false
    private var receivePingSuccess = false

    private var toastHandle: Toast? = null

    private var countDownTimerPingWeb : CountDownTimer ?= null
    var countDownTimerPingWebRunning : Boolean = false
    private var pingWebInterval : Long = 60000
    private var prevPingWebInterval : Long = 60000

    private lateinit var countDownTimerMarquee : CountDownTimer
    var countDownTimerMarqueeRunning : Boolean = false
    private lateinit var countDownTimerImage : CountDownTimer
    var countDownTimerImageRunning : Boolean = false

    private lateinit var countDownTimerMixImageTop : CountDownTimer
    var countDownTimerMixImageTopRunning : Boolean = false

    private lateinit var countDownTimerMixImageCenter : CountDownTimer
    var countDownTimerMixImageCenterRunning : Boolean = false

    private lateinit var countDownTimerMixImageBottom : CountDownTimer
    var countDownTimerMixImageBottomRunning : Boolean = false

    private var videoRunningTop : Boolean = false
    private var videoRunningCenter : Boolean = false
    private var videoRunningBottom : Boolean = false

    private var mixVideoRunningTop : Boolean = false
    private var mixVideoRunningCenter : Boolean = false
    private var mixVideoRunningBottom : Boolean = false

    private var currentOrientation = 0
    companion object {
        @JvmStatic var server_ip_address: String = ""
        @JvmStatic var server_webservice_port: String = ""
        @JvmStatic var base_ip_address_webservice: String = ""

        @JvmStatic var server_banner_folder: String = ""
        @JvmStatic var server_images_folder: String = ""
        @JvmStatic var server_videos_folder: String = ""

        @JvmStatic var dest_banner_folder: String = ""
        @JvmStatic var dest_images_folder: String = ""
        @JvmStatic var dest_videos_folder: String = ""
    }

    private val ipAddressPattern: Pattern = Pattern.compile(
        "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                + "|[1-9][0-9]|[0-9]))"
    )

    private val httpPrefix = "http://"
    private val internalDefaultAddress = "192.168.0.253"
    private val testDefaultAddress = "35.194.240.47"
    private val benzKtvAddress = "34.66.27.68"
    private val kHouseDefaultAddress = "35.202.218.122"
    private val defaultIpAddress = kHouseDefaultAddress

    private val handler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            // length may be negative because it is based on http header

            val (progress, length) = msg.obj as Pair<*, *>
            val progressStr = "$progress/$length"
            textViewProgress!!.text = progressStr
            //Log.d(mTag, "progress = $progress, length = $length")
        }
    }

    private var defaultPlayLayoutDataDB: DefaultPlayLayoutDataDB? = null
    private var defaultPlayAdSettingDataDB: DefaultPlayAdSettingDataDB? = null
    private var defaultPlayBannerDataDB: DefaultPlayBannerDataDB? = null
    private var defaultPlayMarqueeDataDB: DefaultPlayMarqueeDataDB? = null
    private var defaultPlayImagesDataDB: DefaultPlayImagesDataDB? = null
    private var defaultPlayVideosDataDB: DefaultPlayVideosDataDB? = null
    private var defaultPlayMixDataDB: DefaultPlayMixDataDB? = null

    private var defaultLayoutPlayList: ArrayList<DefaultPlayLayoutData> ?= ArrayList()
    private var defaultAdSettingPlayList: ArrayList<DefaultPlayAdSettingData> ?= ArrayList()
    private var defaultBannerPlayList: ArrayList<DefaultPlayBannerData> ?= ArrayList()
    private var defaultMarqueePlayList: ArrayList<DefaultPlayMarqueeData> ?= ArrayList()
    private var defaultImagesPlayList: ArrayList<DefaultPlayImagesData> ?= ArrayList()
    private var defaultVideosPlayList: ArrayList<DefaultPlayVideosData> ?= ArrayList()
    private var defaultMixPlayList: ArrayList<DefaultPlayMixData> ?= ArrayList()

    private var downloadBannerComplete: Int = 0
    private var downloadImageComplete: Int = 0
    private var downloadVideoComplete: Int = 0
    private var downloadMixComplete: Int = 0
    private var downloadBannerReadyArray: ArrayList<Boolean> = ArrayList()
    private var downloadImageReadyArray: ArrayList<Boolean> = ArrayList()
    private var downloadVideoReadyArray: ArrayList<Boolean> = ArrayList()
    private var downloadMixReadyArray: ArrayList<Boolean> = ArrayList()

    private var infoRenew = false
    private var isFirstNetworkError = true

    var pingCount: Int = 0
    //for Log
    private var process: Process? = null
    private var debugLog: Boolean = false

    private var planStartTimeString : String = "--:--"
    private var plan2StartTimeString : String = "--:--"
    private var plan3StartTimeString : String = "--:--"
    private var plan4StartTimeString : String = "--:--"

    private var planStartTime: Long = 0
    private var plan2StartTime: Long = 0
    private var plan3StartTime: Long = 0
    private var plan4StartTime: Long = 0

    private var previousPlanId: Int = 0
    private var currentPlanId: Int = 0
    private var currentPlanUse: Int = 0
    private var currentAdSettingIdx: Int = -1

    private var myPid = -1

    //animation
    var animMoveFromLeft : Animation ?= null
    var animMoveToRight : Animation ?= null
    var animMoveFromRight : Animation ?= null
    var animMoveToLeft : Animation ?= null

    //layout global
    private var layoutTop = 0
    private var layoutCenter = 0
    private var layoutBottom = 0

    private var mixMode = 0
    private var mixImageInterval = 0
    private var mixImageScaleType = 0
    private var mixImageAnime = 0
    private var mixVideoScaleType = 0

    private var mixTopRunning = false
    private var mixCenterRunning = false
    private var mixBottomRunning = false

    private var marqueeMode = 0
    private var imagesMode = 0
    private var videosMode = 0

    private var mainLayoutWeight = 0
    private var layoutTopWeight = 0
    private var layoutCenterWeight = 0
    private var layoutBottomWeight = 0
    private var layoutTriangleWeight = 0

    private var layoutTopWidth = 0
    private var layoutCenterWidth = 0
    private var layoutBottomWidth = 0

    private var layoutTopHeight = 0
    private var layoutCenterHeight = 0
    private var layoutBottomHeight = 0

    private val defaultBackGroundColor = "#000000"
    private var pingError = false

    var operationTime: Int = 0

    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myPid = android.os.Process.myPid()
        Log.d(mTag, "myPid = $myPid")

        rootView = findViewById<View>(android.R.id.content) as ViewGroup


        textViewShowState = findViewById(R.id.textViewShowState)
        textViewShowInitSuccess = findViewById(R.id.textViewShowInitSuccess)
        textViewProgress = findViewById(R.id.textViewProgress)

        //mContext = applicationContext



        //handleUncaughtException()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions()
        } else {
            //create default folder
            Log.d(mTag, "create default folder directly!!")
            dest_banner_folder = Environment.getExternalStorageDirectory().toString() + "/Download/banner/"
            dest_images_folder = Environment.getExternalStorageDirectory().toString() + "/Download/images/"
            dest_videos_folder = Environment.getExternalStorageDirectory().toString() + "/Download/videos/"
            val bannerDir = File(dest_banner_folder)
            bannerDir.mkdirs()
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

        val saveScreenWidth = pref!!.getString("SCREEN_WIDTH", "0") as String
        val saveScreenHeight = pref!!.getString("SCREEN_HEIGHT", "0") as String

        Log.d(mTag, "server_ip_address = $server_ip_address")
        Log.d(mTag, "server_webservice_port = $server_webservice_port")

        if (server_ip_address != "" && server_webservice_port != "") {
            server_banner_folder = "$httpPrefix$server_ip_address:$server_webservice_port/uploads/banners"
            server_images_folder = "$httpPrefix$server_ip_address:$server_webservice_port/uploads/images"
            server_videos_folder = "$httpPrefix$server_ip_address:$server_webservice_port/uploads/videos"
        }

        //get screen width and height
        if (saveScreenWidth.toInt() == 0 && saveScreenHeight.toInt() == 0) {
            val displayMetrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
            {
                windowManager.defaultDisplay.getMetrics(displayMetrics)

                screenHeight = displayMetrics.heightPixels
                screenWidth = displayMetrics.widthPixels
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                this@MainActivity.display!!.getRealMetrics(displayMetrics)

                screenHeight = displayMetrics.heightPixels
                screenWidth = displayMetrics.widthPixels
            } else { //Android 11
                //this@MainActivity!!.display!!.getMetrics(displayMetrics)
                screenHeight = windowManager.currentWindowMetrics.bounds.height()
                screenWidth = windowManager.currentWindowMetrics.bounds.width()
            }
        } else {
            screenWidth = saveScreenWidth.toInt()
            screenHeight = saveScreenHeight.toInt()
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
        defaultPlayLayoutDataDB = Room.databaseBuilder(this@MainActivity, DefaultPlayLayoutDataDB::class.java, DefaultPlayLayoutDataDB.DATABASE_NAME)
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
            receiveLayout.layout2_top = defaultLayoutPlayList!![0].getLayout2_top()
            receiveLayout.layout2_center = defaultLayoutPlayList!![0].getLayout2_center()
            receiveLayout.layout2_bottom = defaultLayoutPlayList!![0].getLayout2_bottom()
            receiveLayout.layout3_top = defaultLayoutPlayList!![0].getLayout3_top()
            receiveLayout.layout3_center = defaultLayoutPlayList!![0].getLayout3_center()
            receiveLayout.layout3_bottom = defaultLayoutPlayList!![0].getLayout3_bottom()
            receiveLayout.layout4_top = defaultLayoutPlayList!![0].getLayout4_top()
            receiveLayout.layout4_center = defaultLayoutPlayList!![0].getLayout4_center()
            receiveLayout.layout4_bottom = defaultLayoutPlayList!![0].getLayout4_bottom()
            receiveLayout.layoutOrientation = defaultLayoutPlayList!![0].getLayoutOrientation()
            receiveLayout.plan_id = defaultLayoutPlayList!![0].getPlan_id()
            receiveLayout.plan2_id = defaultLayoutPlayList!![0].getPlan2_id()
            receiveLayout.plan3_id = defaultLayoutPlayList!![0].getPlan3_id()
            receiveLayout.plan4_id = defaultLayoutPlayList!![0].getPlan4_id()
            receiveLayout.plan_start_time = defaultLayoutPlayList!![0].getPlan_start_time()
            receiveLayout.plan2_start_time = defaultLayoutPlayList!![0].getPlan2_start_time()
            receiveLayout.plan3_start_time = defaultLayoutPlayList!![0].getPlan3_start_time()
            receiveLayout.plan4_start_time = defaultLayoutPlayList!![0].getPlan4_start_time()
            receiveLayout.pingWebInterval = defaultLayoutPlayList!![0].getPingWebInterval()
            receiveLayout.plan_layout_top_weight = defaultLayoutPlayList!![0].getPlan_layout_top_weight()
            receiveLayout.plan_layout_center_weight = defaultLayoutPlayList!![0].getPlan_layout_center_weight()
            receiveLayout.plan_layout_bottom_weight = defaultLayoutPlayList!![0].getPlan_layout_bottom_weight()
            receiveLayout.plan_layout_tri_weight = defaultLayoutPlayList!![0].getPlan_layout_tri_weight()

            receiveLayout.plan2_layout_top_weight = defaultLayoutPlayList!![0].getPlan2_layout_top_weight()
            receiveLayout.plan2_layout_center_weight = defaultLayoutPlayList!![0].getPlan2_layout_center_weight()
            receiveLayout.plan2_layout_bottom_weight = defaultLayoutPlayList!![0].getPlan2_layout_bottom_weight()
            receiveLayout.plan2_layout_tri_weight = defaultLayoutPlayList!![0].getPlan2_layout_tri_weight()

            receiveLayout.plan3_layout_top_weight = defaultLayoutPlayList!![0].getPlan3_layout_top_weight()
            receiveLayout.plan3_layout_center_weight = defaultLayoutPlayList!![0].getPlan3_layout_center_weight()
            receiveLayout.plan3_layout_bottom_weight = defaultLayoutPlayList!![0].getPlan3_layout_bottom_weight()
            receiveLayout.plan3_layout_tri_weight = defaultLayoutPlayList!![0].getPlan3_layout_tri_weight()

            receiveLayout.plan4_layout_top_weight = defaultLayoutPlayList!![0].getPlan4_layout_top_weight()
            receiveLayout.plan4_layout_center_weight = defaultLayoutPlayList!![0].getPlan4_layout_center_weight()
            receiveLayout.plan4_layout_bottom_weight = defaultLayoutPlayList!![0].getPlan4_layout_bottom_weight()
            receiveLayout.plan4_layout_tri_weight = defaultLayoutPlayList!![0].getPlan4_layout_tri_weight()

            pingWebInterval = when(receiveLayout.pingWebInterval) {
                1 -> 1000
                2 -> 5000
                3 -> 10000
                4 -> 15000
                5 -> 30000
                else -> 60000
            }

            prevPingWebInterval = pingWebInterval

            planStartTimeString = receiveLayout.plan_start_time
            plan2StartTimeString = receiveLayout.plan2_start_time
            plan3StartTimeString = receiveLayout.plan3_start_time
            plan4StartTimeString = receiveLayout.plan4_start_time

            layoutList.add(receiveLayout)
        }

        //load AdSetting from DB
        defaultPlayAdSettingDataDB = Room.databaseBuilder(this@MainActivity, DefaultPlayAdSettingDataDB::class.java, DefaultPlayAdSettingDataDB.DATABASE_NAME)
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
                adSetting.plan_banner = defaultAdSettingPlayList!![i].getPlan_banner()
                adSetting.plan_mix = defaultAdSettingPlayList!![i].getPlan_mix()
                adSetting.marquee_mode = defaultAdSettingPlayList!![i].getMarquee_mode()
                adSetting.marquee_background = defaultAdSettingPlayList!![i].getMarquee_background()
                adSetting.marquee_text = defaultAdSettingPlayList!![i].getMarquee_text()
                adSetting.marquee_size = defaultAdSettingPlayList!![i].getMarquee_size()
                adSetting.marquee_locate = defaultAdSettingPlayList!![i].getMarquee_locate()
                adSetting.marquee_speed = defaultAdSettingPlayList!![i].getMarquee_speed()
                adSetting.images_mode = defaultAdSettingPlayList!![i].getImages_mode()
                adSetting.videos_mode = defaultAdSettingPlayList!![i].getVideos_mode()
                adSetting.image_interval = defaultAdSettingPlayList!![i].getImage_interval()
                adSetting.image_scale_type = defaultAdSettingPlayList!![i].getImage_scale_type()
                adSetting.image_anime = defaultAdSettingPlayList!![i].getImage_anime()
                adSetting.video_scale_type = defaultAdSettingPlayList!![i].getVideo_scale_type()
                adSetting.banner_scale_type = defaultAdSettingPlayList!![i].getBanner_scale_type()
                adSetting.mix_mode = defaultAdSettingPlayList!![i].getMix_mode()
                adSetting.mix_image_interval = defaultAdSettingPlayList!![i].getMix_image_interval()
                adSetting.mix_image_scale_type = defaultAdSettingPlayList!![i].getMix_image_scale_type()
                adSetting.mix_image_anime = defaultAdSettingPlayList!![i].getMix_image_anime()
                adSetting.mix_video_scale_type = defaultAdSettingPlayList!![i].getMix_video_scale_type()

                adSettingList.add(adSetting)
            }

        }

        //load marquee from DB
        defaultPlayMarqueeDataDB = Room.databaseBuilder(this@MainActivity, DefaultPlayMarqueeDataDB::class.java, DefaultPlayMarqueeDataDB.DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(migration12)
            .build()
        defaultMarqueePlayList = defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().getAll() as ArrayList<DefaultPlayMarqueeData>
        Log.d(mTag, "defaultMarqueePlayList = ${defaultMarqueePlayList!!.size}")

        if (defaultMarqueePlayList!!.size > 0) {
            playMarqueeList.clear()
            marqueeList.clear()

            for (i in defaultMarqueePlayList!!.indices) {
                val marquee = RecvMarquee()
                marquee.marqueeId = defaultMarqueePlayList!![i].getMarqueeId()
                marquee.name = defaultMarqueePlayList!![i].getName()
                marquee.content = defaultMarqueePlayList!![i].getContent()
                playMarqueeList.add(marquee)
                marqueeList.add(marquee)
            }
        }

        //load banner from DB
        defaultPlayBannerDataDB = Room.databaseBuilder(this@MainActivity, DefaultPlayBannerDataDB::class.java, DefaultPlayBannerDataDB.DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(migration12)
            .build()
        defaultBannerPlayList = defaultPlayBannerDataDB!!.defaultPlayBannerDataDao().getAll() as ArrayList<DefaultPlayBannerData>
        Log.d(mTag, "defaultBannerPlayList = ${defaultBannerPlayList!!.size}")

        if (defaultBannerPlayList!!.size > 0) {
            bannerList.clear()
            downloadBannerReadyArray.clear()
            for (i in defaultBannerPlayList!!.indices) {
                bannerList.add(defaultBannerPlayList!![i].getFileName())

                val destPath = "$dest_banner_folder${defaultBannerPlayList!![i].getFileName()}"
                Log.d(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(!file.exists()) {
                    downloadBannerReadyArray.add(false)
                } else {
                    downloadBannerReadyArray.add(true)
                }
            }
        }

        //load images from DB
        defaultPlayImagesDataDB = Room.databaseBuilder(this@MainActivity, DefaultPlayImagesDataDB::class.java, DefaultPlayImagesDataDB.DATABASE_NAME)
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
        defaultPlayVideosDataDB = Room.databaseBuilder(this@MainActivity, DefaultPlayVideosDataDB::class.java, DefaultPlayVideosDataDB.DATABASE_NAME)
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

        //load mix from db
        defaultPlayMixDataDB = Room.databaseBuilder(this@MainActivity, DefaultPlayMixDataDB::class.java, DefaultPlayMixDataDB.DATABASE_NAME)
            .allowMainThreadQueries()
            .addMigrations(migration12)
            .build()
        defaultMixPlayList = defaultPlayMixDataDB!!.defaultPlayMixDataDao().getAll() as ArrayList<DefaultPlayMixData>
        Log.d(mTag, "defaultMixPlayList = ${defaultMixPlayList!!.size}")

        if (defaultMixPlayList!!.size > 0) {
            mixList.clear()
            downloadMixReadyArray.clear()
            for (i in defaultMixPlayList!!.indices) {
                mixList.add(defaultMixPlayList!![i].getFileName())

                val downloadFile = File(mixList[i])
                //val nameWithoutExtension = downloadFile.nameWithoutExtension
                val downloadFileExt = downloadFile.extension
                //var destPath = ""
                val destPath = if (downloadFileExt == "mp4") {
                    "$dest_videos_folder${defaultMixPlayList!![i].getFileName()}"
                } else {
                    "$dest_images_folder${defaultMixPlayList!![i].getFileName()}"
                }

                Log.d(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(!file.exists()) {
                    downloadMixReadyArray.add(false)
                } else {
                    downloadMixReadyArray.add(true)
                }


            }
        }

        //get current plan id, index
        if (layoutList.size > 0) {
            planStartTime = getTimeStampFromString(layoutList[0].plan_start_time)
            plan2StartTime = getTimeStampFromString(layoutList[0].plan2_start_time)
            plan3StartTime = getTimeStampFromString(layoutList[0].plan3_start_time)
            plan4StartTime = getTimeStampFromString(layoutList[0].plan4_start_time)

            //Log.d(mTag, "planStartTime = $planStartTime")
            //Log.d(mTag, "plan2StartTime = $plan2StartTime")
            //Log.d(mTag, "plan3StartTime = $plan3StartTime")
            //Log.d(mTag, "plan4StartTime = $plan4StartTime")

            val currentTimestamp = getCurrentTimeStamp()
            Log.d(mTag, "currentTimestamp = $currentTimestamp")
            Log.d(mTag, "planStartTime = $planStartTime")
            Log.d(mTag, "plan2StartTime = $plan2StartTime")
            Log.d(mTag, "plan3StartTime = $plan3StartTime")
            Log.d(mTag, "plan4StartTime = $plan4StartTime")

            /*if (plan4StartTime in 1..currentTimestamp && layoutList[0].plan4_id != 0) { //plan4
                currentPlanId = layoutList[0].plan4_id
                currentPlanUse = 4
                Log.d(mTag, "plan4, id = ${layoutList[0].plan4_id}")
            } else if (plan3StartTime in 1..currentTimestamp && layoutList[0].plan3_id != 0) { //plan3
                currentPlanId = layoutList[0].plan3_id
                currentPlanUse = 3
                Log.d(mTag, "plan3, id = ${layoutList[0].plan3_id}")
            } else if (plan2StartTime in 1..currentTimestamp && layoutList[0].plan2_id != 0) { //plan2
                currentPlanId = layoutList[0].plan2_id
                currentPlanUse = 2
                Log.d(mTag, "plan2, id = ${layoutList[0].plan2_id}")
            } //else if (planStartTime in 1..currentTimestamp) { //plan1
            else { //plan1
                if (layoutList[0].plan_id > 0) {
                    currentPlanId = layoutList[0].plan_id
                    currentPlanUse = 1
                    Log.d(mTag, "plan1, id = ${layoutList[0].plan_id}")
                }
            }*/
            /*
            if (planStartTime >= plan2StartTime) { //planStartTime
                if (plan3StartTime >= plan4StartTime) { //plan3StartTime
                    if (planStartTime >= plan3StartTime) { //planStartTime
                        if (currentTimestamp > planStartTime) { //use planStartTime
                            if (layoutList[0].plan_id > 0) {
                                currentPlanId = layoutList[0].plan_id
                                currentPlanUse = 1
                                Log.d(mTag, "plan1, id = ${layoutList[0].plan_id}")
                            }
                        }
                    } else { //plan3StartTime
                        if (currentTimestamp > plan3StartTime) { //use plan3StartTime
                            if (layoutList[0].plan3_id > 0) {
                                currentPlanId = layoutList[0].plan3_id
                                currentPlanUse = 3
                                Log.d(mTag, "plan3, id = ${layoutList[0].plan3_id}")
                            }
                        }
                    }
                } else { //plan4StartTime
                    if (planStartTime >= plan4StartTime) { //planStartTime
                        if (currentTimestamp > planStartTime) { //use planStartTime
                            if (layoutList[0].plan_id > 0) {
                                currentPlanId = layoutList[0].plan_id
                                currentPlanUse = 1
                                Log.d(mTag, "plan1, id = ${layoutList[0].plan_id}")
                            }
                        }
                    } else { //plan4StartTime
                        if (currentTimestamp > plan4StartTime) { //use plan4StartTime
                            if (layoutList[0].plan4_id > 0) {
                                currentPlanId = layoutList[0].plan4_id
                                currentPlanUse = 4
                                Log.d(mTag, "plan4, id = ${layoutList[0].plan4_id}")
                            }
                        }
                    }
                }
            } else { //plan2StartTime
                if (plan3StartTime >= plan4StartTime) { //plan3StartTime
                    if (plan2StartTime >= plan3StartTime) { //plan2StartTime
                        if (currentTimestamp > plan2StartTime) { //use plan2StartTime
                            if (layoutList[0].plan2_id > 0) {
                                currentPlanId = layoutList[0].plan2_id
                                currentPlanUse = 2
                                Log.d(mTag, "plan2, id = ${layoutList[0].plan2_id}")
                            }
                        }
                    } else { //plan3StartTime
                        if (currentTimestamp > plan3StartTime) { //use plan3StartTime
                            if (layoutList[0].plan3_id > 0) {
                                currentPlanId = layoutList[0].plan3_id
                                currentPlanUse = 3
                                Log.d(mTag, "plan3, id = ${layoutList[0].plan3_id}")
                            }
                        }
                    }
                } else { //plan4StartTime
                    if (plan2StartTime >= plan4StartTime) { //planStartTime
                        if (currentTimestamp > plan2StartTime) { //use planStartTime
                            if (layoutList[0].plan2_id > 0) {
                                currentPlanId = layoutList[0].plan2_id
                                currentPlanUse = 2
                                Log.d(mTag, "plan2, id = ${layoutList[0].plan2_id}")
                            }
                        }
                    } else { //plan4StartTime
                        if (currentTimestamp > plan4StartTime) { //use plan4StartTime
                            if (layoutList[0].plan4_id > 0) {
                                currentPlanId = layoutList[0].plan4_id
                                currentPlanUse = 4
                                Log.d(mTag, "plan4, id = ${layoutList[0].plan4_id}")
                            }
                        }
                    }
                }
            }*/
            Log.e(mTag, "1->")
            getPlanUse(currentTimestamp)

            //get current plan idx
            if (currentPlanId > 0) {
                for (i in adSettingList.indices) {
                    if (currentPlanId == adSettingList[i].plan_id) {
                        currentAdSettingIdx = i
                        break
                    }
                }
                previousPlanId = currentPlanId
            }
        }


        if (layoutList.size == 1 && adSettingList.size >= 1 &&
            (playMarqueeList.size > 0 ||
                    bannerList.size > 0 ||
                    imageList.size > 0 ||
                    videoList.size > 0 ) ||
                    mixList.size > 0) {

            //because marquee download is unnecessary, start with image
            //downloadImageComplete = 0
            clearAllNotInAllList()
            if (bannerList.size > 0) {
                checkBannerExists()
                //clearBannersNotInBannerList()
            }

            if (imageList.size > 0) {
                checkImagesExists()
                //clearImagesNotInImageList()
            }

            if (videoList.size > 0) {
                checkVideosExists()
                //clearVideosNotInVideoList()
            }

            if (mixList.size > 0) {
                checkMixExists()
                //clearMixNotInMixList()
            }



            infoRenew = true

        } else {
            Log.d(mTag, "no default setting")
        }

        if (server_ip_address == "" || server_webservice_port == "") {
            showInputServerAddressDialog()
        } else {
            base_ip_address_webservice = "$httpPrefix$server_ip_address:$server_webservice_port/"

            pingWeb()
        }

        //animation for image
        animMoveFromLeft = AnimationUtils.loadAnimation(this@MainActivity, R.anim.move_from_left)
        animMoveToRight = AnimationUtils.loadAnimation(this@MainActivity, R.anim.move_to_right)
        animMoveFromRight = AnimationUtils.loadAnimation(this@MainActivity, R.anim.move_from_right)
        animMoveToLeft = AnimationUtils.loadAnimation(this@MainActivity, R.anim.move_to_left)

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

                        base_ip_address_webservice = "$httpPrefix$server_ip_address:$server_webservice_port/"

                        Log.d(mTag, "base_ip_address_webservice = $base_ip_address_webservice")

                        pingWeb()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_PING_WEB_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_PING_WEB_FAILED")

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_PING_WEB_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_PING_WEB_SUCCESS")

                        val getLayoutIntent = Intent()
                        getLayoutIntent.action = Constants.ACTION.ACTION_GET_LAYOUT
                        this@MainActivity.sendBroadcast(getLayoutIntent)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_LAYOUT, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_LAYOUT")

                        getLayout()
                        textViewShowInitSuccess!!.text = getString(R.string.get_layout_start)
                        textViewShowState!!.text = "ACTION_GET_LAYOUT"

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_LAYOUT_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_LAYOUT_FAILED")

                        textViewShowInitSuccess!!.text = getString(R.string.get_layout_failed_retry)
                        textViewShowState!!.text = "ACTION_GET_LAYOUT_FAILED"
                        getLayout()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_LAYOUT_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_LAYOUT_SUCCESS")

                        textViewShowInitSuccess!!.text = getString(R.string.get_layout_success)
                        textViewShowState!!.text = "ACTION_GET_LAYOUT_SUCCESS"

                        val planId = intent.getIntExtra("PLAN_ID", 0)
                        val plan2Id = intent.getIntExtra("PLAN2_ID", 0)
                        val plan3Id = intent.getIntExtra("PLAN3_ID", 0)
                        val plan4Id = intent.getIntExtra("PLAN4_ID", 0)

                        Log.d(mTag, "planId = $planId")
                        Log.d(mTag, "plan2Id = $plan2Id")
                        Log.d(mTag, "plan3Id = $plan3Id")
                        Log.d(mTag, "plan4Id = $plan4Id")

                        if (planId > 0 || plan2Id > 0 || plan3Id > 0 || plan4Id > 0) {
                            textViewShowInitSuccess!!.text = getString(R.string.get_ad_setting_start)
                            getAdSetting(planId, plan2Id, plan3Id, plan4Id)
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_AD_SETTING_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_AD_SETTING_FAILED")

                        textViewShowInitSuccess!!.text = getString(R.string.get_ad_setting_failed_retry)
                        textViewShowState!!.text = "ACTION_GET_AD_SETTING_FAILED"
                        if (layoutList.size > 0) {
                            getAdSetting(
                                layoutList[0].plan_id,
                                layoutList[0].plan2_id,
                                layoutList[0].plan3_id,
                                layoutList[0].plan4_id
                            )
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_AD_SETTING_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_AD_SETTING_SUCCESS")
                        textViewShowInitSuccess!!.text = getString(R.string.get_layout_success)
                        textViewShowState!!.text = "ACTION_GET_AD_SETTING_SUCCESS"
                        if (adSettingList.size > 0) {

                            val getMarqueeIntent = Intent()
                            getMarqueeIntent.action = Constants.ACTION.ACTION_GET_MARQUEE_START
                            this@MainActivity.sendBroadcast(getMarqueeIntent)
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_AD_SETTING_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_AD_SETTING_EMPTY")
                        textViewShowInitSuccess!!.text = getString(R.string.get_ad_setting_empty)
                        textViewShowState!!.text = "ACTION_GET_AD_SETTING_EMPTY"

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MARQUEE_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MARQUEE_START")
                        //get Marquee
                        textViewShowInitSuccess!!.text = getString(R.string.get_marquee_start)
                        textViewShowState!!.text = "ACTION_GET_MARQUEE_START"
                        getMarquee()
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MARQUEE_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MARQUEE_FAILED")
                        textViewShowInitSuccess!!.text = getString(R.string.get_marquee_failed)
                        textViewShowState!!.text = "ACTION_GET_MARQUEE_FAILED"

                        //then get banner
                        val getBannerIntent = Intent()
                        getBannerIntent.action = Constants.ACTION.ACTION_GET_BANNER_START
                        this@MainActivity.sendBroadcast(getBannerIntent)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MARQUEE_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MARQUEE_EMPTY")
                        textViewShowInitSuccess!!.text = getString(R.string.get_marquee_empty)
                        textViewShowState!!.text = "ACTION_GET_MARQUEE_EMPTY"
                        playMarqueeList.clear()
                        defaultMarqueePlayList!!.clear()
                        defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().clearTable()

                        //then get banner
                        val getBannerIntent = Intent()
                        getBannerIntent.action = Constants.ACTION.ACTION_GET_BANNER_START
                        this@MainActivity.sendBroadcast(getBannerIntent)
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MARQUEE_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MARQUEE_SUCCESS")
                        textViewShowInitSuccess!!.text = getString(R.string.get_marquee_success)
                        textViewShowState!!.text = "ACTION_GET_MARQUEE_SUCCESS"
                        //clear before add
                        playMarqueeList.clear()
                        defaultMarqueePlayList!!.clear()
                        defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().clearTable()
                        //add all marquee
                        if (marqueeList.size > 0) {
                            for (i in marqueeList.indices) {
                                val defaultPlayMarqueeData = DefaultPlayMarqueeData(
                                    marqueeList[i].marqueeId, marqueeList[i].name, marqueeList[i].content)
                                defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().insert(defaultPlayMarqueeData)
                            }
                            defaultMarqueePlayList = defaultPlayMarqueeDataDB!!.defaultPlayMarqueeDataDao().getAll() as ArrayList<DefaultPlayMarqueeData>
                            Log.d(mTag, "defaultMarqueePlayList.size = ${defaultMarqueePlayList!!.size}")
                        }

                        //then find match marquee
                        if (adSettingList.size > 0) {
                            //var playIdx = -1
                            for (i in adSettingList.indices) {
                                if (currentPlanId == adSettingList[i].plan_id) {
                                    currentAdSettingIdx = i
                                    break
                                }
                            }
                            //Log.d(mTag, "plan_marquee = ${adSettingList[0].plan_marquee}")
                            Log.d(mTag, "plan_marquee = ${adSettingList[currentAdSettingIdx].plan_marquee}")

                            if (adSettingList[currentAdSettingIdx].plan_marquee.isNotEmpty()) {
                                val marqueeArray = adSettingList[currentAdSettingIdx].plan_marquee.split(",")
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
                            }

                            //then get banner
                            val getBannerIntent = Intent()
                            getBannerIntent.action = Constants.ACTION.ACTION_GET_BANNER_START
                            this@MainActivity.sendBroadcast(getBannerIntent)

                        } else {
                            Log.d(mTag, "No AdSetting")
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_BANNER_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_BANNER_START")

                        clearAllNotInAllList()

                        textViewShowInitSuccess!!.text = getString(R.string.get_banner_start)
                        textViewShowState!!.text = "ACTION_GET_BANNER_START"
                        if (adSettingList[currentAdSettingIdx].plan_banner.isNotEmpty()) {
                            val bannerArray =
                                adSettingList[currentAdSettingIdx].plan_banner.split(",")
                            if (bannerArray.isNotEmpty()) {
                                bannerList.clear()
                                downloadBannerReadyArray.clear()
                                for (i in bannerArray.indices) {
                                    bannerList.add(bannerArray[i])
                                    downloadBannerReadyArray.add(false)
                                }

                                Log.d(
                                    mTag,
                                    "bannerList = $bannerList, downloadBannerReadyArray = $downloadBannerReadyArray"
                                )

                                checkBannerExists()
                                //clearBannersNotInBannerList()
                                downloadBanner()
                            }
                        } else {
                            //banner is empty, clear list and table
                            bannerList.clear()
                            defaultBannerPlayList!!.clear()
                            defaultPlayBannerDataDB!!.defaultPlayBannerDataDao().clearTable()
                            //then get images
                            val getImagesIntent = Intent()
                            getImagesIntent.action = Constants.ACTION.ACTION_GET_IMAGES_START
                            this@MainActivity.sendBroadcast(getImagesIntent)
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_BANNER_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_BANNER_FAILED")
                        val errorStr = intent.getStringExtra("ERROR_STRING")
                        textViewShowInitSuccess!!.text = errorStr
                        textViewShowState!!.text = "ACTION_GET_BANNER_FAILED"
                        if (downloadBannerComplete < bannerList.size) {
                            downloadBanner()
                        } else { //complete
                            //then get images
                            val getImagesIntent = Intent()
                            getImagesIntent.action = Constants.ACTION.ACTION_GET_IMAGES_START
                            this@MainActivity.sendBroadcast(getImagesIntent)
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_BANNER_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_BANNER_SUCCESS")
                        textViewShowInitSuccess!!.text = getString(R.string.get_banner_success)
                        textViewShowState!!.text = "ACTION_GET_BANNER_SUCCESS"
                        if (downloadBannerComplete < bannerList.size) {
                            downloadBanner()
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_BANNER_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_BANNER_EMPTY")
                        textViewShowInitSuccess!!.text = getString(R.string.get_banner_empty)
                        textViewShowState!!.text = "ACTION_GET_BANNER_EMPTY"
                        //no banner, clear list and table
                        bannerList.clear()
                        defaultBannerPlayList!!.clear()
                        defaultPlayBannerDataDB!!.defaultPlayBannerDataDao().clearTable()

                        //then get images
                        val getImagesIntent = Intent()
                        getImagesIntent.action = Constants.ACTION.ACTION_GET_IMAGES_START
                        this@MainActivity.sendBroadcast(getImagesIntent)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_BANNER_COMPLETE, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_BANNER_COMPLETE")
                        textViewShowInitSuccess!!.text = getString(R.string.get_banner_complete)
                        textViewShowState!!.text = "ACTION_GET_BANNER_COMPLETE"
                        if (bannerList.size > 0) {
                            //clear before add
                            defaultPlayBannerDataDB!!.defaultPlayBannerDataDao().clearTable()
                            for (i in bannerList.indices) {
                                val defaultPlayBannerData = DefaultPlayBannerData(bannerList[i])
                                defaultPlayBannerDataDB!!.defaultPlayBannerDataDao().insert(defaultPlayBannerData)
                            }
                            defaultBannerPlayList!!.clear()
                            defaultBannerPlayList = defaultPlayBannerDataDB!!.defaultPlayBannerDataDao().getAll() as ArrayList<DefaultPlayBannerData>
                            Log.d(mTag, "defaultBannerPlayList.size = ${defaultBannerPlayList!!.size}")
                        }

                        //then get images
                        val getImagesIntent = Intent()
                        getImagesIntent.action = Constants.ACTION.ACTION_GET_IMAGES_START
                        this@MainActivity.sendBroadcast(getImagesIntent)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_IMAGES_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_IMAGES_START")
                        textViewShowInitSuccess!!.text = getString(R.string.get_image_start)
                        textViewShowState!!.text = "ACTION_GET_IMAGES_START"
                        if (adSettingList[currentAdSettingIdx].plan_images.isNotEmpty()) {
                            val imagesArray =
                                adSettingList[currentAdSettingIdx].plan_images.split(",")
                            if (imagesArray.isNotEmpty()) {
                                imageList.clear()
                                downloadImageReadyArray.clear()
                                for (i in imagesArray.indices) {
                                    imageList.add(imagesArray[i])
                                    downloadImageReadyArray.add(false)
                                }

                                Log.d(
                                    mTag,
                                    "imageList = $imageList, downloadImageReadyArray = $downloadImageReadyArray"
                                )
                                //downloadImageComplete = 0
                                checkImagesExists()
                                //clearImagesNotInImageList()
                                downloadImages()
                            }
                        } else {
                            //no images, clear
                            imageList.clear()
                            defaultImagesPlayList!!.clear()
                            defaultPlayImagesDataDB!!.defaultPlayImagesDataDao().clearTable()

                            val getVideosIntent = Intent()
                            getVideosIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_START
                            this@MainActivity.sendBroadcast(getVideosIntent)
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_IMAGES_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_IMAGES_FAILED")
                        val errorStr = intent.getStringExtra("ERROR_STRING")
                        textViewShowInitSuccess!!.text = errorStr
                        textViewShowState!!.text = "ACTION_GET_IMAGES_FAILED"
                        if (downloadImageComplete < imageList.size) {
                            downloadImages()
                        } else { //downloadImageComplete
                            val getVideosIntent = Intent()
                            getVideosIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_START
                            this@MainActivity.sendBroadcast(getVideosIntent)
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_IMAGES_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_IMAGES_SUCCESS")

                        if (downloadImageComplete < imageList.size) {
                            downloadImages()
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_IMAGES_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_IMAGES_EMPTY")
                        textViewShowInitSuccess!!.text = getString(R.string.get_image_empty)
                        textViewShowState!!.text = "ACTION_GET_IMAGES_EMPTY"
                        //clear
                        imageList.clear()
                        defaultImagesPlayList!!.clear()
                        defaultPlayImagesDataDB!!.defaultPlayImagesDataDao().clearTable()

                        //then download videos
                        val getVideosIntent = Intent()
                        getVideosIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_START
                        this@MainActivity.sendBroadcast(getVideosIntent)
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_IMAGES_COMPLETE, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_IMAGES_COMPLETE")
                        textViewShowInitSuccess!!.text = getString(R.string.get_image_complete)
                        textViewShowState!!.text = "ACTION_GET_IMAGES_COMPLETE"
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
                        val getVideosIntent = Intent()
                        getVideosIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_START
                        this@MainActivity.sendBroadcast(getVideosIntent)
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_VIDEOS_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_VIDEOS_START")
                        textViewShowInitSuccess!!.text = getString(R.string.get_video_start)
                        textViewShowState!!.text = "ACTION_GET_VIDEOS_START"
                        if (adSettingList[currentAdSettingIdx].plan_videos.isNotEmpty()) {
                            val videosArray = adSettingList[currentAdSettingIdx].plan_videos.split(",")

                            videoList.clear()
                            downloadVideoReadyArray.clear()
                            for (i in videosArray.indices) {
                                videoList.add(videosArray[i])
                                downloadVideoReadyArray.add(false)
                            }

                            Log.d(
                                mTag,
                                "videoList = $videoList, downloadVideoReadyArray = $downloadVideoReadyArray"
                            )

                            //then download videos
                            //downloadVideoComplete = 0
                            checkVideosExists()
                            //clearVideosNotInVideoList()
                            downloadVideos()
                        } else { //video is empty
                            videoList.clear()
                            defaultVideosPlayList!!.clear()
                            defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().clearTable()

                            //then download mix
                            val getMixIntent = Intent()
                            getMixIntent.action = Constants.ACTION.ACTION_GET_MIX_START
                            this@MainActivity.sendBroadcast(getMixIntent)
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_VIDEOS_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_VIDEOS_FAILED")

                        val errorString = intent.getStringExtra("ERROR_STRING")

                        textViewShowInitSuccess!!.text = "$errorString"
                        textViewShowState!!.text = "ACTION_GET_VIDEOS_FAILED"

                        Log.e(mTag, "downloadVideoComplete = $downloadVideoComplete, videoList.size = ${videoList.size}")

                        if (downloadVideoComplete < videoList.size) {
                            downloadVideos()
                        } else { //download complete
                            val getMixIntent = Intent()
                            getMixIntent.action = Constants.ACTION.ACTION_GET_MIX_START
                            this@MainActivity.sendBroadcast(getMixIntent)
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_VIDEOS_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_VIDEOS_SUCCESS")

                        /*if (checkDownloadVideosAll()) {
                            Log.d(mTag, "ok, there might be some files can't download, but fine, just play!")
                            if (infoRenew) {
                                Log.d(mTag, "start to play!")
                                playAd()
                            }
                        }*/

                        if (downloadVideoComplete < videoList.size) {
                            downloadVideos()
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_VIDEOS_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_VIDEOS_EMPTY")
                        textViewShowInitSuccess!!.text = getString(R.string.get_video_empty)
                        textViewShowState!!.text = "ACTION_GET_VIDEOS_EMPTY"
                        videoList.clear()
                        defaultVideosPlayList!!.clear()
                        defaultPlayVideosDataDB!!.defaultPlayVideosDataDao().clearTable()

                        //then download mix
                        val getMixIntent = Intent()
                        getMixIntent.action = Constants.ACTION.ACTION_GET_MIX_START
                        this@MainActivity.sendBroadcast(getMixIntent)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_VIDEOS_COMPLETE, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_VIDEOS_COMPLETE")



                        textViewShowInitSuccess!!.text = getString(R.string.get_video_complete)
                        textViewShowState!!.text = "ACTION_GET_VIDEOS_COMPLETE"
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

                            //then download mix
                            val getMixIntent = Intent()
                            getMixIntent.action = Constants.ACTION.ACTION_GET_MIX_START
                            this@MainActivity.sendBroadcast(getMixIntent)

                        } else {
                            Log.d(mTag, "Not Yet")
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MIX_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MIX_START")
                        textViewShowInitSuccess!!.text = getString(R.string.get_mix_start)
                        textViewShowState!!.text = "ACTION_GET_MIX_START"
                        if (adSettingList[currentAdSettingIdx].plan_mix.isNotEmpty()) {
                            val mixArray = adSettingList[currentAdSettingIdx].plan_mix.split(",")

                            mixList.clear()
                            downloadMixReadyArray.clear()
                            for (i in mixArray.indices) {
                                mixList.add(mixArray[i])
                                downloadMixReadyArray.add(false)
                            }

                            Log.d(
                                mTag,
                                "mixList = $mixList, downloadMixReadyArray = $downloadMixReadyArray"
                            )

                            //then download videos
                            //downloadVideoComplete = 0
                            checkMixExists()
                            //clearMixNotInMixList()
                            downloadMix()
                        } else { //mix is empty
                            mixList.clear()
                            defaultMixPlayList!!.clear()
                            defaultPlayMixDataDB!!.defaultPlayMixDataDao().clearTable()

                            //then play ad
                            val playAdIntent = Intent()
                            playAdIntent.action = Constants.ACTION.ACTION_CHECK_FILES_INCOMPLETE_ACTION
                            this@MainActivity.sendBroadcast(playAdIntent)
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MIX_SUCCESS, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MIX_SUCCESS")
                        textViewShowState!!.text = "ACTION_GET_MIX_SUCCESS"
                        val mixStr = "mixList.size = ${mixList.size}, downloadMixComplete = $downloadMixComplete"
                        textViewShowInitSuccess!!.text = mixStr
                        if (downloadMixComplete < mixList.size) {
                            downloadMix()
                        } else {
                            textViewShowState!!.text = "ACTION_GET_MIX_COMPLETE"
                            //val mixStr = "mixList.size = ${mixList.size}, downloadMixComplete = $downloadMixComplete"
                            //textViewShowInitSuccess!!.text = mixStr
                            /*
                            val completeIntent = Intent()
                            completeIntent.action = Constants.ACTION.ACTION_GET_MIX_COMPLETE
                            this@MainActivity.sendBroadcast(completeIntent)
                            */
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MIX_FAILED, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MIX_FAILED")
                        val errorStr = intent.getStringExtra("ERROR_STRING")
                        textViewShowInitSuccess!!.text = errorStr
                        textViewShowState!!.text = "ACTION_GET_MIX_FAILED"
                        if (downloadMixComplete < mixList.size) {
                            downloadMix()
                        } else {
                            val playAdIntent = Intent()
                            playAdIntent.action = Constants.ACTION.ACTION_CHECK_FILES_INCOMPLETE_ACTION
                            this@MainActivity.sendBroadcast(playAdIntent)
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MIX_COMPLETE, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MIX_COMPLETE")

                        textViewProgress!!.visibility = View.GONE

                        textViewShowInitSuccess!!.text = getString(R.string.get_mix_complete)
                        textViewShowState!!.text = "ACTION_GET_MIX_COMPLETE"
                        if (downloadMixComplete == mixList.size) {
                            //textViewShowInitSuccess!!.text = "1..."
                            if (mixList.size > 0) {

                                //clear before add
                                defaultPlayMixDataDB!!.defaultPlayMixDataDao().clearTable()
                                for (i in mixList.indices) {
                                    val defaultPlayMixData = DefaultPlayMixData(mixList[i])
                                    defaultPlayMixDataDB!!.defaultPlayMixDataDao().insert(defaultPlayMixData)
                                }
                                defaultMixPlayList!!.clear()
                                defaultMixPlayList = defaultPlayMixDataDB!!.defaultPlayMixDataDao().getAll() as ArrayList<DefaultPlayMixData>
                                Log.d(mTag, "defaultMixPlayList.size = ${defaultMixPlayList!!.size}")
                            }
                            //textViewShowInitSuccess!!.text = "2..."
                            //get current planId
                            val currentTimestamp = getCurrentTimeStamp()
                            Log.d(mTag, "currentTimestamp = $currentTimestamp")
                            //textViewShowInitSuccess!!.text = "3..."
                            if (layoutList.size > 0) {
                                getPlanUse(currentTimestamp)

                                //get current plan idx
                                if (currentPlanId > 0) {
                                    if (adSettingList.size > 0) {
                                        //get current plan idx
                                        for (i in adSettingList.indices) {
                                            if (currentPlanId == adSettingList[i].plan_id) {
                                                currentAdSettingIdx = i
                                                break
                                            }
                                        }
                                    } else {
                                        currentAdSettingIdx = -1
                                    }
                                }
                                //textViewShowInitSuccess!!.text = "currentPlanUse = $currentPlanUse, currentPlanId = $currentPlanId, currentAdSettingIdx = $currentAdSettingIdx"
                            }

                        } else {
                            textViewShowInitSuccess!!.text = "downloadMixComplete != mixList.size"

                        }
                        infoRenew = true

                        //then play ad
                        val playAdIntent = Intent()
                        playAdIntent.action = Constants.ACTION.ACTION_CHECK_FILES_INCOMPLETE_ACTION
                        this@MainActivity.sendBroadcast(playAdIntent)

                    }  else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_MIX_EMPTY, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_MIX_EMPTY")
                        textViewShowInitSuccess!!.text = getString(R.string.get_mix_empty)
                        textViewShowState!!.text = "ACTION_GET_MIX_EMPTY"
                        //get current planId
                        val currentTimestamp = getCurrentTimeStamp()
                        Log.d(mTag, "currentTimestamp = $currentTimestamp")

                        if (layoutList.size > 0) {
                            getPlanUse(currentTimestamp)

                            //get current plan idx
                            if (currentPlanId > 0) {
                                if (adSettingList.size > 0) {
                                    //get current plan idx
                                    for (i in adSettingList.indices) {
                                        if (currentPlanId == adSettingList[i].plan_id) {
                                            currentAdSettingIdx = i
                                            break
                                        }
                                    }
                                } else {
                                    currentAdSettingIdx = -1
                                }
                            }
                        }

                        val startAdIntent = Intent()
                        startAdIntent.action = Constants.ACTION.ACTION_CHECK_FILES_INCOMPLETE_ACTION
                        this@MainActivity.sendBroadcast(startAdIntent)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_CHECK_FILES_INCOMPLETE_ACTION, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_CHECK_FILES_INCOMPLETE_ACTION")

                        textViewShowState!!.text = "ACTION_CHECK_FILES_INCOMPLETE_ACTION"
                        checkUrlAndLocalFiles()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_CHECK_FILES_INCOMPLETE_COMPLETE, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_CHECK_FILES_INCOMPLETE_COMPLETE")

                        //start play ad
                        val startAdIntent = Intent()
                        startAdIntent.action = Constants.ACTION.ACTION_START_PLAY_AD
                        this@MainActivity.sendBroadcast(startAdIntent)

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_START_PLAY_AD, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_START_PLAY_AD")
                        textViewShowInitSuccess!!.text = getString(R.string.start_play_ad)
                        textViewShowState!!.text = "ACTION_START_PLAY_AD"
                        //checkUrlAndLocalFiles()
                        //start to play
                        if (infoRenew) {
                            Log.d(mTag, "start to play!")
                            playAd()
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_TOP_PLAY_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_TOP_PLAY_START")
                        currentMixIndexTop = -1
                        Log.e(mTag, "mixList.size = ${mixList.size}")

                        if (layoutTop == 5) { //mix only
                            if (mixList.size > 0 && checkDownloadMixAll()) { //at least one image or video can play

                                if (mixMode == 1) { //random
                                    var nextTop: Int
                                    do {
                                        nextTop = Random.nextInt(mixList.size)
                                    } while ((nextTop == currentMixIndexTop && mixList.size > 1) || !downloadMixReadyArray[nextTop])
                                    currentMixIndexTop = nextTop
                                } else { //circle
                                    do { //if next downloadImageReadyArray is false, next one
                                        currentMixIndexTop += 1
                                        if (currentMixIndexTop >= mixList.size) {
                                            currentMixIndexTop = 0
                                        }
                                    } while (!downloadMixReadyArray[currentMixIndexTop])

                                }

                                Log.e(mTag, "currentMixIndexTop = $currentMixIndexTop")
                                //detect image or video
                                val playFile = File(mixList[currentMixIndexTop])
                                val downloadFileExt = playFile.extension
                                val mixPlayIntent = Intent()
                                mixPlayIntent.putExtra("MIX_MODE", mixMode)
                                if (downloadFileExt == "mp4") { //video
                                    //mixPlayIntent.action = Constants.ACTION.ACTION_MIX_TOP_PLAY_VIDEO_START
                                    mixPlayIntent.action = Constants.ACTION.ACTION_TOP_VIDEO_PLAY_START
                                    this@MainActivity.sendBroadcast(mixPlayIntent)
                                } else { //image
                                    mixPlayIntent.action = Constants.ACTION.ACTION_MIX_TOP_PLAY_IMAGE_START
                                    this@MainActivity.sendBroadcast(mixPlayIntent)
                                }
                            }
                        }


                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_TOP_PLAY_STOP, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_TOP_PLAY_STOP")


                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_TOP_PLAY_FINISH, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_TOP_PLAY_FINISH")

                        if (layoutTop == 5) { //mix only
                            if (mixList.size > 0 && checkDownloadMixAll()) { //at least one image or video can play
                                Log.e(mTag, "mixList.size > 0")
                                if (mixMode == 1) { //random
                                    var nextTop: Int
                                    do {
                                        nextTop = Random.nextInt(mixList.size)
                                    } while ((nextTop == currentMixIndexTop && mixList.size > 1) || !downloadMixReadyArray[nextTop])
                                    currentMixIndexTop = nextTop
                                } else { //circle
                                    do { //if next downloadImageReadyArray is false, next one
                                        currentMixIndexTop += 1
                                        if (currentMixIndexTop >= mixList.size) {
                                            currentMixIndexTop = 0
                                        }
                                    } while (!downloadMixReadyArray[currentMixIndexTop])

                                }

                                Log.e(mTag, "currentMixIndexTop = $currentMixIndexTop")
                                //detect image or video
                                val playFile = File(mixList[currentMixIndexTop])
                                val downloadFileExt = playFile.extension
                                val mixPlayIntent = Intent()
                                if (downloadFileExt == "mp4") { //video
                                    //mixPlayIntent.action = Constants.ACTION.ACTION_MIX_TOP_PLAY_VIDEO_START
                                    mixPlayIntent.action = Constants.ACTION.ACTION_TOP_VIDEO_PLAY_START
                                } else { //image
                                    mixPlayIntent.action = Constants.ACTION.ACTION_MIX_TOP_PLAY_IMAGE_START
                                }
                                this@MainActivity.sendBroadcast(mixPlayIntent)
                            }
                        } else {
                            mixTopRunning = false
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_TOP_PLAY_IMAGE_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_TOP_PLAY_IMAGE_START")

                        var mixImagesPlayInterval = 7000
                        when(mixImageInterval) {
                            0 -> {
                                mixImagesPlayInterval = 7000
                            }
                            1 -> {
                                mixImagesPlayInterval = 10000
                            }
                            2 -> {
                                mixImagesPlayInterval = 15000
                            }
                        }

                        if (layoutTop == 5) { //mix only
                            //if videoView is playing, stop it
                            videoViewTop!!.visibility = View.GONE
                            videoViewLayoutTop!!.visibility = View.GONE
                            //imageViewTop!!.visibility = View.VISIBLE
                            //imageViewTop2!!.visibility = View.GONE

                            val srcPath = "$dest_images_folder/${mixList[currentMixIndexTop]}"
                            val file = File(srcPath)
                            if (file.exists()) {

                                if (imageViewTop!!.visibility == View.VISIBLE) {
                                    imageViewTop!!.startAnimation(animMoveToRight)
                                    imageViewTop!!.visibility = View.GONE
                                    imageViewTop2!!.visibility = View.VISIBLE
                                    //imageViewTop2!!.setImageURI(Uri.fromFile(file))
                                    Picasso.get()
                                        .load(Uri.fromFile(file))
                                        .resize(layoutTopWidth, layoutTopHeight)
                                        .onlyScaleDown()
                                        .into(imageViewTop2)
                                    imageViewTop2!!.startAnimation(animMoveFromLeft)

                                } else { //imageViewTop2 is visible
                                    imageViewTop2!!.startAnimation(animMoveToRight)
                                    imageViewTop2!!.visibility = View.GONE
                                    imageViewTop!!.visibility = View.VISIBLE
                                    //imageViewTop!!.setImageURI(Uri.fromFile(file))
                                    Picasso.get()
                                        .load(Uri.fromFile(file))
                                        .resize(layoutTopWidth, layoutTopHeight)
                                        .onlyScaleDown()
                                        .into(imageViewTop)
                                    imageViewTop!!.startAnimation(animMoveFromLeft)

                                }
                            }

                            countDownTimerMixImageTopRunning = true
                            countDownTimerMixImageTop = object : CountDownTimer(mixImagesPlayInterval.toLong(), mixImagesPlayInterval.toLong()) {
                                override fun onTick(millisUntilFinished: Long) {
                                    Log.d(mTag, "countDownTimerImage millisUntilFinished = $millisUntilFinished")
                                }
                                override fun onFinish() { //结束的操作
                                    Log.d(mTag, "countDownTimerImage finish")
                                    if (mixList.size > 0 && checkDownloadMixAll()) {
                                        countDownTimerMixImageTopRunning = false

                                        val mixPlayFinishIntent = Intent()
                                        mixPlayFinishIntent.action = Constants.ACTION.ACTION_MIX_TOP_PLAY_FINISH
                                        this@MainActivity.sendBroadcast(mixPlayFinishIntent)
                                    }
                                } //onFinish
                            }.start()
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_TOP_PLAY_VIDEO_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_TOP_PLAY_VIDEO_START")

                    } //center
                    else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_CENTER_PLAY_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_CENTER_PLAY_START")
                        currentMixIndexCenter = -1
                        Log.e(mTag, "mixList.size = ${mixList.size}")

                        if (layoutCenter == 5) { //mix only
                            if (mixList.size > 0 && checkDownloadMixAll()) { //at least one image or video can play

                                if (mixMode == 1) { //random
                                    var nextCenter: Int
                                    do {
                                        nextCenter = Random.nextInt(mixList.size)
                                    } while ((nextCenter == currentMixIndexCenter && mixList.size > 1) || !downloadMixReadyArray[nextCenter])
                                    currentMixIndexCenter = nextCenter
                                } else { //circle
                                    do { //if next downloadImageReadyArray is false, next one
                                        currentMixIndexCenter += 1
                                        if (currentMixIndexCenter >= mixList.size) {
                                            currentMixIndexCenter = 0
                                        }
                                    } while (!downloadMixReadyArray[currentMixIndexCenter])

                                }

                                Log.e(mTag, "currentMixIndexCenter = $currentMixIndexCenter")
                                //detect image or video
                                val playFile = File(mixList[currentMixIndexCenter])
                                val downloadFileExt = playFile.extension
                                val mixPlayIntent = Intent()
                                mixPlayIntent.putExtra("MIX_MODE", mixMode)
                                if (downloadFileExt == "mp4") { //video
                                    //mixPlayIntent.action = Constants.ACTION.ACTION_MIX_CENTER_PLAY_VIDEO_START
                                    mixPlayIntent.action = Constants.ACTION.ACTION_CENTER_VIDEO_PLAY_START
                                    this@MainActivity.sendBroadcast(mixPlayIntent)
                                } else { //image
                                    mixPlayIntent.action = Constants.ACTION.ACTION_MIX_CENTER_PLAY_IMAGE_START
                                    this@MainActivity.sendBroadcast(mixPlayIntent)
                                }
                            }
                        }


                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_CENTER_PLAY_STOP, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_CENTER_PLAY_STOP")


                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_CENTER_PLAY_FINISH, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_CENTER_PLAY_FINISH")

                        if (layoutCenter == 5) { //mix only
                            if (mixList.size > 0 && checkDownloadMixAll()) { //at least one image or video can play
                                Log.e(mTag, "mixList.size > 0")
                                if (mixMode == 1) { //random
                                    var nextCenter: Int
                                    do {
                                        nextCenter = Random.nextInt(mixList.size)
                                    } while ((nextCenter == currentMixIndexCenter && mixList.size > 1) || !downloadMixReadyArray[nextCenter])
                                    currentMixIndexCenter = nextCenter
                                } else { //circle
                                    do { //if next downloadImageReadyArray is false, next one
                                        currentMixIndexCenter += 1
                                        if (currentMixIndexCenter >= mixList.size) {
                                            currentMixIndexCenter = 0
                                        }
                                    } while (!downloadMixReadyArray[currentMixIndexCenter])

                                }

                                Log.e(mTag, "currentMixIndexCenter = $currentMixIndexCenter")
                                //detect image or video
                                val playFile = File(mixList[currentMixIndexCenter])
                                val downloadFileExt = playFile.extension
                                val mixPlayIntent = Intent()
                                if (downloadFileExt == "mp4") { //video
                                    //mixPlayIntent.action = Constants.ACTION.ACTION_MIX_CENTER_PLAY_VIDEO_START
                                    mixPlayIntent.action = Constants.ACTION.ACTION_CENTER_VIDEO_PLAY_START
                                } else { //image
                                    mixPlayIntent.action = Constants.ACTION.ACTION_MIX_CENTER_PLAY_IMAGE_START
                                }
                                this@MainActivity.sendBroadcast(mixPlayIntent)
                            }
                        } else {
                            mixTopRunning = false
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_CENTER_PLAY_IMAGE_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_CENTER_PLAY_IMAGE_START")

                        var mixImagesPlayInterval = 7000
                        when(mixImageInterval) {
                            0 -> {
                                mixImagesPlayInterval = 7000
                            }
                            1 -> {
                                mixImagesPlayInterval = 10000
                            }
                            2 -> {
                                mixImagesPlayInterval = 15000
                            }
                        }

                        if (layoutCenter == 5) { //mix only
                            //if videoView is playing, stop it
                            videoViewCenter!!.visibility = View.GONE
                            videoViewLayoutCenter!!.visibility = View.GONE
                            //imageViewTop!!.visibility = View.VISIBLE
                            //imageViewTop2!!.visibility = View.GONE

                            val srcPath = "$dest_images_folder/${mixList[currentMixIndexCenter]}"
                            val file = File(srcPath)
                            if (file.exists()) {

                                if (imageViewCenter!!.visibility == View.VISIBLE) {
                                    imageViewCenter!!.startAnimation(animMoveToRight)
                                    imageViewCenter!!.visibility = View.GONE
                                    imageViewCenter2!!.visibility = View.VISIBLE
                                    //imageViewCenter2!!.setImageURI(Uri.fromFile(file))
                                    Picasso.get()
                                        .load(Uri.fromFile(file))
                                        .resize(layoutCenterWidth, layoutCenterHeight)
                                        .onlyScaleDown()
                                        .into(imageViewCenter2)
                                    imageViewCenter2!!.startAnimation(animMoveFromLeft)

                                } else { //imageViewTop2 is visible
                                    imageViewCenter2!!.startAnimation(animMoveToRight)
                                    imageViewCenter2!!.visibility = View.GONE
                                    imageViewCenter!!.visibility = View.VISIBLE
                                    //imageViewCenter!!.setImageURI(Uri.fromFile(file))
                                    Picasso.get()
                                        .load(Uri.fromFile(file))
                                        .resize(layoutCenterWidth, layoutCenterHeight)
                                        .onlyScaleDown()
                                        .into(imageViewCenter)
                                    imageViewCenter!!.startAnimation(animMoveFromLeft)

                                }
                            }

                            countDownTimerMixImageCenterRunning = true
                            countDownTimerMixImageCenter = object : CountDownTimer(mixImagesPlayInterval.toLong(), mixImagesPlayInterval.toLong()) {
                                override fun onTick(millisUntilFinished: Long) {
                                    Log.d(mTag, "countDownTimerImageCenter millisUntilFinished = $millisUntilFinished")
                                }
                                override fun onFinish() { //结束的操作
                                    Log.d(mTag, "countDownTimerImageCenter finish")
                                    if (mixList.size > 0 && checkDownloadMixAll()) {
                                        countDownTimerMixImageCenterRunning = false

                                        val mixPlayFinishIntent = Intent()
                                        mixPlayFinishIntent.action = Constants.ACTION.ACTION_MIX_CENTER_PLAY_FINISH
                                        this@MainActivity.sendBroadcast(mixPlayFinishIntent)
                                    }
                                } //onFinish
                            }.start()
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_CENTER_PLAY_VIDEO_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_CENTER_PLAY_VIDEO_START")

                    } //bottom
                    else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_BOTTOM_PLAY_START")
                        //messageViewBottom!!.text = "ACTION_MIX_BOTTOM_PLAY_START"
                        currentMixIndexBottom = -1
                        Log.e(mTag, "mixList.size = ${mixList.size}")

                        if (layoutBottom == 5) { //mix only
                            if (mixList.size > 0 && checkDownloadMixAll()) { //at least one image or video can play

                                if (mixMode == 1) { //random
                                    var nextBottom: Int
                                    do {
                                        nextBottom = Random.nextInt(mixList.size)
                                    } while ((nextBottom == currentMixIndexBottom && mixList.size > 1) || !downloadMixReadyArray[nextBottom])
                                    currentMixIndexBottom = nextBottom
                                } else { //circle
                                    do { //if next downloadImageReadyArray is false, next one
                                        currentMixIndexBottom += 1
                                        if (currentMixIndexBottom >= mixList.size) {
                                            currentMixIndexBottom = 0
                                        }
                                    } while (!downloadMixReadyArray[currentMixIndexBottom])

                                }

                                Log.e(mTag, "currentMixIndexBottom = $currentMixIndexBottom")
                                //detect image or video
                                val playFile = File(mixList[currentMixIndexBottom])
                                val downloadFileExt = playFile.extension
                                val mixPlayIntent = Intent()
                                mixPlayIntent.putExtra("MIX_MODE", mixMode)
                                if (downloadFileExt == "mp4") { //video
                                    //mixPlayIntent.action = Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_VIDEO_START
                                    mixPlayIntent.action = Constants.ACTION.ACTION_BOTTOM_VIDEO_PLAY_START
                                    this@MainActivity.sendBroadcast(mixPlayIntent)
                                } else { //image
                                    mixPlayIntent.action = Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_IMAGE_START
                                    this@MainActivity.sendBroadcast(mixPlayIntent)
                                }
                            }
                        }


                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_STOP, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_BOTTOM_PLAY_STOP")
                        //messageViewBottom!!.text = "ACTION_MIX_BOTTOM_PLAY_STOP"

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_FINISH, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_BOTTOM_PLAY_FINISH")
                        //messageViewBottom!!.text = "ACTION_MIX_BOTTOM_PLAY_FINISH"
                        if (layoutBottom == 5) { //mix only
                            if (mixList.size > 0 && checkDownloadMixAll()) { //at least one image or video can play
                                Log.e(mTag, "mixList.size > 0")
                                if (mixMode == 1) { //random
                                    var nextBottom: Int
                                    do {
                                        nextBottom = Random.nextInt(mixList.size)
                                    } while ((nextBottom == currentMixIndexBottom && mixList.size > 1) || !downloadMixReadyArray[nextBottom])
                                    currentMixIndexBottom = nextBottom
                                } else { //circle
                                    do { //if next downloadImageReadyArray is false, next one
                                        currentMixIndexBottom += 1
                                        if (currentMixIndexBottom >= mixList.size) {
                                            currentMixIndexBottom = 0
                                        }
                                    } while (!downloadMixReadyArray[currentMixIndexBottom])

                                }

                                Log.e(mTag, "currentMixIndexBottom = $currentMixIndexBottom")
                                //detect image or video
                                val playFile = File(mixList[currentMixIndexBottom])
                                val downloadFileExt = playFile.extension
                                val mixPlayIntent = Intent()
                                if (downloadFileExt == "mp4") { //video
                                    //mixPlayIntent.action = Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_VIDEO_START
                                    mixPlayIntent.action = Constants.ACTION.ACTION_BOTTOM_VIDEO_PLAY_START
                                } else { //image
                                    mixPlayIntent.action = Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_IMAGE_START
                                }
                                this@MainActivity.sendBroadcast(mixPlayIntent)
                            }
                        } else {
                            mixTopRunning = false
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_IMAGE_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_BOTTOM_PLAY_IMAGE_START")
                        //messageViewBottom!!.text = "ACTION_MIX_BOTTOM_PLAY_IMAGE_START"
                        var mixImagesPlayInterval = 7000
                        when(mixImageInterval) {
                            0 -> {
                                mixImagesPlayInterval = 7000
                            }
                            1 -> {
                                mixImagesPlayInterval = 10000
                            }
                            2 -> {
                                mixImagesPlayInterval = 15000
                            }
                        }

                        if (layoutBottom == 5) { //mix only
                            //if videoView is playing, stop it
                            videoViewBottom!!.visibility = View.GONE
                            videoViewLayoutBottom!!.visibility = View.GONE
                            //imageViewTop!!.visibility = View.VISIBLE
                            //imageViewTop2!!.visibility = View.GONE

                            val srcPath = "$dest_images_folder/${mixList[currentMixIndexBottom]}"
                            val file = File(srcPath)
                            if (file.exists()) {

                                if (imageViewBottom!!.visibility == View.VISIBLE) {
                                    imageViewBottom!!.startAnimation(animMoveToRight)
                                    imageViewBottom!!.visibility = View.GONE
                                    imageViewBottom2!!.visibility = View.VISIBLE
                                    //imageViewBottom2!!.setImageURI(Uri.fromFile(file))
                                    Picasso.get()
                                        .load(Uri.fromFile(file))
                                        .resize(layoutBottomWidth, layoutBottomHeight)
                                        .onlyScaleDown()
                                        .into(imageViewBottom2)
                                    imageViewBottom2!!.startAnimation(animMoveFromLeft)

                                } else { //imageViewTop2 is visible
                                    imageViewBottom2!!.startAnimation(animMoveToRight)
                                    imageViewBottom2!!.visibility = View.GONE
                                    imageViewBottom!!.visibility = View.VISIBLE
                                    //imageViewBottom!!.setImageURI(Uri.fromFile(file))
                                    Picasso.get()
                                        .load(Uri.fromFile(file))
                                        .resize(layoutBottomWidth, layoutBottomHeight)
                                        .onlyScaleDown()
                                        .into(imageViewBottom)
                                    imageViewBottom!!.startAnimation(animMoveFromLeft)

                                }
                            }

                            countDownTimerMixImageBottomRunning = true
                            countDownTimerMixImageBottom = object : CountDownTimer(mixImagesPlayInterval.toLong(), mixImagesPlayInterval.toLong()) {
                                override fun onTick(millisUntilFinished: Long) {
                                    Log.d(mTag, "countDownTimerImageBottom millisUntilFinished = $millisUntilFinished")
                                }
                                override fun onFinish() { //结束的操作
                                    Log.d(mTag, "countDownTimerImageBottom finish")
                                    if (mixList.size > 0 && checkDownloadMixAll()) {
                                        countDownTimerMixImageBottomRunning = false

                                        val mixPlayFinishIntent = Intent()
                                        mixPlayFinishIntent.action = Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_FINISH
                                        this@MainActivity.sendBroadcast(mixPlayFinishIntent)
                                    }
                                } //onFinish
                            }.start()
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_VIDEO_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_MIX_BOTTOM_PLAY_VIDEO_START")

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_TOP_VIDEO_PLAY_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_TOP_VIDEO_PLAY_START")

                        var sizeAndCheckDownload = false
                        var filePath = ""

                        if (layoutTop == 3) { //video
                            if (videoList.size > 0 && checkDownloadVideosAll()) {
                                sizeAndCheckDownload = true
                                filePath = "$dest_videos_folder${videoList[currentVideoIndexTop]}"
                            }
                        } else if (layoutTop == 5) { //mix only
                            imageViewTop!!.visibility = View.GONE
                            imageViewTop2!!.visibility = View.GONE
                            imageViewTop!!.setImageResource(0)
                            imageViewTop2!!.setImageResource(0)
                            videoViewTop!!.visibility = View.VISIBLE
                            videoViewLayoutTop!!.visibility = View.VISIBLE

                            if (mixList.size > 0 && checkDownloadMixAll()) { //at least one video can play
                                sizeAndCheckDownload = true
                                filePath = "$dest_videos_folder${mixList[currentMixIndexTop]}"
                            }
                        }

                        if (sizeAndCheckDownload) {
                            Log.d(mTag, "start play -> $filePath")
                            val file = File(filePath)
                            if (file.exists()) {
                                val uriTop = Uri.fromFile(file)

                                videoViewTop!!.setVideoURI(uriTop)

                            } else {
                                Log.d(mTag, "video top: play file not exist")
                            }
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_TOP_VIDEO_PLAY_STOP, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_TOP_VIDEO_PLAY_STOP")

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_TOP_VIDEO_PLAY_FINISH, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_TOP_VIDEO_PLAY_FINISH")

                        if (layoutTop == 3) { //video
                            if (videoList.size > 0 && checkDownloadVideosAll()) {
                                //videoRunningBottom = false
                                if (videosMode == 1) { //random
                                    var nextTop: Int
                                    do {
                                        nextTop = Random.nextInt(videoList.size)
                                        Log.d(mTag, "nextTop = $nextTop")
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
                                Log.d(mTag, "videoList.size = ${videoList.size}, currentVideoIndexTop = $currentVideoIndexTop")
                                val srcPath = "$dest_videos_folder${videoList[currentVideoIndexTop]}"
                                Log.d(mTag, "==>srcPath = $srcPath")

                                val videoPlayStartIntent = Intent()
                                videoPlayStartIntent.action =
                                    Constants.ACTION.ACTION_TOP_VIDEO_PLAY_START
                                this@MainActivity.sendBroadcast(videoPlayStartIntent)
                            } else {
                                Log.d(mTag, "videoList.size == 0")
                                videoViewBottom!!.visibility = View.GONE
                            }
                        } else {
                            Log.e(mTag, "Not video anymore, will stop here")
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_CENTER_VIDEO_PLAY_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_CENTER_VIDEO_PLAY_START")

                        var sizeAndCheckDownload = false
                        var filePath = ""

                        if (layoutCenter == 3) { //video
                            if (videoList.size > 0 && checkDownloadVideosAll()) {
                                sizeAndCheckDownload = true
                                filePath = "$dest_videos_folder${videoList[currentVideoIndexCenter]}"
                            }
                        } else if (layoutCenter == 5) { //mix only
                            imageViewCenter!!.visibility = View.GONE
                            imageViewCenter2!!.visibility = View.GONE
                            imageViewCenter!!.setImageResource(0)
                            imageViewCenter2!!.setImageResource(0)
                            videoViewCenter!!.visibility = View.VISIBLE
                            videoViewLayoutCenter!!.visibility = View.VISIBLE

                            if (mixList.size > 0 && checkDownloadMixAll()) { //at least one video can play
                                sizeAndCheckDownload = true
                                filePath = "$dest_videos_folder${mixList[currentMixIndexCenter]}"
                            }
                        }

                        if (sizeAndCheckDownload) {
                            Log.d(mTag, "start play -> $filePath")
                            val file = File(filePath)
                            if (file.exists()) {
                                val uriCenter = Uri.fromFile(file)

                                videoViewCenter!!.setVideoURI(uriCenter)

                            } else {
                                Log.d(mTag, "video center: play file not exist")
                            }
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_CENTER_VIDEO_PLAY_STOP, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_CENTER_VIDEO_PLAY_STOP")

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_CENTER_VIDEO_PLAY_FINISH, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_CENTER_VIDEO_PLAY_FINISH")

                        if (layoutCenter == 3) { //video
                            if (videoList.size > 0 && checkDownloadVideosAll()) {
                                //videoRunningBottom = false
                                if (videosMode == 1) { //random
                                    var nextCenter: Int
                                    do {
                                        nextCenter = Random.nextInt(videoList.size)
                                        Log.d(mTag, "nextBottom = $nextCenter")
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
                                Log.d(mTag, "videoList.size = ${videoList.size}, currentVideoIndexCenter = $currentVideoIndexCenter")
                                val srcPath = "$dest_videos_folder${videoList[currentVideoIndexCenter]}"
                                Log.d(mTag, "==>srcPath = $srcPath")

                                val videoPlayStartIntent = Intent()
                                videoPlayStartIntent.action =
                                    Constants.ACTION.ACTION_CENTER_VIDEO_PLAY_START
                                this@MainActivity.sendBroadcast(videoPlayStartIntent)
                            } else {
                                Log.d(mTag, "videoList.size == 0")
                                videoViewBottom!!.visibility = View.GONE
                            }
                        } else {
                            Log.e(mTag, "Not video anymore, will stop here")
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_BOTTOM_VIDEO_PLAY_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_BOTTOM_VIDEO_PLAY_START")
                        //messageViewBottom!!.text = "ACTION_BOTTOM_VIDEO_PLAY_START"
                        var sizeAndCheckDownload = false
                        var filePath = ""

                        if (layoutBottom == 3) { //video
                            if (videoList.size > 0 && checkDownloadVideosAll()) {
                                sizeAndCheckDownload = true
                                filePath = "$dest_videos_folder${videoList[currentVideoIndexBottom]}"
                            }
                        } else if (layoutBottom == 5) { //mix only
                            imageViewBottom!!.visibility = View.GONE
                            imageViewBottom2!!.visibility = View.GONE
                            imageViewBottom!!.setImageResource(0)
                            imageViewBottom2!!.setImageResource(0)
                            videoViewBottom!!.visibility = View.VISIBLE
                            videoViewLayoutBottom!!.visibility = View.VISIBLE

                            if (mixList.size > 0 && checkDownloadMixAll()) { //at least one video can play
                                sizeAndCheckDownload = true
                                filePath = "$dest_videos_folder${mixList[currentMixIndexBottom]}"
                            }
                        }

                        if (sizeAndCheckDownload) {
                            Log.d(mTag, "start play -> $filePath")
                            val file = File(filePath)
                            if (file.exists()) {
                                val uriBottom = Uri.fromFile(file)

                                videoViewBottom!!.setVideoURI(uriBottom)

                            } else {
                                Log.d(mTag, "video bottom: play file not exist")
                            }
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_BOTTOM_VIDEO_PLAY_STOP, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_BOTTOM_VIDEO_PLAY_STOP")
                        //!!.text = "ACTION_BOTTOM_VIDEO_PLAY_STOP"

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_BOTTOM_VIDEO_PLAY_FINISH, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_BOTTOM_VIDEO_PLAY_FINISH")
                        //messageViewBottom!!.text = "ACTION_BOTTOM_VIDEO_PLAY_FINISH"
                        if (layoutBottom == 3) { //video
                            if (videoList.size > 0 && checkDownloadVideosAll()) {
                                //videoRunningBottom = false
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

                                val videoPlayStartIntent = Intent()
                                videoPlayStartIntent.action =
                                    Constants.ACTION.ACTION_BOTTOM_VIDEO_PLAY_START
                                this@MainActivity.sendBroadcast(videoPlayStartIntent)
                            } else {
                                Log.d(mTag, "videoList.size == 0")
                                videoViewBottom!!.visibility = View.GONE
                            }
                        } else {
                            Log.e(mTag, "Not video anymore, will stop here")
                        }
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_CURRENT_PLAY_CONTENT_START, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_CURRENT_PLAY_CONTENT_START")

                        if (adSettingList.size > 0) {
                            //var playIdx = -1
                            for (i in adSettingList.indices) {
                                if (currentPlanId == adSettingList[i].plan_id) {
                                    currentAdSettingIdx = i
                                    break
                                }
                            }
                            //marquee
                            Log.d(mTag, "plan_marquee = ${adSettingList[currentAdSettingIdx].plan_marquee}")

                            if (adSettingList[currentAdSettingIdx].plan_marquee.isNotEmpty()) {
                                val marqueeArray = adSettingList[currentAdSettingIdx].plan_marquee.split(",")
                                Log.d(mTag, "marqueeArray.size = ${marqueeArray.size}")
                                if (marqueeArray.isNotEmpty()) {
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
                                    Log.d(mTag, "playMarqueeList = $playMarqueeList")
                                }
                            }

                            //banner
                            if (adSettingList[currentAdSettingIdx].plan_banner.isNotEmpty()) {
                                val bannerArray =
                                    adSettingList[currentAdSettingIdx].plan_banner.split(",")
                                if (bannerArray.isNotEmpty()) {
                                    bannerList.clear()
                                    downloadBannerReadyArray.clear()
                                    for (i in bannerArray.indices) {
                                        bannerList.add(bannerArray[i])
                                        downloadBannerReadyArray.add(false)
                                    }

                                    Log.d(
                                        mTag,
                                        "bannerList = $bannerList, downloadBannerReadyArray = $downloadBannerReadyArray"
                                    )

                                    checkBannerExists()
                                }
                            }
                            //image
                            if (adSettingList[currentAdSettingIdx].plan_images.isNotEmpty()) {
                                val imagesArray =
                                    adSettingList[currentAdSettingIdx].plan_images.split(",")
                                if (imagesArray.isNotEmpty()) {
                                    imageList.clear()
                                    downloadImageReadyArray.clear()
                                    for (i in imagesArray.indices) {
                                        imageList.add(imagesArray[i])
                                        downloadImageReadyArray.add(false)
                                    }

                                    Log.d(
                                        mTag,
                                        "imageList = $imageList, downloadImageReadyArray = $downloadImageReadyArray"
                                    )
                                    checkImagesExists()
                                }
                            }
                            //video
                            if (adSettingList[currentAdSettingIdx].plan_videos.isNotEmpty()) {
                                val videosArray = adSettingList[currentAdSettingIdx].plan_videos.split(",")

                                if (videosArray.isNotEmpty()) {
                                    videoList.clear()
                                    downloadVideoReadyArray.clear()
                                    for (i in videosArray.indices) {
                                        videoList.add(videosArray[i])
                                        downloadVideoReadyArray.add(false)
                                    }

                                    Log.d(
                                        mTag,
                                        "videoList = $videoList, downloadVideoReadyArray = $downloadVideoReadyArray"
                                    )

                                    checkVideosExists()
                                }
                            }
                            //mix
                            if (adSettingList[currentAdSettingIdx].plan_mix.isNotEmpty()) {
                                val mixArray = adSettingList[currentAdSettingIdx].plan_mix.split(",")

                                if (mixArray.isNotEmpty()) {
                                    mixList.clear()
                                    downloadMixReadyArray.clear()
                                    for (i in mixArray.indices) {
                                        mixList.add(mixArray[i])
                                        downloadMixReadyArray.add(false)
                                    }

                                    Log.d(
                                        mTag,
                                        "mixList = $mixList, downloadMixReadyArray = $downloadMixReadyArray"
                                    )

                                    checkMixExists()
                                }
                            }

                            val getPlayContentCompleteIntent = Intent()
                            getPlayContentCompleteIntent.action =
                                Constants.ACTION.ACTION_GET_CURRENT_PLAY_CONTENT_COMPLETE
                            this@MainActivity.sendBroadcast(getPlayContentCompleteIntent)
                        } else {
                            Log.d(mTag, "No AdSetting")
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_GET_CURRENT_PLAY_CONTENT_COMPLETE, ignoreCase = true)) {
                        Log.d(mTag, "ACTION_GET_CURRENT_PLAY_CONTENT_COMPLETE")

                        val playAdIntent = Intent()
                        playAdIntent.action = Constants.ACTION.ACTION_CHECK_FILES_INCOMPLETE_ACTION
                        this@MainActivity.sendBroadcast(playAdIntent)
                    }
                }
            }
        }

        if (!isRegister) {
            filter = IntentFilter()

            filter.addAction(Constants.ACTION.ACTION_GET_ADVERTISES)
            filter.addAction(Constants.ACTION.ACTION_SHOW_DIALOG_AGAIN)
            filter.addAction(Constants.ACTION.ACTION_TEST_IP_AND_PORT)

            //filter.addAction(Constants.ACTION.ACTION_PING_WEB)
            filter.addAction(Constants.ACTION.ACTION_PING_WEB_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_PING_WEB_FAILED)

            filter.addAction(Constants.ACTION.ACTION_GET_LAYOUT)
            filter.addAction(Constants.ACTION.ACTION_GET_LAYOUT_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_LAYOUT_FAILED)

            filter.addAction(Constants.ACTION.ACTION_GET_AD_SETTING_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_AD_SETTING_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GET_AD_SETTING_EMPTY)

            filter.addAction(Constants.ACTION.ACTION_GET_MARQUEE_START)
            filter.addAction(Constants.ACTION.ACTION_GET_MARQUEE_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_MARQUEE_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GET_MARQUEE_EMPTY)

            filter.addAction(Constants.ACTION.ACTION_GET_BANNER_START)
            filter.addAction(Constants.ACTION.ACTION_GET_BANNER_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_BANNER_COMPLETE)
            filter.addAction(Constants.ACTION.ACTION_GET_BANNER_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GET_BANNER_EMPTY)

            filter.addAction(Constants.ACTION.ACTION_GET_IMAGES_START)
            filter.addAction(Constants.ACTION.ACTION_GET_IMAGES_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_IMAGES_COMPLETE)
            filter.addAction(Constants.ACTION.ACTION_GET_IMAGES_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GET_IMAGES_EMPTY)

            filter.addAction(Constants.ACTION.ACTION_GET_VIDEOS_START)
            filter.addAction(Constants.ACTION.ACTION_GET_VIDEOS_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_VIDEOS_COMPLETE)
            filter.addAction(Constants.ACTION.ACTION_GET_VIDEOS_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GET_VIDEOS_EMPTY)

            filter.addAction(Constants.ACTION.ACTION_GET_MIX_START)
            filter.addAction(Constants.ACTION.ACTION_GET_MIX_SUCCESS)
            filter.addAction(Constants.ACTION.ACTION_GET_MIX_COMPLETE)
            filter.addAction(Constants.ACTION.ACTION_GET_MIX_FAILED)
            filter.addAction(Constants.ACTION.ACTION_GET_MIX_EMPTY)

            filter.addAction(Constants.ACTION.ACTION_START_PLAY_AD)
            //top
            filter.addAction(Constants.ACTION.ACTION_MIX_TOP_PLAY_START)
            filter.addAction(Constants.ACTION.ACTION_MIX_TOP_PLAY_STOP)
            filter.addAction(Constants.ACTION.ACTION_MIX_TOP_PLAY_FINISH)
            filter.addAction(Constants.ACTION.ACTION_MIX_TOP_PLAY_IMAGE_START)
            filter.addAction(Constants.ACTION.ACTION_MIX_TOP_PLAY_VIDEO_START)
            //center
            filter.addAction(Constants.ACTION.ACTION_MIX_CENTER_PLAY_START)
            filter.addAction(Constants.ACTION.ACTION_MIX_CENTER_PLAY_STOP)
            filter.addAction(Constants.ACTION.ACTION_MIX_CENTER_PLAY_FINISH)
            filter.addAction(Constants.ACTION.ACTION_MIX_CENTER_PLAY_IMAGE_START)
            filter.addAction(Constants.ACTION.ACTION_MIX_CENTER_PLAY_VIDEO_START)
            //bottom
            filter.addAction(Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_START)
            filter.addAction(Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_STOP)
            filter.addAction(Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_FINISH)
            filter.addAction(Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_IMAGE_START)
            filter.addAction(Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_VIDEO_START)

            //for video
            filter.addAction(Constants.ACTION.ACTION_TOP_VIDEO_PLAY_START)
            filter.addAction(Constants.ACTION.ACTION_TOP_VIDEO_PLAY_STOP)
            filter.addAction(Constants.ACTION.ACTION_TOP_VIDEO_PLAY_FINISH)

            filter.addAction(Constants.ACTION.ACTION_CENTER_VIDEO_PLAY_START)
            filter.addAction(Constants.ACTION.ACTION_CENTER_VIDEO_PLAY_STOP)
            filter.addAction(Constants.ACTION.ACTION_CENTER_VIDEO_PLAY_FINISH)

            filter.addAction(Constants.ACTION.ACTION_BOTTOM_VIDEO_PLAY_START)
            filter.addAction(Constants.ACTION.ACTION_BOTTOM_VIDEO_PLAY_STOP)
            filter.addAction(Constants.ACTION.ACTION_BOTTOM_VIDEO_PLAY_FINISH)

            //get current play content
            filter.addAction(Constants.ACTION.ACTION_GET_CURRENT_PLAY_CONTENT_START)
            filter.addAction(Constants.ACTION.ACTION_GET_CURRENT_PLAY_CONTENT_COMPLETE)

            filter.addAction(Constants.ACTION.ACTION_CHECK_FILES_INCOMPLETE_ACTION)
            filter.addAction(Constants.ACTION.ACTION_CHECK_FILES_INCOMPLETE_COMPLETE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this@MainActivity.registerReceiver(mReceiver, filter, RECEIVER_EXPORTED)
            } else {
                this@MainActivity.registerReceiver(mReceiver, filter)
            }
            isRegister = true
            Log.d(mTag, "registerReceiver mReceiver")
        }
    }

    override fun onDestroy() {
        Log.i(mTag, "onDestroy")
        if (isRegister && mReceiver != null) {
            try {
                this@MainActivity.unregisterReceiver(mReceiver)
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
            jsonObject.put("operationTime", operationTime)
            if (currentOrientation == 1) {
                jsonObject.put("screenWidth", screenWidth)
                jsonObject.put("screenHeight", screenHeight)
            } else if (currentOrientation == 2) {
                jsonObject.put("screenWidth", screenHeight)
                jsonObject.put("screenHeight", screenWidth)
            }
            jsonObject.put("orientation", currentOrientation)
            jsonObject.put("androidVersion", Build.VERSION.RELEASE)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        if (countDownTimerPingWebRunning) {
            countDownTimerPingWeb!!.cancel()
            countDownTimerPingWeb = null
            countDownTimerPingWebRunning = false
        }

        Log.e(mTag, "base_ip_address_webservice = $base_ip_address_webservice")

        ApiFunc().getServerPingResponse(jsonObject, getPingCallback)


        if (countDownTimerPingWeb == null && !countDownTimerPingWebRunning) {
            countDownTimerPingWebRunning = true
            countDownTimerPingWeb = object : CountDownTimer(pingWebInterval, pingWebInterval) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.d(mTag, "$pingWebInterval ms passed...")

                }

                override fun onFinish() { //结束后的操作
                    operationTime = operationTime + 60

                    val jsonFObject = JSONObject()
                    try {
                        jsonFObject.put("deviceID", deviceID)
                        jsonFObject.put("deviceName", deviceName)
                        jsonFObject.put("operationTime", operationTime)
                        if (currentOrientation == 1) {
                            jsonFObject.put("screenWidth", screenWidth)
                            jsonFObject.put("screenHeight", screenHeight)
                        } else if (currentOrientation == 2) {
                            jsonFObject.put("screenWidth", screenHeight)
                            jsonFObject.put("screenHeight", screenWidth)
                        }
                        jsonFObject.put("orientation", currentOrientation)
                        jsonFObject.put("androidVersion", Build.VERSION.RELEASE)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    countDownTimerPingWebRunning = false
                    pingCount += 1
                    Log.d(mTag, "pingCount = $pingCount, pingWebInterval = $pingWebInterval")
                    ApiFunc().getServerPingResponse(jsonFObject, getPingCallback)
                    this.start()
                    countDownTimerPingWebRunning = true

                    /*if (pingCount > 2) {
                        rebootDevice()
                    }*/
                }
            }.start()
        }


    }

    private var getPingCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            Log.e(mTag, "e = $e")
            runOnUiThread(netErrRunnable)
            //for auto plan change
            runOnUiThread {
                try {
                    Log.e(mTag, "getPingCallback failed -> check plan change")

                    if (adSettingList.size > 0) {
                        val currentTimestamp = getCurrentTimeStamp()
                        Log.d(mTag, "currentTimestamp = $currentTimestamp")
                        if (layoutList.size > 0) {
                            planStartTime = getTimeStampFromString(layoutList[0].plan_start_time)
                            plan2StartTime = getTimeStampFromString(layoutList[0].plan2_start_time)
                            plan3StartTime = getTimeStampFromString(layoutList[0].plan3_start_time)
                            plan4StartTime = getTimeStampFromString(layoutList[0].plan4_start_time)
                        }

                        Log.d(mTag, "planStartTime = $planStartTime")
                        Log.d(mTag, "plan2StartTime = $plan2StartTime")
                        Log.d(mTag, "plan3StartTime = $plan3StartTime")
                        Log.d(mTag, "plan4StartTime = $plan4StartTime")



                        if (layoutList.size > 0) {
                            Log.e(mTag, "2->")
                            getPlanUse(currentTimestamp)

                            Log.e(mTag, "ping fail: ---->currentPlanUse = $currentPlanUse")

                            //get current plan idx
                            if (currentPlanId > 0) {
                                if (adSettingList.size > 0) {
                                    //get current plan idx
                                    for (i in adSettingList.indices) {
                                        if (currentPlanId == adSettingList[i].plan_id) {
                                            currentAdSettingIdx = i
                                            break
                                        }
                                    }
                                } else {
                                    currentAdSettingIdx = -1
                                }
                            }
                        }

                        Log.d(mTag, "previousPlanId = $previousPlanId, currentPlanId = $currentPlanId ")

                        if (previousPlanId != currentPlanId) {
                            Log.d(mTag, "Time Plan change!!!")
                            previousPlanId = currentPlanId
                            getFirstPingResponse = false
                        }

                        if (!getFirstPingResponse) {
                            getFirstPingResponse = true
                            infoRenew = true

                            val getPlayContentIntent = Intent()
                            getPlayContentIntent.action = Constants.ACTION.ACTION_GET_CURRENT_PLAY_CONTENT_START
                            this@MainActivity.sendBroadcast(getPlayContentIntent)
                        }
                    } else {
                        Log.e(mTag, "adSettingList.size == 0")
                    }
                } catch (ex: Exception) {
                    Log.e(mTag, "check plan change error")
                }
            }
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.d(mTag, "onResponse : "+response.body.toString())
            pingError = false
            //val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            val json = JSONObject(response.body!!.string())
            runOnUiThread {
                try {
                    Log.d(mTag, "getPingCallback json = $json")
                    val currentTimestamp = getCurrentTimeStamp()
                    Log.d(mTag, "currentTimestamp = $currentTimestamp")
                    if (layoutList.size > 0) {
                        planStartTime = getTimeStampFromString(layoutList[0].plan_start_time)
                        plan2StartTime = getTimeStampFromString(layoutList[0].plan2_start_time)
                        plan3StartTime = getTimeStampFromString(layoutList[0].plan3_start_time)
                        plan4StartTime = getTimeStampFromString(layoutList[0].plan4_start_time)
                    }

                    Log.d(mTag, "planStartTime = $planStartTime")
                    Log.d(mTag, "plan2StartTime = $plan2StartTime")
                    Log.d(mTag, "plan3StartTime = $plan3StartTime")
                    Log.d(mTag, "plan4StartTime = $plan4StartTime")

                    if (layoutList.size > 0) {

                        Log.e(mTag, "3->")
                        getPlanUse(currentTimestamp)

                        Log.e(mTag, "ping success: ---->currentPlanUse = $currentPlanUse")

                        //get current plan idx
                        if (currentPlanId > 0) {
                            if (adSettingList.size > 0) {
                                //get current plan idx
                                for (i in adSettingList.indices) {
                                    if (currentPlanId == adSettingList[i].plan_id) {
                                        currentAdSettingIdx = i
                                        break
                                    }
                                }
                            } else {
                                currentAdSettingIdx = -1
                            }
                        }
                    }

                    Log.d(mTag, "previousPlanId = $previousPlanId, currentPlanId = $currentPlanId ")

                    if (previousPlanId != currentPlanId) {
                        Log.d(mTag, "Time Plan change!!!")
                        previousPlanId = currentPlanId
                        getFirstPingResponse = false
                    }

                    if (json["result"] == 0 ) {
                        //connect show
                        if (!receivePingSuccess) {
                            textViewShowInitSuccess!!.visibility = View.VISIBLE
                            textViewShowInitSuccess!!.text = getString(R.string.ad_client_connect_server_success_no_setting)
                            //textViewShowState!!.visibility = View.VISIBLE
                            receivePingSuccess = true
                        }

                        if (!getFirstPingResponse) {
                            getFirstPingResponse = true
                            infoRenew = true



                            if (server_ip_address != "" && server_webservice_port != "") {
                                server_banner_folder = "$httpPrefix$server_ip_address:$server_webservice_port/uploads/banners"
                                server_images_folder = "$httpPrefix$server_ip_address:$server_webservice_port/uploads/images"
                                server_videos_folder = "$httpPrefix$server_ip_address:$server_webservice_port/uploads/videos"
                            }

                            //get layout first
                            val successIntent = Intent()
                            successIntent.action = Constants.ACTION.ACTION_PING_WEB_SUCCESS
                            this@MainActivity.sendBroadcast(successIntent)
                            //getLayout()
                        }



                    } else if (json["result"] == 1) {
                        Log.d(mTag, "====>Layout changed.")

                        //connect show
                        if (!receivePingSuccess) {
                            textViewShowInitSuccess!!.visibility = View.VISIBLE
                            textViewShowInitSuccess!!.text = getString(R.string.ad_client_connect_server_success_no_setting)
                            //textViewShowState!!.visibility = View.VISIBLE
                            receivePingSuccess = true
                        }
                        //orientationChanged = false
                        //getLayout()
                        infoRenew = true
                        val successIntent = Intent()
                        successIntent.action = Constants.ACTION.ACTION_PING_WEB_SUCCESS
                        this@MainActivity.sendBroadcast(successIntent)
                    } else if (json["result"] == 2) {
                        Log.d(mTag, "->reboot")

                        rebootDevice()
                    } else if (json["result"] == -1) {
                        Log.d(mTag, "->no deviceID")
                    }

                } catch (ex: Exception) {

                    Log.e(mTag, "server error")
                    val failedIntent = Intent()
                    failedIntent.action = Constants.ACTION.ACTION_PING_WEB_FAILED
                    this@MainActivity.sendBroadcast(failedIntent)
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
                            layoutList[0].border,
                            layoutList[0].layout_top, layoutList[0].layout_center, layoutList[0].layout_bottom,
                            layoutList[0].layout2_top, layoutList[0].layout2_center, layoutList[0].layout2_bottom,
                            layoutList[0].layout3_top, layoutList[0].layout3_center, layoutList[0].layout3_bottom,
                            layoutList[0].layout4_top, layoutList[0].layout4_center, layoutList[0].layout4_bottom,
                            layoutList[0].layoutOrientation,
                            layoutList[0].plan_id, layoutList[0].plan2_id, layoutList[0].plan3_id, layoutList[0].plan4_id,
                            layoutList[0].plan_start_time, layoutList[0].plan2_start_time, layoutList[0].plan3_start_time, layoutList[0].plan4_start_time,
                            layoutList[0].pingWebInterval,
                            layoutList[0].plan_layout_top_weight, layoutList[0].plan_layout_center_weight,
                            layoutList[0].plan_layout_bottom_weight, layoutList[0].plan_layout_tri_weight,
                            layoutList[0].plan2_layout_top_weight, layoutList[0].plan2_layout_center_weight,
                            layoutList[0].plan2_layout_bottom_weight, layoutList[0].plan2_layout_tri_weight,
                            layoutList[0].plan3_layout_top_weight, layoutList[0].plan3_layout_center_weight,
                            layoutList[0].plan3_layout_bottom_weight, layoutList[0].plan3_layout_tri_weight,
                            layoutList[0].plan4_layout_top_weight, layoutList[0].plan4_layout_center_weight,
                            layoutList[0].plan4_layout_bottom_weight, layoutList[0].plan4_layout_tri_weight)

                        if (defaultLayoutPlayList!!.size == 0) {
                            defaultPlayLayoutDataDB!!.defaultPlayLayoutDataDao().insert(defaultPlayLayoutData)
                        } else {
                            defaultPlayLayoutDataDB!!.defaultPlayLayoutDataDao().update(defaultPlayLayoutData)
                        }

                        planStartTimeString = layoutList[0].plan_start_time
                        plan2StartTimeString = layoutList[0].plan2_start_time
                        plan3StartTimeString = layoutList[0].plan3_start_time
                        plan4StartTimeString = layoutList[0].plan4_start_time

                        planStartTime = getTimeStampFromString(layoutList[0].plan_start_time)
                        plan2StartTime = getTimeStampFromString(layoutList[0].plan2_start_time)
                        plan3StartTime = getTimeStampFromString(layoutList[0].plan3_start_time)
                        plan4StartTime = getTimeStampFromString(layoutList[0].plan4_start_time)

                        //defaultLayoutPlayList = defaultPlayLayoutDataDB!!.defaultPlayLayoutDataDao().getAll() as ArrayList<DefaultPlayLayoutData>
                        //Log.d(mTag, "defaultLayoutPlayList = ${defaultLayoutPlayList!!.size}")
                        val currentTimestamp = getCurrentTimeStamp()
                        Log.d(mTag, "currentTimestamp = $currentTimestamp")
                        Log.d(mTag, "planStartTime = $planStartTime")
                        Log.d(mTag, "plan2StartTime = $plan2StartTime")
                        Log.d(mTag, "plan3StartTime = $plan3StartTime")
                        Log.d(mTag, "plan4StartTime = $plan4StartTime")


                        /*
                        if (plan4StartTime in 1..currentTimestamp && layoutList[0].plan4_id != 0) { //plan4
                            currentPlanId = layoutList[0].plan4_id
                            currentPlanUse = 4
                            Log.d(mTag, "plan4, id = ${layoutList[0].plan4_id}")
                        } else if (plan3StartTime in 1..currentTimestamp && layoutList[0].plan3_id != 0) { //plan3
                            currentPlanId = layoutList[0].plan3_id
                            currentPlanUse = 3
                            Log.d(mTag, "plan3, id = ${layoutList[0].plan3_id}")
                        } else if (plan2StartTime in 1..currentTimestamp && layoutList[0].plan2_id != 0) { //plan2
                            currentPlanId = layoutList[0].plan2_id
                            currentPlanUse = 2
                            Log.d(mTag, "plan2, id = ${layoutList[0].plan2_id}")
                        } //else if (planStartTime in 1..currentTimestamp) { //plan1
                        else { //plan1
                            if (layoutList[0].plan_id > 0) {
                                currentPlanId = layoutList[0].plan_id
                                currentPlanUse = 1
                                Log.d(mTag, "plan1, id = ${layoutList[0].plan_id}")
                            }
                        }*/
                        Log.e(mTag, "4->")
                        getPlanUse(currentTimestamp)

                        if (previousPlanId != currentPlanId) {
                            previousPlanId = currentPlanId
                        }

                        pingWebInterval = when(layoutList[0].pingWebInterval) {
                            0 -> 60000
                            1 -> 1000
                            2 -> 5000
                            3 -> 10000
                            4 -> 15000
                            5 -> 30000
                            else -> 60000
                        }

                        Log.e(mTag, "prevPingWebInterval = $prevPingWebInterval, pingWebInterval = $pingWebInterval")
                        if (prevPingWebInterval != pingWebInterval) {
                            prevPingWebInterval = pingWebInterval

                            if (countDownTimerPingWebRunning) {
                                countDownTimerPingWeb!!.cancel()
                                countDownTimerPingWeb = null
                                pingWeb()
                            }


                        }

                        val successIntent = Intent()
                        successIntent.action = Constants.ACTION.ACTION_GET_LAYOUT_SUCCESS
                        successIntent.putExtra("PLAN_ID", layoutList[0].plan_id)
                        successIntent.putExtra("PLAN2_ID", layoutList[0].plan2_id)
                        successIntent.putExtra("PLAN3_ID", layoutList[0].plan3_id)
                        successIntent.putExtra("PLAN4_ID", layoutList[0].plan4_id)
                        this@MainActivity.sendBroadcast(successIntent)
                    } else {
                        val failedIntent = Intent()
                        failedIntent.action = Constants.ACTION.ACTION_GET_LAYOUT_FAILED
                        this@MainActivity.sendBroadcast(failedIntent)
                    }
                } catch (ex: Exception) {

                    Log.e(mTag, "Server error")
                    val failedIntent = Intent()
                    failedIntent.action = Constants.ACTION.ACTION_GET_LAYOUT_FAILED
                    this@MainActivity.sendBroadcast(failedIntent)
                }
            }

        }//onResponse
    }

    private fun getAdSetting(planId: Int, plan2Id: Int, plan3Id: Int, plan4Id: Int) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("plan_id", planId)
            jsonObject.put("plan2_id", plan2Id)
            jsonObject.put("plan3_id", plan3Id)
            jsonObject.put("plan4_id", plan4Id)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        ApiFunc().getAdSetting(jsonObject, getAdSettingBack)
    }

    private var getAdSettingBack: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread(netErrRunnable)

            Log.e(mTag, "e = $e")
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
                            val defaultPlayAdSettingData = DefaultPlayAdSettingData(
                                adSettingList[i].plan_id, adSettingList[i].plan_name,
                                adSettingList[i].plan_marquee, adSettingList[i].plan_images,
                                adSettingList[i].plan_videos, adSettingList[i].plan_banner,
                                adSettingList[i].plan_mix,
                                adSettingList[i].marquee_mode, adSettingList[i].marquee_background,
                                adSettingList[i].marquee_text, adSettingList[i].marquee_size,
                                adSettingList[i].marquee_locate, adSettingList[i].marquee_speed,
                                adSettingList[i].images_mode,
                                adSettingList[i].videos_mode, adSettingList[i].marquee_interval,
                                adSettingList[i].image_interval, adSettingList[i].image_scale_type,
                                adSettingList[i].image_anime,
                                adSettingList[i].video_scale_type, adSettingList[i].banner_scale_type,
                                adSettingList[i].mix_mode, adSettingList[i].mix_image_interval,
                                adSettingList[i].mix_image_scale_type, adSettingList[i].mix_image_anime,
                                adSettingList[i].mix_video_scale_type)
                            defaultPlayAdSettingDataDB!!.defaultPlayAdSettingDataDao().insert(defaultPlayAdSettingData)
                            /*if (defaultAdSettingPlayList!!.size == 0) {
                                defaultPlayAdSettingDataDB!!.defaultPlayAdSettingDataDao().insert(defaultPlayAdSettingData)
                            } else {
                                defaultPlayAdSettingDataDB!!.defaultPlayAdSettingDataDao().update(defaultPlayAdSettingData)
                            }*/
                        }

                        //get current plan idx
                        if (currentPlanId > 0) {
                            for (i in adSettingList.indices) {
                                if (currentPlanId == adSettingList[i].plan_id) {
                                    currentAdSettingIdx = i
                                    break
                                }
                            }
                        }

                        Log.e(mTag, "getAdSettingBack -> currentAdSettingIdx = $currentAdSettingIdx ")

                        val successIntent = Intent()
                        successIntent.action = Constants.ACTION.ACTION_GET_AD_SETTING_SUCCESS
                        this@MainActivity.sendBroadcast(successIntent)
                    } else {
                        val emptyIntent = Intent()
                        emptyIntent.action = Constants.ACTION.ACTION_GET_AD_SETTING_EMPTY
                        this@MainActivity.sendBroadcast(emptyIntent)
                    }
                } catch (ex: Exception) {

                    Log.e(mTag, "Server error")
                    val failedIntent = Intent()
                    failedIntent.action = Constants.ACTION.ACTION_GET_AD_SETTING_FAILED
                    this@MainActivity.sendBroadcast(failedIntent)
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
                        this@MainActivity.sendBroadcast(successIntent)
                    } else {
                        val emptyIntent = Intent()
                        emptyIntent.action = Constants.ACTION.ACTION_GET_MARQUEE_EMPTY
                        this@MainActivity.sendBroadcast(emptyIntent)
                    }

                } catch (ex: Exception) {

                    Log.e(mTag, "Server error")
                    val failedIntent = Intent()
                    failedIntent.action = Constants.ACTION.ACTION_GET_MARQUEE_FAILED
                    this@MainActivity.sendBroadcast(failedIntent)
                }
            }

        }//onResponse
    }

    internal var netErrRunnable: Runnable = Runnable {
        Log.e(mTag, "->Network Error")

        pingError = true

        if (isFirstNetworkError) {
            isFirstNetworkError = false

            if (adSettingList.size >= 1) {
                Log.d(mTag, "currentAdSettingIdx = $currentAdSettingIdx")
                Log.d(mTag, "playMarqueeList.size = ${playMarqueeList.size}")
                Log.d(mTag, "imageList.size = ${imageList.size}")
                Log.d(mTag, "videoList.size = ${videoList.size}")
                playAd()
            }
        }


    }

    fun downloadBanner() {
        Log.d(mTag, "=== downloadBanner start ===")

        if (bannerList.size > 0) {
            var downloadIdx = -1
            for (i in bannerList.indices) {
                val srcPath = server_banner_folder
                val destPath = "$dest_banner_folder${bannerList[i]}"
                Log.d(mTag, "srcPath = $srcPath")
                Log.d(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(!file.exists() && i <= downloadBannerComplete) {
                    downloadIdx = i
                    break
                }/* else {
                    Log.d(mTag, "download file exist!")
                    downloadImageComplete += 1
                    downloadImageReadyArray[i] = true
                }*/
            }

            Log.d(mTag, "downloadBannerComplete = $downloadBannerComplete, downloadBannerReadyArray = $downloadBannerReadyArray")

            if (downloadIdx >= 0 ) {

                val srcPath = server_banner_folder
                val destPath = "$dest_banner_folder${bannerList[downloadIdx]}"
                Log.d(mTag, "start download file : ${bannerList[downloadIdx]} to $dest_banner_folder")
                val downloadStr = "download ${bannerList[downloadIdx]}"
                textViewShowInitSuccess!!.text = downloadStr
                textViewProgress!!.visibility = View.VISIBLE
                Thread {
                    try {
                        val totalSize = download(srcPath, destPath, bannerList[downloadIdx]) { progress, length ->
                            // handling the result on main thread
                            handler.sendMessage(handler.obtainMessage(0, progress to length))
                        }

                        Log.d(mTag, "totalSize = $totalSize")

                        downloadBannerComplete += 1
                        downloadBannerReadyArray[downloadIdx] = true

                        if (downloadBannerComplete == bannerList.size) {
                            val completeIntent = Intent()
                            completeIntent.action = Constants.ACTION.ACTION_GET_BANNER_COMPLETE
                            this@MainActivity.sendBroadcast(completeIntent)
                        } else {
                            val successIntent = Intent()
                            successIntent.action = Constants.ACTION.ACTION_GET_BANNER_SUCCESS
                            successIntent.putExtra("idx", downloadIdx)
                            successIntent.putExtra("fileName", bannerList[downloadIdx])
                            this@MainActivity.sendBroadcast(successIntent)
                        }
                    } catch (ex: Exception) {
                        Log.e(mTag, ex.toString())
                        //we can't stuck on download failed, keep try next one
                        downloadBannerComplete += 1
                        downloadBannerReadyArray[downloadIdx] = false
                        val failedIntent = Intent()
                        failedIntent.action = Constants.ACTION.ACTION_GET_BANNER_FAILED
                        failedIntent.putExtra("ERROR_STRING", ex.toString())
                        this@MainActivity.sendBroadcast(failedIntent)

                    }
                }.start()
            } else { //downloadIdx == -1
                if (downloadBannerComplete == bannerList.size) {
                    val completeIntent = Intent()
                    completeIntent.action = Constants.ACTION.ACTION_GET_BANNER_COMPLETE
                    this@MainActivity.sendBroadcast(completeIntent)
                }
            }



        } else {
            val emptyIntent = Intent()
            emptyIntent.action = Constants.ACTION.ACTION_GET_BANNER_EMPTY
            this@MainActivity.sendBroadcast(emptyIntent)
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

                if (file.exists() && !downloadImageReadyArray[i]) { //get 2 same filename while download
                    downloadImageReadyArray[i] = true
                    downloadImageComplete += 1
                }

                if(!file.exists() && i <= downloadImageComplete) {
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
                val downloadStr = "download ${imageList[downloadIdx]}"
                textViewShowInitSuccess!!.text = downloadStr
                textViewProgress!!.visibility = View.VISIBLE
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
                            this@MainActivity.sendBroadcast(completeIntent)
                        } else {
                            val successIntent = Intent()
                            successIntent.action = Constants.ACTION.ACTION_GET_IMAGES_SUCCESS
                            successIntent.putExtra("idx", downloadIdx)
                            successIntent.putExtra("fileName", imageList[downloadIdx])
                            this@MainActivity.sendBroadcast(successIntent)
                        }
                    } catch (ex: Exception) {
                        Log.e(mTag, ex.toString())
                        //we can't stuck on download failed, keep try next one
                        downloadImageComplete += 1
                        downloadImageReadyArray[downloadIdx] = false
                        val failedIntent = Intent()
                        failedIntent.action = Constants.ACTION.ACTION_GET_IMAGES_FAILED
                        failedIntent.putExtra("ERROR_STRING", ex.toString())
                        this@MainActivity.sendBroadcast(failedIntent)

                    }
                }.start()
            } else { //downloadIdx == -1
                if (downloadImageComplete == imageList.size) {
                    val completeIntent = Intent()
                    completeIntent.action = Constants.ACTION.ACTION_GET_IMAGES_COMPLETE
                    this@MainActivity.sendBroadcast(completeIntent)
                }
            }



        } else {
            val emptyIntent = Intent()
            emptyIntent.action = Constants.ACTION.ACTION_GET_IMAGES_EMPTY
            this@MainActivity.sendBroadcast(emptyIntent)
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

                if (file.exists() && !downloadVideoReadyArray[i]) { //get 2 same filename while download
                    downloadVideoReadyArray[i] = true
                    downloadVideoComplete += 1
                }

                if(!file.exists() && i <= downloadVideoComplete) {
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
                val downloadStr = "download ${videoList[downloadIdx]}"
                textViewShowInitSuccess!!.text = downloadStr
                textViewProgress!!.visibility = View.VISIBLE
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
                            this@MainActivity.sendBroadcast(completeIntent)
                        } else {
                            val successIntent = Intent()
                            successIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_SUCCESS
                            //successIntent.putExtra("idx", downloadIdx)
                            //successIntent.putExtra("fileName", videoList[downloadIdx])
                            this@MainActivity.sendBroadcast(successIntent)
                        }
                    } catch (ex: Exception) {
                        Log.e(mTag, ex.toString())
                        //we can't stuck on download failed, keep try next one
                        downloadVideoComplete += 1
                        downloadVideoReadyArray[downloadIdx] = false
                        val failedIntent = Intent()
                        failedIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_FAILED
                        failedIntent.putExtra("ERROR_STRING", ex.toString())
                        this@MainActivity.sendBroadcast(failedIntent)
                    }

                }.start()
            } else { //downloadIdx == -1
                if (downloadVideoComplete == videoList.size) {
                    val completeIntent = Intent()
                    completeIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_COMPLETE
                    this@MainActivity.sendBroadcast(completeIntent)
                } else {
                    val successIntent = Intent()
                    successIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_SUCCESS
                    //successIntent.putExtra("idx", downloadIdx)
                    //successIntent.putExtra("fileName", videoList[downloadIdx])
                    this@MainActivity.sendBroadcast(successIntent)
                }
            }


        } else {
            val emptyIntent = Intent()
            emptyIntent.action = Constants.ACTION.ACTION_GET_VIDEOS_EMPTY
            this@MainActivity.sendBroadcast(emptyIntent)
        }
    }

    fun downloadMix() {
        Log.d(mTag, "downloadMix")

        if (mixList.size > 0) {
            var downloadIdx = -1
            var srcPath = ""
            var destPath = ""
            for (i in mixList.indices) {

                val downloadFile = File(mixList[i])
                //val nameWithoutExtension = downloadFile.nameWithoutExtension
                val downloadFileExt = downloadFile.extension


                if (downloadFileExt == "mp4") { //video
                    srcPath = server_videos_folder
                    destPath = "${dest_videos_folder}/${mixList[i]}"
                } else { //jpg,png
                    srcPath = server_images_folder
                    destPath = "${dest_images_folder}/${mixList[i]}"
                }

                Log.d(mTag, "srcPath = $srcPath")
                Log.d(mTag, "destPath = $destPath")

                val file = File(destPath)

                if (file.exists() && !downloadMixReadyArray[i]) { //get 2 same filename while download
                    downloadMixReadyArray[i] = true
                    downloadMixComplete += 1
                }

                if(!file.exists() && i <= downloadMixComplete) {
                    downloadIdx = i
                    break
                }
            }

            Log.d(mTag, "downloadMixComplete = $downloadMixComplete, downloadMixReadyArray = $downloadMixReadyArray")
            Log.d(mTag, "srcPath = $srcPath")
            Log.d(mTag, "destPath = $destPath")
            Log.d(mTag, "downloadIdx = $downloadIdx")
            //file not exist, download it
            if (downloadIdx >= 0 ) {
                //val srcPath = server_videos_folder
                //val destPath = "$dest_videos_folder${videoList[downloadIdx]}"
                Log.d(mTag, "start download file : ${mixList[downloadIdx]} as $destPath")
                val downloadStr = "download ${mixList[downloadIdx]}"
                textViewShowInitSuccess!!.text = downloadStr
                Thread {
                    try {
                        val totalSize = download(srcPath, destPath, mixList[downloadIdx]) { progress, length ->
                            // handling the result on main thread
                            handler.sendMessage(handler.obtainMessage(0, progress to length))
                        }

                        Log.d(mTag, "totalSize = $totalSize")

                        downloadMixComplete += 1
                        downloadMixReadyArray[downloadIdx] = true

                        if (downloadMixComplete == mixList.size) {
                            //textViewShowInitSuccess!!.text = "Get mix complete"
                            val completeIntent = Intent()
                            completeIntent.action = Constants.ACTION.ACTION_GET_MIX_COMPLETE
                            this@MainActivity.sendBroadcast(completeIntent)
                        } else {
                            /*
                            var mixText = "mixList = $mixList\n"
                            val readyText = "downloadMixReadyArray = $downloadMixReadyArray"
                            mixText += readyText
                            textViewShowInitSuccess!!.text = mixText
                            */
                            val successIntent = Intent()
                            successIntent.action = Constants.ACTION.ACTION_GET_MIX_SUCCESS
                            //successIntent.putExtra("idx", downloadIdx)
                            //successIntent.putExtra("fileName", mixList[downloadIdx])
                            this@MainActivity.sendBroadcast(successIntent)
                        }
                    } catch (ex: Exception) {
                        Log.e(mTag, ex.toString())
                        //we can't stuck on download failed, keep try next one
                        downloadMixComplete += 1
                        downloadMixReadyArray[downloadIdx] = false
                        //textViewShowInitSuccess!!.text = "Get mix failed"
                        val failedIntent = Intent()
                        failedIntent.action = Constants.ACTION.ACTION_GET_MIX_FAILED
                        failedIntent.putExtra("ERROR_STRING", ex.toString())
                        this@MainActivity.sendBroadcast(failedIntent)
                    }

                }.start()
            } else { //downloadIdx == -1
                if (downloadMixComplete == mixList.size) {
                    //textViewShowInitSuccess!!.text = "Get mix complete"
                    val completeIntent = Intent()
                    completeIntent.action = Constants.ACTION.ACTION_GET_MIX_COMPLETE
                    this@MainActivity.sendBroadcast(completeIntent)
                } else {
                    val successIntent = Intent()
                    successIntent.action = Constants.ACTION.ACTION_GET_MIX_SUCCESS
                    //successIntent.putExtra("idx", downloadIdx)
                    //successIntent.putExtra("fileName", mixList[downloadIdx])
                    this@MainActivity.sendBroadcast(successIntent)
                }
            }


        } else {
            //textViewShowInitSuccess!!.text = "Get mix empty"
            val emptyIntent = Intent()
            emptyIntent.action = Constants.ACTION.ACTION_GET_MIX_EMPTY
            this@MainActivity.sendBroadcast(emptyIntent)
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

    private fun checkDownloadBannerAll(): Boolean {
        var ret = false

        for (i in downloadBannerReadyArray.indices) {
            if (downloadBannerReadyArray[i]) {
                ret = true
                break
            }
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

    private fun checkDownloadMixAll(): Boolean {
        var ret = false

        for (i in downloadMixReadyArray.indices) {
            if (downloadMixReadyArray[i]) {
                ret = true
                break
            }
        }

        return ret
    }

    fun getUrlFileLength(url: String): Long {
        return try {
            val urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.requestMethod = "HEAD"
            urlConnection.getHeaderField("content-length")?.toLongOrNull()?.coerceAtLeast(-1L)
                ?: -1L
        } catch (ignored: Exception) {
            -1L
        }
    }

    private fun getFileSize(url: URL): Int {
        var conn: URLConnection? = null
        return try {
            conn = url.openConnection()
            if (conn is HttpURLConnection) {
                conn.requestMethod = "HEAD"
            }
            conn.getInputStream()
            conn.contentLength
        } catch (e: IOException) {
            Log.e(mTag, "e = $e")
        } finally {
            if (conn is HttpURLConnection) {
                conn.disconnect()
            }
        }
    }

    fun checkUrlAndLocalFiles() {
        Log.d(mTag, "=== checkUrlAndLocalFiles start ===")

        if (!pingError) {
            if (adSettingList.size > 0) {
                val bannerDirectory = File(dest_banner_folder)
                val imageDirectory = File(dest_images_folder)
                val videoDirectory = File(dest_videos_folder)
                val bannerFiles = bannerDirectory.listFiles()
                val imageFiles = imageDirectory.listFiles()
                val videoFiles = videoDirectory.listFiles()

                Log.d(mTag, "bannerFiles -> $bannerFiles")
                Log.d(mTag, "imageFiles -> $imageFiles")
                Log.d(mTag, "videoFiles -> $videoFiles")

                Thread {
                    //banner
                    if (bannerDirectory.isDirectory && bannerFiles != null) {
                        for (i in bannerFiles.indices) {
                            var match = false
                            val srcPath = "$server_banner_folder/${URLEncoder.encode(bannerFiles[i].name, "UTF-8")}"
                            val destPath = "$dest_banner_folder${bannerFiles[i].name}"

                            Log.d(mTag, "srcPath = $srcPath")
                            Log.d(mTag, "destPath = $destPath")

                            var fileUrlLength = 0

                            try {
                                val fileUrl = URL(srcPath)

                                val urlConnection: URLConnection = fileUrl.openConnection()
                                urlConnection.setRequestProperty("Accept-Encoding", "identity");
                                urlConnection.connect()
                                //fileUrlLength = getFileSize(fileUrl)
                                fileUrlLength = urlConnection.getContentLength()
                                Log.e(mTag, "fileUrlLength = $fileUrlLength")
                                val destFile = File(destPath)
                                Log.e(mTag,"destFile = ${destFile.length()}")
                                if (fileUrlLength.toLong() == destFile.length()) {
                                    Log.e(mTag, "match!")
                                    match = true
                                }

                                if (!match && fileUrlLength > 0) { //file size not match, delete it!
                                    val deletePath = "$dest_banner_folder${bannerFiles[i].name}"
                                    val deleteFile = File(deletePath)
                                    val deleteUri  = Uri.fromFile(deleteFile)
                                    Log.d(mTag, "deleteUri = $deleteUri")
                                    try {
                                        if (deleteFile.exists()) {
                                            deleteFile.delete()
                                            Log.d(mTag, "Delete $deletePath")
                                        }

                                    } catch (e: java.lang.Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            } catch (e: IOException) {
                                Log.e(mTag, "e = $e")
                            }
                        }
                    }

                    //images
                    if (imageDirectory.isDirectory && imageFiles != null) {

                        for (i in imageFiles.indices) {
                            var match = false
                            val srcPath = "$server_images_folder/${URLEncoder.encode(imageFiles[i].name, "UTF-8")}"
                            val destPath = "$dest_images_folder${imageFiles[i].name}"

                            Log.d(mTag, "srcPath = $srcPath")
                            Log.d(mTag, "destPath = $destPath")

                            var fileUrlLength = 0

                            try {
                                val fileUrl = URL(srcPath)
                                //fileUrlLength = fileUrl.openConnection().contentLength

                                fileUrlLength = getFileSize(fileUrl)
                                Log.e(mTag, "fileUrlLength = $fileUrlLength")
                                val destFile = File(destPath)
                                Log.e(mTag,"destFile = ${destFile.length()}")

                                if (fileUrlLength.toLong() == destFile.length()) {
                                    Log.e(mTag, "match!")
                                    match = true
                                }

                                if (!match && fileUrlLength > 0) { //file size not match, delete it!
                                    val deletePath = "$dest_images_folder${imageFiles[i].name}"
                                    val deleteFile = File(deletePath)
                                    val deleteUri  = Uri.fromFile(deleteFile)
                                    Log.d(mTag, "deleteUri = $deleteUri")
                                    try {
                                        if (deleteFile.exists()) {
                                            deleteFile.delete()
                                            //val cr = contentResolver
                                            //cr.delete(deleteUri, null, null)
                                            Log.d(mTag, "Delete $deletePath")
                                        }

                                    } catch (e: java.lang.Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            } catch (e: IOException) {
                                Log.e(mTag, "e = $e")
                            }
                        }
                    }

                    //videos
                    if (videoDirectory.isDirectory && videoFiles != null) {

                        for (i in videoFiles.indices) {
                            var match = false
                            val srcPath = "$server_videos_folder/${URLEncoder.encode(videoFiles[i].name, "UTF-8")}"
                            val destPath = "$dest_videos_folder${videoFiles[i].name}"

                            Log.d(mTag, "srcPath = $srcPath")
                            Log.d(mTag, "destPath = $destPath")

                            var fileUrlLength = 0

                            try {
                                val fileUrl = URL(srcPath)
                                //fileUrlLength = fileUrl.openConnection().contentLength
                                fileUrlLength = getFileSize(fileUrl)
                                Log.e(mTag, "fileUrlLength = $fileUrlLength")
                                val destFile = File(destPath)
                                Log.e(mTag,"destFile = ${destFile.length()}")

                                if (fileUrlLength.toLong() == destFile.length()) {
                                    Log.e(mTag, "match!")
                                    match = true
                                }

                                if (!match && fileUrlLength > 0) { //file size not match, delete it!
                                    val deletePath = "$dest_videos_folder${videoFiles[i].name}"
                                    val deleteFile = File(deletePath)
                                    val deleteUri  = Uri.fromFile(deleteFile)
                                    Log.d(mTag, "deleteUri = $deleteUri")
                                    try {
                                        if (deleteFile.exists()) {
                                            deleteFile.delete()
                                            //val cr = contentResolver
                                            //cr.delete(deleteUri, null, null)
                                            Log.d(mTag, "Delete $deletePath")
                                        }

                                    } catch (e: java.lang.Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            } catch (e: IOException) {
                                Log.e(mTag, "e = $e")
                            }
                        }
                    }

                    val completeIntent = Intent()
                    completeIntent.action =
                        Constants.ACTION.ACTION_CHECK_FILES_INCOMPLETE_COMPLETE
                    this@MainActivity.sendBroadcast(completeIntent)
                }.start()

            } else {
                Log.e(mTag , "adSettingList.size == 0")
            }
        } else {
            Log.e(mTag, "pingError = $pingError, wont check files")
        }


        Log.d(mTag, "=== checkUrlAndLocalFiles end ===")
    }

    fun checkBannerExists() {
        Log.e(mTag, "=== checkBannerExists start, bannerList.size = ${bannerList.size}")
        downloadBannerComplete = 0
        if (bannerList.size > 0) {
            for (i in bannerList.indices) {
                val srcPath = server_banner_folder
                val destPath = "$dest_banner_folder${bannerList[i]}"
                Log.d(mTag, "srcPath = $srcPath")
                Log.d(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(file.exists()) {
                    downloadBannerComplete += 1
                    downloadBannerReadyArray[i] = true

                }
            }
        }

        Log.e(mTag, "bannerList = $bannerList, downloadBannerReadyArray = $downloadBannerReadyArray")
        Log.e(mTag, "=== checkBannerExists end ===")
    }

    fun checkImagesExists() {
        Log.e(mTag, "=== checkImagesExists start ===")
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
        Log.e(mTag, "imageList = $imageList, downloadImageReadyArray = $downloadImageReadyArray")
        Log.e(mTag, "=== checkImagesExists end ===")
    }

    fun checkVideosExists() {
        Log.e(mTag, "=== checkVideosExists start ===")
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
        Log.e(mTag, "videoList = $videoList, downloadVideoReadyArray = $downloadVideoReadyArray")
        Log.e(mTag, "=== checkVideosExists end ===")
    }

    fun checkMixExists() {
        Log.e(mTag, "=== checkMixExists start ===")
        downloadMixComplete = 0
        if (mixList.size > 0) {
            for (i in mixList.indices) {
                //var srcPath = ""
                //var destPath = ""
                val downloadFile = File(mixList[i])
                //val nameWithoutExtension = downloadFile.nameWithoutExtension
                val downloadFileExt = downloadFile.extension


                val destPath = if (downloadFileExt == "mp4") { //video
                    //srcPath = server_videos_folder
                    "$dest_videos_folder${mixList[i]}"
                } else { //jpg,png
                    //srcPath = server_images_folder
                    "$dest_images_folder${mixList[i]}"
                }
                //Log.d(mTag, "srcPath = $srcPath")
                Log.d(mTag, "destPath = $destPath")

                val file = File(destPath)
                if(file.exists()) {
                    downloadMixComplete += 1
                    downloadMixReadyArray[i] = true

                }
            }
        }
        Log.e(mTag, "mixList = $mixList, downloadMixReadyArray = $downloadMixReadyArray")
        Log.e(mTag, "=== checkMixExists end ===")
    }

    fun clearBannersNotInBannerList() {
        Log.d(mTag, "=== clearBannersNotInBannerList start ===")

        if (adSettingList.size > 0) {

            val directory = File(dest_banner_folder)
            val files = directory.listFiles()

            if (directory.isDirectory && files != null) {
                for (i in files.indices) {
                    var found = false

                    for (j in adSettingList.indices) {
                        if (adSettingList[j].plan_banner.isNotEmpty()) {
                            val imagesArray = adSettingList[j].plan_banner.split(",")
                            for (k in imagesArray.indices) {
                                if (files[i].name == imagesArray[k]) {
                                    found = true
                                    break
                                }
                            }
                        }
                    }

                    if (!found) { //not found in bannerList, delete it!
                        val deletePath = "$dest_banner_folder${files[i].name}"
                        val deleteFile = File(deletePath)
                        val deleteUri  = Uri.fromFile(deleteFile)
                        Log.d(mTag, "deleteUri = $deleteUri")
                        try {
                            if (deleteFile.exists()) {
                                deleteFile.delete()
                                //val cr = contentResolver
                                //cr.delete(deleteUri, null, null)
                                Log.d(mTag, "Delete $deletePath")
                            }

                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } else {
            Log.e(mTag , "adSettingList.size == 0")
        }
        Log.d(mTag, "=== clearBannersNotInBannerList end ===")
    }

    fun clearImagesNotInImageList() {
        Log.d(mTag, "=== clearImagesNotInImageList start ===")
        if (adSettingList.size > 0) {

            val directory = File(dest_images_folder)
            val files = directory.listFiles()

            if (directory.isDirectory && files != null) {
                for (i in files.indices) {
                    var found = false

                    for (j in adSettingList.indices) {
                        if (adSettingList[j].plan_images.isNotEmpty()) {
                            val imagesArray = adSettingList[j].plan_images.split(",")
                            for (k in imagesArray.indices) {
                                if (files[i].name == imagesArray[k]) {
                                    found = true
                                    break
                                }
                            }
                        }
                    }

                    if (!found) { //not found in imageList, delete it!
                        val deletePath = "$dest_images_folder${files[i].name}"
                        val deleteFile = File(deletePath)
                        val deleteUri  = Uri.fromFile(deleteFile)
                        Log.d(mTag, "deleteUri = $deleteUri")
                        try {
                            if (deleteFile.exists()) {
                                deleteFile.delete()
                                //val cr = contentResolver
                                //cr.delete(deleteUri, null, null)
                                Log.d(mTag, "Delete $deletePath")
                            }

                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } else {
            Log.e(mTag , "adSettingList.size == 0")
        }
        Log.d(mTag, "=== clearImagesNotInImageList end ===")
    }

    fun clearVideosNotInVideoList() {
        Log.d(mTag, "=== clearVideosNotInVideoList start ===")

        if (adSettingList.size > 0) {
            val directory = File(dest_videos_folder)
            val files = directory.listFiles()

            Log.d(mTag, "files -> $files")

            if (directory.isDirectory && files != null) {

                for (i in files.indices) {
                    var found = false

                    for (j in adSettingList.indices) {
                        if (adSettingList[j].plan_videos.isNotEmpty()) {
                            val videosArray = adSettingList[j].plan_videos.split(",")
                            for (k in videosArray.indices) {
                                if (files[i].name == videosArray[k]) {
                                    found = true
                                    break
                                }
                            }
                        }
                    }

                    if (!found) { //not found in imageList, delete it!
                        val deletePath = "$dest_videos_folder${files[i].name}"
                        val deleteFile = File(deletePath)
                        val deleteUri  = Uri.fromFile(deleteFile)
                        Log.d(mTag, "deleteUri = $deleteUri")
                        try {
                            if (deleteFile.exists()) {
                                deleteFile.delete()
                                //val cr = contentResolver
                                //cr.delete(deleteUri, null, null)
                                Log.d(mTag, "Delete $deletePath")
                            }

                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } else {
            Log.e(mTag , "adSettingList.size == 0")
        }
        Log.d(mTag, "=== clearVideosNotInVideoList end ===")
    }

    fun clearMixNotInMixList() {
        Log.d(mTag, "=== clearMixNotInMixList start ===")

        if (adSettingList.size > 0) {
            val imageDirectory = File(dest_images_folder)
            val videoDirectory = File(dest_videos_folder)
            val imageFiles = imageDirectory.listFiles()
            val videoFiles = videoDirectory.listFiles()


            Log.d(mTag, "imageFiles -> $imageFiles")
            Log.d(mTag, "videoFiles -> $videoFiles")

            //images
            if (imageDirectory.isDirectory && imageFiles != null) {

                for (i in imageFiles.indices) {
                    var found = false

                    if (adSettingList.size > 0) {
                        for (j in adSettingList.indices) {
                            //images
                            if (adSettingList[j].plan_images.isNotEmpty()) {
                                val imagesArray = adSettingList[j].plan_images.split(",")
                                for (k in imagesArray.indices) {
                                    if (imageFiles[i].name == imagesArray[k]) {
                                        found = true
                                        break
                                    }
                                }
                            }
                            //mix
                            if (adSettingList[j].plan_mix.isNotEmpty()) {
                                val mixArray = adSettingList[j].plan_mix.split(",")
                                for (k in mixArray.indices) {
                                    if (imageFiles[i].name == mixArray[k]) {
                                        found = true
                                        break
                                    }
                                }
                            }

                            if (found) {
                                break
                            }
                        }
                    }

                    if (!found) { //not found in imageList and mixList, delete it!
                        val deletePath = "$dest_images_folder${imageFiles[i].name}"
                        val deleteFile = File(deletePath)
                        val deleteUri  = Uri.fromFile(deleteFile)
                        Log.d(mTag, "deleteUri = $deleteUri")
                        try {
                            if (deleteFile.exists()) {
                                deleteFile.delete()
                                //val cr = contentResolver
                                //cr.delete(deleteUri, null, null)
                                Log.d(mTag, "Delete $deletePath")
                            }

                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            //videos
            if (videoDirectory.isDirectory && videoFiles != null) {

                for (i in videoFiles.indices) {
                    var found = false

                    if (adSettingList.size > 0) {
                        for (j in adSettingList.indices) {
                            //videos
                            if (adSettingList[j].plan_videos.isNotEmpty()) {
                                val videosArray = adSettingList[j].plan_videos.split(",")
                                for (k in videosArray.indices) {
                                    if (videoFiles[i].name == videosArray[k]) {
                                        found = true
                                        break
                                    }
                                }
                            }
                            //mix
                            if (adSettingList[j].plan_mix.isNotEmpty()) {
                                val mixArray = adSettingList[j].plan_mix.split(",")
                                for (k in mixArray.indices) {
                                    if (videoFiles[i].name == mixArray[k]) {
                                        found = true
                                        break
                                    }
                                }
                            }

                            if (found) {
                                break
                            }
                        }
                    }

                    if (!found) { //not found in videoList and mixList, delete it!
                        val deletePath = "$dest_videos_folder${videoFiles[i].name}"
                        val deleteFile = File(deletePath)
                        val deleteUri  = Uri.fromFile(deleteFile)
                        Log.d(mTag, "deleteUri = $deleteUri")
                        try {
                            if (deleteFile.exists()) {
                                deleteFile.delete()
                                //val cr = contentResolver
                                //cr.delete(deleteUri, null, null)
                                Log.d(mTag, "Delete $deletePath")
                            }

                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } else {
            Log.e(mTag , "adSettingList.size == 0")
        }
        Log.d(mTag, "=== clearMixNotInMixList end ===")
    }

    fun clearAllNotInAllList() {
        Log.d(mTag, "=== clearAllNotInAllList start ===")

        if (adSettingList.size > 0) {
            val bannerDirectory = File(dest_banner_folder)
            val imageDirectory = File(dest_images_folder)
            val videoDirectory = File(dest_videos_folder)
            val bannerFiles = bannerDirectory.listFiles()
            val imageFiles = imageDirectory.listFiles()
            val videoFiles = videoDirectory.listFiles()

            Log.d(mTag, "bannerFiles -> $bannerFiles")
            Log.d(mTag, "imageFiles -> $imageFiles")
            Log.d(mTag, "videoFiles -> $videoFiles")

            //banner
            if (bannerDirectory.isDirectory && bannerFiles != null) {
                for (i in bannerFiles.indices) {
                    var found = false

                    for (j in adSettingList.indices) {
                        if (adSettingList[j].plan_banner.isNotEmpty()) {
                            val imagesArray = adSettingList[j].plan_banner.split(",")
                            for (k in imagesArray.indices) {
                                if (bannerFiles[i].name == imagesArray[k]) {
                                    found = true
                                    break
                                }
                            }
                        }
                    }

                    if (!found) { //not found in bannerList, delete it!
                        val deletePath = "$dest_banner_folder${bannerFiles[i].name}"
                        val deleteFile = File(deletePath)
                        val deleteUri  = Uri.fromFile(deleteFile)
                        Log.d(mTag, "deleteUri = $deleteUri")
                        try {
                            if (deleteFile.exists()) {
                                deleteFile.delete()
                                //val cr = contentResolver
                                //cr.delete(deleteUri, null, null)
                                Log.d(mTag, "Delete $deletePath")
                            }

                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            //images
            if (imageDirectory.isDirectory && imageFiles != null) {

                for (i in imageFiles.indices) {
                    var found = false

                    if (adSettingList.size > 0) {
                        for (j in adSettingList.indices) {
                            //images
                            if (adSettingList[j].plan_images.isNotEmpty()) {
                                val imagesArray = adSettingList[j].plan_images.split(",")
                                for (k in imagesArray.indices) {
                                    if (imageFiles[i].name == imagesArray[k]) {
                                        found = true
                                        break
                                    }
                                }
                            }
                            //mix
                            if (adSettingList[j].plan_mix.isNotEmpty()) {
                                val mixArray = adSettingList[j].plan_mix.split(",")
                                for (k in mixArray.indices) {
                                    if (imageFiles[i].name == mixArray[k]) {
                                        found = true
                                        break
                                    }
                                }
                            }

                            if (found) {
                                break
                            }
                        }
                    }

                    if (!found) { //not found in imageList and mixList, delete it!
                        val deletePath = "$dest_images_folder${imageFiles[i].name}"
                        val deleteFile = File(deletePath)
                        val deleteUri  = Uri.fromFile(deleteFile)
                        Log.d(mTag, "deleteUri = $deleteUri")
                        try {
                            if (deleteFile.exists()) {
                                deleteFile.delete()
                                //val cr = contentResolver
                                //cr.delete(deleteUri, null, null)
                                Log.d(mTag, "Delete $deletePath")
                            }

                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            //videos
            if (videoDirectory.isDirectory && videoFiles != null) {

                for (i in videoFiles.indices) {
                    var found = false

                    if (adSettingList.size > 0) {
                        for (j in adSettingList.indices) {
                            //videos
                            if (adSettingList[j].plan_videos.isNotEmpty()) {
                                val videosArray = adSettingList[j].plan_videos.split(",")
                                for (k in videosArray.indices) {
                                    if (videoFiles[i].name == videosArray[k]) {
                                        found = true
                                        break
                                    }
                                }
                            }
                            //mix
                            if (adSettingList[j].plan_mix.isNotEmpty()) {
                                val mixArray = adSettingList[j].plan_mix.split(",")
                                for (k in mixArray.indices) {
                                    if (videoFiles[i].name == mixArray[k]) {
                                        found = true
                                        break
                                    }
                                }
                            }

                            if (found) {
                                break
                            }
                        }
                    }

                    if (!found) { //not found in videoList and mixList, delete it!
                        val deletePath = "$dest_videos_folder${videoFiles[i].name}"
                        val deleteFile = File(deletePath)
                        val deleteUri  = Uri.fromFile(deleteFile)
                        Log.d(mTag, "deleteUri = $deleteUri")
                        try {
                            if (deleteFile.exists()) {
                                deleteFile.delete()
                                //val cr = contentResolver
                                //cr.delete(deleteUri, null, null)
                                Log.d(mTag, "Delete $deletePath")
                            }

                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } else {
            Log.e(mTag , "adSettingList.size == 0")
        }
        Log.d(mTag, "=== clearAllNotInAllList end ===")
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
        //if (videoViewTop != null && videoViewTop!!.isPlaying) {
        if (videoViewTop != null) {
            videoViewTop!!.stopPlayback()
            videoViewTop!!.seekTo(0)

            //videoViewTop!!.visibility = View.INVISIBLE
        }
        //if (videoViewCenter != null && videoViewCenter!!.isPlaying) {
        if (videoViewCenter != null) {
            videoViewCenter!!.stopPlayback()
            videoViewCenter!!.seekTo(0)
            //videoViewCenter!!.visibility = View.INVISIBLE
        }
        //if (videoViewBottom != null && videoViewBottom!!.isPlaying) {
        if (videoViewBottom != null) {
            videoViewBottom!!.stopPlayback()
            videoViewBottom!!.seekTo(0)

            //videoViewBottom!!.visibility = View.INVISIBLE
        }

        //clear layout
        rootView!!.removeAllViews()
        rootView!!.setBackgroundColor(Color.BLACK)
        //rootView!!.setBackgroundResource(R.drawable.customborder)

        //init play index
        currentTextIndexTop = -1
        currentImageIndexTop = -1
        currentVideoIndexTop = -1
        //currentMixIndexTop = -1
        currentTextIndexCenter = -1
        currentImageIndexCenter = -1
        currentVideoIndexCenter = -1
        //currentMixIndexCenter = -1
        currentTextIndexBottom = -1
        currentImageIndexBottom = -1
        currentVideoIndexBottom = -1
        //currentMixIndexBottom = -1

        //init layout
        if (layoutList.size > 0 ) {
            if (layoutList.size == 1) { //only one layout
                if (adSettingList.size > 0) { // must have adSetting
                    val orientation = layoutList[0].orientation
                    layoutTop = 0
                    layoutCenter = 0
                    layoutBottom = 0

                    mainLayoutWeight = 0
                    layoutTopWeight = 0
                    layoutCenterWeight = 0
                    layoutBottomWeight = 0
                    layoutTriangleWeight = 0

                    layoutTopWidth = 0
                    layoutCenterWidth = 0
                    layoutBottomWidth = 0

                    layoutTopHeight = 0
                    layoutCenterHeight = 0
                    layoutBottomHeight = 0

                    when(layoutList[0].border) {
                        1 -> rootView!!.setBackgroundResource(R.drawable.border_gold)
                        2 -> rootView!!.setBackgroundResource(R.drawable.border_silver)

                    }



                    Log.d(mTag, "playAd currentAdSettingIdx = $currentAdSettingIdx")
                    Log.d(mTag, "playAd currentPlanUse = $currentPlanUse")
                    //when(currentAdSettingIdx) {
                    when(currentPlanUse) {
                        1 -> { //plan1
                            layoutTop = layoutList[0].layout_top
                            layoutCenter = layoutList[0].layout_center
                            layoutBottom = layoutList[0].layout_bottom

                            layoutTopWeight = layoutList[0].plan_layout_top_weight
                            layoutCenterWeight = layoutList[0].plan_layout_center_weight
                            layoutBottomWeight = layoutList[0].plan_layout_bottom_weight
                            layoutTriangleWeight = layoutList[0].plan_layout_tri_weight
                        }
                        2 -> { //plan2
                            layoutTop = layoutList[0].layout2_top
                            layoutCenter = layoutList[0].layout2_center
                            layoutBottom = layoutList[0].layout2_bottom

                            layoutTopWeight = layoutList[0].plan2_layout_top_weight
                            layoutCenterWeight = layoutList[0].plan2_layout_center_weight
                            layoutBottomWeight = layoutList[0].plan2_layout_bottom_weight
                            layoutTriangleWeight = layoutList[0].plan2_layout_tri_weight


                        }
                        3 -> { //plan3
                            layoutTop = layoutList[0].layout3_top
                            layoutCenter = layoutList[0].layout3_center
                            layoutBottom = layoutList[0].layout3_bottom

                            layoutTopWeight = layoutList[0].plan3_layout_top_weight
                            layoutCenterWeight = layoutList[0].plan3_layout_center_weight
                            layoutBottomWeight = layoutList[0].plan3_layout_bottom_weight
                            layoutTriangleWeight = layoutList[0].plan3_layout_tri_weight
                        }
                        4 -> { //plan4
                            layoutTop = layoutList[0].layout4_top
                            layoutCenter = layoutList[0].layout4_center
                            layoutBottom = layoutList[0].layout4_bottom

                            layoutTopWeight = layoutList[0].plan4_layout_top_weight
                            layoutCenterWeight = layoutList[0].plan4_layout_center_weight
                            layoutBottomWeight = layoutList[0].plan4_layout_bottom_weight
                            layoutTriangleWeight = layoutList[0].plan4_layout_tri_weight
                        }
                    }
                    //val layoutTop = layoutList[0].layout_top
                    //val layoutCenter = layoutList[0].layout_center
                    //val layoutBottom = layoutList[0].layout_bottom
                    val layoutOrientation = layoutList[0].layoutOrientation

                    marqueeMode = adSettingList[currentAdSettingIdx].marquee_mode
                    imagesMode = adSettingList[currentAdSettingIdx].images_mode
                    videosMode = adSettingList[currentAdSettingIdx].videos_mode


                    val imageInterval = adSettingList[currentAdSettingIdx].image_interval
                    val marqueeInterval = adSettingList[currentAdSettingIdx].marquee_interval


                    val imageScaleType = adSettingList[currentAdSettingIdx].image_scale_type
                    val imageAnime = adSettingList[currentAdSettingIdx].image_anime
                    val videoScaleType = adSettingList[currentAdSettingIdx].video_scale_type
                    val bannerScaleType = adSettingList[currentAdSettingIdx].banner_scale_type


                    val marqueeBackground = adSettingList[currentAdSettingIdx].marquee_background
                    val marqueeText = adSettingList[currentAdSettingIdx].marquee_text
                    val marqueeSize = adSettingList[currentAdSettingIdx].marquee_size
                    val marqueeLocate = adSettingList[currentAdSettingIdx].marquee_locate
                    val marqueeSpeed = adSettingList[currentAdSettingIdx].marquee_speed
                    //for mix
                    mixMode = adSettingList[currentAdSettingIdx].mix_mode
                    mixImageInterval = adSettingList[currentAdSettingIdx].mix_image_interval
                    mixImageScaleType = adSettingList[currentAdSettingIdx].mix_image_scale_type
                    mixImageAnime = adSettingList[currentAdSettingIdx].mix_image_anime
                    mixVideoScaleType = adSettingList[currentAdSettingIdx].mix_video_scale_type

                    var marqueePlayInterval = 60000 // 60 seconds
                    when(marqueeInterval) {
                        0 -> {
                            marqueePlayInterval = 60000
                        }
                        1 -> {
                            marqueePlayInterval = 90000
                        }
                        2 -> {
                            marqueePlayInterval = 120000
                        }
                    }

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
                    Log.d(mTag, "marqueeMode = $marqueeMode, imagesMode = $imagesMode, videosMode = $videosMode, mixMode = $mixMode, layoutOrientation = $layoutOrientation")

                    Log.d(mTag, "imageScaleType = $imageScaleType, videoScaleType = $videoScaleType, bannerScaleType = $bannerScaleType")

                    Log.d(mTag, "marqueeBackground = $marqueeBackground, marqueeText = $marqueeText, marqueeSize = $marqueeSize, marqueeLocate = $marqueeLocate, marqueeSpeed = $marqueeSpeed")

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
                        mainLinearLayout = LinearLayout(this@MainActivity)
                        mainLinearLayout!!.setPadding(12,12,12,12)
                    }
                    mainLinearLayout!!.removeAllViews()
                    mainLinearLayout!!.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)


                    //layout select
                    when (layoutOrientation) {
                        1 -> { //horizontal
                            mainLinearLayout!!.orientation = LinearLayout.HORIZONTAL
                            Log.d(mTag, "mainLinearLayout: HORIZONTAL")
                            if (currentOrientation == 1) { // portrait
                                mainLayoutWeight = layoutList[0].screenWidth

                                layoutTopWidth = layoutTopWeight
                                layoutCenterWidth = layoutCenterWeight
                                layoutBottomWidth = layoutCenterWeight

                                layoutTopHeight = layoutList[0].screenHeight
                                layoutCenterHeight = layoutList[0].screenHeight
                                layoutBottomHeight = layoutList[0].screenHeight
                            } else { // landscape
                                mainLayoutWeight = layoutList[0].screenHeight

                                layoutTopWidth = layoutTopWeight
                                layoutCenterWidth = layoutCenterWeight
                                layoutBottomWidth = layoutBottomWeight

                                layoutTopHeight = layoutList[0].screenWidth
                                layoutCenterHeight = layoutList[0].screenWidth
                                layoutBottomHeight = layoutList[0].screenWidth
                            }
                        }
                        2 -> { //left triangle
                            mainLinearLayout!!.orientation = LinearLayout.HORIZONTAL
                            Log.d(mTag, "mainLinearLayout: HORIZONTAL")
                            if (currentOrientation == 1) { // portrait
                                mainLayoutWeight = layoutList[0].screenWidth
                                layoutTriangleWeight = layoutList[0].screenWidth - layoutTopWeight

                                layoutTopWidth = layoutTopWeight
                                layoutCenterWidth = layoutTriangleWeight
                                layoutBottomWidth = layoutTriangleWeight

                                layoutTopHeight = layoutList[0].screenHeight
                                layoutCenterHeight = layoutCenterWeight
                                layoutBottomHeight = layoutBottomWeight
                            } else { // landscape
                                mainLayoutWeight = layoutList[0].screenHeight
                                layoutTriangleWeight = layoutList[0].screenHeight - layoutTopWeight

                                layoutTopWidth = layoutTopWeight
                                layoutCenterWidth = layoutTriangleWeight
                                layoutBottomWidth = layoutTriangleWeight

                                layoutTopHeight = layoutList[0].screenWidth
                                layoutCenterHeight = layoutCenterWeight
                                layoutBottomHeight = layoutBottomWeight
                            }
                        }
                        3 -> { //right triangle
                            mainLinearLayout!!.orientation = LinearLayout.HORIZONTAL
                            Log.d(mTag, "mainLinearLayout: HORIZONTAL")
                            if (currentOrientation == 1) { // portrait
                                mainLayoutWeight = layoutList[0].screenWidth
                                layoutTriangleWeight = layoutList[0].screenWidth - layoutBottomWeight

                                layoutTopWidth = mainLayoutWeight - layoutBottomWeight
                                layoutCenterWidth = layoutTopWidth
                                layoutBottomWidth = layoutBottomWeight

                                layoutTopHeight = layoutTopWeight
                                layoutCenterHeight = layoutCenterWeight
                                layoutBottomHeight = layoutList[0].screenHeight
                            } else { // landscape
                                mainLayoutWeight = layoutList[0].screenHeight
                                layoutTriangleWeight = layoutList[0].screenHeight - layoutBottomWeight

                                layoutTopWidth = mainLayoutWeight - layoutBottomWeight
                                layoutCenterWidth = layoutTopWidth
                                layoutBottomWidth = layoutBottomWeight

                                layoutTopHeight = layoutTopWeight
                                layoutCenterHeight = layoutCenterWeight
                                layoutBottomHeight = layoutList[0].screenWidth
                            }
                        }
                        4 -> { //up triangle
                            mainLinearLayout!!.orientation = LinearLayout.VERTICAL
                            Log.d(mTag, "mainLinearLayout: VERTICAL")
                            if (currentOrientation == 1) { // portrait
                                mainLayoutWeight = layoutList[0].screenHeight
                                layoutTriangleWeight = layoutList[0].screenHeight - layoutTopWeight

                                layoutTopWidth =  layoutList[0].screenWidth
                                layoutCenterWidth = layoutCenterWeight
                                layoutBottomWidth = layoutBottomWeight

                                layoutTopHeight = layoutTopWeight
                                layoutCenterHeight = layoutTriangleWeight
                                layoutBottomHeight = layoutTriangleWeight
                            } else { // landscape
                                mainLayoutWeight = layoutList[0].screenWidth
                                layoutTriangleWeight = layoutList[0].screenWidth - layoutTopWeight

                                layoutTopWidth = layoutList[0].screenHeight
                                layoutCenterWidth = layoutCenterWeight
                                layoutBottomWidth = layoutBottomWeight

                                layoutTopHeight = layoutTopWeight
                                layoutCenterHeight = layoutTriangleWeight
                                layoutBottomHeight = layoutTriangleWeight
                            }
                        }
                        5 -> { //down triangle
                            mainLinearLayout!!.orientation = LinearLayout.VERTICAL
                            Log.d(mTag, "mainLinearLayout: VERTICAL")
                            if (currentOrientation == 1) { // portrait
                                mainLayoutWeight = layoutList[0].screenHeight
                                layoutTriangleWeight = layoutList[0].screenHeight - layoutBottomWeight

                                layoutTopWidth =  layoutTopWeight
                                layoutCenterWidth = layoutCenterWeight
                                layoutBottomWidth = layoutList[0].screenWidth

                                layoutTopHeight = layoutTriangleWeight
                                layoutCenterHeight = layoutTriangleWeight
                                layoutBottomHeight = layoutBottomWeight
                            } else {  // landscape
                                mainLayoutWeight = layoutList[0].screenWidth
                                layoutTriangleWeight = layoutList[0].screenWidth - layoutBottomWeight

                                layoutTopWidth =  layoutTopWeight
                                layoutCenterWidth = layoutCenterWeight
                                layoutBottomWidth = layoutList[0].screenHeight

                                layoutTopHeight = layoutTriangleWeight
                                layoutCenterHeight = layoutTriangleWeight
                                layoutBottomHeight = layoutBottomWeight
                            }
                        }
                        else -> { //vertical
                            mainLinearLayout!!.orientation = LinearLayout.VERTICAL
                            Log.d(mTag, "mainLinearLayout: VERTICAL")
                            if (currentOrientation == 1) { // Vertical
                                //weightSum = screen Height
                                mainLayoutWeight = layoutList[0].screenHeight

                                layoutTopWidth = layoutList[0].screenWidth
                                layoutCenterWidth = layoutList[0].screenWidth
                                layoutBottomWidth = layoutList[0].screenWidth

                                layoutTopHeight = layoutTopWeight
                                layoutCenterHeight = layoutCenterWeight
                                layoutBottomHeight = layoutBottomWeight
                            } else { // Horizontal
                                mainLayoutWeight = layoutList[0].screenWidth

                                layoutTopWidth = layoutTopWeight
                                layoutCenterWidth = layoutCenterWeight
                                layoutBottomWidth = layoutBottomWeight

                                layoutTopHeight = layoutList[0].screenHeight
                                layoutCenterHeight = layoutList[0].screenHeight
                                layoutBottomHeight = layoutList[0].screenHeight
                            }
                        }
                    }

                    Log.e(mTag, "mainLayoutWeight = $mainLayoutWeight")
                    Log.e(mTag, "layoutTopWeight = $layoutTopWeight")
                    Log.e(mTag, "layoutCenterWeight = $layoutCenterWeight")
                    Log.e(mTag, "layoutBottomWeight = $layoutBottomWeight")
                    Log.e(mTag, "layoutTriangleWeight = $layoutTriangleWeight")
                    rootView!!.addView(mainLinearLayout)

                    //linearLayoutTriangle for new layout
                    if (linearLayoutTriangle == null) {
                        linearLayoutTriangle = LinearLayout(this@MainActivity)
                    }
                    linearLayoutTriangle!!.removeAllViews()
                    linearLayoutTriangle!!.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
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
                        linearLayoutTop = LinearLayout(this@MainActivity)
                    }
                    linearLayoutTop!!.removeAllViews()
                    //linearLayoutTopWeight = layoutTopWeight / mainLayoutWeight * 100
                    //Log.e(mTag, "linearLayoutTopWeight = $linearLayoutTopWeight")
                    linearLayoutTop!!.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    linearLayoutTop!!.orientation = LinearLayout.VERTICAL
                    //linearLayoutTop!!.setBackgroundColor(Color.parseColor(marqueeBackground))
                    //linearLayoutTop!!.weightSum = 2.0F

                    when (layoutTop) {
                        1 -> {
                            //textViewTop
                            if (textViewTop == null) {
                                textViewTop = ScrollTextView(this@MainActivity as  Context)
                                textViewTop!!.isFocusable = false
                                textViewTop!!.ellipsize = TextUtils.TruncateAt.MARQUEE
                                textViewTop!!.isSingleLine = true
                                textViewTop!!.freezesText = true
                                textViewTop!!.setHorizontallyScrolling(true)
                                textViewTop!!.marqueeRepeatLimit = -1
                                textViewTop!!.resumeScroll()
                                textViewTop!!.isFocusableInTouchMode = true
                                textViewTop!!.isSelected = true

                                /*textViewTop!!.onFocusChangeListener =
                                    OnFocusChangeListener { v, hasFocus ->
                                        Log.e(mTag, "textViewTopO nFocusChangeListener")
                                    }
                                textViewTop!!.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                                    Log.e(mTag, "textViewTop OnLayoutChangeListener")
                                    val params: LayoutParams = v.layoutParams as LayoutParams
                                    params.width = right - left
                                    params.height = bottom - top

                                    Log.e(mTag, "params.width = ${params.width}, params.height = ${params.height}")

                                    params.weight = 0f
                                    v.layoutParams = params

                                }*/
                            }
                            //background
                            textViewTop!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            if (marqueeBackground.isNotEmpty()) {
                                textViewTop!!.setBackgroundColor(Color.parseColor(marqueeBackground))
                            }
                            //text color
                            textViewTop!!.textSize = marqueeSize.toFloat()
                            if (marqueeText.isNotEmpty()) {
                                textViewTop!!.setTextColor(Color.parseColor(marqueeText))
                            }
                            //locate
                            when(marqueeLocate) {
                                0 -> {
                                    textViewTop!!.gravity = Gravity.CENTER_VERTICAL
                                }
                                1 -> {
                                    textViewTop!!.gravity = Gravity.TOP
                                }
                                2 -> {
                                    textViewTop!!.gravity = Gravity.BOTTOM
                                }
                            }
                            //speed
                            //textViewTop!!.setSpeed(marqueeSpeed.toFloat())
                            textViewTop!!.rndDuration = 100000 / (marqueeSpeed / 50)
                            textViewTop!!.visibility = View.GONE
                            /*
                            textViewTop!!.post(Runnable {
                                Log.e(mTag, "textViewTop!!.width = ${textViewTop!!.width}, textViewTop!!.height = ${textViewTop!!.height}")
                                textViewTop!!.layoutParams = LayoutParams(
                                    textViewTop!!.width,
                                    textViewTop!!.height
                                )
                            })*/

                            linearLayoutTop!!.addView(textViewTop)
                            linearLayoutTop!!.setBackgroundColor(Color.parseColor(marqueeBackground))
                            when(marqueeLocate) {
                                0 -> {
                                    linearLayoutTop!!.gravity = Gravity.CENTER_VERTICAL
                                }
                                1 -> {
                                    linearLayoutTop!!.gravity = Gravity.TOP
                                }
                                2 -> {
                                    linearLayoutTop!!.gravity = Gravity.BOTTOM
                                }
                            }
                        }
                        2 -> {
                            //imageViewTop
                            if (imageViewTop == null) {
                                imageViewTop = ImageView(this@MainActivity)
                            }
                            imageViewTop!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewTop!!.visibility = View.GONE
                            //imageViewTop!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            linearLayoutTop!!.addView(imageViewTop)
                            //imageViewTop2
                            if (imageViewTop2 == null) {
                                imageViewTop2 = ImageView(this@MainActivity)
                            }
                            imageViewTop2!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewTop2!!.visibility = View.GONE
                            //imageViewTop2!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            linearLayoutTop!!.addView(imageViewTop2)
                            linearLayoutTop!!.setBackgroundColor(Color.parseColor(defaultBackGroundColor))
                            if (imageScaleType == 1) { //fillXY
                                imageViewTop!!.scaleType = ImageView.ScaleType.FIT_XY
                                imageViewTop2!!.scaleType = ImageView.ScaleType.FIT_XY
                            } else { //default
                                imageViewTop!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                imageViewTop2!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            }
                        }
                        3 -> {
                            //videoViewLayoutTop
                            if (videoViewLayoutTop == null) {
                                videoViewLayoutTop = RelativeLayout(this@MainActivity)
                            }
                            videoViewLayoutTop!!.removeAllViews()
                            videoViewLayoutTop!!.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                            videoViewLayoutTop!!.gravity = Gravity.CENTER
                            videoViewLayoutTop!!.visibility = View.GONE
                            linearLayoutTop!!.addView(videoViewLayoutTop)
                            linearLayoutTop!!.setBackgroundColor(Color.parseColor(defaultBackGroundColor))
                            //videoViewTop
                            if (videoViewTop == null) {
                                videoViewTop = VideoView(this@MainActivity)
                                /*videoViewTop!!.setOnTouchListener(OnTouchListener { v, event -> // do nothing here......
                                    true
                                })*/
                            }
                            //videoViewTop!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            if (videoScaleType == 1) {
                                val layoutParams = RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.MATCH_PARENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT
                                )
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                                videoViewTop!!.layoutParams = layoutParams
                            } else {
                                videoViewTop!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            }

                            videoViewLayoutTop!!.addView(videoViewTop)
                        }
                        4 -> { //banner
                            //imageViewTop
                            if (imageViewTop == null) {
                                imageViewTop = ImageView(this@MainActivity)
                            }
                            imageViewTop!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewTop!!.visibility = View.GONE
                            linearLayoutTop!!.addView(imageViewTop)
                            linearLayoutTop!!.setBackgroundColor(Color.parseColor(defaultBackGroundColor))
                            if (bannerScaleType == 1) { //fillXY
                                imageViewTop!!.scaleType = ImageView.ScaleType.FIT_XY
                            } else { //default
                                imageViewTop!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            }
                            //imageViewTop2
                            /*if (imageViewTop2 == null) {
                                imageViewTop2 = ImageView(this@MainActivity)
                            }
                            imageViewTop2!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewTop2!!.visibility = View.GONE
                            linearLayoutTop!!.addView(imageViewTop2)
                            */
                        }
                        5 -> { //mix
                            //image
                            if (imageViewTop == null) {
                                imageViewTop = ImageView(this@MainActivity)
                            }
                            imageViewTop!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewTop!!.visibility = View.GONE
                            //imageViewTop!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            linearLayoutTop!!.addView(imageViewTop)
                            //imageViewTop2
                            if (imageViewTop2 == null) {
                                imageViewTop2 = ImageView(this@MainActivity)
                            }
                            imageViewTop2!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewTop2!!.visibility = View.GONE
                            //imageViewTop2!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            linearLayoutTop!!.addView(imageViewTop2)
                            if (mixImageScaleType == 1) { //fillXY
                                imageViewTop!!.scaleType = ImageView.ScaleType.FIT_XY
                                imageViewTop2!!.scaleType = ImageView.ScaleType.FIT_XY
                            } else { //default
                                imageViewTop!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                imageViewTop2!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            }
                            //video
                            if (videoViewLayoutTop == null) {
                                videoViewLayoutTop = RelativeLayout(this@MainActivity)
                            }
                            videoViewLayoutTop!!.removeAllViews()
                            videoViewLayoutTop!!.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                            videoViewLayoutTop!!.gravity = Gravity.CENTER
                            videoViewLayoutTop!!.visibility = View.GONE
                            linearLayoutTop!!.addView(videoViewLayoutTop)
                            linearLayoutTop!!.setBackgroundColor(Color.parseColor(defaultBackGroundColor))
                            //videoViewTop
                            if (videoViewTop == null) {
                                videoViewTop = VideoView(this@MainActivity)
                                /*videoViewTop!!.setOnTouchListener(OnTouchListener { v, event -> // do nothing here......
                                    true
                                })*/
                            }
                            //videoViewTop!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            if (mixVideoScaleType == 1) {
                                val layoutParams = RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.MATCH_PARENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT
                                )
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                                videoViewTop!!.layoutParams = layoutParams
                            } else {
                                videoViewTop!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            }

                            videoViewLayoutTop!!.addView(videoViewTop)
                        }
                    }

                    //LinearLayoutCenter
                    if (linearLayoutCenter == null) {
                        linearLayoutCenter = LinearLayout(this@MainActivity)
                    }
                    linearLayoutCenter!!.removeAllViews()
                    //linearLayoutCenterWeight = layoutCenterWeight / mainLayoutWeight * 100
                    //Log.e(mTag, "linearLayoutCenterWeight = $linearLayoutCenterWeight")
                    linearLayoutCenter!!.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    linearLayoutCenter!!.orientation = LinearLayout.VERTICAL

                    //linearLayoutCenter!!.setBackgroundColor(Color.parseColor(marqueeBackground))
                    //linearLayoutCenter!!.weightSum = 2.0F

                    when (layoutCenter) {
                        1 -> {
                            //textViewCenter
                            if (textViewCenter == null) {
                                textViewCenter = ScrollTextView(this@MainActivity)
                                textViewCenter!!.ellipsize = TextUtils.TruncateAt.MARQUEE
                                textViewCenter!!.isSingleLine = true
                                textViewCenter!!.freezesText = true
                                textViewCenter!!.setHorizontallyScrolling(true)
                                textViewCenter!!.marqueeRepeatLimit = -1
                                //textViewCenter!!.resumeScroll()
                                textViewCenter!!.isFocusableInTouchMode = true
                                textViewCenter!!.isSelected = true
                            }

                            //background
                            textViewCenter!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            if (marqueeBackground.isNotEmpty()) {
                                textViewCenter!!.setBackgroundColor(Color.parseColor(marqueeBackground))
                            }
                            //text color
                            textViewCenter!!.textSize = marqueeSize.toFloat()
                            if (marqueeText.isNotEmpty()) {
                                textViewCenter!!.setTextColor(Color.parseColor(marqueeText))
                            }
                            //locate
                            when(marqueeLocate) {
                                0 -> {
                                    textViewCenter!!.gravity = Gravity.CENTER_VERTICAL
                                }
                                1 -> {
                                    textViewCenter!!.gravity = Gravity.TOP
                                }
                                2 -> {
                                    textViewCenter!!.gravity = Gravity.BOTTOM
                                }
                            }
                            //speed
                            //textViewCenter!!.setSpeed(marqueeSpeed.toFloat())
                            textViewCenter!!.rndDuration = 100000 / (marqueeSpeed / 50)
                            textViewCenter!!.startScroll()

                            textViewCenter!!.visibility = View.GONE

                            linearLayoutCenter!!.addView(textViewCenter)
                            linearLayoutCenter!!.setBackgroundColor(Color.parseColor(marqueeBackground))
                            when(marqueeLocate) {
                                0 -> {
                                    linearLayoutCenter!!.gravity = Gravity.CENTER_VERTICAL
                                }
                                1 -> {
                                    linearLayoutCenter!!.gravity = Gravity.TOP
                                }
                                2 -> {
                                    linearLayoutCenter!!.gravity = Gravity.BOTTOM
                                }
                            }
                        }
                        2 -> {
                            //imageViewCenter
                            if (imageViewCenter == null) {
                                imageViewCenter = ImageView(this@MainActivity)
                            }
                            imageViewCenter!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewCenter!!.visibility = View.GONE
                            linearLayoutCenter!!.addView(imageViewCenter)
                            //imageViewCenter2
                            if (imageViewCenter2 == null) {
                                imageViewCenter2 = ImageView(this@MainActivity)
                            }
                            imageViewCenter2!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewCenter2!!.visibility = View.GONE
                            linearLayoutCenter!!.addView(imageViewCenter2)
                            linearLayoutCenter!!.setBackgroundColor(Color.parseColor(defaultBackGroundColor))
                            if (imageScaleType == 1) { //fillXY
                                imageViewCenter!!.scaleType = ImageView.ScaleType.FIT_XY
                                imageViewCenter2!!.scaleType = ImageView.ScaleType.FIT_XY
                            } else { //default
                                imageViewCenter!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                imageViewCenter2!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            }
                        }
                        3 -> {
                            //videoViewLayoutCenter
                            if (videoViewLayoutCenter == null) {
                                videoViewLayoutCenter = RelativeLayout(this@MainActivity)
                            }
                            videoViewLayoutCenter!!.removeAllViews()
                            videoViewLayoutCenter!!.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                            videoViewLayoutCenter!!.layoutParams
                            videoViewLayoutCenter!!.gravity = Gravity.CENTER
                            videoViewLayoutCenter!!.visibility = View.GONE
                            linearLayoutCenter!!.addView(videoViewLayoutCenter)
                            linearLayoutCenter!!.setBackgroundColor(Color.parseColor(defaultBackGroundColor))
                            //videoViewCenter
                            if (videoViewCenter == null) {
                                videoViewCenter = VideoView(this@MainActivity)
                                /*videoViewCenter!!.setOnTouchListener(OnTouchListener { v, event -> // do nothing here......
                                    true
                                })*/
                            }
                            //videoViewCenter!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            if (videoScaleType == 1) {
                                val layoutParams = RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.MATCH_PARENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT
                                )
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                                videoViewCenter!!.layoutParams = layoutParams
                            } else {
                                videoViewCenter!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            }

                            videoViewLayoutCenter!!.addView(videoViewCenter)
                        }
                        4 -> { //banner
                            //imageViewCenter
                            if (imageViewCenter == null) {
                                imageViewCenter = ImageView(this@MainActivity)
                            }
                            imageViewCenter!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewCenter!!.visibility = View.GONE
                            linearLayoutCenter!!.addView(imageViewCenter)
                            linearLayoutCenter!!.setBackgroundColor(Color.parseColor(defaultBackGroundColor))
                            if (bannerScaleType == 1) { //fillXY
                                imageViewCenter!!.scaleType = ImageView.ScaleType.FIT_XY
                            } else { //default
                                imageViewCenter!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            }
                            //imageViewCenter2
                            /*
                            if (imageViewCenter2 == null) {
                                imageViewCenter2 = ImageView(this@MainActivity)
                            }
                            imageViewCenter2!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewCenter2!!.visibility = View.GONE
                            linearLayoutCenter!!.addView(imageViewCenter2)
                            */
                        }
                        5 -> { //mix
                            //image
                            if (imageViewCenter == null) {
                                imageViewCenter = ImageView(this@MainActivity)
                            }
                            imageViewCenter!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewCenter!!.visibility = View.GONE
                            linearLayoutCenter!!.addView(imageViewCenter)
                            //imageViewTop2
                            if (imageViewCenter2 == null) {
                                imageViewCenter2 = ImageView(this@MainActivity)
                            }
                            imageViewCenter2!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewCenter2!!.visibility = View.GONE
                            //imageViewTop2!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            linearLayoutCenter!!.addView(imageViewCenter2)
                            if (mixImageScaleType == 1) { //fillXY
                                imageViewCenter!!.scaleType = ImageView.ScaleType.FIT_XY
                                imageViewCenter2!!.scaleType = ImageView.ScaleType.FIT_XY
                            } else { //default
                                imageViewCenter!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                imageViewCenter2!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            }
                            //video
                            if (videoViewLayoutCenter == null) {
                                videoViewLayoutCenter = RelativeLayout(this@MainActivity)

                            }
                            videoViewLayoutCenter!!.removeAllViews()
                            videoViewLayoutCenter!!.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                            videoViewLayoutCenter!!.gravity = Gravity.CENTER
                            videoViewLayoutCenter!!.visibility = View.GONE
                            linearLayoutCenter!!.addView(videoViewLayoutCenter)
                            linearLayoutCenter!!.setBackgroundColor(Color.parseColor(defaultBackGroundColor))
                            //videoViewCenter
                            if (videoViewCenter == null) {
                                videoViewCenter = VideoView(this@MainActivity)
                                /*videoViewCenter!!.setOnTouchListener(OnTouchListener { v, event -> // do nothing here......
                                    true
                                })*/
                            }
                            if (mixVideoScaleType == 1) {
                                val layoutParams = RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT
                                )
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                                videoViewCenter!!.layoutParams = layoutParams
                            } else {
                                videoViewCenter!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            }

                            videoViewLayoutCenter!!.addView(videoViewCenter)
                        }
                    }

                    //LinearLayoutBottom
                    if (linearLayoutBottom == null) {
                        linearLayoutBottom = LinearLayout(this@MainActivity)
                    }
                    linearLayoutBottom!!.removeAllViews()
                    //linearLayoutBottomWeight = layoutBottomWeight / mainLayoutWeight * 100
                    //Log.e(mTag, "linearLayoutBottomWeight = $linearLayoutBottomWeight")
                    linearLayoutBottom!!.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    linearLayoutBottom!!.orientation = LinearLayout.VERTICAL
                    //linearLayoutBottom!!.setBackgroundColor(Color.parseColor(marqueeBackground))
                    //linearLayoutBottom!!.weightSum = 2.0F
                    //messageView
                    /*
                    if (messageViewBottom == null) {
                        messageViewBottom = TextView(this@MainActivity)
                        messageViewBottom!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    }
                    messageViewBottom!!.text = "Hello"
                    messageViewBottom!!.setTextColor(Color.parseColor("#FFFFFF"))
                    linearLayoutBottom!!.addView(messageViewBottom)

                     */
                    when (layoutBottom) {
                        1 -> {
                            //textViewBottom
                            if (textViewBottom == null) {
                                textViewBottom = ScrollTextView(this@MainActivity)
                                textViewBottom!!.isFocusable = false
                                textViewBottom!!.ellipsize = TextUtils.TruncateAt.MARQUEE
                                textViewBottom!!.isSingleLine = true
                                textViewBottom!!.freezesText = true
                                textViewBottom!!.marqueeRepeatLimit = -1
                                textViewBottom!!.resumeScroll()
                                textViewBottom!!.isFocusableInTouchMode = true
                                textViewBottom!!.isSelected = true
                            }
                            //background
                            textViewBottom!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            if (marqueeBackground.isNotEmpty()) {
                                textViewBottom!!.setBackgroundColor(Color.parseColor(marqueeBackground))
                            }
                            //text size
                            textViewBottom!!.textSize = marqueeSize.toFloat()
                            if (marqueeText.isNotEmpty()) {
                                textViewBottom!!.setTextColor(Color.parseColor(marqueeText))
                            }
                            //locate
                            when(marqueeLocate) {
                                0 -> {
                                    textViewBottom!!.gravity = Gravity.CENTER_VERTICAL
                                }
                                1 -> {
                                    textViewBottom!!.gravity = Gravity.TOP
                                }
                                2 -> {
                                    textViewBottom!!.gravity = Gravity.BOTTOM
                                }
                            }
                            //speed
                            //textViewBottom!!.setSpeed(marqueeSpeed.toFloat())
                            textViewBottom!!.rndDuration = 100000 / (marqueeSpeed / 50)
                            textViewBottom!!.visibility = View.GONE

                            linearLayoutBottom!!.addView(textViewBottom)
                            linearLayoutBottom!!.setBackgroundColor(Color.parseColor(marqueeBackground))

                            when(marqueeLocate) {
                                0 -> {
                                    linearLayoutBottom!!.gravity = Gravity.CENTER_VERTICAL
                                }
                                1 -> {
                                    linearLayoutBottom!!.gravity = Gravity.TOP
                                }
                                2 -> {
                                    linearLayoutBottom!!.gravity = Gravity.BOTTOM
                                }
                            }
                        }
                        2 -> {
                            //imageViewBottom
                            if (imageViewBottom == null) {
                                imageViewBottom = ImageView(this@MainActivity)
                            }
                            imageViewBottom!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewBottom!!.visibility = View.GONE
                            linearLayoutBottom!!.addView(imageViewBottom)
                            //imageViewBottom2
                            if (imageViewBottom2 == null) {
                                imageViewBottom2 = ImageView(this@MainActivity)
                            }
                            imageViewBottom2!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewBottom2!!.visibility = View.GONE
                            linearLayoutBottom!!.addView(imageViewBottom2)
                            linearLayoutBottom!!.setBackgroundColor(Color.parseColor(defaultBackGroundColor))
                            if (imageScaleType == 1) { //fillXY
                                imageViewBottom!!.scaleType = ImageView.ScaleType.FIT_XY
                                imageViewBottom2!!.scaleType = ImageView.ScaleType.FIT_XY
                            } else { //default
                                imageViewBottom!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                imageViewBottom2!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            }
                        }
                        3 -> {
                            //videoViewLayoutBottom
                            if (videoViewLayoutBottom == null) {
                                videoViewLayoutBottom = RelativeLayout(this@MainActivity)
                            }
                            videoViewLayoutBottom!!.removeAllViews()
                            videoViewLayoutBottom!!.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                            videoViewLayoutBottom!!.gravity = Gravity.CENTER
                            videoViewLayoutBottom!!.visibility = View.GONE
                            linearLayoutBottom!!.addView(videoViewLayoutBottom)
                            linearLayoutBottom!!.setBackgroundColor(Color.parseColor(defaultBackGroundColor))
                            //videoViewBottom
                            if (videoViewBottom == null) {
                                videoViewBottom = VideoView(this@MainActivity)
                                /*videoViewBottom!!.setOnTouchListener(OnTouchListener { v, event -> // do nothing here......
                                    true
                                })*/
                            }
                            //videoViewBottom!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            if (videoScaleType == 1) {
                                val layoutParams = RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.MATCH_PARENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT
                                )
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                                videoViewBottom!!.layoutParams = layoutParams
                            } else {
                                videoViewBottom!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            }
                            videoViewLayoutBottom!!.addView(videoViewBottom)
                        }
                        4 -> {
                            //imageViewBottom
                            if (imageViewBottom == null) {
                                imageViewBottom = ImageView(this@MainActivity)
                            }
                            imageViewBottom!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewBottom!!.visibility = View.GONE
                            linearLayoutBottom!!.addView(imageViewBottom)
                            linearLayoutBottom!!.setBackgroundColor(Color.parseColor(defaultBackGroundColor))
                            if (bannerScaleType == 1) { //fillXY
                                imageViewBottom!!.scaleType = ImageView.ScaleType.FIT_XY
                            } else { //default
                                imageViewBottom!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            }
                            /*
                            //imageViewBottom2
                            if (imageViewBottom2 == null) {
                                imageViewBottom2 = ImageView(this@MainActivity)
                            }
                            imageViewBottom2!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewBottom2!!.visibility = View.GONE
                            linearLayoutBottom!!.addView(imageViewBottom2)
                            */
                        }
                        5 -> { //mix
                            //image
                            if (imageViewBottom == null) {
                                imageViewBottom = ImageView(this@MainActivity)
                            }
                            imageViewBottom!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewBottom!!.visibility = View.GONE
                            linearLayoutBottom!!.addView(imageViewBottom)
                            //imageViewTop2
                            if (imageViewBottom2 == null) {
                                imageViewBottom2 = ImageView(this@MainActivity)
                            }
                            imageViewBottom2!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            imageViewBottom2!!.visibility = View.GONE
                            //imageViewTop2!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            linearLayoutBottom!!.addView(imageViewBottom2)
                            if (mixImageScaleType == 1) { //fillXY
                                imageViewBottom!!.scaleType = ImageView.ScaleType.FIT_XY
                                imageViewBottom2!!.scaleType = ImageView.ScaleType.FIT_XY
                            } else { //default
                                imageViewBottom!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                imageViewBottom2!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            }
                            //video
                            if (videoViewLayoutBottom == null) {
                                videoViewLayoutBottom = RelativeLayout(this@MainActivity)
                            }
                            videoViewLayoutBottom!!.removeAllViews()
                            videoViewLayoutBottom!!.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                            videoViewLayoutBottom!!.gravity = Gravity.CENTER
                            videoViewLayoutBottom!!.visibility = View.GONE
                            linearLayoutBottom!!.addView(videoViewLayoutBottom)
                            linearLayoutBottom!!.setBackgroundColor(Color.parseColor(defaultBackGroundColor))
                            //videoViewBottom
                            if (videoViewBottom == null) {
                                videoViewBottom = VideoView(this@MainActivity)
                                /*videoViewBottom!!.setOnTouchListener(OnTouchListener { v, event -> // do nothing here......
                                    true
                                })*/
                            }
                            if (mixVideoScaleType == 1) {
                                val layoutParams = RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT
                                )
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                                videoViewBottom!!.layoutParams = layoutParams
                            } else {
                                videoViewBottom!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            }

                            videoViewLayoutBottom!!.addView(videoViewBottom)
                        }
                    }



                    when(layoutOrientation) {
                        1 -> { //horizontal
                            if (layoutTop > 0) {
                                mainLinearLayout!!.addView(linearLayoutTop)
                            }
                            if (layoutCenter > 0) {
                                mainLinearLayout!!.addView(linearLayoutCenter)
                            }
                            if (layoutBottom > 0) {
                                mainLinearLayout!!.addView(linearLayoutBottom)
                            }
                        }
                        2 -> { //left triangle
                            Log.d(mTag, "left triangle")
                            Log.d(mTag, "linearLayoutTop add to mainLinearLayout")
                            if (layoutTop > 0) {
                                mainLinearLayout!!.addView(linearLayoutTop)
                            }
                            if (layoutCenter > 0) {
                                linearLayoutTriangle!!.addView(linearLayoutCenter)
                            }
                            if (layoutBottom > 0) {
                                linearLayoutTriangle!!.addView(linearLayoutBottom)
                            }
                            mainLinearLayout!!.addView(linearLayoutTriangle)
                            Log.d(mTag, "linearLayoutTriangle add to mainLinearLayout")
                        }
                        3 -> { //right triangle
                            Log.d(mTag, "right triangle")
                            if (layoutTop > 0) {
                                linearLayoutTriangle!!.addView(linearLayoutTop)
                            }
                            if (layoutCenter > 0) {
                                linearLayoutTriangle!!.addView(linearLayoutCenter)
                            }
                            if (layoutBottom > 0) {
                                mainLinearLayout!!.addView(linearLayoutTriangle)
                            }
                            mainLinearLayout!!.addView(linearLayoutBottom)
                        }
                        4 -> { //top triangle
                            Log.d(mTag, "top triangle")
                            if (layoutTop > 0) {
                                mainLinearLayout!!.addView(linearLayoutTop)
                            }
                            if (layoutCenter > 0) {
                                linearLayoutTriangle!!.addView(linearLayoutCenter)
                            }
                            if (layoutBottom > 0) {
                                linearLayoutTriangle!!.addView(linearLayoutBottom)
                            }
                            mainLinearLayout!!.addView(linearLayoutTriangle)
                        }
                        5 -> { //down triangle
                            Log.d(mTag, "down triangle")
                            if (layoutTop > 0) {
                                linearLayoutTriangle!!.addView(linearLayoutTop)
                            }
                            if (layoutCenter > 0) {
                                linearLayoutTriangle!!.addView(linearLayoutCenter)
                            }
                            if (layoutBottom > 0) {
                                mainLinearLayout!!.addView(linearLayoutTriangle)
                            }
                            mainLinearLayout!!.addView(linearLayoutBottom)
                        }
                        else -> { //0 vertical
                            if (layoutTop > 0) {
                                mainLinearLayout!!.addView(linearLayoutTop)
                            }
                            if (layoutCenter > 0) {
                                mainLinearLayout!!.addView(linearLayoutCenter)
                            }
                            if (layoutBottom > 0) {
                                mainLinearLayout!!.addView(linearLayoutBottom)
                            }
                        }
                    }

                    //for marquee
                    if (textViewTop != null) {
                        textViewTop!!.isSelected = true
                    }
                    if (textViewCenter != null) {
                        textViewCenter!!.isSelected = true
                    }
                    if (textViewBottom != null) {
                        textViewBottom!!.isSelected = true
                    }

                    if (videoViewTop != null) {
                        if (mediaControllerTop == null) {
                            mediaControllerTop = MediaController(this@MainActivity)
                        }
                        // anchor view for the videoView
                        mediaControllerTop!!.setAnchorView(videoViewTop)
                        // sets the media player to the videoView
                        mediaControllerTop!!.setMediaPlayer(videoViewTop)
                        // sets the media controller to the videoView
                        videoViewTop!!.setMediaController(mediaControllerTop)

                        videoViewTop!!.setOnTouchListener(OnTouchListener { v, event -> // do nothing here......
                            true
                        })

                        videoViewTop!!.setOnPreparedListener { mp ->
                            Log.d(mTag, "videoViewTop prepared")
                            //mp.setWakeMode(this@MainActivity, PowerManager.PARTIAL_WAKE_LOCK)
                            mp.seekTo(0)
                            mp.start()
                            if (layoutTop == 3) {
                                videoRunningTop = true
                            } else if (layoutTop == 5){
                                mixVideoRunningTop = true
                            }
                        }

                        videoViewTop!!.setOnErrorListener { mp, _, _ ->
                            Log.d("video", "videoViewTop setOnErrorListener ")

                            mp.reset()
                            videoRunningTop = false
                            mixVideoRunningTop = false
                            if (layoutTop == 3) {
                                val mixPlayFinishIntent = Intent()
                                mixPlayFinishIntent.action =
                                    Constants.ACTION.ACTION_TOP_VIDEO_PLAY_FINISH
                                this@MainActivity.sendBroadcast(mixPlayFinishIntent)
                            } else if (layoutTop == 5){
                                val mixPlayFinishIntent = Intent()
                                mixPlayFinishIntent.action =
                                    Constants.ACTION.ACTION_MIX_TOP_PLAY_FINISH
                                this@MainActivity.sendBroadcast(mixPlayFinishIntent)
                            }
                            true
                        }
                        videoViewTop!!.setOnCompletionListener { mp ->

                            mp.reset()
                            videoRunningTop = false
                            mixVideoRunningTop = false
                            if (layoutTop == 3) {
                                val videoPlayFinishIntent = Intent()
                                videoPlayFinishIntent.action =
                                    Constants.ACTION.ACTION_TOP_VIDEO_PLAY_FINISH
                                this@MainActivity.sendBroadcast(videoPlayFinishIntent)
                            } else if (layoutTop == 5){
                                val mixPlayFinishIntent = Intent()
                                mixPlayFinishIntent.action =
                                    Constants.ACTION.ACTION_MIX_TOP_PLAY_FINISH
                                this@MainActivity.sendBroadcast(mixPlayFinishIntent)
                            }


                        }
                    }

                    if (videoViewCenter != null) {
                        if (mediaControllerCenter == null) {
                            mediaControllerCenter = MediaController(this@MainActivity)
                        }
                        // anchor view for the videoView
                        mediaControllerCenter!!.setAnchorView(videoViewCenter)
                        // sets the media player to the videoView
                        mediaControllerCenter!!.setMediaPlayer(videoViewCenter)
                        // sets the media controller to the videoView
                        videoViewCenter!!.setMediaController(mediaControllerCenter)

                        videoViewCenter!!.setOnTouchListener(OnTouchListener { v, event -> // do nothing here......
                            true
                        })

                        videoViewCenter!!.setOnPreparedListener { mp ->
                            Log.d(mTag, "videoViewCenter prepared")
                            //mp.setWakeMode(this@MainActivity, PowerManager.PARTIAL_WAKE_LOCK)
                            mp.seekTo(0)
                            mp.start()
                            if (layoutCenter == 3) {
                                videoRunningCenter = true
                            } else if (layoutCenter == 5){
                                mixVideoRunningCenter = true
                            }
                        }
                        videoViewCenter!!.setOnErrorListener { mp, _, _ ->
                            Log.d("video", "videoViewCenter setOnErrorListener ")

                            mp.reset()
                            videoRunningCenter = false
                            mixVideoRunningCenter = false
                            if (layoutCenter == 3) {
                                val videoPlayFinishIntent = Intent()
                                videoPlayFinishIntent.action =
                                    Constants.ACTION.ACTION_CENTER_VIDEO_PLAY_FINISH
                                this@MainActivity.sendBroadcast(videoPlayFinishIntent)
                            } else if (layoutCenter == 5){
                                val mixPlayFinishIntent = Intent()
                                mixPlayFinishIntent.action =
                                    Constants.ACTION.ACTION_MIX_CENTER_PLAY_FINISH
                                this@MainActivity.sendBroadcast(mixPlayFinishIntent)
                            }
                            true
                        }
                        videoViewCenter!!.setOnCompletionListener { mp ->

                            mp.reset()
                            videoRunningCenter = false
                            mixVideoRunningCenter = false
                            if (layoutCenter == 3) {
                                val videoPlayFinishIntent = Intent()
                                videoPlayFinishIntent.action =
                                    Constants.ACTION.ACTION_CENTER_VIDEO_PLAY_FINISH
                                this@MainActivity.sendBroadcast(videoPlayFinishIntent)
                            } else if (layoutCenter == 5){
                                val mixPlayFinishIntent = Intent()
                                mixPlayFinishIntent.action =
                                    Constants.ACTION.ACTION_MIX_CENTER_PLAY_FINISH
                                this@MainActivity.sendBroadcast(mixPlayFinishIntent)
                            }
                        }
                    }

                    if (videoViewBottom != null) {
                        if (mediaControllerBottom == null) {
                            mediaControllerBottom = MediaController(this@MainActivity)
                        }
                        // anchor view for the videoView
                        mediaControllerBottom!!.setAnchorView(videoViewBottom)
                        // sets the media player to the videoView
                        mediaControllerBottom!!.setMediaPlayer(videoViewBottom)
                        // sets the media controller to the videoView
                        videoViewBottom!!.setMediaController(mediaControllerBottom)

                        videoViewBottom!!.setOnTouchListener(OnTouchListener { v, event -> // do nothing here......

                            true
                        })

                        videoViewBottom!!.setOnPreparedListener { mp ->
                            Log.d(mTag, "videoViewBottom prepared")
                            //mp.setWakeMode(this@MainActivity, PowerManager.PARTIAL_WAKE_LOCK)
                            mp.seekTo(0)
                            mp.start()
                            if (layoutBottom == 3) {
                                videoRunningBottom = true
                            } else if (layoutBottom == 5){
                                mixVideoRunningBottom = true
                            }
                        }

                        videoViewBottom!!.setOnErrorListener { mp, _, _ ->
                            Log.d("video", "mix videoViewBottom setOnErrorListener ")

                            //mp.reset()
                            videoRunningBottom = false
                            mixVideoRunningBottom = false
                            if (layoutBottom == 3) {
                                val mixPlayFinishIntent = Intent()
                                mixPlayFinishIntent.action =
                                    Constants.ACTION.ACTION_BOTTOM_VIDEO_PLAY_FINISH
                                this@MainActivity.sendBroadcast(mixPlayFinishIntent)
                            } else if (layoutBottom == 5){
                                val mixPlayFinishIntent = Intent()
                                mixPlayFinishIntent.action =
                                    Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_FINISH
                                this@MainActivity.sendBroadcast(mixPlayFinishIntent)
                            }



                            true
                        }
                        videoViewBottom!!.setOnCompletionListener { mp ->

                            mp.reset()
                            videoRunningBottom = false
                            mixVideoRunningBottom = false
                            if (layoutBottom == 3) {
                                val videoPlayFinishIntent = Intent()
                                videoPlayFinishIntent.action =
                                    Constants.ACTION.ACTION_BOTTOM_VIDEO_PLAY_FINISH
                                this@MainActivity.sendBroadcast(videoPlayFinishIntent)
                            } else if (layoutBottom == 5){
                                val mixPlayFinishIntent = Intent()
                                mixPlayFinishIntent.action =
                                    Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_FINISH
                                this@MainActivity.sendBroadcast(mixPlayFinishIntent)
                            }


                        }
                    }

                    //top
                    when (layoutTop) {
                        0-> { //no
                            Log.d(mTag, "layoutTop => no")
                        }
                        1 -> { //text
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutTopWidth
                            param.height = layoutTopHeight
                            linearLayoutTop!!.layoutParams = param
                            linearLayoutTop!!.visibility = View.VISIBLE
                            textViewTop!!.visibility = View.VISIBLE
                        }
                        2 -> { //image
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutTopWidth
                            param.height = layoutTopHeight
                            linearLayoutTop!!.layoutParams = param
                            linearLayoutTop!!.visibility = View.VISIBLE
                            imageViewTop!!.visibility = View.VISIBLE
                        }
                        3 -> { //video
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutTopWidth
                            param.height = layoutTopHeight
                            linearLayoutTop!!.layoutParams = param
                            linearLayoutTop!!.visibility = View.VISIBLE
                            videoViewLayoutTop!!.visibility = View.VISIBLE
                            videoViewTop!!.visibility = View.VISIBLE
                        }
                        4 -> { //banner
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutTopWidth
                            param.height = layoutTopHeight
                            linearLayoutTop!!.layoutParams = param
                            linearLayoutTop!!.visibility = View.VISIBLE
                            imageViewTop!!.visibility = View.VISIBLE
                        }
                        5 -> { //mix
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutTopWidth
                            param.height = layoutTopHeight
                            linearLayoutTop!!.layoutParams = param
                            linearLayoutTop!!.visibility = View.VISIBLE
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
                            )
                            param.width = layoutCenterWidth
                            param.height = layoutCenterHeight
                            linearLayoutCenter!!.layoutParams = param
                            linearLayoutCenter!!.visibility = View.VISIBLE
                            textViewCenter!!.visibility = View.VISIBLE
                        }
                        2 -> { //image
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutCenterWidth
                            param.height = layoutCenterHeight
                            linearLayoutCenter!!.layoutParams = param
                            linearLayoutCenter!!.visibility = View.VISIBLE
                            imageViewCenter!!.visibility = View.VISIBLE
                        }
                        3 -> { //video
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutCenterWidth
                            param.height = layoutCenterHeight
                            linearLayoutCenter!!.layoutParams = param
                            linearLayoutCenter!!.visibility = View.VISIBLE
                            videoViewLayoutCenter!!.visibility = View.VISIBLE
                            videoViewCenter!!.visibility = View.VISIBLE
                            //exoPlayerViewCenter!!.visibility = View.VISIBLE
                        }
                        4 -> { //banner
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutCenterWidth
                            param.height = layoutCenterHeight
                            linearLayoutCenter!!.layoutParams = param
                            linearLayoutCenter!!.visibility = View.VISIBLE
                            imageViewCenter!!.visibility = View.VISIBLE
                        }
                        5 -> { //mix
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutCenterWidth
                            param.height = layoutCenterHeight
                            linearLayoutCenter!!.layoutParams = param
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
                            )
                            param.width = layoutBottomWidth
                            param.height = layoutBottomHeight
                            linearLayoutBottom!!.layoutParams = param
                            linearLayoutBottom!!.visibility = View.VISIBLE
                            textViewBottom!!.visibility = View.VISIBLE
                        }
                        2 -> { //image
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutBottomWidth
                            param.height = layoutBottomHeight
                            linearLayoutBottom!!.layoutParams = param
                            linearLayoutBottom!!.visibility = View.VISIBLE
                            imageViewBottom!!.visibility = View.VISIBLE
                        }
                        3 -> { //video
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutBottomWidth
                            param.height = layoutBottomHeight
                            linearLayoutBottom!!.layoutParams = param
                            linearLayoutBottom!!.visibility = View.VISIBLE
                            videoViewLayoutBottom!!.visibility = View.VISIBLE
                            videoViewBottom!!.visibility = View.VISIBLE
                        }
                        4 -> { //banner
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutBottomWidth
                            param.height = layoutBottomHeight
                            linearLayoutBottom!!.layoutParams = param
                            linearLayoutBottom!!.visibility = View.VISIBLE
                            imageViewBottom!!.visibility = View.VISIBLE
                        }
                        5 -> { //mix
                            val param = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            param.width = layoutBottomWidth
                            param.height = layoutBottomHeight
                            linearLayoutBottom!!.layoutParams = param
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
                            countDownTimerMarquee = object : CountDownTimer(marqueePlayInterval.toLong(), marqueePlayInterval.toLong()) {
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

                    // banner
                    if (layoutTop == 4 || layoutCenter == 4 || layoutBottom == 4) {
                        if (layoutTop == 4) {
                            if (bannerList.size > 0 && checkDownloadBannerAll()) { //at least one image cant play
                                //top
                                imageViewTop!!.visibility = View.VISIBLE
                                //Picasso.with(this@MainActivity).load(imageUrlTop).into(imageViewTop)
                                val srcPath = "$dest_banner_folder/${bannerList[0]}"
                                val file = File(srcPath)
                                if (file.exists()) {
                                    //imageViewTop!!.setImageURI(Uri.fromFile(file))
                                    Picasso.get()
                                        .load(Uri.fromFile(file))
                                        .resize(layoutTopWidth, layoutTopHeight)
                                        .onlyScaleDown()
                                        .into(imageViewTop)
                                } else {
                                    imageViewTop!!.setImageResource(R.drawable.baseline_image_search_24)
                                }
                            } else {
                                imageViewTop!!.setImageResource(R.drawable.baseline_image_24)
                            }

                        } else {
                            Log.d(mTag, "layoutTop not banner")
                        }

                        if (layoutCenter == 4) {
                            if (bannerList.size > 0 && checkDownloadBannerAll()) {
                                //center
                                imageViewCenter!!.visibility = View.VISIBLE
                                //Picasso.with(this@MainActivity).load(imageUrlCenter).into(imageViewCenter)
                                val srcPath = "$dest_banner_folder/${bannerList[0]}"
                                val file = File(srcPath)
                                if (file.exists()) {
                                    //imageViewCenter!!.setImageURI(Uri.fromFile(file))
                                    Picasso.get()
                                        .load(Uri.fromFile(file))
                                        .resize(layoutCenterWidth, layoutCenterHeight)
                                        .onlyScaleDown()
                                        .into(imageViewCenter)
                                } else {
                                    imageViewCenter!!.setImageResource(R.drawable.baseline_image_search_24)
                                }
                            } else {
                                imageViewCenter!!.setImageResource(R.drawable.baseline_image_24)
                            }
                        } else {
                            Log.d(mTag, "layoutCenter not banner")
                        }
                        //init bottom start
                        if (layoutBottom == 4) {
                            if (bannerList.size > 0 && checkDownloadBannerAll()) {
                                //bottom
                                imageViewBottom!!.visibility = View.VISIBLE
                                //Picasso.with(this@MainActivity).load(imageUrlBottom).into(imageViewBottom)
                                val srcPath = "$dest_banner_folder/${bannerList[0]}"
                                val file = File(srcPath)
                                if (file.exists()) {
                                    //imageViewBottom!!.setImageURI(Uri.fromFile(file))
                                    Picasso.get()
                                        .load(Uri.fromFile(file))
                                        .resize(layoutBottomWidth, layoutBottomHeight)
                                        .onlyScaleDown()
                                        .into(imageViewBottom)
                                } else {
                                    imageViewBottom!!.setImageResource(R.drawable.baseline_image_search_24)
                                }
                            } else {
                                imageViewBottom!!.setImageResource(R.drawable.baseline_image_24)
                            }
                        } else {
                            Log.d(mTag, "layoutBottom not banner")
                            //imageViewBottom!!.visibility = View.GONE
                            //imageViewBottom2!!.visibility = View.GONE
                        }

                    } else { //all is not banner
                        Log.e(mTag, "all are not banner")
                    }

                    // image play time
                    if (layoutTop == 2 || layoutCenter == 2 || layoutBottom == 2) {


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
                                //Picasso.with(this@MainActivity).load(imageUrlTop).into(imageViewTop)
                                val srcPath = "$dest_images_folder/${imageList[currentImageIndexTop]}"
                                val file = File(srcPath)
                                if (file.exists()) {

                                    Picasso.get()
                                        .load(Uri.fromFile(file))
                                        .resize(layoutTopWidth, layoutTopHeight)
                                        .onlyScaleDown()
                                        .into(imageViewTop)
                                    imageViewTop!!.startAnimation(animMoveFromLeft)
                                }
                            }

                        } else {
                            Log.d(mTag, "layoutTop not image")
                            //imageViewTop!!.visibility = View.GONE
                            //imageViewTop2!!.visibility = View.GONE
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
                                //Picasso.with(this@MainActivity).load(imageUrlCenter).into(imageViewCenter)
                                val srcPath = "$dest_images_folder/${imageList[currentImageIndexCenter]}"
                                val file = File(srcPath)
                                if (file.exists()) {
                                    Picasso.get()
                                        .load(Uri.fromFile(file))
                                        .resize(layoutCenterWidth, layoutCenterHeight)
                                        .onlyScaleDown()
                                        .into(imageViewCenter)
                                    //imageViewCenter!!.setImageURI(Uri.fromFile(file))
                                    imageViewCenter!!.startAnimation(animMoveFromLeft)
                                }
                            }
                        } else {
                            Log.d(mTag, "layoutCenter not image")
                            //imageViewCenter!!.visibility = View.GONE
                            //imageViewCenter2!!.visibility = View.GONE
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
                                //Picasso.with(this@MainActivity).load(imageUrlBottom).into(imageViewBottom)
                                val srcPath = "$dest_images_folder/${imageList[currentImageIndexBottom]}"
                                val file = File(srcPath)
                                if (file.exists()) {
                                    Picasso.get()
                                        .load(Uri.fromFile(file))
                                        .resize(layoutBottomWidth, layoutBottomHeight)
                                        .onlyScaleDown()
                                        .into(imageViewBottom)
                                    //imageViewBottom!!.setImageURI(Uri.fromFile(file))
                                    imageViewBottom!!.startAnimation(animMoveFromLeft)
                                }
                            }
                        } else {
                            Log.d(mTag, "layoutBottom not image")
                            //imageViewBottom!!.visibility = View.GONE
                            //imageViewBottom2!!.visibility = View.GONE
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
                                                } while (!downloadImageReadyArray[currentImageIndexTop])
                                            }

                                            //top
                                            val srcPath = "$dest_images_folder/${imageList[currentImageIndexTop]}"
                                            val file = File(srcPath)
                                            if (file.exists()) {
                                                if (imageViewTop!!.visibility == View.VISIBLE) {
                                                    imageViewTop!!.startAnimation(animMoveToRight)
                                                    imageViewTop!!.visibility = View.GONE
                                                    //imageViewTop2!!.setImageURI(Uri.fromFile(file))
                                                    Picasso.get()
                                                        .load(Uri.fromFile(file))
                                                        .resize(layoutTopWidth, layoutTopHeight)
                                                        .onlyScaleDown()
                                                        .into(imageViewTop2)
                                                    imageViewTop2!!.visibility = View.VISIBLE
                                                    //Picasso.with(this@MainActivity).load(imageUrlTop).into(imageViewTop2)
                                                    imageViewTop2!!.startAnimation(animMoveFromLeft)
                                                } else {
                                                    imageViewTop2!!.startAnimation(animMoveToRight)
                                                    imageViewTop2!!.visibility = View.GONE
                                                    //imageViewTop!!.setImageURI(Uri.fromFile(file))
                                                    Picasso.get()
                                                        .load(Uri.fromFile(file))
                                                        .resize(layoutTopWidth, layoutTopHeight)
                                                        .onlyScaleDown()
                                                        .into(imageViewTop)
                                                    imageViewTop!!.visibility = View.VISIBLE

                                                    //Picasso.with(this@MainActivity).load(imageUrlTop).into(imageViewTop)
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
                                                    //imageViewCenter2!!.setImageURI(Uri.fromFile(file))
                                                    Picasso.get()
                                                        .load(Uri.fromFile(file))
                                                        .resize(layoutCenterWidth, layoutCenterHeight)
                                                        .onlyScaleDown()
                                                        .into(imageViewCenter2)
                                                    imageViewCenter2!!.visibility = View.VISIBLE
                                                    //Picasso.with(this@MainActivity).load(imageUrlCenter).into(imageViewCenter2)
                                                    imageViewCenter2!!.startAnimation(animMoveFromRight)
                                                } else { //imageViewCenter!!.visibility == View.GONE
                                                    imageViewCenter2!!.startAnimation(animMoveToLeft)
                                                    imageViewCenter2!!.visibility = View.GONE
                                                    //imageViewCenter!!.setImageURI(Uri.fromFile(file))
                                                    Picasso.get()
                                                        .load(Uri.fromFile(file))
                                                        .resize(layoutCenterWidth, layoutCenterHeight)
                                                        .onlyScaleDown()
                                                        .into(imageViewCenter)
                                                    imageViewCenter!!.visibility = View.VISIBLE
                                                    //Picasso.with(this@MainActivity).load(imageUrlCenter).into(imageViewCenter)
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
                                                    //imageViewBottom2!!.setImageURI(Uri.fromFile(file))
                                                    Picasso.get()
                                                        .load(Uri.fromFile(file))
                                                        .resize(layoutBottomWidth, layoutBottomHeight)
                                                        .onlyScaleDown()
                                                        .into(imageViewBottom2)
                                                    imageViewBottom2!!.visibility = View.VISIBLE
                                                    //Picasso.with(this@MainActivity).load(imageUrlBottom).into(imageViewBottom2)
                                                    imageViewBottom2!!.startAnimation(animMoveFromLeft)
                                                } else {
                                                    imageViewBottom2!!.startAnimation(animMoveToRight)
                                                    imageViewBottom2!!.visibility = View.GONE
                                                    //imageViewBottom!!.setImageURI(Uri.fromFile(file))
                                                    Picasso.get()
                                                        .load(Uri.fromFile(file))
                                                        .resize(layoutBottomWidth, layoutBottomHeight)
                                                        .onlyScaleDown()
                                                        .into(imageViewBottom)
                                                    imageViewBottom!!.visibility = View.VISIBLE
                                                    //Picasso.with(this@MainActivity).load(imageUrlBottom).into(imageViewBottom)
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
                        if (layoutCenter == 3) {
                            if (!videoRunningTop) {
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

                                val mixPlayIntent = Intent()
                                mixPlayIntent.action = Constants.ACTION.ACTION_TOP_VIDEO_PLAY_START
                                this@MainActivity.sendBroadcast(mixPlayIntent)
                            }
                        } else {
                            Log.d(mTag, "layoutTop not video")
                            if (videoViewTop != null) {
                                videoViewTop!!.stopPlayback()
                            }
                            //exoPlayerViewTop!!.player!!.stop()
                            videoRunningTop = false
                        }

                        //center
                        if (layoutCenter == 3) {
                            if (!videoRunningCenter) {
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

                                val mixPlayIntent = Intent()
                                mixPlayIntent.action = Constants.ACTION.ACTION_CENTER_VIDEO_PLAY_START
                                this@MainActivity.sendBroadcast(mixPlayIntent)
                            }
                        } else {
                            Log.d(mTag, "layoutCenter not video")
                            if (videoViewCenter != null) {
                                videoViewCenter!!.stopPlayback()
                            }
                            //exoPlayerViewCenter!!.player!!.stop()
                            videoRunningCenter = false
                        }

                        //bottom
                        if (layoutBottom == 3) {
                            if (!videoRunningBottom) {
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
                                }

                                val mixPlayIntent = Intent()
                                mixPlayIntent.action = Constants.ACTION.ACTION_BOTTOM_VIDEO_PLAY_START
                                this@MainActivity.sendBroadcast(mixPlayIntent)
                            }
                        } else {
                            Log.d(mTag, "layoutBottom not video")
                            if (videoViewBottom != null) {
                                videoViewBottom!!.stopPlayback()
                            }
                            //exoPlayerViewBottom!!.player!!.stop()
                            videoRunningBottom = false
                        }
                    } else { //all are not video
                        Log.d(mTag, "all are not video")
                        videoRunningTop = false
                        videoRunningCenter = false
                        videoRunningBottom = false
                    }


                    //image and video mix
                    if (layoutTop == 5 || layoutCenter == 5 || layoutBottom == 5) {
                        Log.d(mTag, "mix play time")
                        //top
                        if (layoutTop == 5) {
                            if (!mixTopRunning) {
                                mixTopRunning = true
                                val mixPlayIntent = Intent()
                                mixPlayIntent.action = Constants.ACTION.ACTION_MIX_TOP_PLAY_START
                                this@MainActivity.sendBroadcast(mixPlayIntent)
                            }
                        } else {
                            Log.d(mTag, "layoutTop not mix")
                        }
                        //center
                        if (layoutCenter == 5) {
                            if (!mixCenterRunning) {
                                mixCenterRunning = true
                                val mixPlayIntent = Intent()
                                mixPlayIntent.action = Constants.ACTION.ACTION_MIX_CENTER_PLAY_START
                                this@MainActivity.sendBroadcast(mixPlayIntent)
                            }
                        } else {
                            Log.d(mTag, "layoutCenter not mix")
                        }

                        //bottom
                        if (layoutBottom == 5) {
                            Log.e(mTag, "mixBottomRunning = $mixBottomRunning")
                            if (!mixBottomRunning) {
                                mixBottomRunning = true
                                val mixPlayIntent = Intent()
                                mixPlayIntent.action = Constants.ACTION.ACTION_MIX_BOTTOM_PLAY_START
                                this@MainActivity.sendBroadcast(mixPlayIntent)
                            }
                        } else {
                            Log.d(mTag, "layoutBottom not mix")
                        }


                    } else {
                        Log.e(mTag, "all are not mix")
                    }
                } else {
                    Log.d(mTag, "No AdSetting!")
                }


            }
        }
    }

    private fun getPlanUse(currentTimestamp: Long) {

        val longArray : ArrayList<Long> = ArrayList()

        //must have set the plan
        if (layoutList[0].plan_id > 0) {
            longArray.add(planStartTime)
        }
        if (layoutList[0].plan2_id > 0 && plan2StartTimeString != "--:--") {
            longArray.add(plan2StartTime)
        }
        if (layoutList[0].plan3_id > 0 && plan3StartTimeString != "--:--") {
            longArray.add(plan3StartTime)
        }
        if (layoutList[0].plan4_id > 0 && plan4StartTimeString != "--:--") {
            longArray.add(plan4StartTime)
        }

        Log.e(mTag,"longArray before = $longArray")

        if (longArray.size > 1) {
            Collections.sort(longArray)
        }

        Log.e(mTag,"longArray after = $longArray")

        var idx = -1
        for (i in longArray.indices) {
            if (currentTimestamp >= longArray[i]) {
                //currentPlanId = layoutList[0].plan_id
                //currentPlanUse = 1
                idx = i
            } else {
                break
            }
        }

        if (idx == -1) { //use the last one
            idx = longArray.size - 1
        }

        if (idx >= 0) {
            when(longArray[idx]) {
                planStartTime -> {
                    currentPlanId = layoutList[0].plan_id
                    currentPlanUse = 1
                    Log.d(mTag, "plan1, id = ${layoutList[0].plan_id}")
                }
                plan2StartTime -> {
                    currentPlanId = layoutList[0].plan2_id
                    currentPlanUse = 2
                    Log.d(mTag, "plan2, id = ${layoutList[0].plan2_id}")
                }
                plan3StartTime -> {
                    currentPlanId = layoutList[0].plan3_id
                    currentPlanUse = 3
                    Log.d(mTag, "plan3, id = ${layoutList[0].plan3_id}")
                }
                plan4StartTime -> {
                    currentPlanId = layoutList[0].plan4_id
                    currentPlanUse = 4
                    Log.d(mTag, "plan4, id = ${layoutList[0].plan4_id}")
                }
            }
        }


    }

    private fun checkAndRequestPermissions() {
        var readPermission: Int = -1
        var writePermission: Int = -1
        var readMediaVideoPermission: Int = -1
        var readMediaImagesPermission: Int = -1
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) { //API < 30, Android 11
            readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

            writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else { //API > 30, Android 12+
            readMediaVideoPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)

            readMediaImagesPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
        }

        val networkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)

        //val foregroundServicePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)

        //val accessNetworkStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)

        //val accessWiFiStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)

        //val changeWifiStatePermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)

        //val coarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        //val installPackagesPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_INSTALL_PACKAGES)

        val listPermissionsNeeded = ArrayList<String>()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            if (readPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            if (readMediaVideoPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_VIDEO)
            }

            if (readMediaImagesPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }






        if (networkPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET)
        }

        /*if (foregroundServicePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.FOREGROUND_SERVICE)
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
        }*/

        /*if (installPackagesPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        }
        */

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
            dest_banner_folder = Environment.getExternalStorageDirectory().toString() + "/Download/banner/"
            dest_images_folder = Environment.getExternalStorageDirectory().toString() + "/Download/images/"
            dest_videos_folder = Environment.getExternalStorageDirectory().toString() + "/Download/videos/"
            val bannerDir = File(dest_banner_folder)
            bannerDir.mkdirs()
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
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                    perms[Manifest.permission.READ_EXTERNAL_STORAGE] =
                        PackageManager.PERMISSION_GRANTED
                    perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] =
                        PackageManager.PERMISSION_GRANTED
                } else {
                    perms[Manifest.permission.READ_MEDIA_VIDEO] = PackageManager.PERMISSION_GRANTED
                    perms[Manifest.permission.READ_MEDIA_IMAGES] = PackageManager.PERMISSION_GRANTED
                }
                //perms[Manifest.permission.FOREGROUND_SERVICE] = PackageManager.PERMISSION_GRANTED
                //perms[Manifest.permission.INTERNET] = PackageManager.PERMISSION_GRANTED
                //perms[Manifest.permission.ACCESS_NETWORK_STATE] = PackageManager.PERMISSION_GRANTED
                //perms[Manifest.permission.ACCESS_WIFI_STATE] = PackageManager.PERMISSION_GRANTED
                //perms[Manifest.permission.CHANGE_WIFI_STATE] = PackageManager.PERMISSION_GRANTED
                //perms[Manifest.permission.ACCESS_COARSE_LOCATION] = PackageManager.PERMISSION_GRANTED
                //perms[Manifest.permission.REQUEST_INSTALL_PACKAGES] = PackageManager.PERMISSION_GRANTED
                //perms.put(Manifest.permission.ACCESS_WIFI_STATE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                //if (grantResults.size > 0) {
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices) {
                        perms[permissions[i]] = grantResults[i]
                        Log.d(mTag, "perms[permissions[$i]] = ${perms[permissions[i]]}")

                    }

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        // Check for both permissions
                        if (perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            //&& perms[Manifest.permission.READ_MEDIA_VIDEO] == PackageManager.PERMISSION_GRANTED
                            //&& perms[Manifest.permission.READ_MEDIA_IMAGES] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.FOREGROUND_SERVICE] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.INTERNET] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.ACCESS_NETWORK_STATE] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.ACCESS_WIFI_STATE] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.CHANGE_WIFI_STATE] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.ACCESS_COARSE_LOCATION] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.REQUEST_INSTALL_PACKAGES] == PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.d(mTag, "permission granted")
                            //create local folder
                            dest_banner_folder = Environment.getExternalStorageDirectory().toString() + "/Download/banner/"
                            dest_images_folder = Environment.getExternalStorageDirectory().toString() + "/Download/images/"
                            dest_videos_folder = Environment.getExternalStorageDirectory().toString() + "/Download/videos/"
                            val bannerDir = File(dest_banner_folder)
                            bannerDir.mkdirs()
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
                                )/*
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_MEDIA_VIDEO
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_MEDIA_IMAGES
                                )*/
                                ||ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.INTERNET
                                )/*
                            ||ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.FOREGROUND_SERVICE
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
                            || ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.REQUEST_INSTALL_PACKAGES
                            )*/
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
                            }
                        }
                    } else {
                        // Check for both permissions
                        if (//perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            //&& perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            perms[Manifest.permission.READ_MEDIA_VIDEO] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.READ_MEDIA_IMAGES] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.FOREGROUND_SERVICE] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.INTERNET] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.ACCESS_NETWORK_STATE] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.ACCESS_WIFI_STATE] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.CHANGE_WIFI_STATE] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.ACCESS_COARSE_LOCATION] == PackageManager.PERMISSION_GRANTED
                        //&& perms[Manifest.permission.REQUEST_INSTALL_PACKAGES] == PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.d(mTag, "permission granted")
                            //create local folder
                            dest_banner_folder = Environment.getExternalStorageDirectory().toString() + "/Download/banner/"
                            dest_images_folder = Environment.getExternalStorageDirectory().toString() + "/Download/images/"
                            dest_videos_folder = Environment.getExternalStorageDirectory().toString() + "/Download/videos/"
                            val bannerDir = File(dest_banner_folder)
                            bannerDir.mkdirs()
                            val imagesDir = File(dest_images_folder)
                            imagesDir.mkdirs()
                            val videoDir = File(dest_videos_folder)
                            videoDir.mkdirs()
                            if (debugLog) {
                                initLog()
                            }
                        } else {
                            Log.d(mTag, "Some permissions are not granted ask again ")

                            if (/*ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )*/
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_MEDIA_VIDEO
                                )
                                || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.READ_MEDIA_IMAGES
                                )
                                ||ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.INTERNET
                                )/*
                            ||ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.FOREGROUND_SERVICE
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
                            || ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.REQUEST_INSTALL_PACKAGES
                            )*/
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
                            }
                        }
                    }


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
        val editTextDialogScreenWidth = promptView.findViewById<EditText>(R.id.editTextDialogScreenWidth)
        val editTextDialogScreenHeight = promptView.findViewById<EditText>(R.id.editTextDialogScreenHeight)
        val btnClear = promptView.findViewById<Button>(R.id.btnDialogClear)
        val btnConfirm = promptView.findViewById<Button>(R.id.btnDialogConfirm)

        //editTextDialogServerIP.text = "http://"
        //val initStr = "http://"
        editTextDialogServerIP.setText(defaultIpAddress)
        //editTextDialogServerIP.setSelection(initStr.length)
        editTextDialogServerPort.inputType = InputType.TYPE_CLASS_NUMBER
        editTextDialogServerPort.setText("3000")
        editTextDialogScreenWidth.inputType = InputType.TYPE_CLASS_NUMBER
        editTextDialogScreenWidth.setText(screenWidth.toString())
        editTextDialogScreenHeight.inputType = InputType.TYPE_CLASS_NUMBER
        editTextDialogScreenHeight.setText(screenHeight.toString())

        alertDialogBuilder.setCancelable(false)
        btnClear!!.setOnClickListener {
            editTextDialogServerIP.setText("")
            //editTextDialogServerIP.setSelection(initStr.length)
            //editTextDialogServerIP.text = "http://"
            editTextDialogServerPort.setText("")
        }
        btnConfirm!!.setOnClickListener {

            if (editTextDialogServerIP.text.toString() != "" &&
                editTextDialogServerPort.text.toString() != "") {

                val matcher: Matcher = ipAddressPattern.matcher(editTextDialogServerIP.text.toString())
                val isValidIP = matcher.matches()
                val isValidPort = checkValidPort(editTextDialogServerPort.text.toString())
                if (isValidIP && isValidPort) {
                    Log.d(mTag,"IP = ${ editTextDialogServerIP.text}")
                    Log.d(mTag,"Port = ${ editTextDialogServerPort.text}")

                    server_ip_address = editTextDialogServerIP.text.toString()
                    editTextDialogServerIP.setSelection(editTextDialogServerIP.length())
                    server_webservice_port = editTextDialogServerPort.text.toString()
                    screenWidth = editTextDialogScreenWidth.text.toString().toInt()
                    screenHeight= editTextDialogScreenHeight.text.toString().toInt()

                    editor = pref!!.edit()
                    editor!!.putString("SERVER_IP_ADDRESS", editTextDialogServerIP.text.toString())
                    editor!!.putString("SERVER_WEBSERVICE_PORT", editTextDialogServerPort.text.toString())
                    editor!!.putString("SCREEN_WIDTH", editTextDialogScreenWidth.text.toString())
                    editor!!.putString("SCREEN_HEIGHT", editTextDialogScreenHeight.text.toString())
                    editor!!.apply()

                    val testServerIPAndPortIntent = Intent()
                    testServerIPAndPortIntent.action = Constants.ACTION.ACTION_TEST_IP_AND_PORT
                    this@MainActivity.sendBroadcast(testServerIPAndPortIntent)

                    textViewShowInitSuccess!!.text = getString(R.string.ad_client_wait_for_server)
                    textViewShowInitSuccess!!.visibility = View.VISIBLE
                    textViewShowState!!.text = "ACTION_TEST_IP_AND_PORT"
                    //textViewShowState!!.visibility = View.VISIBLE

                    alertDialogBuilder.dismiss()
                } else {
                    if (!isValidIP) {
                        toast(getString(R.string.server_ip_input_mismatch))
                    }
                    if (!isValidPort) {
                        toast(getString(R.string.server_port_input_mismatch))
                    }
                }
            } else {
                toast(getString(R.string.server_ip_input_empty))
            }




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

    private fun getCurrentTimeStamp(): Long {

        //val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        //Log.d(mTag, "currentTime = $currentTime")

        val tsLong = System.currentTimeMillis() / 1000
        //val ts = tsLong.toString()

        Log.d(mTag, "tsLong = $tsLong")

        return tsLong
    }

    private fun getTimeStampFromString(startTime: String): Long {

        var timestampRet: Long = 0

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        Log.d(mTag, "Today = $today")
        var combineString = ""
        val combineStrArray = startTime.split(":")
        combineString = if (combineStrArray[0] == "--" && combineStrArray[1] == "--") {
            "$today 00:00"
        } else if (combineStrArray[0] == "--" && combineStrArray[1] != "--") {
            "$today 00:${combineStrArray[1]}"
        } else if (combineStrArray[0] != "--" && combineStrArray[1] == "--") {
            "$today ${combineStrArray[0]}:00"
        } else {
            "$today $startTime"
        }



        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        try {
            val date: Date = format.parse(combineString) as Date
            Log.d(mTag, "date = $date")

            timestampRet = date.time
            Log.d(mTag, "timestampRet = ${timestampRet/1000}")

            return timestampRet/1000

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return timestampRet
    }

    private fun Activity.handleUncaughtException() {
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->
            // here you can report the throwable exception to Sentry or Crashlytics or whatever crash reporting service you're using, otherwise you may set the throwable variable to _ if it'll remain unused
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("isCrashed", true)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
            //Process.killProcess(Process.myPid())
            android.os.Process.killProcess(myPid)
            exitProcess(2)
        }
    }


    fun Activity.showUncaughtExceptionDialog() {
        if (intent.getBooleanExtra("isCrashed", false)) {
            AlertDialog.Builder(this).apply {
                setTitle("Something went wrong.")
                setMessage("Something went wrong.\nWe'll work on fixing it.")
                setPositiveButton("OK") { _, _ -> }
            }.show()
        }
    }

    fun rebootDevice() {
        // android 5.1.1,
        Log.e(mTag, "== rebootDevice ==")
        try {
            //Runtime.getRuntime().exec(arrayOf("/system/bin/su", "-c", "reboot now"))
            val process = Runtime.getRuntime().exec(arrayOf("reboot"))
            process.waitFor()
        } catch (e: IOException) {
            Log.e(mTag, "first: $e")
            try {
                Runtime.getRuntime().exec(arrayOf("/system/xbin/su", "-c", "reboot now"))
            } catch (ex: IOException) {
                Log.e(mTag, "second: $ex")
            }


        }

    }


    private fun checkValidPort(portStr: String): Boolean {
        var ret = false

        val portInt = portStr.toInt()
        if (portInt in 0..65535) {
            ret = true
        }


        return ret
    }

    private fun getIPAddress(useIPv4 : Boolean): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses);
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress;
                        val isIPv4: Boolean = sAddr.indexOf(':')<0
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                if (delim < 0) {
                                    return sAddr.toUpperCase()
                                }
                                else {
                                    return sAddr.substring(0, delim).toUpperCase()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) { }
        return ""
    }
}