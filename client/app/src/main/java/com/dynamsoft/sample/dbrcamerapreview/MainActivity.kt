package com.dynamsoft.sample.dbrcamerapreview

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TabHost
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.dynamsoft.dbr.*
import com.dynamsoft.sample.dbrcamerapreview.util.DBRCache
import com.google.android.material.tabs.TabLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    var mainBarcodeReader: BarcodeReader? = null
        private set
    private lateinit var mCache: DBRCache
    private val cameraFragment: Camera2BasicFragment = Camera2BasicFragment.newInstance()
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            mainBarcodeReader = BarcodeReader("t0077xQAAAIdnzZzxS9/5MdTdFjC6/0M43GI074tpK4cjizCHLBDVqySaW+BxqiYS/gOHT8+sA3HsJAbNI+Qi1MWBt8iK0iKGatKIWA+8KY0=")
            mainBarcodeReader!!.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"Balance\",\"DeblurLevel\":5,\"ExpectedBarcodesCount\":512,\"LocalizationModes\":[{\"Mode\":\"LM_CONNECTED_BLOCKS\"},{\"Mode\":\"LM_SCAN_DIRECTLY\"}]}}", EnumConflictMode.CM_OVERWRITE)
            val settings = mainBarcodeReader!!.runtimeSettings
            settings.intermediateResultTypes = EnumIntermediateResultType.IRT_TYPED_BARCODE_ZONE
            settings.barcodeFormatIds = EnumBarcodeFormat.BF_ONED or EnumBarcodeFormat.BF_DATAMATRIX or EnumBarcodeFormat.BF_QR_CODE or EnumBarcodeFormat.BF_PDF417
            settings.barcodeFormatIds_2 = 0
            mainBarcodeReader!!.updateRuntimeSettings(settings)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mCache = DBRCache[this]
        mCache["linear"] = "1"
        mCache["qrcode"] = "1"
        mCache["pdf417"] = "1"
        mCache["matrix"] = "1"
        mCache["aztec"] = "0"
        mCache["databar"] = "0"
        mCache["patchcode"] = "0"
        mCache["maxicode"] = "0"
        mCache["microqr"] = "0"
        mCache["micropdf417"] = "0"
        mCache["gs1compositecode"] = "0"
        mCache["postalcode"] = "0"
        mCache["dotcode"] = "0"

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (null == savedInstanceState) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, cameraFragment)
                    .commit()
        }
        FirebaseApp.initializeApp(this)
        requestQueue = Volley.newRequestQueue(this)
        createNotificationChannel()
        receiveNotifications()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ScanX"
            val descriptionText = "Stock Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CIS3760_1337", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun receiveNotifications() {
        val url = "http://173.34.40.62:5000/subscribe"
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val jsonObject = JSONObject(mapOf("id" to it.result))
                val request = JsonObjectRequest(Request.Method.POST, url, jsonObject, {}, null)
                requestQueue.add(request)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_settings) {
            val intent = Intent(this@MainActivity, SettingActivity::class.java)
            // intent.putExtra("type", barcodeType);
            startActivityForResult(intent, 0)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            var nBarcodeFormat = 0
            var nBarcodeFormat2 = 0
            if (mCache.getAsString("linear") == "1") {
                nBarcodeFormat = nBarcodeFormat or EnumBarcodeFormat.BF_ONED
            }
            if (mCache.getAsString("qrcode") == "1") {
                nBarcodeFormat = nBarcodeFormat or EnumBarcodeFormat.BF_QR_CODE
            }
            if (mCache.getAsString("pdf417") == "1") {
                nBarcodeFormat = nBarcodeFormat or EnumBarcodeFormat.BF_PDF417
            }
            if (mCache.getAsString("matrix") == "1") {
                nBarcodeFormat = nBarcodeFormat or EnumBarcodeFormat.BF_DATAMATRIX
            }
            if (mCache.getAsString("aztec") == "1") {
                nBarcodeFormat = nBarcodeFormat or EnumBarcodeFormat.BF_AZTEC
            }
            if (mCache.getAsString("databar") == "1") {
                nBarcodeFormat = nBarcodeFormat or EnumBarcodeFormat.BF_GS1_DATABAR
            }
            if (mCache.getAsString("patchcode") == "1") {
                nBarcodeFormat = nBarcodeFormat or EnumBarcodeFormat.BF_PATCHCODE
            }
            if (mCache.getAsString("maxicode") == "1") {
                nBarcodeFormat = nBarcodeFormat or EnumBarcodeFormat.BF_MAXICODE
            }
            if (mCache.getAsString("microqr") == "1") {
                nBarcodeFormat = nBarcodeFormat or EnumBarcodeFormat.BF_MICRO_QR
            }
            if (mCache.getAsString("micropdf417") == "1") {
                nBarcodeFormat = nBarcodeFormat or EnumBarcodeFormat.BF_MICRO_PDF417
            }
            if (mCache.getAsString("gs1compositecode") == "1") {
                nBarcodeFormat = nBarcodeFormat or EnumBarcodeFormat.BF_GS1_COMPOSITE
            }
            if (mCache.getAsString("postalcode") == "1") {
                nBarcodeFormat2 = nBarcodeFormat2 or EnumBarcodeFormat_2.BF2_POSTALCODE
            }
            if (mCache.getAsString("dotcode") == "1") {
                nBarcodeFormat2 = nBarcodeFormat2 or EnumBarcodeFormat_2.BF2_DOTCODE
            }
            val runtimeSettings = mainBarcodeReader!!.runtimeSettings
            runtimeSettings.barcodeFormatIds = nBarcodeFormat
            runtimeSettings.barcodeFormatIds_2 = nBarcodeFormat2
            mainBarcodeReader!!.updateRuntimeSettings(runtimeSettings)
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    /**
     * Event listeners for main page buttons
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun onFlash(view: View?) {
        cameraFragment.onFlash(view)
    }

    fun onInventory(view: View?) {
        // This method is the event listener for the inventory button
        //Intent intent = new Intent(this@MainActivity, ScanList::class.java)
        startActivity(Intent(this@MainActivity, ScanList::class.java))
    }
}
