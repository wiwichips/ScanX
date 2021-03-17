package com.dynamsoft.sample.dbrcamerapreview

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.CheckBox
import android.widget.CompoundButton
import com.dynamsoft.sample.dbrcamerapreview.util.DBRCache

class SettingActivity : Activity(), CompoundButton.OnCheckedChangeListener {

    private lateinit var checkboxes: Map<String, CheckBox>
    private lateinit var mCache: DBRCache

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_type)

        checkboxes = mapOf(
                "linear" to findViewById(R.id.ckbLinear),
                "qrcode" to findViewById(R.id.ckbQR),
                "pdf417" to findViewById(R.id.ckbPDF417),
                "matrix" to findViewById(R.id.ckbDataMatrix),
                "aztec" to findViewById(R.id.ckbAztec),
                "dotcode" to findViewById(R.id.ckbDotCode),
                "databar" to findViewById(R.id.ckbDatabar),
                "patchcode" to findViewById(R.id.ckbPatchCode),
                "maxicode" to findViewById(R.id.ckbMaxiCode),
                "microqr" to findViewById(R.id.ckbMicroQR),
                "micropdf417" to findViewById(R.id.ckbMicroPDF417),
                "gs1compositecode" to findViewById(R.id.ckbGS1Composite),
                "postalcode" to findViewById(R.id.ckbPostalCode))

        val toolbar: Toolbar = findViewById(R.id.settoolbar)
        toolbar.title = "Types Setting"
        toolbar.setNavigationOnClickListener { onBackPressed() }

        for (checkbox in checkboxes.values) {
            checkbox.setOnCheckedChangeListener(this)
        }

        mCache = DBRCache[this]

        for (string in checkboxes.keys) {
            if ("1" == mCache.getAsString(string)) {
                checkboxes[string]?.isChecked = true
            }
        }

        updateFormatCheckboxState()
    }

    private fun updateFormatCheckboxState() {
        var nState = 0
        var enabledCheckBox: CheckBox? = null

        for (checkbox in checkboxes.values) {
            if (checkbox.isChecked) {
                nState++
                enabledCheckBox = checkbox
            }
        }

        if (nState == 1) {
            enabledCheckBox!!.isEnabled = false
        } else {
            for (checkbox in checkboxes.values) {
                checkbox.isEnabled = true
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        for (entry in checkboxes.entries) {
            if (entry.value.isChecked) {
                mCache.put(entry.key, "1")
            } else {
                mCache.put(entry.key, "0")
            }
        }
        setResult(0)
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        updateFormatCheckboxState()
    }
}