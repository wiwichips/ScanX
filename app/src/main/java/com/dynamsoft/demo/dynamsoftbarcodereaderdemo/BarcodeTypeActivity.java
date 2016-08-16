package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.dynamsoft.barcode.Barcode;

public class BarcodeTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_type);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Barcode Type");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLinear = (CheckBox)findViewById(R.id.ckbLinear);
        mQRCode = (CheckBox)findViewById(R.id.ckbQR);
        mPDF417 = (CheckBox)findViewById(R.id.ckbPDF417);
        mDataMatrix = (CheckBox)findViewById(R.id.ckbDataMatrix);

        mBarcodeFormat = getIntent().getLongExtra(BarcodeFormat, Barcode.UNKNOWN);
        if ((mBarcodeFormat & Barcode.OneD) > 0)
            mLinear.setChecked(true);
        if ((mBarcodeFormat & Barcode.QR_CODE) > 0)
            mQRCode.setChecked(true);
        if ((mBarcodeFormat & Barcode.PDF417) > 0)
            mPDF417.setChecked(true);
        if ((mBarcodeFormat & Barcode.DATAMATRIX) > 0)
            mDataMatrix.setChecked(true);
    }

    public static final String BarcodeFormat = "barcode format";
    private CheckBox mLinear;
    private CheckBox mQRCode;
    private CheckBox mPDF417;
    private CheckBox mDataMatrix;
    private long mBarcodeFormat;

    public void onCheck(View v) {
        CheckBox chb = (CheckBox)v;
        switch (v.getId()) {
            case R.id.ckbLinear:
                if (!chb.isChecked())
                    if (mBarcodeFormat == Barcode.OneD) {
                        chb.setChecked(true);
                        return;
                    }
                    else
                        mBarcodeFormat ^= Barcode.OneD;
                else
                    mBarcodeFormat |= Barcode.OneD;
                break;
            case R.id.ckbQR:
                if (!chb.isChecked())
                    if (mBarcodeFormat == Barcode.QR_CODE) {
                        chb.setChecked(true);
                        return;
                    }
                    else
                        mBarcodeFormat ^= Barcode.QR_CODE;
                else
                    mBarcodeFormat |= Barcode.QR_CODE;
                break;
            case R.id.ckbPDF417:
                if (!chb.isChecked())
                    if (mBarcodeFormat == Barcode.PDF417) {
                        chb.setChecked(true);
                        return;
                    }
                    else
                        mBarcodeFormat ^= Barcode.PDF417;
                else
                    mBarcodeFormat |= Barcode.PDF417;
                break;
            case R.id.ckbDataMatrix:
                if (!chb.isChecked())
                    if (mBarcodeFormat == Barcode.DATAMATRIX) {
                        chb.setChecked(true);
                        return;
                    }
                    else
                        mBarcodeFormat ^= Barcode.DATAMATRIX;
                else
                    mBarcodeFormat |= Barcode.DATAMATRIX;
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra(BarcodeFormat, mBarcodeFormat);
        setResult(0, data);
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
