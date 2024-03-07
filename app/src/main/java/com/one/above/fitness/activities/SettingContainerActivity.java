package com.one.above.fitness.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.one.above.fitness.R;
import com.one.above.fitness.database.SQLiteDatabaseHandler;
import com.one.above.fitness.database.SharedPreference;
import com.one.above.fitness.helper.Connection;
import com.one.above.fitness.pojo.BranchData;
import com.one.above.fitness.pojo.FaceData;
import com.one.above.fitness.pojo.FaceImgData;
import com.one.above.fitness.service.WebService;
import com.one.above.fitness.utility.FirebaseUtility;
import com.one.above.fitness.utility.Utility;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class SettingContainerActivity extends AppCompatActivity {
    LinearLayout scanQR_saveAttendance, saveDirectoryData, bluetoothSetting, branchSetting, faceRegister, viewMembers, uploadData, downloadData, dialogTimeSetting, hyperParameterSetting;
    SQLiteDatabaseHandler db;
    public static Dialog dialog;
    public static SettingContainerActivity settingContainerActivity;

    public static Context context;
    ArrayAdapter<String> branchNoDetailsAdapter;
    List<String> dataList = new ArrayList<>();
    BranchData branchData = null;
    ImageButton backBtn;
    Interpreter tfLite;
    String modelFile = "mobile_face_net.tflite";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.setting_container_activity);
            db = new SQLiteDatabaseHandler(this);

            backBtn = findViewById(R.id.backBtn);
            branchSetting = findViewById(R.id.branchSetting);
            faceRegister = findViewById(R.id.faceRegister);
            viewMembers = findViewById(R.id.viewMembers);
            uploadData = findViewById(R.id.uploadData);
            downloadData = findViewById(R.id.downloadData);
            dialogTimeSetting = findViewById(R.id.dialogTimeSetting);
            hyperParameterSetting = findViewById(R.id.hyperParameterSetting);
            bluetoothSetting = findViewById(R.id.bluetoothSetting);
            saveDirectoryData = findViewById(R.id.saveDirectoryData);
            scanQR_saveAttendance = findViewById(R.id.scanQR_saveAttendance);

            context = getApplicationContext();
            settingContainerActivity = this;

            try {
                tfLite = new Interpreter(Utility.loadModelFile(SettingContainerActivity.this, modelFile));
            } catch (IOException e) {
                e.printStackTrace();
            }

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            bluetoothSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.vibrateWithAnim(v);
                    startActivity(new Intent(context, BluetoothSettingActivity.class));
                }
            });

            saveDirectoryData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Utility.vibrateWithAnim(v);
                        getAllImgFromDir();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            scanQR_saveAttendance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.vibrateWithAnim(v);
                    startActivity(new Intent(SettingContainerActivity.this, ScanQRActivity.class));
                }
            });
            branchSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.vibrateWithAnim(v);
                    if (!Connection.isOnline(SettingContainerActivity.this) && SharedPreference.getBranchServerDataList(context).length() == 0) {
                        Utility.showToast(context, "Please turn on internet connection !!");
                        return;
                    }
                    confirmToAdd();
                }
            });

            hyperParameterSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.vibrateWithAnim(v);
                    hyperparameters();
                    /*AlertDialog.Builder builder = new AlertDialog.Builder(SettingContainerActivity.this, R.style.MyAlertDialogTheme);

                    builder.setTitle("Change Parameters !!!");
                    LinearLayout linearLayout = new LinearLayout(SettingContainerActivity.this);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    final EditText password = new EditText(SettingContainerActivity.this);
                    password.setInputType(InputType.TYPE_CLASS_TEXT);
                    password.setHint("Enter password \"123456\" to change Hyper Parameters...");
                    linearLayout.addView(password);
                    builder.setView(linearLayout);

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            try {
                                if (password.getText().toString().equalsIgnoreCase(Utility.DEFAULT_PASSWORD)) {
                                    hyperparameters();
                                } else {
                                    Toast.makeText(SettingContainerActivity.this, "Invalid Password !!!", Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();*/
                }
            });

            dialogTimeSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.vibrateWithAnim(v);
                    changeDialogTime();
                }
            });
            downloadData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.vibrateWithAnim(v);
                    FirebaseUtility.getDataFromServer(SettingContainerActivity.this, db);
                }
            });
            uploadData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.vibrateWithAnim(v);
                    FirebaseUtility.uploadDataToServer(SettingContainerActivity.this, db);
                }
            });

            faceRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Utility.vibrateWithAnim(v);
                    FaceData faceData1 = null;
                    v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.bounce));
                    Intent intent = new Intent(getApplicationContext(), FaceRegisterActivity.class);
                    intent.putExtra("USER-OBJ", faceData1);
                    startActivity(intent);

                   /* AlertDialog.Builder builder = new AlertDialog.Builder(SettingContainerActivity.this, R.style.MyAlertDialogTheme);

                    builder.setTitle("Register Face !!!");

                    LinearLayout linearLayout = new LinearLayout(SettingContainerActivity.this);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    final EditText password = new EditText(SettingContainerActivity.this);

                    password.setInputType(InputType.TYPE_CLASS_TEXT);

                    password.setHint("Enter password \"123456\" to register...");

                    linearLayout.addView(password);

                    builder.setView(linearLayout);

                    builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            try {

                                if (password.getText().toString().equalsIgnoreCase(Utility.DEFAULT_PASSWORD)) {

                                    FaceData faceData1 = null;
                                    v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.bounce));
                                    Intent intent = new Intent(getApplicationContext(), FaceRegisterActivity.class);
                                    intent.putExtra("USER-OBJ", faceData1);
                                    startActivity(intent);

                                } else {
                                    Toast.makeText(SettingContainerActivity.this, "Invalid Password.", Toast.LENGTH_LONG).show();
                                }

                            } catch (Exception e) {

                                e.printStackTrace();
                            }
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();*/
                }
            });

            viewMembers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.vibrateWithAnim(v);
                    startActivity(new Intent(getApplicationContext(), FaceListActivity.class));
                }
            });
        } catch (Exception e) {

        }
    }

    private void confirmToAdd() {
        openBranchDetailsPopup();
       /* AlertDialog.Builder builder = new AlertDialog.Builder(SettingContainerActivity.this, R.style.MyAlertDialogTheme);

        builder.setTitle("Confirm !!!");

        LinearLayout linearLayout = new LinearLayout(SettingContainerActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText password = new EditText(SettingContainerActivity.this);

        password.setInputType(InputType.TYPE_CLASS_TEXT);

        password.setHint("Enter password \"123456\" to proceed...");

        linearLayout.addView(password);

        builder.setView(linearLayout);

        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {

                    if (password.getText().toString().trim().equalsIgnoreCase(Utility.DEFAULT_PASSWORD)) {
                        //addBranchDetail();
                        openBranchDetailsPopup();

                    } else {
                        Toast.makeText(SettingContainerActivity.this, "Invalid password !!!", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();*/
    }

    private void openBranchDetailsPopup() {
        final String defaultName = "Please select branch name";
        Dialog customDialog = new Dialog(SettingContainerActivity.this);
        Button saveBtn, cancelBtn;
        RadioButton inRadio, outRadio;
        Spinner branchNameSpinner;
        EditText branchNo, branchAdd;
        JSONArray branchArray = new JSONArray();
        HashMap<String, BranchData> branchDataHashMap = new HashMap<>();
        try {

            customDialog.setContentView(R.layout.custom_branch_layout);
            customDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            customDialog.getWindow().getAttributes().windowAnimations = R.style.animation;

            WebService.getBranchDetails(context);
            branchNameSpinner = customDialog.findViewById(R.id.branchNameSpinner);
            branchNo = customDialog.findViewById(R.id.branchNo);
            branchAdd = customDialog.findViewById(R.id.branchAdd);
            inRadio = customDialog.findViewById(R.id.inRadio);
            outRadio = customDialog.findViewById(R.id.outRadio);
            saveBtn = customDialog.findViewById(R.id.saveDetails);
            cancelBtn = customDialog.findViewById(R.id.cancel);
            dataList = new ArrayList<>();
            dataList.add(defaultName);
            branchArray = SharedPreference.getBranchServerDataList(context);
            try {
                JSONArray temp = WebService.getBranchDetails(context);
                if (temp != null && temp.length() > 0) {
                    branchArray = temp;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(">>> branchArray >>>" + new Gson().toJson(branchArray));
            for (int i = 0; i < branchArray.length(); i++) {
                JSONObject jsonObject = branchArray.getJSONObject(i);
                BranchData branchData = new BranchData(jsonObject.getString("Branchno"), jsonObject.getString("Branchname"), jsonObject.getString("Branchadd"), "");
                branchDataHashMap.put(branchData.getBranchname(), branchData);
            }
            dataList.addAll(new ArrayList<String>(branchDataHashMap.keySet()));

            branchNoDetailsAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, dataList);
            branchNoDetailsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            branchNameSpinner.setAdapter(branchNoDetailsAdapter);
            branchNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        String selectedItem = parent.getItemAtPosition(position).toString();
                        if (!selectedItem.contains(defaultName) && branchDataHashMap != null && branchDataHashMap.get(selectedItem) != null) {
                            branchNo.setText(branchDataHashMap.get(selectedItem).getBranchno());
                            branchAdd.setText(branchDataHashMap.get(selectedItem).getBranchadd());
                        }
                        Log.d("branchDataHashMap", new Gson().toJson(branchDataHashMap));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedItem = branchNameSpinner.getSelectedItem().toString();
                    if (selectedItem.equalsIgnoreCase(defaultName) || branchNo.getText().toString().trim().length() == 0) {
                        Utility.showToast(context, "Please enter valid details to add !!");
                        return;
                    }
                    String status = inRadio.isChecked() ? "IN" : "OUT";
                    BranchData branchData1 = new BranchData(branchNo.getText().toString(), selectedItem, branchAdd.getText().toString(), status);
                    SharedPreference.setBranchDetails(getApplicationContext(), branchData1);
                    customDialog.dismiss();
                    Utility.showToast(context, "Branch details successfully saved !!");
                }
            });

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                }
            });
            branchData = SharedPreference.getBranchDetails(context);
            if (branchData != null) {
                Log.d("selected item", branchNoDetailsAdapter.getPosition(branchData.getBranchname()) + "");
                branchNameSpinner.setSelection(branchNoDetailsAdapter.getPosition(branchData.getBranchname()), true);
                branchNo.setText(branchData.getBranchno());
                branchAdd.setText(branchData.getBranchadd());
                if (branchData.getStatus().contains("IN")) {
                    inRadio.setChecked(true);
                    outRadio.setChecked(false);
                } else {
                    inRadio.setChecked(false);
                    outRadio.setChecked(true);
                }
            }
            customDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeDialogTime() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingContainerActivity.this, R.style.MyAlertDialogTheme);
        builder.setTitle("Change Dialog Time !!!");
        builder.setMessage("Enter default time for popup screen in seconds !!");
        final EditText timeTxt = new EditText(SettingContainerActivity.this);

        timeTxt.setInputType(InputType.TYPE_CLASS_NUMBER);
        timeTxt.setHint("Enter default time in second for dialog panel .... ");
        timeTxt.setText((SharedPreference.getDialogTimer(context) / 1000) + "");

        builder.setView(timeTxt);

        builder.setPositiveButton("SET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    if (timeTxt.getText().toString().trim().length() == 0) {
                        Utility.showToast(context, "Please enter valid time to add !!");
                        return;
                    }
                    SharedPreference.setDialogTimer(context, (Integer.parseInt(timeTxt.getText().toString().trim()) * 1000) + "");
                    Utility.showToast(context, "Time successfully set to dialog !!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void hyperparameters() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingContainerActivity.this, R.style.MyAlertDialogTheme);
        builder.setTitle("Euclidean Distance");
        builder.setMessage("0.00 -> Perfect Match\n1.00 -> Default\n\nCurrent Value:");
        // Set up the input
        final EditText input = new EditText(context);

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);
        Utility.FACE_DISTANCE = SharedPreference.getHyperParameter(context);
        input.setText(String.valueOf(Utility.FACE_DISTANCE));
        // Set up the buttons
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(context, input.getText().toString(), Toast.LENGTH_SHORT).show();
                float val = Float.parseFloat(input.getText().toString());
                if (val > 1.0 || val < 0.0f) {
                    Utility.showToast(context, "Please enter values in between 0 and 1 !!");
                    return;
                }
                Utility.FACE_DISTANCE = val;
                SharedPreference.settHyperParameter(context, Utility.FACE_DISTANCE + "");

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void getAllImgFromDir() {
        try {
            final int SELECT_PICTURES = 1;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*"); //allows any image file type. Change * to specific extension to limit it
            //**These following line is the important one!
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURES);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                dialog = Utility.showLoadingDialog("Saving Data...", SettingContainerActivity.this);
                dialog.show();
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    int currentItem = 0;
                    while (currentItem < count) {
                        Uri imageUri = data.getClipData().getItemAt(currentItem).getUri();
                        //do something with the image (save it to some directory or whatever you need to do with it here)
                        currentItem = currentItem + 1;
                        Log.w("IMG", "imageUri >>>" + imageUri.getPath());
                        saveImgData(imageUri);
                    }
                } else if (data.getData() != null) {
                    String imagePath = data.getData().getPath();
                    Log.w("IMG", "imagePath >>>" + imagePath);
                    Uri imageUri = data.getData();
                    saveImgData(imageUri);
                    //do something with the image (save it to some directory or whatever you need to do with it here)
                }
                dialog.dismiss();
            }
        }
    }
    private void saveImgData(Uri imageUri) {
        try {
            boolean flipX = false;
            InputImage impphoto = InputImage.fromBitmap(getBitmapFromUri(imageUri), 0);

            FaceDetectorOptions highAccuracyOpts = new FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).build();
            FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);
            detector.process(impphoto).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                @Override
                public void onSuccess(List<Face> faces) {

                    if (faces.size() != 0) {

                        Face face = faces.get(0);
                        Bitmap frame_bmp = null;
                        try {
                            frame_bmp = getBitmapFromUri(imageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Bitmap frame_bmp1 = Utility.rotateBitmap(frame_bmp, 0, flipX, false);
                        RectF boundingBox = new RectF(face.getBoundingBox());
                        Bitmap cropped_face = Utility.getCropBitmapByCPU(frame_bmp1, boundingBox);
                        Bitmap scaled = Utility.getResizedBitmap(cropped_face, 112, 112);

                        String memberId = getFileName(imageUri);
                        Log.w("MemberId", "memberId >>>>>" + memberId);
                        saveData(memberId, scaled);

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
                    Toast.makeText(context, "Failed to add !!", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result.replaceAll(".jpg", "").replaceAll(".png", "").trim();
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void saveData(String memberID, Bitmap userImg) {
        String timeFormat = "Fixed";
        String startTime = "";
        String endTime = "";
        float[][] embeedings = getFaceEmbedding(userImg);
        Log.w("Embeddings", embeedings.toString());
        try {

            BranchData branchData = SharedPreference.getBranchDetails(context);
            if (branchData == null) {
                Utility.showToast(context, "Please add branch details first ..");
                return;
            }

            JSONArray jsonArray = WebService.getLoginData(memberID, memberID,"Member");

            if (jsonArray.length() == 0 || jsonArray.getJSONObject(0) == null || jsonArray.getJSONObject(0).getString("MemberNo").contains("No")) {
                Utility.showToast(context, "Invalid user !! " + memberID);
                return;
            }

            if (memberID.trim().length() == 0) {
                Utility.showToast(context, "Please enter a valid details.");
                return;
            }

            if (db.getDetailsById(memberID).size() > 0) {
                Utility.showToast(context, "User already exist !! " + memberID);
                return;
            }

            String name = jsonArray.getJSONObject(0).getString("MemberName");
            String branchNo = branchData.getBranchno();
            FaceData faceData = new FaceData(db.getFaceDataCount() + "", name, memberID, -1f, embeedings, startTime, endTime, timeFormat, userImg, branchNo,"Member");

            FaceImgData faceImgData = new FaceImgData(Utility.getUniqueId(), faceData.getMemberID(), faceData.getUserImage(), faceData.getExtra(), 1);

            db.addFaceData(faceData);
            db.addImageData(faceImgData);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public float[][] getFaceEmbedding(final Bitmap bitmap) {
        int[] intValues;
        int inputSize = 112;
        final int OUTPUT_SIZE = 192;
        float[][] embeedings = new float[1][OUTPUT_SIZE];
        float IMAGE_MEAN = 128.0f;
        float IMAGE_STD = 128.0f;
        boolean isModelQuantized = false;
        try {

            ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);

            imgData.order(ByteOrder.nativeOrder());

            intValues = new int[inputSize * inputSize];

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

            tfLite.runForMultipleInputsOutputs(inputArray, outputMap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return embeedings;
    }

}
