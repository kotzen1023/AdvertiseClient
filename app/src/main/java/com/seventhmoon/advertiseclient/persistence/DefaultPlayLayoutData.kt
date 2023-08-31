package com.seventhmoon.advertiseclient.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = DefaultPlayLayoutData.TABLE_NAME)
class DefaultPlayLayoutData(layout_id: Int, screenWidth: Int, screenHeight: Int, orientation: Int,
                            layout_top: Int, layout_center: Int, layout_bottom: Int, layoutOrientation: Int, plan_id: Int) {
    companion object {
        const val TABLE_NAME = "DefaultPlayLayoutData"
    }

    @PrimaryKey(autoGenerate = false)
    private var layout_id : Int = 0

    @ColumnInfo(name = "screenWidth")
    private var screenWidth: Int = 0

    @ColumnInfo(name = "screenHeight")
    private var screenHeight: Int = 0

    @ColumnInfo(name = "orientation")
    private var orientation: Int = 0

    @ColumnInfo(name = "layout_top")
    private var layout_top: Int = 0

    @ColumnInfo(name = "layout_center")
    private var layout_center: Int = 0

    @ColumnInfo(name = "layout_bottom")
    private var layout_bottom: Int = 0

    @ColumnInfo(name = "layoutOrientation")
    private var layoutOrientation: Int = 0

    @ColumnInfo(name = "plan_id")
    private var plan_id: Int = 0

    init {
        this.layout_id = layout_id
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
        this.orientation = orientation
        this.layout_top = layout_top
        this.layout_center = layout_center
        this.layout_bottom = layout_bottom
        this.layoutOrientation = layoutOrientation
        this.plan_id = plan_id
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

    fun getOrientation(): Int {
        return orientation
    }

    fun setOrientation(orientation : Int) {
        this.orientation = orientation
    }

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
}