package com.dynamsoft.sample.dbrcamerapreview

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics

class PopUp : Activity() {
    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popupwindow)
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels
        val height = dm.heightPixels
        window.setLayout((width * 0.80).toInt(), (height * 0.40).toInt())
    }
}