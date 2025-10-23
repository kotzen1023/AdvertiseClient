package com.seventhmoon.advertiseclient.data.defaultData

class DefaultDataAdSetting(plan_id: Int, plan_name: String, plan_marquee: String,
                           plan_images: String, plan_videos: String, plan_banner: String,
                           plan_mix: String,
                           marquee_mode: Int, marquee_background: String,
                           marquee_text: String, marquee_size: Int,
                           marquee_locate: Int, marquee_speed: Int,
                           images_mode: Int, videos_mode: Int,
                           marquee_interval: Int, image_interval: Int, image_scale_type: Int,
                           image_anime: Int,
                           video_scale_type: Int, banner_scale_type: Int,
                           mix_mode: Int, mix_image_interval: Int,
                           mix_image_scale_type: Int, mix_image_anime: Int,
                           mix_video_scale_type: Int ) {

    private var plan_id : Int = 0
    private var plan_name: String = ""
    private var plan_marquee: String = ""
    private var plan_images: String = ""
    private var plan_videos: String = ""
    private var plan_banner: String = ""
    private var plan_mix: String = ""
    private var marquee_mode: Int = 0
    private var marquee_background: String = ""
    private var marquee_text: String = ""
    private var marquee_size: Int = 0
    private var marquee_locate: Int = 0
    private var marquee_speed: Int = 0
    private var images_mode: Int = 0
    private var videos_mode: Int = 0
    private var marquee_interval: Int = 0
    private var image_interval: Int = 0
    private var image_scale_type: Int = 0
    private var image_anime: Int = 0
    private var video_scale_type: Int = 0
    private var banner_scale_type: Int = 0
    private var mix_mode: Int = 0
    private var mix_image_interval: Int = 0
    private var mix_image_scale_type: Int = 0
    private var mix_image_anime: Int = 0
    private var mix_video_scale_type: Int = 0

    init {
        this.plan_id = plan_id
        this.plan_name = plan_name
        this.plan_marquee = plan_marquee
        this.plan_images = plan_images
        this.plan_videos = plan_videos
        this.plan_banner = plan_banner
        this.plan_mix = plan_mix
        this.marquee_mode = marquee_mode
        this.marquee_background = marquee_background
        this.marquee_text = marquee_text
        this.marquee_size = marquee_size
        this.marquee_locate = marquee_locate
        this.marquee_speed = marquee_speed
        this.images_mode = images_mode
        this.videos_mode = videos_mode
        this.marquee_interval = marquee_interval
        this.image_interval = image_interval
        this.image_scale_type = image_scale_type
        this.image_anime = image_anime
        this.video_scale_type = video_scale_type
        this.banner_scale_type = banner_scale_type
        this.mix_mode = mix_mode
        this.mix_image_interval = mix_image_interval
        this.mix_image_scale_type = mix_image_scale_type
        this.mix_image_anime = mix_image_anime
        this.mix_video_scale_type = mix_video_scale_type
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

    fun getPlan_banner(): String {
        return plan_banner
    }

    fun setPlan_banner(plan_banner : String) {
        this.plan_banner = plan_banner
    }

    fun getPlan_mix(): String {
        return plan_mix
    }

    fun setPlan_mix(plan_mix : String) {
        this.plan_mix = plan_mix
    }

    fun getMarquee_mode (): Int {
        return marquee_mode
    }

    fun setMarquee_mode (marquee_mode  : Int) {
        this.marquee_mode  = marquee_mode
    }

    fun getMarquee_background (): String {
        return marquee_background
    }

    fun setMarquee_background (marquee_background  : String) {
        this.marquee_background  = marquee_background
    }

    fun getMarquee_text (): String {
        return marquee_text
    }

    fun setMarquee_text (marquee_text  : String) {
        this.marquee_text  = marquee_text
    }

    fun getMarquee_size (): Int {
        return marquee_size
    }

    fun setMarquee_size (marquee_size  : Int) {
        this.marquee_size  = marquee_size
    }

    fun getMarquee_locate (): Int {
        return marquee_locate
    }

    fun setMarquee_locate (marquee_locate  : Int) {
        this.marquee_locate  = marquee_locate
    }

    fun getMarquee_speed (): Int {
        return marquee_speed
    }

    fun setMarquee_speed (marquee_speed  : Int) {
        this.marquee_speed  = marquee_speed
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

    fun getImage_scale_type (): Int {
        return image_scale_type
    }

    fun setImage_scale_type (image_scale_type : Int) {
        this.image_scale_type  = image_scale_type
    }

    fun getImage_anime (): Int {
        return image_anime
    }

    fun setImage_anime (image_anime : Int) {
        this.image_anime  = image_anime
    }

    fun getVideo_scale_type (): Int {
        return video_scale_type
    }

    fun setVideo_scale_type (video_scale_type : Int) {
        this.video_scale_type  = video_scale_type
    }

    fun getBanner_scale_type (): Int {
        return banner_scale_type
    }

    fun setBanner_scale_type (banner_scale_type : Int) {
        this.banner_scale_type  = banner_scale_type
    }

    fun getMix_mode (): Int {
        return mix_mode
    }

    fun setMix_mode (mix_mode : Int) {
        this.mix_mode  = mix_mode
    }

    fun getMix_image_interval (): Int {
        return mix_image_interval
    }

    fun setMix_image_interval (mix_image_interval : Int) {
        this.mix_image_interval  = mix_image_interval
    }

    fun getMix_image_scale_type (): Int {
        return mix_image_scale_type
    }

    fun setMix_image_scale_type (mix_image_scale_type : Int) {
        this.mix_image_scale_type  = mix_image_scale_type
    }

    fun getMix_image_anime (): Int {
        return mix_image_anime
    }

    fun setMix_image_anime (mix_image_anime : Int) {
        this.mix_image_anime  = mix_image_anime
    }

    fun getMix_video_scale_type (): Int {
        return mix_video_scale_type
    }

    fun setMix_video_scale_type (mix_video_scale_type : Int) {
        this.mix_video_scale_type  = mix_video_scale_type
    }
}