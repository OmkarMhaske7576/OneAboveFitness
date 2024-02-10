package com.one.above.fitness.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.one.above.fitness.pojo.BranchData;
import com.one.above.fitness.pojo.FaceData;
import com.one.above.fitness.pojo.FaceImgData;
import com.one.above.fitness.pojo.FirebaseImgData;
import com.one.above.fitness.pojo.FirebaseUserData;
import com.one.above.fitness.utility.Utility;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SQLiteDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "faceData";
    public static final String TABLE_FACE_DATA = "FaceData";
    public static final String TABLE_IMAGE_DATA = "imgData";

    public SQLiteDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_FACEDATA = "CREATE TABLE " + TABLE_FACE_DATA + "(" + "id INTEGER PRIMARY KEY, name TEXT, memberID TEXT, distance LONG, extra BLOB, startTime TEXT, endTime TEXT, timeFormat TEXT, userImg BLOB , Branchno TEXT " + ")";
        String CREATE_TABLE_IMGDATA = "CREATE TABLE " + TABLE_IMAGE_DATA + "(" + "id TEXT PRIMARY KEY, userId TEXT, imageData BLOB, extra BLOB, isSelected INTEGER " + ")";

        db.execSQL(CREATE_TABLE_FACEDATA);
        db.execSQL(CREATE_TABLE_IMGDATA);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FACE_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGE_DATA);
        // Create tables again
        onCreate(db);
    }

    public void addImageData(FaceImgData faceImgData) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", faceImgData.getId());
        values.put("userId", faceImgData.getUserId());
        values.put("imageData", getBitmapAsByteArray(faceImgData.getImageData()));
        values.put("extra", Utility.makeByte(faceImgData.getExtra()));
        values.put("isSelected", faceImgData.getIsSelected());

        db.insert(TABLE_IMAGE_DATA, null, values);
        db.close();
    }

    public LinkedList<FaceImgData> getImageDataById(String userId) {

        LinkedList<FaceImgData> faceImgDataList = new LinkedList<>();

        try {

            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.query(true, TABLE_IMAGE_DATA, new String[]{"id", "userId", "imageData", "extra", "isSelected"}, "userId = ?", new String[]{userId}, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {

                    byte[] imgByte = cursor.getBlob(2);

                    FaceImgData faceImgData = new FaceImgData(cursor.getString(0), cursor.getString(1), BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length), Utility.readByte(cursor.getBlob(3)), Integer.parseInt(cursor.getString(4)));

                    faceImgDataList.add(faceImgData);

                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

        Log.d("faceImgList >>  {}", faceImgDataList.toString());
        System.out.println(">>>> face list >>>" + faceImgDataList.toString());

        return faceImgDataList;
    }

    public void updateSelectedImg(String id, int isSelected) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("isSelected", isSelected);
        db.update(TABLE_IMAGE_DATA, cv, "id = ?", new String[]{id});
        db.close();
    }

    public void deleteImgData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_IMAGE_DATA, "id" + "=?", new String[]{id});
        db.close();
    }

    public void addFaceData(FaceData faceData) {

        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("name", faceData.getName());
            values.put("memberID", faceData.getMemberID());
            values.put("distance", faceData.getDistance());
            values.put("extra", Utility.makeByte(faceData.getExtra()));
            values.put("startTime", faceData.getStartTime());
            values.put("endTime", faceData.getEndTime());
            values.put("timeFormat", faceData.getTimeFormat());
            values.put("userImg", getBitmapAsByteArray(faceData.getUserImage()));
            values.put("Branchno", faceData.getBranchno());

            db.insert(TABLE_FACE_DATA, null, values);
            db.close();

            System.out.println(">>> face successfully added >>>" + faceData.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<FaceData> getDetailsById(String memberID) {

        List<FaceData> faceDataList = new ArrayList();

        try {

            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.query(true, TABLE_FACE_DATA, new String[]{"id", "name", "memberID", "distance", "extra", "startTime", "endTime", "timeFormat", "userImg", "Branchno"}, "memberID = ?", new String[]{memberID}, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {

                    byte[] imgByte = cursor.getBlob(8);

                    FaceData faceData = new FaceData(cursor.getString(0), cursor.getString(1), cursor.getString(2), Float.parseFloat(cursor.getString(3)), Utility.readByte(cursor.getBlob(4)), cursor.getString(5), cursor.getString(6), cursor.getString(7), BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length), cursor.getString(9));

                    faceDataList.add(faceData);

                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
        return faceDataList;
    }

    public List<FaceData> getFaceByName(String name) {

        List<FaceData> faceDataList = new ArrayList();

        try {

            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.query(true, TABLE_FACE_DATA, new String[]{"id", "name", "memberID", "distance", "extra", "startTime", "endTime", "timeFormat", "userImg", "Branchno"}, "name" + " LIKE ?", new String[]{name + "%"}, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {

                    byte[] imgByte = cursor.getBlob(8);

                    FaceData faceData = new FaceData(cursor.getString(0), cursor.getString(1), cursor.getString(2), Float.parseFloat(cursor.getString(3)), Utility.readByte(cursor.getBlob(4)), cursor.getString(5), cursor.getString(6), cursor.getString(7), BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length), cursor.getString(9));

                    faceDataList.add(faceData);

                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();


        } catch (Exception e) {

            e.printStackTrace();
        }

        System.out.println(">>>> faceDataList >>" + new Gson().toJson(faceDataList));
        return faceDataList;
    }

    public List<FaceData> getAllFaces(Context context) {

        List<FaceData> faceList = new ArrayList();

        BranchData branchDetails = SharedPreference.getBranchDetails(context);
        if (branchDetails == null || branchDetails.getBranchno().length() == 0) {
            Utility.showToast(context, "Please enter branch details first !!");
            return faceList;
        }

     /*   String selectQuery = "SELECT  * FROM " + TABLE_FACE_DATA;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);*/

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_FACE_DATA, new String[]{"id", "name", "memberID", "distance", "extra", "startTime", "endTime", "timeFormat", "userImg", "Branchno"}, "Branchno = ?", new String[]{branchDetails.getBranchno()}, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                byte[] imgByte = cursor.getBlob(8);

                FaceData faceData = new FaceData(cursor.getString(0), cursor.getString(1), cursor.getString(2), Float.parseFloat(cursor.getString(3)), Utility.readByte(cursor.getBlob(4)), cursor.getString(5), cursor.getString(6), cursor.getString(7), BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length), cursor.getString(9));
                faceList.add(faceData);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        Log.d("Database", ">>> facelist >>>" + new Gson().toJson(faceList));
        return faceList;
    }

    public List<FaceImgData> getAllFaceImages() {

        List<FaceImgData> faceImgDataList = new ArrayList();

        String selectQuery = "SELECT  * FROM " + TABLE_IMAGE_DATA;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                byte[] imgByte = cursor.getBlob(2);

                FaceImgData faceImgData = new FaceImgData(cursor.getString(0), cursor.getString(1), BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length), Utility.readByte(cursor.getBlob(3)), Integer.parseInt(cursor.getString(4)));

                faceImgDataList.add(faceImgData);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return faceImgDataList;
    }

    public int getFaceImageCount() {
        Cursor cursor = null;
        SQLiteDatabase db = null;
        int cnt = 0;
        try {
            String countQuery = "SELECT  * FROM " + TABLE_IMAGE_DATA;
            db = this.getReadableDatabase();
            cursor = db.rawQuery(countQuery, null);
            cnt = cursor.getCount();
            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cnt;
    }

    public int getFaceDataCount() {
        Cursor cursor = null;
        SQLiteDatabase db = null;
        int cnt = 0;
        try {
            String countQuery = "SELECT  * FROM " + TABLE_FACE_DATA;
            db = this.getReadableDatabase();
            cursor = db.rawQuery(countQuery, null);
            cnt = cursor.getCount();
            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cnt;
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    public void loadUserDataTolocal(HashMap<String, FirebaseUserData> data) {
        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_FACE_DATA, "1", null);
        try {

            for (FirebaseUserData firebaseUserData : data.values()) {
                ContentValues values = new ContentValues();
                values.put("name", firebaseUserData.getName());
                values.put("memberID", firebaseUserData.getMemberID());
                values.put("distance", Float.parseFloat(firebaseUserData.getDistance()));
                values.put("extra", Utility.makeByte(new Gson().fromJson(firebaseUserData.getExtra(), Object.class)));
                values.put("startTime", firebaseUserData.getStartTime());
                values.put("endTime", firebaseUserData.getEndTime());
                values.put("timeFormat", firebaseUserData.getTimeFormat());
                values.put("userImg", getBitmapAsByteArray(Utility.getBitmapFromString(firebaseUserData.getUserImage())));
                values.put("Branchno", firebaseUserData.getBranchno());

                int rows = db.update(TABLE_FACE_DATA, values, "memberID" + " = " + firebaseUserData.getMemberID(), null);
                if (rows == 0) {
                    db.insert(TABLE_FACE_DATA, null, values);
                }
            }

            db.close();

        } catch (Exception e) {
            Log.w("Error !!", e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadImgDataToLocal(HashMap<String, FirebaseImgData> data) {
        SQLiteDatabase db = this.getWritableDatabase();
        //  db.delete(TABLE_IMAGE_DATA, "1", null);

        try {
            for (FirebaseImgData firebaseImgData : data.values()) {
                ContentValues values = new ContentValues();
                values.put("id", firebaseImgData.getId());
                values.put("userId", firebaseImgData.getUserId());
                values.put("imageData", getBitmapAsByteArray(Utility.getBitmapFromString(firebaseImgData.getImageData())));
                values.put("extra", Utility.makeByte(new Gson().fromJson(firebaseImgData.getExtra(), Object.class)));
                values.put("isSelected", Integer.parseInt(firebaseImgData.getIsSelected()));

                int rows = db.update(TABLE_IMAGE_DATA, values, "id ='" + firebaseImgData.getId() + "'", null);
                if (rows == 0) {
                    long d = db.insert(TABLE_IMAGE_DATA, null, values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
    }

    public void deleteMemberfromDB(FaceData faceData) {
        SQLiteDatabase db1 = this.getWritableDatabase();
        SQLiteDatabase db2 = this.getWritableDatabase();
        db1.delete(TABLE_FACE_DATA, "id" + "=?", new String[]{faceData.getId()});
        db2.delete(TABLE_IMAGE_DATA, "userId" + "=?", new String[]{faceData.getMemberID()});
        db1.close();
        db2.close();
    }
}