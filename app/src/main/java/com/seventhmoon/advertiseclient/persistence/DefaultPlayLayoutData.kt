package com.seventhmoon.advertiseclient.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = DefaultPlayLayoutData.TABLE_NAME)
class DefaultPlayLayoutData(layout_id: Int, screenWidth: Int, screenHeight: Int, orientation: Int,
                            border: Int,
                            layout_top: Int, layout_center: Int, layout_bottom: Int,
                            layout2_top: Int, layout2_center: Int, layout2_bottom: Int,
                            layout3_top: Int, layout3_center: Int, layout3_bottom: Int,
                            layout4_top: Int, layout4_center: Int, layout4_bottom: Int,
                            layoutOrientation: Int,
                            plan_id: Int, plan2_id: Int, plan3_id: Int, plan4_id: Int,
                            plan_start_time: String, plan2_start_time: String, plan3_start_time: String, plan4_start_time: String) {
    companion object {
        const val TABLE_NAME = "DefaultPlayLayoutData"
    }

    @PrimaryKey(autoGenerate = false)
    private var layout_id : Int = 0

    @ColumnInfo(name = "screenWidth")
    private var screenWidth: Int = 0

    @ColumnInfo(name = "screenHeight")
    private var screenHeight: Int = 0

    @ColumnInfo(name = "border")
    private var border: Int = 0

    @ColumnInfo(name = "orientation")
    private var orientation: Int = 0

    @ColumnInfo(name = "layout_top")
    private var layout_top: Int = 0

    @ColumnInfo(name = "layout_center")
    private var layout_center: Int = 0

    @ColumnInfo(name = "layout_bottom")
    private var layout_bottom: Int = 0

    @ColumnInfo(name = "layout2_top")
    private var layout2_top: Int = 0

    @ColumnInfo(name = "layout2_center")
    private var layout2_center: Int = 0

    @ColumnInfo(name = "layout2_bottom")
    private var layout2_bottom: Int = 0

    @ColumnInfo(name = "layout3_top")
    private var layout3_top: Int = 0

    @ColumnInfo(name = "layout3_center")
    private var layout3_center: Int = 0

    @ColumnInfo(name = "layout3_bottom")
    private var layout3_bottom: Int = 0

    @ColumnInfo(name = "layout4_top")
    private var layout4_top: Int = 0

    @ColumnInfo(name = "layout4_center")
    private var layout4_center: Int = 0

    @ColumnInfo(name = "layout4_bottom")
    private var layout4_bottom: Int = 0

    @ColumnInfo(name = "layoutOrientation")
    private var layoutOrientation: Int = 0

    @ColumnInfo(name = "plan_id")
    private var plan_id: Int = 0

    @ColumnInfo(name = "plan2_id")
    private var plan2_id: Int = 0

    @ColumnInfo(name = "plan3_id")
    private var plan3_id: Int = 0

    @ColumnInfo(name = "plan4_id")
    private var plan4_id: Int = 0

    @ColumnInfo(name = "plan_start_time")
    private var plan_start_time: String = ""

    @ColumnInfo(name = "plan2_start_time")
    private var plan2_start_time: String = ""

    @ColumnInfo(name = "plan3_start_time")
    private var plan3_start_time: String = ""

    @ColumnInfo(name = "plan4_start_time")
    private var plan4_start_time: String = ""

    init {
        this.layout_id = layout_id
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
        this.orientation = orientation
        this.layout_top = layout_top
        this.layout_center = layout_center
        this.layout_bottom = layout_bottom
        this.layout2_top = layout2_top
        this.layout2_center = layout2_center
        this.layout2_bottom = layout2_bottom
        this.layout3_top = layout3_top
        this.layout3_center = layout3_center
        this.layout3_bottom = layout3_bottom
        this.layout4_top = layout4_top
        this.layout4_center = layout4_center
        this.layout4_bottom = layout4_bottom
        this.layoutOrientation = layoutOrientation
        this.plan_id = plan_id
        this.plan2_id = plan2_id
        this.plan3_id = plan3_id
        this.plan4_id = plan4_id
        this.plan_start_time = plan_start_time
        this.plan2_start_time = plan2_start_time
        this.plan3_start_time = plan3_start_time
        this.plan4_start_time = plan4_start_time
    }

    fun getLayout_id (): Int {
        return layout_id
    }

    fun setLayout_id (layout_id  : Int) {
        this.layout_id  = layout_id
    }

    fun getScreenWidth(): Int {
        return screenWidth
    }

    fun setScreenWidth(screenWidth : Int) {
        this.screenWidth = screenWidth
    }

    fun getScreenHeight(): Int {
        return screenHeight
    }

    fun setScreenHeight(screenHeight : Int) {
        this.screenHeight = screenHeight
    }

    fun getBorder(): Int {
        return border
    }

    fun setBorder(border : Int) {
        this.border = border
    }

    fun getOrientation(): Int {
        return orientation
    }

    fun setOrientation(orientation : Int) {
        this.orientation = orientation
    }
    //layout1
    fun getLayout_top(): Int {
        return layout_top
    }

    fun setLayout_top(layout_top : Int) {
        this.layout_top = layout_top
    }

    fun getLayout_center(): Int {
        return layout_center
    }

    fun setLayout_center(layout_center : Int) {
        this.layout_center = layout_center
    }

    fun getLayout_bottom(): Int {
        return layout_bottom
    }

    fun setLayout_bottom(layout_bottom : Int) {
        this.layout_bottom = layout_bottom
    }
    //layout2
    fun getLayout2_top(): Int {
        return layout2_top
    }

    fun setLayout2_top(layout2_top : Int) {
        this.layout2_top = layout2_top
    }

    fun getLayout2_center(): Int {
        return layout2_center
    }

    fun setLayout2_center(layout2_center : Int) {
        this.layout2_center = layout2_center
    }

    fun getLayout2_bottom(): Int {
        return layout2_bottom
    }

    fun setLayout2_bottom(layout2_bottom : Int) {
        this.layout2_bottom = layout2_bottom
    }
    //layout3
    fun getLayout3_top(): Int {
        return layout3_top
    }

    fun setLayout3_top(layout3_top : Int) {
        this.layout3_top = layout3_top
    }

    fun getLayout3_center(): Int {
        return layout3_center
    }

    fun setLayout3_center(layout3_center : Int) {
        this.layout3_center = layout3_center
    }

    fun getLayout3_bottom(): Int {
        return layout3_bottom
    }

    fun setLayout3_bottom(layout3_bottom : Int) {
        this.layout3_bottom = layout3_bottom
    }

    //layout4
    fun getLayout4_top(): Int {
        return layout4_top
    }

    fun setLayout4_top(layout4_top : Int) {
        this.layout4_top = layout4_top
    }

    fun getLayout4_center(): Int {
        return layout4_center
    }

    fun setLayout4_center(layout4_center : Int) {
        this.layout4_center = layout4_center
    }

    fun getLayout4_bottom(): Int {
        return layout4_bottom
    }

    fun setLayout4_bottom(layout4_bottom : Int) {
        this.layout4_bottom = layout4_bottom
    }

    fun getLayoutOrientation(): Int {
        return layoutOrientation
    }

    fun setLayoutOrientation(layoutOrientation : Int) {
        this.layoutOrientation = layoutOrientation
    }

    fun getPlan_id(): Int {
        return plan_id
    }

    fun setPlan_id(plan_id : Int) {
        this.plan_id = plan_id
    }

    fun getPlan2_id(): Int {
        return plan2_id
    }

    fun setPlan2_id(plan2_id : Int) {
        this.plan2_id = plan2_id
    }

    fun getPlan3_id(): Int {
        return plan3_id
    }

    fun setPlan3_id(plan3_id : Int) {
        this.plan3_id = plan3_id
    }

    fun getPlan4_id(): Int {
        return plan4_id
    }

    fun setPlan4_id(plan3_id : Int) {
        this.plan4_id = plan4_id
    }

    fun getPlan_start_time(): String {
        return plan_start_time
    }

    fun setPlan_start_time(plan_start_time : String) {
        this.plan_start_time = plan_start_time
    }

    fun getPlan2_start_time(): String {
        return plan2_start_time
    }

    fun setPlan2_start_time(plan2_start_time : String) {
        this.plan2_start_time = plan2_start_time
    }

    fun getPlan3_start_time(): String {
        return plan3_start_time
    }

    fun setPlan3_start_time(plan3_start_time : String) {
        this.plan3_start_time = plan3_start_time
    }

    fun getPlan4_start_time(): String {
        return plan4_start_time
    }

    fun setPlan4_start_time(plan4_start_time : String) {
        this.plan4_start_time = plan4_start_time
    }
}