package com.one.above.fitness.activities;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.one.above.fitness.R;
import com.one.above.fitness.database.SharedPreference;
import com.one.above.fitness.utility.Utility;

public class BluetoothSettingActivity extends AppCompatActivity {
    Context context;
    EditText bluetoothDeviceName;
    String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.bluetooth_setting_activity);
            context = getApplicationContext();
            bluetoothDeviceName = findViewById(R.id.bluetoothDeviceName);

            bluetoothDeviceName.setText(SharedPreference.getBluetoothDeviceName(context));

            ((ImageButton) (findViewById(R.id.backBtn))).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            Keyboard mKeyboard = new Keyboard(this, R.xml.key_preview);
            // Lookup the KeyboardView
            KeyboardView mKeyboardView = (KeyboardView) findViewById(R.id.keyboardview);
            // Attach the keyboard to the view
            mKeyboardView.setKeyboard(mKeyboard);

            // Do not show the preview balloons
            mKeyboardView.setPreviewEnabled(false);

            // Install the key handler
            mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);

            ((Button) (findViewById(R.id.SaveSetting))).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    name = bluetoothDeviceName.getText().toString().trim();

                    if (name.length() > 0) {
                        SharedPreference.setBluetoothDeviceName(context, name);
                        onBackPressed();
                        Utility.showToast(context, "Setting Saved successfully !!");
                    } else {
                        Utility.showToast(context, "Please enter valid bluetooth device name !!");
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            if (primaryCode == 1) {
                Log.i("Key", "You just pressed 1 button");
            }
        }

        @Override
        public void onPress(int arg0) {
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onText(CharSequence text) {
            Log.i("Key", "You just pressed " + text.toString() + " button");
            Utility.showToast(context, "You just pressed " + text.toString() + " button");
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeUp() {
        }
    };
}
