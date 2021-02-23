package com.dynamsoft.sample.dbrcamerapreview

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.dynamsoft.dbr.*
import com.dynamsoft.sample.dbrcamerapreview.util.DBRCache

class MainActivity : AppCompatActivity() {
    var mainBarcodeReader: BarcodeReader? = null
        private set
    private lateinit var mCache: DBRCache
    private val cameraFragment: Camera2BasicFragment = Camera2BasicFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            mainBarcodeReader = BarcodeReader("t0068MgAAACquj133Eq/dO8e++8vZnJoxnoBQt7war3uxkJrf6f7pbI12H1a2vxoCyEQvQvjcZH9UrfqDHfbYYFpcwVAvASA=")
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
        mCache.put("linear", "1")
        mCache.put("qrcode", "1")
        mCache.put("pdf417", "1")
        mCache.put("matrix", "1")
        mCache.put("aztec", "0")
        mCache.put("databar", "0")
        mCache.put("patchcode", "0")
        mCache.put("maxicode", "0")
        mCache.put("microqr", "0")
        mCache.put("micropdf417", "0")
        mCache.put("gs1compositecode", "0")
        mCache.put("postalcode", "0")
        mCache.put("dotcode", "0")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (null == savedInstanceState) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, cameraFragment)
                    .commit()
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
    fun onScan(view: View?) {
        // This method is the event listener for the scan button
        println("Scan - #7 https://gitlab.socs.uoguelph.ca/skaplan/cis3760/-/issues/7")

        /*
         * TODO: Delete this comment after it is implemented
         *
         * This comment describes the code that will be written at a later date. This code will
         * likely be contained in separate classes, but the general logic flow will be as follows
         *
         * Make a request to the backend to see if there is an entry in the db for the barcode
         *
         * If there is not an entry:
         *      prompt the user to enter the name of the product and the quantity to add to the db
         *
         * Else, if there is an entry:
         *      Make a request to the backend to get the info and display it in the page
         *
         *      If the user edits the quantity, make a request to the backend that changes the
         *      quantity
         */

        // Make a request to the backend to see if there is an entry in the db for the barcode
        // ... get (user, barcodeId) ...

        // If there is not an entry,
        // ... if (isExist == false) ...
        // ... startActivity(new Intent(MainActivity.this, PopUpCreate.class));

        // Else, if there is an entry
        // ... else ...
        startActivity(Intent(this@MainActivity, PopUp::class.java))
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun onFlash(view: View?) {
        cameraFragment.onFlash(view)
    }

    fun onInventory(view: View?) {
        // This method is the event listener for the inventory button
    }
}