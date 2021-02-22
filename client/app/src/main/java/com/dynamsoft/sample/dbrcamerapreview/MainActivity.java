package com.dynamsoft.sample.dbrcamerapreview;

import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.EnumBarcodeFormat;
import com.dynamsoft.dbr.EnumBarcodeFormat_2;
import com.dynamsoft.dbr.EnumConflictMode;
import com.dynamsoft.dbr.EnumIntermediateResultType;
import com.dynamsoft.dbr.PublicRuntimeSettings;
import com.dynamsoft.sample.dbrcamerapreview.util.DBRCache;

import static com.dynamsoft.sample.dbrcamerapreview.util.CameraConstants.CAMERA_BACK;

public class MainActivity extends AppCompatActivity {
    private BarcodeReader mbarcodeReader;
    private DBRCache mCache;
    private boolean isTorchOn;
    private Camera2BasicFragment cameraFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mbarcodeReader = new BarcodeReader("t0068MgAAACquj133Eq/dO8e++8vZnJoxnoBQt7war3uxkJrf6f7pbI12H1a2vxoCyEQvQvjcZH9UrfqDHfbYYFpcwVAvASA=");
//            Use license 2.0 init BarcodeReader
//            mbarcodeReader = new BarcodeReader();
//            DMLTSConnectionParameters parameters = new DMLTSConnectionParameters();
//            parameters.handshakeCode = "Put your handshake code here";
//            mbarcodeReader.initLicenseFromLTS(parameters, new DBRLTSLicenseVerificationListener() {
//                @Override
//                public void LTSLicenseVerificationCallback(boolean isSuccess, Exception error) {
//                    if (!isSuccess) {
//                        error.printStackTrace();
//                    }
//                }
//            });            
            //Best Coverage settings
            //mbarcodeReader.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"BestCoverage\",\"DeblurLevel\":9,\"ExpectedBarcodesCount\":512,\"ScaleDownThreshold\":100000,\"LocalizationModes\":[{\"Mode\":\"LM_CONNECTED_BLOCKS\"},{\"Mode\":\"LM_SCAN_DIRECTLY\"},{\"Mode\":\"LM_STATISTICS\"},{\"Mode\":\"LM_LINES\"},{\"Mode\":\"LM_STATISTICS_MARKS\"}],\"GrayscaleTransformationModes\":[{\"Mode\":\"GTM_ORIGINAL\"},{\"Mode\":\"GTM_INVERTED\"}]}}", EnumConflictMode.CM_OVERWRITE);
            //Best Speed settings
            //mbarcodeReader.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"BestSpeed\",\"DeblurLevel\":3,\"ExpectedBarcodesCount\":512,\"LocalizationModes\":[{\"Mode\":\"LM_SCAN_DIRECTLY\"}],\"TextFilterModes\":[{\"MinImageDimension\":262144,\"Mode\":\"TFM_GENERAL_CONTOUR\"}]}}", EnumConflictMode.CM_OVERWRITE);
            //Balance settings
            mbarcodeReader.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"Balance\",\"DeblurLevel\":5,\"ExpectedBarcodesCount\":512,\"LocalizationModes\":[{\"Mode\":\"LM_CONNECTED_BLOCKS\"},{\"Mode\":\"LM_SCAN_DIRECTLY\"}]}}", EnumConflictMode.CM_OVERWRITE);
            PublicRuntimeSettings settings = mbarcodeReader.getRuntimeSettings();
            settings.intermediateResultTypes = EnumIntermediateResultType.IRT_TYPED_BARCODE_ZONE;
            settings.barcodeFormatIds = EnumBarcodeFormat.BF_ONED | EnumBarcodeFormat.BF_DATAMATRIX | EnumBarcodeFormat.BF_QR_CODE | EnumBarcodeFormat.BF_PDF417;
            settings.barcodeFormatIds_2 = 0;
            mbarcodeReader.updateRuntimeSettings(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCache = DBRCache.get(this);
        mCache.put("linear", "1");
        mCache.put("qrcode", "1");
        mCache.put("pdf417", "1");
        mCache.put("matrix", "1");
        mCache.put("aztec", "0");
        mCache.put("databar", "0");
        mCache.put("patchcode", "0");
        mCache.put("maxicode", "0");
        mCache.put("microqr", "0");
        mCache.put("micropdf417", "0");
        mCache.put("gs1compositecode", "0");
        mCache.put("postalcode", "0");
        mCache.put("dotcode", "0");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        if (null == savedInstanceState) {
            cameraFragment = Camera2BasicFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, cameraFragment)
                    .commit();
        }
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
            // intent.putExtra("type", barcodeType);
            startActivityForResult(intent, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            int nBarcodeFormat = 0;
            int nBarcodeFormat_2 = 0;
            if (mCache.getAsString("linear").equals("1")) {
                nBarcodeFormat = nBarcodeFormat | EnumBarcodeFormat.BF_ONED;
            }
            if (mCache.getAsString("qrcode").equals("1")) {
                nBarcodeFormat = nBarcodeFormat | EnumBarcodeFormat.BF_QR_CODE;
            }
            if (mCache.getAsString("pdf417").equals("1")) {
                nBarcodeFormat = nBarcodeFormat | EnumBarcodeFormat.BF_PDF417;
            }
            if (mCache.getAsString("matrix").equals("1")) {
                nBarcodeFormat = nBarcodeFormat | EnumBarcodeFormat.BF_DATAMATRIX;
            }
            if (mCache.getAsString("aztec").equals("1")) {
                nBarcodeFormat = nBarcodeFormat | EnumBarcodeFormat.BF_AZTEC;
            }
            if (mCache.getAsString("databar").equals("1")) {
                nBarcodeFormat = nBarcodeFormat | EnumBarcodeFormat.BF_GS1_DATABAR;
            }
            if (mCache.getAsString("patchcode").equals("1")) {
                nBarcodeFormat = nBarcodeFormat | EnumBarcodeFormat.BF_PATCHCODE;
            }
            if (mCache.getAsString("maxicode").equals("1")) {
                nBarcodeFormat = nBarcodeFormat | EnumBarcodeFormat.BF_MAXICODE;
            }
            if (mCache.getAsString("microqr").equals("1")) {
                nBarcodeFormat = nBarcodeFormat | EnumBarcodeFormat.BF_MICRO_QR;
            }
            if (mCache.getAsString("micropdf417").equals("1")) {
                nBarcodeFormat = nBarcodeFormat | EnumBarcodeFormat.BF_MICRO_PDF417;
            }
            if (mCache.getAsString("gs1compositecode").equals("1")) {
                nBarcodeFormat = nBarcodeFormat | EnumBarcodeFormat.BF_GS1_COMPOSITE;
            }
            if (mCache.getAsString("postalcode").equals("1")) {
                nBarcodeFormat_2 = nBarcodeFormat_2 | EnumBarcodeFormat_2.BF2_POSTALCODE;
            }
            if (mCache.getAsString("dotcode").equals("1")) {
                nBarcodeFormat_2 = nBarcodeFormat_2 | EnumBarcodeFormat_2.BF2_DOTCODE;
            }

            PublicRuntimeSettings runtimeSettings = mbarcodeReader.getRuntimeSettings();
            runtimeSettings.barcodeFormatIds = nBarcodeFormat;
            runtimeSettings.barcodeFormatIds_2 = nBarcodeFormat_2;
            mbarcodeReader.updateRuntimeSettings(runtimeSettings);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BarcodeReader getMainBarcdoeReader() {
        return mbarcodeReader;
    }


    /**
     * Event listeners for main page buttons
     */

    public void onScan(View view) {
        // This method is the event listener for the scan button
        System.out.println("Scan - #7 https://gitlab.socs.uoguelph.ca/skaplan/cis3760/-/issues/7");

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
        startActivity(new Intent(MainActivity.this, PopUp.class));
    }

    // This method is the event listener for the flashlight button
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    public void onFlash(View view) {
//        // Create the CameraManger instance by getting the Camera Service
//        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
//
//        try {
//            // Get Camera Characteristics
//            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(CAMERA_BACK);
//
//            // If flash is enabled, switch on/off
//            boolean isAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
//            if (isAvailable) {
//                //cameraManager.setTorchMode(CAMERA_BACK, !isTorchOn);
//                isTorchOn = !this.isTorchOn;
//            }
//        } catch (CameraAccessException e) {
//            System.out.println(e.getMessage());
//        }
//    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onFlash(View view) {
        cameraFragment.onFlash(view);
    }

    public void onInventory(View view) {
        // This method is the event listener for the inventory button
    }
}
