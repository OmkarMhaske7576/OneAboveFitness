package com.one.above.fitness.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.one.above.fitness.R;
import com.one.above.fitness.activities.SettingContainerActivity;
import com.one.above.fitness.activities.SplashScreenActivity;
import com.one.above.fitness.database.SQLiteDatabaseHandler;
import com.one.above.fitness.database.SharedPreference;
import com.one.above.fitness.pojo.BranchData;
import com.one.above.fitness.pojo.FaceData;
import com.one.above.fitness.pojo.FaceImgData;
import com.one.above.fitness.pojo.FirebaseImgData;
import com.one.above.fitness.pojo.FirebaseUserData;

import java.util.HashMap;
import java.util.List;

public class FirebaseUtility {
    public static final String FIREBASE_APP_ID = "1:263888773749:android:33a3a86dcad5085c924e3c";
    public static final String FIREBASE_API_KEY = "AIzaSyCe8BQMz_6OpfQ470Ges3Xk-DdOY6hCSHI";
    public static final String FIREBASE_DATABASE_URL = "https://oneabovefitness-78c1e-default-rtdb.firebaseio.com";
    public static final String DEFAULT = "default";
    public static final String ROOT_USER = "USER";
    public static final String ROOT_IMAGES = "IMAGES";
    public static final String ROOT_BRANCH = "BRANCH";
    public static DatabaseReference userDataRef, imageRef, branchRef;
    public static FirebaseDatabase firebaseDatabase;
    public static FirebaseApp firebaseApp;

    public static FirebaseOptions options = new FirebaseOptions.Builder()
            .setApplicationId(FIREBASE_APP_ID) // Required for Analytics.
            .setApiKey(FIREBASE_API_KEY) // Required for Auth.
            .setDatabaseUrl(FIREBASE_DATABASE_URL)// Get it from service account
            .build();

    public static void uploadDataToServer(Context context, SQLiteDatabaseHandler db) {

        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogTheme);
            builder.setTitle("Upload Data !!!");
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final EditText password = new EditText(context);
            password.setInputType(InputType.TYPE_CLASS_TEXT);
            password.setHint("Enter password \"123456\" to upload data to server !!!");
            linearLayout.addView(password);
            builder.setView(linearLayout);

            builder.setPositiveButton("Upload data", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (password.getText().toString().equalsIgnoreCase(Utility.DEFAULT_PASSWORD)) {
                        BranchData branchDetails = SharedPreference.getBranchDetails(context);

                        if (branchDetails != null) {

                            //String key = userDataRef.push().getKey();
                            String branchNo = branchDetails.getBranchno().trim();
                            SettingContainerActivity.dialog = Utility.showLoadingDialog("Uploading Data ...", context);
                            SettingContainerActivity.dialog.show();
                            uploadBranchData(branchDetails, context);
                            uploadUserData(db, branchNo, context);
                            uploadUserImgData(db, branchNo, context);
                            //  Utility.showToast(context, "Data successfully uploaded to server !!");

                        } else {
                            Utility.showToast(context, "Please add branch details first !!");
                            dialog.cancel();
                        }
                    } else {
                        Utility.showToast(context, "Invalid Password !!!");
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

        } catch (Exception e) {
            System.out.println(">>> error !!!!" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void getDataFromServer(Context context, SQLiteDatabaseHandler db) {

        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogTheme);
            builder.setTitle("Download data !!!");
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final EditText password = new EditText(context);
            password.setInputType(InputType.TYPE_CLASS_TEXT);
            password.setHint("Enter password \"123456\" to download data !!!");
            linearLayout.addView(password);
            builder.setView(linearLayout);

            builder.setPositiveButton("Download data", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (password.getText().toString().equalsIgnoreCase(Utility.DEFAULT_PASSWORD)) {

                        BranchData branchDetails = SharedPreference.getBranchDetails(context);

                        if (branchDetails != null) {

                            String branchNo = branchDetails.getBranchno();
                            SettingContainerActivity.dialog = Utility.showLoadingDialog("Downloading Data ...", context);
                            SettingContainerActivity.dialog.show();

                            if (FirebaseUtility.userDataRef == null)
                                SplashScreenActivity.initFirebaseConfig(context);

                            userDataRef.child(branchNo).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    //  db.deleteAndRecreateTables();
                                    HashMap<String, FirebaseUserData> userData = new HashMap<>();
                                    if (task.isSuccessful()) {
                                        for (DataSnapshot ds : task.getResult().getChildren()) {
                                            userData.put(ds.getKey(), ds.getValue(FirebaseUserData.class));
                                        }
                                    } else {
                                        Log.d("TAG", task.getException().getMessage()); //Don't ignore potential errors!
                                    }
                                    Log.d("SERVER USER DATA", new Gson().toJson(userData));
                                    if (userData.size() > 0) {

                                        db.loadUserDataTolocal(userData);
                                    }
                                }
                            });

                            if (FirebaseUtility.imageRef == null)
                                SplashScreenActivity.initFirebaseConfig(context);

                            imageRef.child(branchNo).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    HashMap<String, FirebaseImgData> imgData = new HashMap<>();
                                    if (task.isSuccessful()) {
                                        for (DataSnapshot ds : task.getResult().getChildren()) {
                                            for (DataSnapshot ds2 : ds.getChildren()) {
                                                imgData.put(ds2.getKey(), ds2.getValue(FirebaseImgData.class));
                                            }
                                        }
                                    } else {
                                        Log.d("TAG", task.getException().getMessage()); //Don't ignore potential errors!
                                    }
                                    Log.d("SERVER IMAGE DATA", new Gson().toJson(imgData));
                                    if (imgData.size() > 0) {
                                        db.loadImgDataToLocal(imgData);
                                    }
                                    SettingContainerActivity.dialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    SettingContainerActivity.dialog.dismiss();
                                    Utility.showToast(SettingContainerActivity.settingContainerActivity, "Something went wrong\nTry again!!");
                                }
                            });

                        } else {
                            Utility.showToast(context, "Please add branch details first \nto download user details and images. !!");
                            dialog.cancel();
                        }
                    } else {
                        Utility.showToast(context, "Invalid Password !!!");
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

        } catch (Exception e) {
            System.out.println(">>> error >>>" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void uploadBranchData(BranchData branchDetails, Context context) {
        try {
            if (FirebaseUtility.branchRef == null)
                SplashScreenActivity.initFirebaseConfig(context);

            FirebaseUtility.branchRef.child(branchDetails.getBranchno()).setValue(branchDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uploadUserImgData(SQLiteDatabaseHandler db, String branchNo, Context context) {
        try {
            List<FaceImgData> imgData = db.getAllFaceImages();
            List<FaceData> memberList = db.getAllFaces(context);
            HashMap<String, FirebaseImgData> dataHashMap = new HashMap<>();
            HashMap<String, HashMap<String, FirebaseImgData>> finalMap = new HashMap<>();

            for (FaceData faceData : memberList) {
                dataHashMap = new HashMap<>();
                for (FaceImgData faceImgData : imgData) {
                    FirebaseImgData firebaseImgData = new FirebaseImgData(faceImgData.getId(), faceImgData.getUserId(), Utility.getStringFromBitmap(faceImgData.getImageData()), new Gson().toJson(faceImgData.getExtra()),
                            new Gson().toJson(faceImgData.getIsSelected()));
                    if (faceData.getMemberID().equalsIgnoreCase(firebaseImgData.getUserId())) {
                        dataHashMap.put(firebaseImgData.getId(), firebaseImgData);
                    }
                }
                finalMap.put(faceData.getMemberID(), dataHashMap);
            }
            if (FirebaseUtility.imageRef == null)
                SplashScreenActivity.initFirebaseConfig(context);

            FirebaseUtility.imageRef.child(branchNo).setValue(finalMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    SettingContainerActivity.dialog.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void uploadUserData(SQLiteDatabaseHandler db, String branchNo, Context context) {
        try {
            List<FaceData> faceDataList = db.getAllFaces(context);
            HashMap<String, FirebaseUserData> finalMap = new HashMap<>();
            for (FaceData faceData : faceDataList) {
                FirebaseUserData firebaseUserData = new FirebaseUserData(faceData.getId(), faceData.getName(), faceData.getMemberID(),
                        new Gson().toJson(faceData.getDistance()), new Gson().toJson(faceData.getExtra()), faceData.getStartTime(),
                        faceData.getEndTime(), faceData.getTimeFormat(), Utility.getStringFromBitmap(faceData.getUserImage()), faceData.getBranchno());
                finalMap.put(faceData.getMemberID(), firebaseUserData);
            }
            if (FirebaseUtility.userDataRef == null)
                SplashScreenActivity.initFirebaseConfig(context);

            FirebaseUtility.userDataRef.child(branchNo).setValue(finalMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // SettingContainerActivity.dialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    SettingContainerActivity.dialog.dismiss();
                    Utility.showToast(SettingContainerActivity.settingContainerActivity, "Something went wrong\nTry again!!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getBranchDetails(Context context) {
        if (FirebaseUtility.branchRef == null)
            SplashScreenActivity.initFirebaseConfig(context);

        FirebaseUtility.branchRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Log.d("branchRef", " ============= getBranchDetails ============ = =");
                if (task.isSuccessful()) {
                    for (DataSnapshot ds : task.getResult().getChildren()) {
                        Log.d("DataSnapshot", ds.getKey());
                    }
                } else {
                    Log.d("TAG", task.getException().getMessage()); //Don't ignore potential errors!
                }
            }
        });
    }
}
