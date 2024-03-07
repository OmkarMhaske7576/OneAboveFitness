package com.one.above.fitness.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.one.above.fitness.MainActivity;
import com.one.above.fitness.R;
import com.one.above.fitness.database.SQLiteDatabaseHandler;
import com.one.above.fitness.database.SharedPreference;
import com.one.above.fitness.pojo.BranchData;
import com.one.above.fitness.pojo.FaceData;
import com.one.above.fitness.pojo.FaceImgData;
import com.one.above.fitness.service.WebService;
import com.one.above.fitness.utility.Utility;

import org.json.JSONArray;
import org.tensorflow.lite.Interpreter;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FaceRegisterActivity extends AppCompatActivity implements Serializable {
    private static int SELECT_PICTURE = 1;
    boolean updateFlag = false;
    FaceDetector detector;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    ImageView face_preview;
    Interpreter tfLite;
    TextView reco_name, preview_info, textAbove_preview;
    Button camera_switch, openCamera, browseImg;
    CameraSelector cameraSelector;
    boolean start = true, flipX = false;
    Context context = this;
    int cam_face = CameraSelector.LENS_FACING_FRONT;
    int[] intValues;
    int inputSize = 112;  //Input size for model
    boolean isModelQuantized = false;
    float[][] embeedings;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE = 192; //Output size of model
    ProcessCameraProvider cameraProvider;
    String modelFile = "mobile_face_net.tflite";
    ImageButton addFace;
    FrameLayout cameraPreviewLayout;
    ConstraintLayout imgContainer;
    String timeFormat = "Fixed";
    String userType = "Member";
    LinearLayout timeLayout;
    SQLiteDatabaseHandler db;
    Bitmap userImg = null;
    FaceData userObj = null;

    ImageButton backBtn;

    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {

            db = new SQLiteDatabaseHandler(this);

            super.onCreate(savedInstanceState);

            setContentView(R.layout.face_register_activity);
            userObj = (FaceData) getIntent().getSerializableExtra("USER-OBJ");

            if (userObj != null) {
                Log.e("userImg Data >> {}", db.getImageDataById(userObj.getMemberID()).toString());
            }

            addFace = findViewById(R.id.addFace);
            browseImg = findViewById(R.id.browseImg);
            openCamera = findViewById(R.id.openCamera);
            backBtn = findViewById(R.id.backBtn);

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            imgContainer = findViewById(R.id.imgContainer);
            cameraPreviewLayout = findViewById(R.id.cameraPreviewLayout);

            face_preview = findViewById(R.id.imageView);

            reco_name = findViewById(R.id.textView);

            preview_info = findViewById(R.id.textView2);

            textAbove_preview = findViewById(R.id.textAbovePreview);

            textAbove_preview.setText("Face Preview: ");

            camera_switch = findViewById(R.id.cameraBtn);

            addFace.setVisibility(View.VISIBLE);

            reco_name.setVisibility(View.INVISIBLE);

            face_preview.setVisibility(View.VISIBLE);

            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.CAMERA}, Utility.MY_CAMERA_REQUEST_CODE);
            }

            preview_info.setText("1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.\n\n3.Click Add button to save face.");

            addFace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.vibrateWithAnim(v);
                    if (userObj != null) {
                        updateImgInfo();

                    } else {
                        openCustomDialog();
                    }
                }
            });

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

            try {
                tfLite = new Interpreter(Utility.loadModelFile(FaceRegisterActivity.this, modelFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            FaceDetectorOptions highAccuracyOpts =
                    new FaceDetectorOptions.Builder()
                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                            .build();
            detector = FaceDetection.getClient(highAccuracyOpts);

            cameraBind();

            openCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    start = true;
                    cameraPreviewLayout.setVisibility(View.VISIBLE);
                    imgContainer.setVisibility(View.VISIBLE);
                    cameraProvider.unbindAll();
                    cameraBind();
                }
            });

            browseImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    start = false;
                    cameraPreviewLayout.setVisibility(View.GONE);
                    imgContainer.setVisibility(View.VISIBLE);
                    cameraProvider.unbindAll();
                    loadphoto();
                }
            });

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

        Preview preview = new Preview.Builder()

                .build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cam_face)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
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

                @SuppressLint("UnsafeExperimentalUsageError")
                Image mediaImage = imageProxy.getImage();

                if (mediaImage != null) {

                    image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                }

                Task<List<Face>> result =
                        detector.process(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<Face>>() {
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

                                                    if (start)
                                                        recognizeImage(scaled);

                                                } else {

                                                    System.out.println(">>>>>>>> no face found >>>>>");
                                                }

                                            }
                                        })

                                .addOnFailureListener(
                                        new OnFailureListener() {
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
    }

    public void recognizeImage(final Bitmap bitmap) {

        face_preview.setImageBitmap(bitmap);

        userImg = bitmap;

        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
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
        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();

        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable

        outputMap.put(0, embeedings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model

    }

    public void updateImgInfo() {

        try {
            start = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(FaceRegisterActivity.this, R.style.MyAlertDialogTheme);

            builder.setMessage("Are you sure you want to add image ?")
                    .setTitle("Add Image !!")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            saveData(userObj.getName(), userObj.getMemberID(), userObj.getStartTime(), userObj.getEndTime(), userObj.getType());
                            Utility.showToast(context, "Image successfully added !!");
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // CANCEL
                            start = true;
                        }
                    });

            builder.create();

            builder.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void openCustomDialog() {

        try {

            start = false;

            Button saveBtn, cancelBtn;
            EditText userName, memberId, startTime, endTime;
            Dialog dialog = new Dialog(FaceRegisterActivity.this);
            RadioButton memberRadio, empRadio;

            dialog.setContentView(R.layout.custom_dialog_layout);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        dialog.setCancelable(false);
            dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

            saveBtn = dialog.findViewById(R.id.saveBtn);
            cancelBtn = dialog.findViewById(R.id.CancelButton);
            userName = dialog.findViewById(R.id.userName);
            memberId = dialog.findViewById(R.id.memberId);
            startTime = dialog.findViewById(R.id.startTime);
            endTime = dialog.findViewById(R.id.endTime);
            memberRadio = dialog.findViewById(R.id.memberRadio);
            empRadio = dialog.findViewById(R.id.empRadio);

            endTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setTime(endTime);

                }
            });

            startTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setTime(startTime);

                }
            });

            timeLayout = dialog.findViewById(R.id.timeLayout);

            RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup);
            RadioGroup userTypeRadioGrp = dialog.findViewById(R.id.userTypeRadioGrp);

            RadioButton slotTimeRadio = dialog.findViewById(R.id.slotTimeRadio);
            RadioButton fixTimeRadio = dialog.findViewById(R.id.fixTimeRadio);
            userTypeRadioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    if (memberRadio.isChecked()) {
                        userType = "Member";
                    } else {
                        userType = "Employee";
                    }
                }
            });
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    if (slotTimeRadio.isChecked()) {
                        timeLayout.setVisibility(View.VISIBLE);
                        timeFormat = "Slot";
                    } else {
                        timeLayout.setVisibility(View.GONE);
                        timeFormat = "Fixed";
                    }
                }
            });

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    start = true;
                    dialog.dismiss();
                }
            });

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (slotTimeRadio.isChecked()) {
                        timeFormat = "Slot";
                    }
                    saveData(userName.getText().toString(), memberId.getText().toString(), startTime.getText().toString(), endTime.getText().toString(), userType);
                    dialog.dismiss();
                }
            });

            if (userObj != null) {
                memberId.setText(userObj.getMemberID());
                if (userObj.getTimeFormat().equalsIgnoreCase("Slot")) {
                    slotTimeRadio.setChecked(true);
                    fixTimeRadio.setChecked(false);
                } else {
                    slotTimeRadio.setChecked(false);
                    fixTimeRadio.setChecked(true);
                }
                startTime.setText(userObj.getStartTime());
                endTime.setText(userObj.getEndTime());
            }

            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveData(String name, String memberID, String startTime, String endTime, String type) {

        try {

            BranchData branchData = SharedPreference.getBranchDetails(context);
            if (branchData == null) {
                Utility.showToast(context, "Please add branch details first ..");
                start = true;
                return;
            }

            if (memberID.trim().length() == 0) {
                Utility.showToast(context, "Please enter a valid details.");
                start = true;
                return;
            }

            if (db.getDetailsById(memberID).size() > 0 && userObj == null) {
                Utility.showToast(context, "User already exist !!");
                start = true;
                return;
            }

            JSONArray jsonArray = WebService.getLoginData(memberID, memberID, type);
            if (type.equalsIgnoreCase("Member")) {
                if (jsonArray.length() == 0 || jsonArray.getJSONObject(0) == null || jsonArray.getJSONObject(0).getString("MemberNo").contains("No")) {
                    Utility.showToast(context, "Invalid user !!!");
                    start = true;
                    return;
                }
            } else {
                if (jsonArray.length() == 0 || jsonArray.getJSONObject(0) == null) {
                    Utility.showToast(context, "Invalid user !!!");
                    start = true;
                    return;
                }
            }

            if (type.equalsIgnoreCase("Member")) {
                name = jsonArray.getJSONObject(0).getString("MemberName");
            } else {
                name = jsonArray.getJSONObject(0).getString("EmpName");
            }

            String Branchno = SharedPreference.getBranchDetails(context).getBranchno();

            FaceData faceData = new FaceData(db.getFaceDataCount() + "", name, memberID, -1f, embeedings, startTime, endTime, timeFormat, userImg, Branchno, userType);

            int selected = userObj == null ? 1 : 0;
            FaceImgData faceImgData = new FaceImgData(Utility.getUniqueId(), faceData.getMemberID(), faceData.getUserImage(), faceData.getExtra(), selected);

            if (userObj == null) {
                db.addFaceData(faceData);
                new asyncForToast().execute("User " + name + " successfully added !!");
            }
            db.addImageData(faceImgData);

            Intent intent = new Intent(FaceRegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void setTime(EditText editText) {

        int mHour, mMinute;

        final Calendar c = Calendar.getInstance();

        mHour = c.get(Calendar.HOUR_OF_DAY);

        mMinute = c.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(FaceRegisterActivity.this, R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view1, int hourOfDay,
                                  int minute) {

                boolean isPM = (hourOfDay >= 12);

                editText.setText(String.format("%02d:%02d %s", (hourOfDay == 12 || hourOfDay == 0) ? 12 : hourOfDay % 12, minute, isPM ? "PM" : "AM"));

            }

        }, mHour, mMinute, false);

        timePicker.show();

    }

    private void loadphoto() {
        start = false;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                try {
                    InputImage impphoto = InputImage.fromBitmap(getBitmapFromUri(selectedImageUri), 0);
                    detector.process(impphoto).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                        @Override
                        public void onSuccess(List<Face> faces) {

                            if (faces.size() != 0) {

                                Face face = faces.get(0);
                                Bitmap frame_bmp = null;
                                try {
                                    frame_bmp = getBitmapFromUri(selectedImageUri);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                Bitmap frame_bmp1 = rotateBitmap(frame_bmp, 0, flipX, false);
                                RectF boundingBox = new RectF(face.getBoundingBox());
                                Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);

                                Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);

                                recognizeImage(scaled);

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            start = true;
                            Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show();
                        }
                    });

                    face_preview.setImageBitmap(getBitmapFromUri(selectedImageUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas cavas = new Canvas(resultBitmap);

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        cavas.drawRect(
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        cavas.drawBitmap(source, matrix, paint);

        if (source != null && !source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public class asyncForToast extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String msg = strings[0];
            runOnUiThread(new Runnable() {
                public void run() {
                    Utility.showToast(context, msg);
                }
            });
            return null;
        }
    }
}
