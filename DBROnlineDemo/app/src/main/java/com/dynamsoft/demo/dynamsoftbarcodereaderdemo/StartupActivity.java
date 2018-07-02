package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Elemen on 2018/7/2.
 */
public class StartupActivity extends AppCompatActivity {
	@BindView(R.id.imageButton)
	ImageButton imageButton;
	@BindView(R.id.imageButton2)
	ImageButton imageButton2;
	@BindView(R.id.imageButton3)
	ImageButton imageButton3;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		ButterKnife.bind(this);
	}


	@OnClick({R.id.imageButton, R.id.imageButton2, R.id.imageButton3})
	public void onViewClicked(View view) {
		switch (view.getId()) {
			case R.id.imageButton:

			case R.id.imageButton2:

			case R.id.imageButton3:
				startActivity(new Intent(StartupActivity.this,MainActivity.class));
			default:
				break;
		}
	}
}
