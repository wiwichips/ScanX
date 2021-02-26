package com.dynamsoft.sample.dbrcamerapreview.util

object CameraConstants {
    val DEFAULT_ASPECT_RATIO = AspectRatio.of(4, 3)
    const val AUTO_FOCUS_TIMEOUT_MS: Long = 800 // 800ms timeout, Under normal circumstances need to a few hundred milliseconds
    const val OPEN_CAMERA_TIMEOUT_MS: Long = 2500 // 2.5s
    const val FOCUS_HOLD_MILLIS = 3000
    const val METERING_REGION_FRACTION = 0.1225f
    const val ZOOM_REGION_DEFAULT = 1
    const val FLASH_OFF = 0
    const val FLASH_ON = 1
    const val FLASH_TORCH = 2
    const val FLASH_AUTO = 3
    const val FLASH_RED_EYE = 4
    const val CAMERA_BACK = "0"
    const val CAMERA_FRONT = "1"
}