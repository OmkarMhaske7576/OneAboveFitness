package com.one.above.fitness.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.one.above.fitness.R;
import com.one.above.fitness.service.BluetoothService;
import com.one.above.fitness.utility.Utility;

public class ScanQRActivity extends AppCompatActivity {

    AppCompatButton scanQR;
    Context context;
    ImageButton backBtn;
    BluetoothService bluetoothService;
    String TAG = "ScanQRActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_scan_qr);
            scanQR = findViewById(R.id.scanQR);
            backBtn = findViewById(R.id.backBtn);
            context = getApplicationContext();

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            scanQR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Utility.isBlutoothEnabled) {
                        Utility.showToast(context, "Please turn on Bluetooth !!");
                        return;
                    }

                    IntentIntegrator intentIntegrator = new IntentIntegrator(ScanQRActivity.this);
                    intentIntegrator.setPrompt("Scan a barcode or QR Code");
                    intentIntegrator.setOrientationLocked(true);
                    intentIntegrator.setCameraId(1);
                    intentIntegrator.initiateScan();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Utility.showToast(context, "Cancelled !!");
            } else {
                // QR format memberId@@branchNo@@date@@time
                String barcodeData = intentResult.getContents();
                Log.d(TAG, "barcode data  >>> " + barcodeData);
                new AsyncCall().execute(barcodeData.split("@@")[0]);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void saveAttendance(String memberNo) {
        Log.d("memberNo", memberNo);
        if (memberNo.trim().length() > 0) {
            bluetoothService = new BluetoothService(ScanQRActivity.this);
            bluetoothService.startBluetoothService("ON", memberNo);
        }
    }

    private class AsyncCall extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            saveAttendance(strings[0]);
            return null;
        }
    }

}