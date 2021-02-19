package com.dynamsoft.sample.dbrcamerapreview;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.dynamsoft.sample.dbrcamerapreview.util.DBRCache;

public class SettingActivity extends
        Activity implements CompoundButton.OnCheckedChangeListener {

    CheckBox mLinear;
    CheckBox mQRCode;
    CheckBox mPDF417;
    CheckBox mDataMatrix;
    CheckBox mDataAztec;
    CheckBox mDataBar;
    CheckBox mPatchCode;
    CheckBox mMaxiCode;
    CheckBox mMicroQR;
    CheckBox mMicroPDF417;
    CheckBox mGS1Composite;
    CheckBox mPostalCode;
    CheckBox mDotCode;
    private int mBarcodeFormat;
    private DBRCache mCache;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_type);
        mLinear = findViewById(R.id.ckbLinear);
        mQRCode = findViewById(R.id.ckbQR);
        mPDF417 = findViewById(R.id.ckbPDF417);
        mDataMatrix = findViewById(R.id.ckbDataMatrix);
        mDataAztec = findViewById(R.id.ckbAztec);
        mDataBar = findViewById(R.id.ckbDatabar);
        mPatchCode = findViewById(R.id.ckbPatchCode);
        mMaxiCode = findViewById(R.id.ckbMaxiCode);
        mMicroQR = findViewById(R.id.ckbMicroQR);
        mMicroPDF417 = findViewById(R.id.ckbMicroPDF417);
        mGS1Composite = findViewById(R.id.ckbGS1Composite);
        mPostalCode = findViewById(R.id.ckbPostalCode);
        mDotCode = findViewById(R.id.ckbDotCode);


        Toolbar toolbar = findViewById(R.id.settoolbar);
        toolbar.setTitle("Types Setting");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mLinear.setOnCheckedChangeListener(this);
        mQRCode.setOnCheckedChangeListener(this);
        mPDF417.setOnCheckedChangeListener(this);
        mDataMatrix.setOnCheckedChangeListener(this);
        mDataAztec.setOnCheckedChangeListener(this);
        mDataBar.setOnCheckedChangeListener(this);
        mPatchCode.setOnCheckedChangeListener(this);
        mMaxiCode.setOnCheckedChangeListener(this);
        mMicroQR.setOnCheckedChangeListener(this);
        mMicroPDF417.setOnCheckedChangeListener(this);
        mGS1Composite.setOnCheckedChangeListener(this);
        mPostalCode.setOnCheckedChangeListener(this);
        mDotCode.setOnCheckedChangeListener(this);

        mCache = DBRCache.get(this);
        if ("1".equals(mCache.getAsString("linear"))) {
            mLinear.setChecked(true);
        }
        if ("1".equals(mCache.getAsString("qrcode"))) {
            mQRCode.setChecked(true);
        }
        if ("1".equals(mCache.getAsString("pdf417"))) {
            mPDF417.setChecked(true);
        }
        if ("1".equals(mCache.getAsString("matrix"))) {
            mDataMatrix.setChecked(true);
        }
        if ("1".equals(mCache.getAsString("aztec"))) {
            mDataAztec.setChecked(true);
        }
        if ("1".equals(mCache.getAsString("databar"))) {
            mDataBar.setChecked(true);
        }
        if ("1".equals(mCache.getAsString("patchcode"))) {
            mPatchCode.setChecked(true);
        }
        if ("1".equals(mCache.getAsString("maxicode"))) {
            mMaxiCode.setChecked(true);
        }
        if ("1".equals(mCache.getAsString("microqr"))) {
            mMicroQR.setChecked(true);
        }
        if ("1".equals(mCache.getAsString("micropdf417"))) {
            mMicroPDF417.setChecked(true);
        }
        if ("1".equals(mCache.getAsString("gs1compositecode"))) {
            mGS1Composite.setChecked(true);
        }
        if ("1".equals(mCache.getAsString("postalcode"))) {
            mPostalCode.setChecked(true);
        }
        if ("1".equals(mCache.getAsString("dotcode"))) {
            mDotCode.setChecked(true);
        }
        updateFormatCheckboxsState();

    }

    private void updateFormatCheckboxsState() {
        int nState = 0;
        CheckBox enabledCheckBox = null;
        if (mLinear.isChecked()) {
            nState++;
            enabledCheckBox = mLinear;
        }
        if (mQRCode.isChecked()) {
            nState++;
            enabledCheckBox = mQRCode;
        }
        if (mPDF417.isChecked()) {
            nState++;
            enabledCheckBox = mPDF417;
        }
        if (mDataMatrix.isChecked()) {
            nState++;
            enabledCheckBox = mDataMatrix;
        }
        if (mDataAztec.isChecked()) {
            nState++;
            enabledCheckBox = mDataAztec;
        }
        if (mDataBar.isChecked()) {
            nState++;
            enabledCheckBox = mDataBar;
        }
        if (mPatchCode.isChecked()) {
            nState++;
            enabledCheckBox = mPatchCode;
        }
        if (mMaxiCode.isChecked()) {
            nState++;
            enabledCheckBox = mMaxiCode;
        }
        if (mMicroQR.isChecked()) {
            nState++;
            enabledCheckBox = mMicroQR;
        }
        if (mMicroPDF417.isChecked()) {
            nState++;
            enabledCheckBox = mMicroPDF417;
        }
        if (mGS1Composite.isChecked()) {
            nState++;
            enabledCheckBox = mGS1Composite;
        }
        if (mPostalCode.isChecked()) {
            nState++;
            enabledCheckBox = mPostalCode;
        }
        if (mDotCode.isChecked()) {
            nState++;
            enabledCheckBox = mDotCode;
        }


        if (nState == 1) {
            enabledCheckBox.setEnabled(false);
        } else {
            mLinear.setEnabled(true);
            mQRCode.setEnabled(true);
            mPDF417.setEnabled(true);
            mDataMatrix.setEnabled(true);
            mDataAztec.setEnabled(true);
            mDataBar.setEnabled(true);
            mPatchCode.setEnabled(true);
            mMaxiCode.setEnabled(true);
            mMicroQR.setEnabled(true);
            mMicroPDF417.setEnabled(true);
            mGS1Composite.setEnabled(true);
            mPostalCode.setEnabled(true);
            mDotCode.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mLinear.isChecked()) {
            mCache.put("linear", "1");
        } else {
            mCache.put("linear", "0");
        }
        if (mQRCode.isChecked()) {
            mCache.put("qrcode", "1");
        } else {
            mCache.put("qrcode", "0");
        }
        if (mPDF417.isChecked()) {
            mCache.put("pdf417", "1");
        } else {
            mCache.put("pdf417", "0");
        }
        if (mDataMatrix.isChecked()) {
            mCache.put("matrix", "1");
        } else {
            mCache.put("matrix", "0");
        }
        if (mDataAztec.isChecked()) {
            mCache.put("aztec", "1");
        } else {
            mCache.put("aztec", "0");
        }
        if (mDataBar.isChecked()) {
            mCache.put("databar", "1");
        } else {
            mCache.put("databar", "0");
        }
        if (mPatchCode.isChecked()) {
            mCache.put("patchcode", "1");
        } else {
            mCache.put("patchcode", "0");
        }
        if (mMaxiCode.isChecked()) {
            mCache.put("maxicode", "1");
        } else {
            mCache.put("maxicode", "0");
        }
        if (mMicroQR.isChecked()) {
            mCache.put("microqr", "1");
        } else {
            mCache.put("microqr", "0");
        }
        if (mMicroPDF417.isChecked()) {
            mCache.put("micropdf417", "1");
        } else {
            mCache.put("micropdf417", "0");
        }
        if (mGS1Composite.isChecked()) {
            mCache.put("gs1compositecode", "1");
        } else {
            mCache.put("gs1compositecode", "0");
        }
        if (mPostalCode.isChecked()) {
            mCache.put("postalcode", "1");
        } else {
            mCache.put("postalcode", "0");
        }
        if (mDotCode.isChecked()) {
            mCache.put("dotcode", "1");
        } else {
            mCache.put("dotcode", "0");
        }
        setResult(0);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        updateFormatCheckboxsState();
    }
}
