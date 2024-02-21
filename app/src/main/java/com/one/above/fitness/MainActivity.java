package com.one.above.fitness;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.one.above.fitness.activities.LoginSuccessActivity;
import com.one.above.fitness.activities.SettingContainerActivity;
import com.one.above.fitness.database.SQLiteDatabaseHandler;
import com.one.above.fitness.database.SharedPreference;
import com.one.above.fitness.helper.Connection;
import com.one.above.fitness.helper.MyKeyboard;
import com.one.above.fitness.pojo.BranchData;
import com.one.above.fitness.pojo.FaceData;
import com.one.above.fitness.pojo.FaceImgData;
import com.one.above.fitness.service.BluetoothService;
import com.one.above.fitness.service.WebService;
import com.one.above.fitness.utility.Utility;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    TextView reco_name, preview_info, textAbove_preview, bluetoothTxt;
    ProcessCameraProvider cameraProvider;
    FaceDetector detector;
    CameraSelector cameraSelector;
    Interpreter tfLite;
    int[] intValues;
    public BluetoothDevice mBTDevice;
    String TAG = "MainActivity";
    BluetoothService bluetoothService = null;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    boolean start = true, flipX = false;
    String userName = "";
    int inputSize = 112;  //Input size for model
    boolean isModelQuantized = false;
    float[][] embeedings;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE = 192; //Output size of model
    Button setting;
    String modelFile = "mobile_face_net.tflite";
    int cam_face = CameraSelector.LENS_FACING_FRONT;
    PreviewView previewView;
    ImageView face_preview;
    String username = "";
    String deviceName;
    public static List<FaceData> faceDataList = new ArrayList<>();
    SQLiteDatabaseHandler db;
    public static Handler userTimeHandler = null;
    public static boolean isUserFound = false;
    Button homeBtn;
    Context context;
    EditText editText;
    String[] data = {"FON", ""};
    public BluetoothAdapter mBluetoothAdapter;
    public static MainActivity mainActivity;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_PRIVILEGED,};
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_PRIVILEGED};

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {

            db = new SQLiteDatabaseHandler(this);
            userName = "";
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            context = getApplicationContext();
            mainActivity = this;

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            deviceName = SharedPreference.getBluetoothDeviceName(context);

            MyKeyboard keyboard = (MyKeyboard) findViewById(R.id.keyboard);
            editText = (EditText) findViewById(R.id.editText);
            editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
            editText.setTextIsSelectable(true);
            editText.setOnTouchListener(otl);

            InputConnection ic = editText.onCreateInputConnection(new EditorInfo());
            keyboard.setInputConnection(ic);

            face_preview = findViewById(R.id.imageView);
            homeBtn = findViewById(R.id.homeBtn);
            setting = findViewById(R.id.setting);

            previewView = findViewById(R.id.previewView);

            reco_name = findViewById(R.id.textView);

            preview_info = findViewById(R.id.textView2);

            textAbove_preview = findViewById(R.id.textAbovePreview);
            bluetoothTxt = findViewById(R.id.bluetoothTxt);

            textAbove_preview.setText("Face Preview: ");

            bluetoothTxt.setText("Bluetooth : Off || Device name : " + deviceName);
            if (mBluetoothAdapter.isEnabled()) {
                Utility.isBlutoothEnabled = true;
                bluetoothTxt.setTextColor(context.getColor(R.color.SuccessColor));
                bluetoothTxt.setText(bluetoothTxt.getText().toString().replaceAll("Off", "On"));
            }

            preview_info.setText("1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.");

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    try {
                        Log.d("Editable", s.toString());
                        if (!Utility.isBlutoothEnabled && s.toString().length() == 3) {
                            Utility.showToast(context, "Please turn on bluetooth !!");
                            return;
                        }
                        if (s.toString().length() == 3) {
                            data[0] = "FON";
                            data[1] = "";
                            new AsyncCallForBluetooth().execute(data);
                        }
                        if (s.toString().length() >= 3) {
                            username = s.toString().trim();
                            refreshCamera();
                        } else {
                            start = false;
                            if (cameraProvider != null) {
                                cameraProvider.unbindAll();
                                previewView.setVisibility(View.GONE);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            getPermissionsForBluetooth();
            checkBTPermissions();
            //enableDisableBT();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);

            tfLite = new Interpreter(Utility.loadModelFile(MainActivity.this, modelFile));

            FaceDetectorOptions highAccuracyOpts = new FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).build();
            detector = FaceDetection.getClient(highAccuracyOpts);

            setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.vibrateWithAnim(v);
                    startActivity(new Intent(getApplicationContext(), SettingContainerActivity.class));
                }
            });

            cameraBind();
            generateQR();

            homeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Utility.vibrateWithAnim(v);
                    editText.setText("");
                    start = false;
                    cameraProvider.unbindAll();
                   /* if (BluetoothCommunication.mBluetoothConnection != null)
                        BluetoothCommunication.mBluetoothConnection.write("TEST".getBytes(Charset.defaultCharset()));
                   */
                }
            });

/*
            camera_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (cam_face == CameraSelector.LENS_FACING_BACK) {
                        cam_face = CameraSelector.LENS_FACING_FRONT;
                        flipX = true;
                    } else {
                        cam_face = CameraSelector.LENS_FACING_BACK;
                        flipX = false;
                    }
                    cameraProvider.unbindAll();
                    cameraBind();
                }
            });
*/
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private void cameraBind() {

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        previewView = findViewById(R.id.previewView);

        cameraProviderFuture.addListener(() -> {

            try {

                cameraProvider = cameraProviderFuture.get();

                bindPreview(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {

            }

        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        try {

            Preview preview = new Preview.Builder()

                    .build();

            cameraSelector = new CameraSelector.Builder().requireLensFacing(cam_face).build();

            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(640, 480)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
                    .build();

            Executor executor = Executors.newSingleThreadExecutor();

            imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
                @Override
                public void analyze(@NonNull ImageProxy imageProxy) {

                    try {
                        Thread.sleep(0);  //Camera preview refreshed every 10 millisec(adjust as required)
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    InputImage image = null;

                    @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = imageProxy.getImage();

                    if (mediaImage != null) {

                        image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                    }

                    Task<List<Face>> result = detector.process(image).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                                @Override
                                public void onSuccess(List<Face> faces) {

                                    if (faces.size() != 0) {

                                        Face face = faces.get(0); //Get first face from detected faces

                                        Bitmap frame_bmp = Utility.toBitmap(mediaImage);

                                        int rot = imageProxy.getImageInfo().getRotationDegrees();

                                        Bitmap frame_bmp1 = Utility.rotateBitmap(frame_bmp, rot, false, false);

                                        RectF boundingBox = new RectF(face.getBoundingBox());

                                        Bitmap cropped_face = Utility.getCropBitmapByCPU(frame_bmp1, boundingBox);

                                        if (flipX)
                                            cropped_face = Utility.rotateBitmap(cropped_face, 0, flipX, false);
                                        Bitmap scaled = Utility.getResizedBitmap(cropped_face, 112, 112);

                                        if (start) recognizeImage(scaled, username);

                                    }
                                }
                            })

                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            })

                            .addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                                @Override
                                public void onComplete(@NonNull Task<List<Face>> task) {

                                    imageProxy.close(); //v.important to acquire next frame for analysis

                                }
                            });

                }
            });

            cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);

        } catch (Exception e) {

        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            generateQR();
            String dname = SharedPreference.getBluetoothDeviceName(context);
            bluetoothTxt.setText(bluetoothTxt.getText().toString().replaceAll(deviceName, dname));
            deviceName = dname;
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, Utility.MY_CAMERA_REQUEST_CODE);
            }
            if (!Connection.isOnline(context))
                Utility.showToast(context, "Please turn on internet connection !!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateQR() {

        BranchData branchData = SharedPreference.getBranchDetails(getApplicationContext());

        if (branchData != null) {

            if (branchData != null) {
                String Qr = branchData.getBranchno() + "##" + Utility.getDateInDDMMYY();
                MultiFormatWriter mWriter = new MultiFormatWriter();
                try {
                    ImageView imageCode = findViewById(R.id.qr);
                    BitMatrix mMatrix = mWriter.encode(Qr, BarcodeFormat.QR_CODE, 400, 400);
                    BarcodeEncoder mEncoder = new BarcodeEncoder();
                    Bitmap mBitmap = mEncoder.createBitmap(mMatrix);//creating bitmap of code
                    imageCode.setImageBitmap(mBitmap);//Setting generated QR code to imageView
                    imageCode.setVisibility(View.VISIBLE);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public void recognizeImage(final Bitmap bitmap, String userName) {

        face_preview.setImageBitmap(bitmap);

        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {

            for (int j = 0; j < inputSize; ++j) {

                int pixelValue = intValues[i * inputSize + j];

                if (isModelQuantized) {

                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));

                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));

                    imgData.put((byte) (pixelValue & 0xFF));

                } else { // Float model

                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                }
            }
        }

        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();

        embeedings = new float[1][OUTPUT_SIZE];

        outputMap.put(0, embeedings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model

        float distance_local = Float.MAX_VALUE;

        final List<Pair<String, Float>> nearest = findNearest(embeedings[0], userName);//Find 2 closest matching face

        Log.d("nearest", nearest.toString());

        if (nearest.size() > 0 && nearest.get(0) != null) {

            final String name = nearest.get(0).first; //get name and distance of closest matching face

            distance_local = nearest.get(0).second;
            Utility.FACE_DISTANCE = SharedPreference.getHyperParameter(context);

            if (distance_local < Utility.FACE_DISTANCE) {

                if (Utility.currentLoginUser != null) {
                    Log.d("Utility.currentLoginUser >>", Utility.currentLoginUser.toString());
                    checkValidUser(Utility.currentLoginUser.getMemberID(), Utility.currentLoginUser.getMemberID(), getWindow().getDecorView().getRootView());
                }

            }

        }
    }

    private List<Pair<String, Float>> findNearest(float[] emb, String userStr) {

        Log.e("emb", emb + "");
        List<Pair<String, Float>> neighbour_list = new ArrayList<Pair<String, Float>>();

        faceDataList = db.getFaceByName(userStr);
        Object extraObject = null;

        try {

            Pair<String, Float> ret = null; //to get closest match

            Pair<String, Float> prev_ret = null; //to get second closest match

            for (FaceData faceData : faceDataList) {

                final String memberID = faceData.getMemberID();
                LinkedList<FaceImgData> faceImgDataList = db.getImageDataById(memberID);
                for (FaceImgData faceImgData : faceImgDataList) {
                    if (faceImgData.getIsSelected() == 1) {
                        extraObject = faceImgData.getExtra();
                        Log.e("Image Data", faceImgData.toString());
                        break;
                    }
                }

                Log.d(">>>knownEmbBefore", new Gson().toJson(extraObject));
                final float[] knownEmb = (Utility.convertStringTo2DArray(new Gson().toJson(extraObject)))[0];
                Log.d(">>knownEmbAfter >>>", knownEmb + "");

                float distance = 0;

                for (int i = 0; i < emb.length; i++) {

                    float diff = emb[i] - knownEmb[i];

                    distance += diff * diff;

                }

                distance = (float) Math.sqrt(distance);

                if (ret == null || distance < ret.second) {

                    prev_ret = ret;

                    ret = new Pair<>(memberID, distance);

                    Utility.currentLoginUser = faceData;

                }

                if (prev_ret == null) prev_ret = ret;

                neighbour_list.add(ret);

                neighbour_list.add(prev_ret);

            }

        } catch (Exception e) {

        }
        return neighbour_list;
    }

    public void refreshCamera() {
        isUserFound = false;
        isUserFound();
        username = editText.getText().toString().trim();
        if (username.length() >= 3) {
            start = true;
            previewView.setVisibility(View.VISIBLE);
            face_preview.setVisibility(View.VISIBLE);
            cameraProvider.unbindAll();
            cameraBind();

        }
    }

    public void openPopupWindow(final View view, JSONObject jsonObject) {

        try {

            String memberNo = jsonObject.getString("MemberNo");

            //   save attendance and call bluetooth service
            Log.d("memberNo", memberNo);
            if (memberNo.trim().length() > 0) {
                data[0] = "ON";
                data[1] = memberNo;
                new AsyncCallForBluetooth().execute(data);
            }

            isUserFound = true;

            int dialogTime = SharedPreference.getDialogTimer(getApplicationContext());

            start = false;

            TextView nameTxt, accTxt, membershipTxt;

            LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);

            View popupView = inflater.inflate(R.layout.show_details_layout, null);

            int width = LinearLayout.LayoutParams.MATCH_PARENT;

            int height = LinearLayout.LayoutParams.MATCH_PARENT;

            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

            Button cancel = popupView.findViewById(R.id.cancel);

            Button CancelBtn = popupView.findViewById(R.id.CancelBtn);

            TextView userName = popupView.findViewById(R.id.userName);

            ImageView userImg = popupView.findViewById(R.id.userImg);

            TextView planName = popupView.findViewById(R.id.planName);
            TextView endDate = popupView.findViewById(R.id.endDate);
            TextView ProgramName = popupView.findViewById(R.id.ProgramName);

            nameTxt = popupView.findViewById(R.id.nameTxt);
            accTxt = popupView.findViewById(R.id.accTxt);
            membershipTxt = popupView.findViewById(R.id.membershipTxt);


            Button successBtn = popupView.findViewById(R.id.successBtn);

            userName.setText(userName.getText().toString() + "\"" + jsonObject.getString("MemberName") + "\"");

            nameTxt.setText(jsonObject.getString("MemberName"));
            accTxt.setText(jsonObject.getString("Active"));
            membershipTxt.setText(jsonObject.getString("Membershipstatus"));

            userImg.setImageBitmap(Utility.currentLoginUser.getUserImage());

            JSONObject planDetails = WebService.getPlanDetails(jsonObject.getString("MemberNo"), jsonObject.getString("Branchno")).getJSONObject(0);
            if (planDetails.length() > 0) {
                planName.setText(planName.getText().toString() + planDetails.getString("PlanName"));
                endDate.setText(endDate.getText().toString() + planDetails.getString("EndDt"));
                ProgramName.setText(ProgramName.getText().toString() + planDetails.getString("ProgramName"));
            }

            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {

                    homeBtn.callOnClick();
                }
            });

            successBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {

                        Toast.makeText(getApplicationContext(), "Welcome " + Utility.currentLoginUser.getName(), Toast.LENGTH_LONG).show();

                        popupWindow.dismiss();

                        Intent intent = new Intent(getApplicationContext(), LoginSuccessActivity.class);

                        finish();

                        startActivity(intent);

                    } catch (Exception e) {
                    }
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cameraProvider.unbindAll();
                    popupWindow.dismiss();
                }
            });

            CancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {

                        Toast.makeText(getApplicationContext(), "By user " + Utility.currentLoginUser.getName(), Toast.LENGTH_LONG).show();

                        popupWindow.dismiss();

                        Intent intent = new Intent(getApplicationContext(), LoginSuccessActivity.class);

                        finish();

                        startActivity(intent);

                    } catch (Exception e) {

                    }
                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupWindow.dismiss();
                }
            }, dialogTime);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean checkValidUser(String userName, String password, View view) {

        try {

            JSONArray jsonArray = WebService.getLoginData(userName, password);

            if (jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                if (jsonObject != null && !jsonObject.getString("MemberNo").equalsIgnoreCase("No")) {
                    openPopupWindow(view, jsonObject);
                    start = false;
                }

               /* if (jsonObject.getString("Active").equalsIgnoreCase("No") || jsonObject.getString("Membershipstatus").equalsIgnoreCase("Expired")) {

                    start = false;
                    Toast.makeText(this, "your account is not active try to connect with branch !!!", Toast.LENGTH_LONG).show();
                    showErrorWindow(view, jsonObject);

                    return false;

                } else if (false) {


                } else {
                    openPopupWindow(view, jsonObject);
                }*/

            }

            Log.d(TAG, jsonArray.getJSONObject(0).getString("MemberName"));

        } catch (Exception e) {

            e.printStackTrace();
        }
        return true;
    }

    private void getPermissionsForBluetooth() {
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
        }
        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, 1);
        }
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.e(TAG, " >>> Discovering devices !!!" + isGpsEnabled);
        if (!isGpsEnabled) {
            Utility.showToast(context, "Please Enabled location permission to discover devices !!");
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
        }
    }

    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Utility.showToast(context, "Android version is not supported for bluetooth !!");
            Log.d("checkBTPermissions", "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    private View.OnTouchListener otl = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            return true; // the listener has consumed the event
        }
    };

    public void isUserFound() {
        int time = 15000;
        if (userTimeHandler == null) {
            userTimeHandler = new Handler();
        } else {
            userTimeHandler.removeCallbacksAndMessages(null);
        }
        userTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isUserFound && start) {
                    bluetoothService.startBluetoothService("FOFF","");
                    Utility.showToast(context, "USER NOT FOUND !! \nPLEASE RETRY AGAIN !!");
                    homeBtn.callOnClick();
                }
            }
        }, time);

    }

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
                    bluetoothTxt.setTextColor(context.getColor(R.color.SuccessColor));
                    bluetoothTxt.setText("Bluetooth : On || Device name : " + deviceName);
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    public void enableDisableBT() {
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);

        } else {
            bluetoothTxt.setTextColor(context.getColor(R.color.SuccessColor));
            bluetoothTxt.setText("Bluetooth : On || Device name : " + deviceName);
        }
    }

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE OFF");
                        Utility.isBlutoothEnabled = false;
                        bluetoothTxt.setText("Bluetooth : off || Device name : " + deviceName);
                        bluetoothTxt.setTextColor(context.getColor(R.color.errorColor));
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Utility.isBlutoothEnabled = true;
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        bluetoothTxt.setTextColor(context.getColor(R.color.SuccessColor));
                        bluetoothTxt.setText("Bluetooth : On || Device name : " + deviceName);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private class AsyncCallForBluetooth extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String message = strings[0];
            String memberNo = strings[1];
            Log.d(TAG, ">>> message >>>" + message + ">>>> member no >>" + memberNo);
            bluetoothService = new BluetoothService(MainActivity.this);
            bluetoothService.startBluetoothService(message, memberNo);
            return null;
        }
    }

}