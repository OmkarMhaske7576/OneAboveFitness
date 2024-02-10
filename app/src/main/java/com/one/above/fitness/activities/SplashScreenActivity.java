package com.one.above.fitness.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.one.above.fitness.MainActivity;
import com.one.above.fitness.R;
import com.one.above.fitness.database.SharedPreference;
import com.one.above.fitness.service.ApplicationService;
import com.one.above.fitness.utility.FirebaseUtility;
import com.one.above.fitness.utility.Utility;

public class SplashScreenActivity extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.splash_screen_activity);
            startService(new Intent(SplashScreenActivity.this, ApplicationService.class));

            context = getApplicationContext();

            initFirebaseConfig(context);

            Utility.FACE_DISTANCE = SharedPreference.getHyperParameter(context);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            Utility.vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            getScreenSize(context);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                }
            }, 4000);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private int getScreenSizeInMM() {

        DisplayMetrics metrics = null;

        float widthDpi;

        double screenMM = 0;

        try {

            metrics = new DisplayMetrics();

            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            widthDpi = metrics.xdpi;

            screenMM = (metrics.widthPixels / widthDpi) * 25.4;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return (int) screenMM;

    }

    public void getScreenSize(Context context) {

        try {

            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

            float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

            float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

            int widthMM = getScreenSizeInMM();

            Utility.SCREEN_HEIGHT = dpHeight;

            Utility.SCREEN_WIDTH = dpWidth;

            System.out.println(">>>>> height >>>" + dpHeight + ">>>>> width >>>>>" + dpWidth);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static void initFirebaseConfig(Context context) {

        Log.d("SplashScreen","================================ initFirebaseConfig ================== ");
        try {

            FirebaseApp.initializeApp(context, FirebaseUtility.options, FirebaseUtility.DEFAULT);

            FirebaseUtility.firebaseApp = FirebaseApp.getInstance(FirebaseUtility.DEFAULT);

            FirebaseUtility.firebaseDatabase = FirebaseDatabase.getInstance(FirebaseUtility.firebaseApp);

            FirebaseUtility.userDataRef = FirebaseUtility.firebaseDatabase.getReference(FirebaseUtility.ROOT_USER);
            FirebaseUtility.imageRef = FirebaseUtility.firebaseDatabase.getReference(FirebaseUtility.ROOT_IMAGES);
            FirebaseUtility.branchRef = FirebaseUtility.firebaseDatabase.getReference(FirebaseUtility.ROOT_BRANCH);

//            FirebaseUtility.branchRef = FirebaseDatabase.getInstance().getReference(FirebaseUtility.ROOT_BRANCH);

            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            FirebaseUtility.firebaseDatabase.getInstance().setPersistenceEnabled(true);

            FirebaseUtility.userDataRef.keepSynced(true);
            FirebaseUtility.imageRef.keepSynced(true);
            FirebaseUtility.branchRef.keepSynced(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
