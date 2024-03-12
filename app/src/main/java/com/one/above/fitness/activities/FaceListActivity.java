package com.one.above.fitness.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.one.above.fitness.R;
import com.one.above.fitness.database.SQLiteDatabaseHandler;
import com.one.above.fitness.helper.MemberListAdapter;
import com.one.above.fitness.pojo.FaceData;
import com.one.above.fitness.utility.FirebaseUtility;

@SuppressLint("MissingInflatedId")
public class FaceListActivity extends AppCompatActivity {
    public static SQLiteDatabaseHandler db;
    EditText searchEdit;
    public static MemberListAdapter accessoryListAdapter;
    public static RecyclerView recycler;
    public static FaceListActivity faceListActivity;
    ImageButton backBtn;
    Button refreshBtn;
    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.face_list_activity);

            db = new SQLiteDatabaseHandler(this);
            faceListActivity = this;

            recycler = findViewById(R.id.recycler);
            searchEdit = findViewById(R.id.searchEdit);
            backBtn = findViewById(R.id.backBtn);
            refreshBtn = findViewById(R.id.refreshBtn);

            mContext = getApplicationContext();

            recycler.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            recycler.setLayoutManager(linearLayoutManager);

            accessoryListAdapter = new MemberListAdapter(getApplicationContext(), db.getAllFaces(getApplicationContext()));
            recycler.setAdapter(accessoryListAdapter);

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            refreshBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseUtility.getDataFromServer(FaceListActivity.this, db);
                }
            });

            searchEdit.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Call back the Adapter with current character to Filter
                    accessoryListAdapter.getFilter().filter(s.toString());
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });


        } catch (Exception e) {


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accessoryListAdapter != null) {
            accessoryListAdapter.notifyDataSetChanged();
        }
    }

    public static void deleteMember(FaceData member) {

        AlertDialog.Builder builder = new AlertDialog.Builder(FaceListActivity.faceListActivity, R.style.MyAlertDialogTheme);
        builder.setTitle("Delete member");
        builder.setMessage("Are you sure you want to delete member " + member.getName() + " ?");

        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    db.deleteMemberfromDB(member);
                    accessoryListAdapter = new MemberListAdapter(FaceListActivity.faceListActivity, db.getAllFaces(mContext));
                    recycler.setAdapter(accessoryListAdapter);
                    accessoryListAdapter.notifyDataSetChanged();
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
}
