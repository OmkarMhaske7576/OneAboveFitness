package com.one.above.fitness.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.one.above.fitness.R;
import com.one.above.fitness.database.SQLiteDatabaseHandler;
import com.one.above.fitness.helper.FaceListAdapter;
import com.one.above.fitness.pojo.FaceData;
import com.one.above.fitness.pojo.FaceImgData;
import com.one.above.fitness.utility.Utility;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FaceImageListContainer extends AppCompatActivity {
    public static int selectedPosition = -1;
    public static int parentPosition;
    Button save;
    RecyclerView recyclerImg;
    SQLiteDatabaseHandler db;
    FaceData userObj = null;
    LinkedList<FaceImgData> imageDataById;
    FaceListAdapter faceListAdapter;
    TextView name;
    Spinner actionSpinner;
    ImageButton backBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.face_img_list_container);
            db = new SQLiteDatabaseHandler(this);

            userObj = (FaceData) getIntent().getSerializableExtra("USER-OBJ");
            backBtn = findViewById(R.id.backBtn);
            save = findViewById(R.id.save);
            name = findViewById(R.id.name);
            recyclerImg = findViewById(R.id.recyclerImg);
            actionSpinner = findViewById(R.id.actionSpinner);

            name.setText("Name : " + userObj.getName());

            recyclerImg.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerImg.setLayoutManager(linearLayoutManager);

            imageDataById = db.getImageDataById(userObj.getMemberID());
            for (int i = 0; i < imageDataById.size(); i++) {
                FaceImgData faceImgData = imageDataById.get(i);
                if (faceImgData.getIsSelected() == 1) {
                    selectedPosition = i;
                    parentPosition = i;
                    break;
                }
            }

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            faceListAdapter = new FaceListAdapter(getApplicationContext(), imageDataById);
            recyclerImg.setAdapter(faceListAdapter);

            List<String> data = new ArrayList<>();
            data.add("Select action");
            data.add("Set photo");
            data.add("Delete photo");

            ArrayAdapter ActionSpinnerData = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, data);
            ActionSpinnerData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            actionSpinner.setAdapter(ActionSpinnerData);

            actionSpinner.setSelection(ActionSpinnerData.getPosition("Select action"));

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (selectedPosition != -1) {
                        FaceImgData faceImgData = imageDataById.get(selectedPosition);
                        if (actionSpinner.getSelectedItem().toString().contains("Set photo")) {
                            db.updateSelectedImg(faceImgData.getId(), 1);
                            if (imageDataById.size() > 1) {
                                db.updateSelectedImg(imageDataById.get(parentPosition).getId(), 0);
                            }
                            faceListAdapter.notifyDataSetChanged();
                            //FaceListActivity.accessoryListAdapter.notifyDataSetChanged();
                            onBackPressed();
                        } else if (actionSpinner.getSelectedItem().toString().contains("Delete photo") && faceImgData.getIsSelected() == 0) {
                            db.deleteImgData(faceImgData.getId());
                            faceListAdapter.notifyDataSetChanged();
                            //FaceListActivity.accessoryListAdapter.notifyDataSetChanged();
                            onBackPressed();
                        } else if (actionSpinner.getSelectedItem().toString().contains("Select action")) {
                            Utility.showToast(getApplicationContext(), "Please select an action !!");
                        }
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
