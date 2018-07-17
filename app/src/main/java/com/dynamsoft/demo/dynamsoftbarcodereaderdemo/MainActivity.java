package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.dynamsoft.barcode.jni.BarcodeReader;
import com.dynamsoft.barcode.jni.EnumImagePixelFormat;
import com.dynamsoft.barcode.jni.TextResult;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
	@BindView(R.id.cameraView)
	CameraView cameraView;
	@BindView(R.id.qr_view)
	QRCodeView qrView;
	@BindView(R.id.tv_flash)
	TextView mFlash;


	private BarcodeReader reader;
	private TextResult[] result;
	private boolean isDetected = true;
	private int barcodeType = 0;
	private DBRCache mCache;
	private String name = "";
	private boolean isFlashOn = false;
	private boolean isCameraOpen = false;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 0:
					isDetected = false;
					TextResult result = (TextResult) msg.obj;
					final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					if (result.localizationResult != null && result.localizationResult.resultPoints != null && result.localizationResult.resultPoints.length > 0) {
						int x0 = result.localizationResult.resultPoints[0].x;
						int y0 = result.localizationResult.resultPoints[0].y;
						int x1 = result.localizationResult.resultPoints[1].x;
						int y1 = result.localizationResult.resultPoints[1].y;
						int x2 = result.localizationResult.resultPoints[2].x;
						int y2 = result.localizationResult.resultPoints[2].y;
						int x3 = result.localizationResult.resultPoints[3].x;
						int y3 = result.localizationResult.resultPoints[3].y;
						int[] xAarray = new int[]{x0, x1, x2, x3};
						int[] yAarray = new int[]{y0, y1, y2, y3};
						Arrays.sort(xAarray);
						Arrays.sort(yAarray);
						String barcodeFormat = "";
						switch (result.barcodeFormat) {
							case 234882047:
								barcodeFormat = "all";
								break;
							case 1023:
								barcodeFormat = "OneD";
								break;
							case 1:
								barcodeFormat = "CODE_39";
								break;
							case 2:
								barcodeFormat = "CODE_128";
								break;
							case 4:
								barcodeFormat = "CODE_93";
								break;
							case 8:
								barcodeFormat = "CODABAR";
								break;
							case 16:
								barcodeFormat = "ITF";
								break;
							case 32:
								barcodeFormat = "EAN_13";
								break;
							case 64:
								barcodeFormat = "EAN_8";
								break;
							case 128:
								barcodeFormat = "UPC_A";
								break;
							case 256:
								barcodeFormat = "UPC_E";
								break;
							case 512:
								barcodeFormat = "INDUSTRIAL_25";
								break;
							case 33554432:
								barcodeFormat = "PDF417";
								break;
							case 67108864:
								barcodeFormat = "QR_CODE";
								break;
							case 134217728:
								barcodeFormat = "DATAMATAIX";
								break;
							default:
								break;
						}
						builder.setMessage("Type : " + barcodeFormat + "\n\nResult : " + result.barcodeText + "\n\nRegion : {Left : " + xAarray[0]
								+ " Top : " + yAarray[0] + " Right : " + xAarray[3] + " Bottom : " + yAarray[3]
								+ "}");
					} else {
						builder.setMessage("type : " + result.barcodeFormat + "\n\n result : " + result.barcodeText);
					}
					builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							isDetected = true;

						}
					});
					if (!MainActivity.this.isFinishing()) {
						builder.show();
					}
					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		try {
			reader = new BarcodeReader(getString(R.string.dbr_license));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage("Dynamsoft Barcode Reader Mobile App Demo(Dynamsoft Barcode Reader" +
						" SDK v6.2)\n\n© 2018 Dynamsoft. All rights reserved. " +
						"\n\nIntegrate Barcode Reader Functionality into Your own Mobile App? " +
						"\n\nClick 'Overview' button for further info.\n\n");
				builder.setPositiveButton("Overview", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse("https://www.dynamsoft.com/Products/barcode-scanner-sdk-android.aspx");
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
					}
				});
				builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.show();
			}
		});
		mCache = DBRCache.get(this);
		mCache.put("linear", "1");
		mCache.put("qrcode", "1");
		mCache.put("pdf417", "1");
		mCache.put("matrix", "1");

		cameraView.addCameraListener(new CameraListener() {
			@Override
			public void onCameraOpened(CameraOptions options) {
				super.onCameraOpened(options);
				isCameraOpen = true;
			}
		});
		cameraView.addFrameProcessor(new FrameProcessor() {
			@SuppressLint("NewApi")
			@Override
			public void process(@NonNull Frame frame) {
				try {
					if (isDetected && isCameraOpen) {
						isDetected = false;
						YuvImage yuvImage = new YuvImage(frame.getData(), ImageFormat.NV21,
								frame.getSize().getWidth(), frame.getSize().getHeight(), null);
						int hgt = frame.getSize().getHeight();
						int wid = frame.getSize().getWidth();
						int[] stride = yuvImage.getStrides();
						result = reader.decodeBuffer(yuvImage.getYuvData(), wid, hgt, stride[0], EnumImagePixelFormat.IPF_NV21, name);
						Log.d("barcode result", "result" + result);
						if (result != null && result.length > 0) {
							isDetected = false;
							Log.d("barcode result", "process: " + result);
							Message message = handler.obtainMessage();
							message.obj = result[0];
							message.what = 0;
							handler.sendMessage(message);
						} else {
							isDetected = true;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			Intent intent = new Intent(MainActivity.this, SettingActivity.class);
			intent.putExtra("type", barcodeType);
			startActivityForResult(intent, 0);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			reader = new BarcodeReader(getString(R.string.dbr_license));
			JSONObject object = new JSONObject("{\n" +
					"  \"ImageParameters\": {\n" +
					"    \"Name\": \"linear\",\n" +
					"    \"BarcodeFormatIds\": [],\n" +
					"    \"DeblurLevel\": 9,\n" +
					"    \"AntiDamageLevel\": 9,\n" +
					"    \"TextFilterMode\": \"Enable\"\n" +
					"  }\n" +
					"}");
			JSONArray jsonArray = object.getJSONObject("ImageParameters").getJSONArray("BarcodeFormatIds");
			if (mCache.getAsString("linear").equals("1")) {
				jsonArray.put("OneD");
			}
			if (mCache.getAsString("qrcode").equals("1")) {
				jsonArray.put("QR_CODE");
			}
			if (mCache.getAsString("pdf417").equals("1")) {
				jsonArray.put("PDF417");
			}
			if (mCache.getAsString("matrix").equals("1")) {
				jsonArray.put("DATAMATRIX");
			}
			Log.d("code type", "type : " + object.toString());
			reader.appendParameterTemplate(object.toString());
			name = "linear";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		cameraView.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		cameraView.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cameraView.destroy();
	}

	@OnClick(R.id.tv_flash)
	public void onFlashClick() {
		if (isFlashOn) {
			isFlashOn = false;
			cameraView.setFlash(Flash.OFF);
		} else {
			isFlashOn = true;
			cameraView.setFlash(Flash.TORCH);
		}
	}
}

