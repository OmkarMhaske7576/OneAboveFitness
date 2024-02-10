package com.one.above.fitness.helper;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;

import com.one.above.fitness.MainActivity;
import com.one.above.fitness.R;
import com.one.above.fitness.utility.Utility;

public class MyKeyboard extends LinearLayout implements View.OnClickListener {
    Context globalContext;

    // constructors
    public MyKeyboard(Context context) {
        this(context, null, 0);
    }

    public MyKeyboard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private Button mButtonDelete;
    private Button mButtonEnter;
    SparseArray<String> keyValues = new SparseArray<>();
    InputConnection inputConnection;

    private void init(Context context, AttributeSet attrs) {

        globalContext = context;

        // initialize buttons
        LayoutInflater.from(context).inflate(R.layout.custom_keyboard, this, true);

        int[] alphabets = new int[]{R.id.A_btn, R.id.B_btn, R.id.C_btn, R.id.D_btn, R.id.E_btn, R.id.F_btn, R.id.G_btn, R.id.H_btn, R.id.I_btn, R.id.J_btn, R.id.K_btn, R.id.L_btn, R.id.M_btn, R.id.N_btn, R.id.O_btn, R.id.P_btn, R.id.Q_btn, R.id.R_btn, R.id.S_btn, R.id.T_btn, R.id.U_btn, R.id.V_btn, R.id.W_btn, R.id.X_btn, R.id.Y_btn, R.id.Z_btn};

        mButtonDelete = (Button) findViewById(R.id.button_delete);
        mButtonEnter = (Button) findViewById(R.id.button_enter);

        for (int i = 0; i < alphabets.length; i++) {
            Button button = (Button) findViewById(alphabets[i]);
            button.setOnClickListener(this);
            Log.d("ButtonText", button.getText().toString());
            keyValues.put(alphabets[i], button.getText().toString().trim());
        }

        mButtonDelete.setOnClickListener(this);
        mButtonEnter.setOnClickListener(this);
        keyValues.put(R.id.button_enter, "");
    }

    @Override
    public void onClick(View v) {

        if (inputConnection == null) return;

        if (v.getId() == R.id.button_delete) {
            CharSequence selectedText = inputConnection.getSelectedText(0);
            if (TextUtils.isEmpty(selectedText)) {
                inputConnection.deleteSurroundingText(1, 0);
            } else {
                inputConnection.commitText("", 1);
            }
        } else if (v.getId() == R.id.button_enter) {
            if (MainActivity.mainActivity != null) {
                MainActivity.mainActivity.refreshCamera();
            }
        } else {
            String value = keyValues.get(v.getId());
            inputConnection.commitText(value, 1);
        }
        Utility.vibrateWithAnim(v);
    }
    public void setInputConnection(InputConnection ic) {
        this.inputConnection = ic;
    }
}