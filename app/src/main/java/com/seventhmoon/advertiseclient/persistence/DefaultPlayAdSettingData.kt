package com.seventhmoon.advertiseclient.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = DefaultPlayAdSettingData.TABLE_NAME)
class DefaultPlayAdSettingData(plan_id: Int, plan_name: String, plan_marquee: String, plan_images: String,
                               plan_videos: String, marquee_mode: Int, images_mode: Int, videos_mode: Int,
                               marquee_interval: Int, image_interval: Int) {
    companion object {
        const val TABLE_NAME = "DefaultPlayAdSettingData"
    }

    @PrimaryKey(autoGenerate = false)
    private var plan_id : Int = 0

    @ColumnInfo(name = "plan_name")
    private var plan_name: String = ""

    @ColumnInfo(name = "plan_marquee")
    private var plan_marquee: String = ""

    @ColumnInfo(name = "plan_images")
    private var plan_images: String = ""

    @ColumnInfo(name = "plan_videos")
    private var plan_videos: String = ""

    @ColumnInfo(name = "marquee_mode")
    private var marquee_mode: Int = 0

    @ColumnInfo(name = "images_mode")
    private var images_mode: Int = 0

    @ColumnInfo(name = "videos_mode")
    private var videos_mode: Int = 0

    @ColumnInfo(name = "marquee_interval")
    private var marquee_interval: Int = 0

    @ColumnInfo(name = "image_interval")
    private var image_interval: Int = 0

    init {
        this.plan_id = plan_id
        this.plan_name = plan_name
        this.plan_marquee = plan_marquee
        this.plan_images = plan_images
        this.plan_videos = plan_videos
        this.marquee_mode = marquee_mode
        this.images_mode = images_mode
        this.videos_mode = videos_mode
        this.marquee_interval = marquee_interval
        this.image_interval = image_interval
    }

    fun getPlan_id (): Int {
        return plan_id
    }

    fun setPlan_id (plan_id  : Int) {
        this.plan_id  = plan_id
    }

    fun getPlan_name(): String {
        return plan_name
    }

    fun setPlan_name(plan_name : String) {
        this.plan_name = plan_name
    }

    fun getPlan_marquee(): String {
        return plan_marquee
    }

    fun setPlan_marquee(plan_marquee : String) {
        this.plan_marquee = plan_marquee
    }

    fun getPlan_images(): String {
        return plan_images
    }

    fun setPlan_images(plan_images : String) {
        this.plan_images = plan_images
    }

    fun getPlan_videos(): String {
        return plan_videos
    }

    fun setPlan_videos(plan_videos : String) {
        this.plan_videos = plan_videos
    }

    fun getMarquee_mode (): Int {
        return marquee_mode
    }

    fun setMarquee_mode (marquee_mode  : Int) {
        this.marquee_mode  = marquee_mode
    }

    fun getImages_mode (): Int {
        return images_mode
    }

    fun setImages_mode (images_mode  : Int) {
        this.images_mode  = images_mode
    }

    fun getVideos_mode (): Int {
        return videos_mode
    }

    fun setVideos_mode (videos_mode  : Int) {
        this.videos_mode  = videos_mode
    }

    fun getMarquee_interval (): Int {
        return marquee_interval
    }

    fun setMarquee_interval (marquee_interval : Int) {
        this.marquee_interval  = marquee_interval
    }

    fun getImage_interval (): Int {
        return image_interval
    }

    fun setImage_interval (image_interval : Int) {
        this.image_interval  = image_interval
    }
}