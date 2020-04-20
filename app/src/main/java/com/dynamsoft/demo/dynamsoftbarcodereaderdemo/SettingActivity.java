package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

	@BindView(R.id.ckbLinear)
	CheckBox mLinear;
	@BindView(R.id.ckbQR)
	CheckBox mQRCode;
	@BindView(R.id.ckbPDF417)
	CheckBox mPDF417;
	@BindView(R.id.ckbDataMatrix)
	CheckBox mDataMatrix;
	@BindView(R.id.ckbAztec)
	CheckBox mDataAztec;
	@BindView(R.id.ckbDatabar)
	CheckBox mDataBar;
	@BindView(R.id.ckbPatchCode)
	CheckBox mPatchCode;
	@BindView(R.id.ckbMaxiCode)
	CheckBox mMaxiCode;
	@BindView(R.id.ckbMicroQR)
	CheckBox mMicroQR;
	@BindView(R.id.ckbMicroPDF417)
	CheckBox mMicroPDF417;
	@BindView(R.id.ckbGS1Composite)
	CheckBox mGS1Composite;
	@BindView(R.id.ckbPostalCode)
	CheckBox mPostalCode;
	@BindView(R.id.ckbDotCode)
	CheckBox mDotCode;
	private int mBarcodeFormat;
	private DBRCache mCache;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_type);
		ButterKnife.bind(this);
		Toolbar toolbar = (Toolbar) findViewById(R.id.settoolbar);
		setSupportActionBar(toolbar);

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
		if (mDotCode.isChecked()) {
			nState++;
			enabledCheckBox = mDotCode;
		}
		updateFormatCheckboxsState();

	}

	private void updateFormatCheckboxsState(){
		int nState = 0;
		CheckBox enabledCheckBox = null;
		if(mLinear.isChecked()) {
			nState++;
			enabledCheckBox = mLinear;
		}
		if(mQRCode.isChecked()) {
			nState++;
			enabledCheckBox = mQRCode;
		}
		if(mPDF417.isChecked()) {
			nState++;
			enabledCheckBox = mPDF417;
		}
		if(mDataMatrix.isChecked()){
			nState++;
			enabledCheckBox = mDataMatrix;
		}
		if(mDataAztec.isChecked()) {
			nState++;
			enabledCheckBox = mDataAztec;
		}
		if(mDataBar.isChecked()) {
			nState++;
			enabledCheckBox = mDataBar;
		}
		if(mPatchCode.isChecked()) {
			nState++;
			enabledCheckBox = mPatchCode;
		}
		if(mMaxiCode.isChecked()) {
			nState++;
			enabledCheckBox = mMaxiCode;
		}
		if(mMicroQR.isChecked()) {
			nState++;
			enabledCheckBox = mMicroQR;
		}
		if(mMicroPDF417.isChecked()) {
			nState++;
			enabledCheckBox = mMicroPDF417;
		}
		if(mGS1Composite.isChecked()) {
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

		if(nState ==1){
			enabledCheckBox.setEnabled(false);
		}else{
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
		}if (mDataBar.isChecked()) {
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
