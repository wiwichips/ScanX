package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.dynamsoft.barcode.BarcodeReader;
import com.dynamsoft.barcode.EnumBarcodeFormat;
import com.dynamsoft.barcode.EnumConflictMode;
import com.dynamsoft.barcode.EnumImagePixelFormat;
import com.dynamsoft.barcode.PublicRuntimeSettings;
import com.dynamsoft.barcode.TextResult;
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

	private TextView textView;

	private BarcodeReader reader;
	private TextResult[] result;
	private boolean isDetected = true;
	private int barcodeType = 0;
	private DBRCache mCache;
	private String name = "";
	private boolean isFlashOn = false;
	private boolean isCameraOpen = false;

	private boolean bUpateDrawBox = true;

	private int mOrientationDisplayOffset = 0;
	private ImageView  m_iv_prevew;
	private TextView   m_tFlasht;
	private QRCodeView mQRView;

	private Rect mFrameDetectRegion = new Rect(0,0,500,500);

	private Rect mViewDetectRegion = new Rect(0,0,800,800);
	private FrameLayout frameLayout;
	private int mSensorOffset;

	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			"android.permission.READ_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE" };

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 0:
					TextResult result = (TextResult) msg.obj;
					textView.setText(result.barcodeText);
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
							case EnumBarcodeFormat.BF_All:
								barcodeFormat = "all";
								break;
							case EnumBarcodeFormat.BF_OneD:
								barcodeFormat = "OneD";
								break;
							case EnumBarcodeFormat.BF_CODE_39:
								barcodeFormat = "CODE_39";
								break;
							case EnumBarcodeFormat.BF_CODE_128:
								barcodeFormat = "CODE_128";
								break;
							case EnumBarcodeFormat.BF_CODE_93:
								barcodeFormat = "CODE_93";
								break;
							case EnumBarcodeFormat.BF_CODABAR:
								barcodeFormat = "CODABAR";
								break;
							case EnumBarcodeFormat.BF_ITF:
								barcodeFormat = "ITF";
								break;
							case EnumBarcodeFormat.BF_EAN_13:
								barcodeFormat = "EAN_13";
								break;
							case EnumBarcodeFormat.BF_EAN_8:
								barcodeFormat = "EAN_8";
								break;
							case EnumBarcodeFormat.BF_UPC_A:
								barcodeFormat = "UPC_A";
								break;
							case EnumBarcodeFormat.BF_UPC_E:
								barcodeFormat = "UPC_E";
								break;
							case EnumBarcodeFormat.BF_INDUSTRIAL_25:
								barcodeFormat = "INDUSTRIAL_25";
								break;
							case EnumBarcodeFormat.BF_PDF417:
								barcodeFormat = "PDF417";
								break;
							case EnumBarcodeFormat.BF_QR_CODE:
								barcodeFormat = "QR_CODE";
								break;
							case EnumBarcodeFormat.BF_DATAMATRIX:
								barcodeFormat = "DATAMATAIX";
								break;
							case EnumBarcodeFormat.BF_AZTEC:
								barcodeFormat="AZTEC";
								break;
							default:
								break;
						}
						builder.setMessage("Type : " + barcodeFormat + "\n\nResult : " + result.barcodeText + "\n\n RegionPoints:"
								+result.localizationResult.resultPoints[0].x
								+","+result.localizationResult.resultPoints[0].y
								+"  "
								+result.localizationResult.resultPoints[1].x
								+","+result.localizationResult.resultPoints[1].y
								+"  "
								+result.localizationResult.resultPoints[2].x
								+","+result.localizationResult.resultPoints[2].y
								+"  "
								+result.localizationResult.resultPoints[3].x
								+","+result.localizationResult.resultPoints[3].y
						);
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
					}
					break;
				case 0x001:
				{
					int nViewW = cameraView.getWidth();
					int nViewH = cameraView.getHeight();

					//TODO:make the view region show in the center of camera view. You can change the region's position if you want.
					mViewDetectRegion=new Rect((nViewW-mViewDetectRegion.width())/2,(nViewH-mViewDetectRegion.height())/2,
							(nViewW-mViewDetectRegion.width())/2+mViewDetectRegion.width(),(nViewH-mViewDetectRegion.height())/2+mViewDetectRegion.height());
					//End_TODO

					Rect frameSize = (Rect) msg.obj;
					if(mViewDetectRegion!=null){
                        Rect frameRegion = ConvertViewRegionToVideoFrameRegion(mViewDetectRegion,frameSize);
						setFrameRegion(frameRegion.left,frameRegion.top,frameRegion.right,frameRegion.bottom);
                    }
					Rect viewRect = ConvertFrameRegionToViewRegion(mFrameDetectRegion, frameSize);
                    //}
					int boxLeft  =viewRect.left + (frameLayout.getWidth()-nViewW)/2;
					int boxTop   = viewRect.top + (frameLayout.getHeight()-nViewH)/2;
					int boxRight = Math.min((viewRect.width()+boxLeft),frameLayout.getWidth()-1);
					int boxBottom = Math.min((viewRect.height()+boxTop),frameLayout.getHeight()-1);
					if(boxLeft>=0 && boxTop>=0 )
						mQRView.reSetboxview(boxLeft, boxTop, boxRight-boxLeft, boxBottom-boxTop);
				}
					break;
				case 0x02:
					textView.setText((String)msg.obj);
					break;
				default:
					break;
			}
		}
	};


	void setFrameRegion(int left,int top ,int right,int bottom){
        mFrameDetectRegion.left = left;
        mFrameDetectRegion.right = right;
        mFrameDetectRegion.top = top;
        mFrameDetectRegion.bottom = bottom;
        try {
        	PublicRuntimeSettings runtimeSettings = reader.getRuntimeSettings();
			String tempTemplateJsonWithRegion =
					"{" +
							"\"Version\": \"2.0\"," +
							"\"ImageParameter\": " +
							"{" +
							"\"Name\": \"" + "All_DEFAULT_WITHREGION" + "\"," +
							"\"RegionDefinitionNameArray\": [\"Region\"]" +
							"}," +
							"\"RegionDefinitionArray\": " +
							"[" +
							"{" +
							"\"Name\": \"Region\"," +
							"\"MeasuredByPercentage\": false" + "," +
							"\"Left\":" + mFrameDetectRegion.left  + "," +
							"\"Top\":"  + mFrameDetectRegion.top + "," +
							"\"Right\":"  + mFrameDetectRegion.right + "," +
							"\"Bottom\":" + mFrameDetectRegion.bottom + "" +
							"}" +
							"]}";

			JSONObject object = new JSONObject(tempTemplateJsonWithRegion);
			String strContent = object.toString();
			if(reader!=null) {
				reader.initRuntimeSettingsWithString(strContent, EnumConflictMode.ECM_Overwrite);
				reader.updateRuntimeSettings(runtimeSettings);
			}
			name = "linear";

           // initBarcodeReader();
        }catch (Exception e){
            e.printStackTrace();
        }
	}


	void initBarcodeReader() throws Exception {

		String tempTemplateJsonWithRegion =
				"{" +
						"\"Version\": \"2.0\"," +
						"\"ImageParameter\": " +
							"{" +
							"\"Name\": \"" + "All_DEFAULT_WITHREGION" + "\"," +
							"\"BarcodeFormatIds\": [\"OneD\"]," +
							"\"RegionPredetectionMode\": \"Disable\"," +
							"\"RegionDefinitionNameArray\": [\"Region\"]" +
						"}," +
						"\"RegionDefinitionArray\": " +
						"[" +
							"{" +
								"\"Name\": \"Region\"," +
								"\"MeasuredByPercentage\": false" + "," +
								"\"Left\":" + mFrameDetectRegion.left  + "," +
								"\"Top\":"  + mFrameDetectRegion.top + "," +
								"\"Right\":"  + mFrameDetectRegion.right + "," +
								"\"Bottom\":" + mFrameDetectRegion.bottom + "" +
							"}" +
						"]}";

		JSONObject object = new JSONObject(tempTemplateJsonWithRegion);
		JSONArray jsonArray = object.getJSONObject("ImageParameter").getJSONArray("BarcodeFormatIds");
		jsonArray.put("QR_CODE");
		jsonArray.put("PDF417");
		jsonArray.put("DATAMATRIX");
		Log.d("code type", "type : " + object.toString());
		String strContent = object.toString();
		if(reader!=null) {
            reader.initRuntimeSettingsWithString(strContent, EnumConflictMode.ECM_Overwrite);
        }
		name = "linear";
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		try {
			reader = new BarcodeReader("t0068MgAAAI8iSA2hQZp0yylHuenu/rOZ4I89KcP51PybzIWUStBDLcU+4OfXlAet2aEfGnEDKyrVGf/OzIFKg6Io+cBp7TI=");
			initBarcodeReader();

		} catch (Exception e) {
			e.printStackTrace();
		}

		m_iv_prevew = findViewById(R.id.iv_prevew);
		m_tFlasht = findViewById(R.id.tv_flash);
		mQRView = findViewById(R.id.qr_view);
		frameLayout = findViewById(R.id.fl_main_content);
		textView = findViewById(R.id.textView);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage("Dynamsoft Barcode Reader Mobile App Demo(Dynamsoft Barcode Reader" +
						" SDK v6.4)\n\n© 2018 Dynamsoft. All rights reserved. " +
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
		mCache.put("aztec", "0");

		cameraView.addCameraListener(new CameraListener() {
			@Override
			public void onCameraOpened(CameraOptions options) {

				super.onCameraOpened(options);
				isCameraOpen = true;
			}
			@Override
			public void onOrientationChanged(int orientation) {
				bUpateDrawBox = true;
				super.onOrientationChanged(orientation);
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

						int width = yuvImage.getWidth();
						int height = yuvImage.getHeight();
						int[] strides = yuvImage.getStrides();

							Log.d("", "begin:");
							reader.decodeBuffer(yuvImage.getYuvData(), width, height, strides[0], EnumImagePixelFormat.IPF_NV21, "");
							Log.d("", "finish:");
							result = reader.getAllTextResults();
							if (result != null && result.length > 0) {
								Message message = handler.obtainMessage();
								message.what = 0x02;
								String str = "";
								for(int i = 0; i<result.length; i++){
									if(i==0)
										str = result[i].barcodeText;
									else
										str = str + "\n\n" + result[i].barcodeText;
								}
								message.obj = str;
								handler.sendMessage(message);
							}
							else{
								Message message = handler.obtainMessage();
								message.what = 0x02;
								message.obj = "";
								handler.sendMessage(message);
							}
							if(bUpateDrawBox){
								bUpateDrawBox = false;
								Message message = handler.obtainMessage();
								Rect imageRect = new Rect(0,0,width,height);
								message.obj = imageRect;
								message.what = 0x001;
								handler.sendMessage(message);
							}
							isDetected = true;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});


		android.hardware.Camera.CameraInfo info =	new android.hardware.Camera.CameraInfo();
		for (int i = 0; i < Camera.getNumberOfCameras(); i++)
		{
			Camera.getCameraInfo(i, info);
			if (info.facing ==  Camera.CameraInfo.CAMERA_FACING_BACK) {
				mSensorOffset = info.orientation;
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		requestPermissions();
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
			int nBarcodeFormat =0;
			if (mCache.getAsString("linear").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_OneD;
			}
			if (mCache.getAsString("qrcode").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_QR_CODE;
			}
			if (mCache.getAsString("pdf417").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_PDF417;
			}
			if (mCache.getAsString("matrix").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_DATAMATRIX;
			}
			if (mCache.getAsString("aztec").equals("1")) {
				nBarcodeFormat = nBarcodeFormat|EnumBarcodeFormat.BF_AZTEC;
			}

			PublicRuntimeSettings runtimeSettings =  reader.getRuntimeSettings();
			runtimeSettings.mBarcodeFormatIds = nBarcodeFormat;
			reader.updateRuntimeSettings(runtimeSettings);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		cameraView.start();


		int mDisplayOffset=0;
		Display display = ((WindowManager)  MainActivity.this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		switch (display.getRotation()) {
			case Surface.ROTATION_0: mDisplayOffset = 0; break;
			case Surface.ROTATION_90: mDisplayOffset = 90; break;
			case Surface.ROTATION_180: mDisplayOffset = 180; break;
			case Surface.ROTATION_270: mDisplayOffset = 270; break;
			default: mDisplayOffset = 0; break;
		}
//		if (mFacing == Facing.FRONT) {
//			// Here we had ((mSensorOffset - mDisplayOffset) + 360 + 180) % 360
//			// And it seemed to give the same results for various combinations, but not for all (e.g. 0 - 270).
//			return (360 - ((mSensorOffset + mDisplayOffset) % 360)) % 360;
//		} else
			{
			mOrientationDisplayOffset =  (mSensorOffset - mDisplayOffset + 360) % 360;
		}
		bUpateDrawBox = true;

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
			m_tFlasht.setText("Flash ON");
		} else {
			isFlashOn = true;
			cameraView.setFlash(Flash.TORCH);
			m_tFlasht.setText("Flash OFF");
		}
	}
	private void requestPermissions(){
		if (Build.VERSION.SDK_INT>22){
			try {
				if (ContextCompat.checkSelfPermission(MainActivity.this,"android.permission.WRITE_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			// do nothing
		}
	}

	private Rect ConvertViewRegionToVideoFrameRegion(Rect viewRegion, Rect frameSize){
		Rect convertRegion ;
		final int rotateDegree = mOrientationDisplayOffset;
		if(rotateDegree == 90){
			convertRegion =  boundaryRotate(new Point(cameraView.getWidth()/2,cameraView.getHeight()/2),viewRegion,true);
		}else if(rotateDegree == 180){
			convertRegion = boundaryRotate180(new Point(cameraView.getWidth()/2,cameraView.getHeight()/2),viewRegion);
		}else if(mOrientationDisplayOffset == 270){
			convertRegion =  boundaryRotate(new Point(cameraView.getWidth()/2,cameraView.getHeight()/2),viewRegion,false);
		}else{
			convertRegion = viewRegion;
		}

		int nViewW = cameraView.getWidth();
		int nViewH = cameraView.getHeight();
		float fScaleH = (mOrientationDisplayOffset%180 ==0)? 1.0f*frameSize.height()/nViewH:1.0f*frameSize.height()/nViewW;
		float fScaleW = (mOrientationDisplayOffset%180 ==0)?1.0f *frameSize.width()/nViewW:1.0f*frameSize.width()/nViewH;
		float fScale   = (fScaleH>fScaleW)?fScaleW:fScaleH;
		int boxLeft  =(int)(convertRegion.left*fScale);
		int boxTop   = (int)(convertRegion.top*fScale);
		int boxWidth = (int)(convertRegion.width()*fScale);
		int boxHeight = (int)(convertRegion.height()*fScale);
		Rect frameRegion = new Rect(boxLeft,boxTop,boxWidth+boxLeft,boxTop+boxHeight);
		return frameRegion;
	}

    private Rect ConvertFrameRegionToViewRegion(Rect frameRegion, Rect frameSize){

		Rect imageRect = frameSize;
		Rect roateRect =frameRegion;
		int rotateDegree = mOrientationDisplayOffset;

		if(rotateDegree == 90){
			roateRect =  boundaryRotate(new Point(imageRect.width()/2,imageRect.height()/2),frameRegion,false);
		}else if(rotateDegree == 180){
			roateRect = boundaryRotate180(new Point(imageRect.width()/2,imageRect.height()/2),frameRegion);
		}else if(mOrientationDisplayOffset == 270){
			roateRect =  boundaryRotate(new Point(imageRect.width()/2,imageRect.height()/2),frameRegion,true);
		}

		int nViewW = cameraView.getWidth();
		int nViewH = cameraView.getHeight();

		float fScaleH = (mOrientationDisplayOffset%180 ==0)? 1.0f*nViewH /imageRect.height():1.0f*nViewH /imageRect.width();
		float fScaleW = (mOrientationDisplayOffset%180 ==0)?1.0f *nViewW/imageRect.width():1.0f*nViewW /imageRect.height();
		float fScale   = (fScaleH>fScaleW)?fScaleW:fScaleH;

		int boxLeft  =(int)(roateRect.left*fScale);
		int boxTop   = (int)(roateRect.top*fScale);
		int boxWidth = (int)(roateRect.width()*fScale);
		int boxHeight = (int)(roateRect.height()*fScale);
		Rect viewRegion = new Rect(boxLeft,boxTop,boxWidth+boxLeft,boxTop+boxHeight);
		return viewRegion;

    }
	private Rect boundaryRotate(Point orgPt,Rect rect , boolean bLeft ){
		float orgx = orgPt.x;
		float orgy = orgPt.y;

		float rotatex =orgy;
		float rotatey = orgx;
		float[]currentBoundary = new float [8];
		currentBoundary[0] = rect.left;
		currentBoundary[1] = rect.top;

		currentBoundary[2] = rect.right;
		currentBoundary[3] = rect.top;

		currentBoundary[4] = rect.right;
		currentBoundary[5] = rect.bottom;

		currentBoundary[6] = rect.left;
		currentBoundary[7] = rect.bottom;

		int[] rotateBoundary = new int [8];
		if(bLeft){
			rotateBoundary[6] = (int)((currentBoundary[0]-orgx)*Math.cos(Math.PI*0.5f)+(currentBoundary[1] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
			rotateBoundary[7] = (int)(-(currentBoundary[0]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[1] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);

			rotateBoundary[0] = (int)((currentBoundary[2]-orgx)*Math.cos(Math.PI*0.5f)+(currentBoundary[3] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
			rotateBoundary[1] = (int)(-(currentBoundary[2]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[3] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);

			rotateBoundary[2] = (int)((currentBoundary[4]-orgx)*Math.cos(Math.PI*0.5f)+(currentBoundary[5] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
			rotateBoundary[3] = (int)(-(currentBoundary[4]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[5] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);

			rotateBoundary[4] = (int)((currentBoundary[6]-orgx)*Math.cos(Math.PI*0.5f)+(currentBoundary[7] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
			rotateBoundary[5] = (int)(-(currentBoundary[6]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[7] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);
		}else
		{
			rotateBoundary[2] = (int)((currentBoundary[0]-orgx)*Math.cos(Math.PI*0.5f)-(currentBoundary[1] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
			rotateBoundary[3] = (int)(((currentBoundary[0]-orgx)*Math.sin(Math.PI*0.5f))+((currentBoundary[1] - orgy)*Math.cos(Math.PI*0.5f))+rotatey);

			rotateBoundary[4] = (int)((currentBoundary[2]-orgx)*Math.cos(Math.PI*0.5f)-(currentBoundary[3] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
			rotateBoundary[5] = (int)((currentBoundary[2]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[3] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);

			rotateBoundary[6] = (int)((currentBoundary[4]-orgx)*Math.cos(Math.PI*0.5f)-(currentBoundary[5] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
			rotateBoundary[7] = (int)((currentBoundary[4]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[5] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);

			rotateBoundary[0] = (int)((currentBoundary[6]-orgx)*Math.cos(Math.PI*0.5f)-(currentBoundary[7] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
			rotateBoundary[1] = (int)((currentBoundary[6]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[7] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);
		}

		Rect rotateRect  = new Rect(rotateBoundary[0],rotateBoundary[1],rotateBoundary[2],rotateBoundary[5]);

		return rotateRect;
	}

	private Rect boundaryRotate180(Point orgPt,Rect rect ){
		float orgx = orgPt.x;
		float orgy = orgPt.y;

		float rotatex =orgy;
		float rotatey = orgx;
		float[]currentBoundary = new float [8];
		currentBoundary[0] = rect.left;
		currentBoundary[1] = rect.top;

		currentBoundary[2] = rect.right;
		currentBoundary[3] = rect.top;

		currentBoundary[4] = rect.right;
		currentBoundary[5] = rect.bottom;

		currentBoundary[6] = rect.left;
		currentBoundary[7] = rect.bottom;
		int[] rotateBoundary = new int [8];
		rotateBoundary[4] =(int)(orgx - (currentBoundary[0]-orgx));
		rotateBoundary[5] =(int)(orgy - (currentBoundary[1]-orgy));

		rotateBoundary[6] =(int)(orgx - (currentBoundary[2]-orgx));
		rotateBoundary[7] =(int)(orgy - (currentBoundary[3]-orgy));

		rotateBoundary[0] =(int)(orgx - (currentBoundary[4]-orgx));
		rotateBoundary[1] =(int)(orgy - (currentBoundary[5]-orgy));

		rotateBoundary[2] =(int)(orgx - (currentBoundary[6]-orgx));
		rotateBoundary[3] =(int)(orgy - (currentBoundary[7]-orgy));
		Rect rotateRect  = new Rect(rotateBoundary[0],rotateBoundary[1],rotateBoundary[2],rotateBoundary[5]);

		return rotateRect;
	}

}

