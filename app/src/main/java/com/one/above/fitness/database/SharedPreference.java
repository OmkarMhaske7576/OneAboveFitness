package com.one.above.fitness.database;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.one.above.fitness.pojo.BranchData;

import org.json.JSONArray;

public class SharedPreference {

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void clearSharedPreference(Context ctx) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear();
        editor.apply();
    }

    public static int getDialogTimer(Context ctx) {
        return Integer.parseInt(new Gson().fromJson(getSharedPreferences(ctx).getString("DIALOG_TIME", "10000"), String.class));
    }

    public static void setDialogTimer(Context ctx, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString("DIALOG_TIME", value);
        editor.commit();
    }

    public static String getBluetoothDeviceName(Context ctx) {
        return getSharedPreferences(ctx).getString("BluetoothDeviceName", "Microcub");
    }

    public static void setBluetoothDeviceName(Context ctx, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString("BluetoothDeviceName", value);
        editor.commit();
    }

    public static float getHyperParameter(Context ctx) {
        return Float.parseFloat(new Gson().fromJson(getSharedPreferences(ctx).getString("FACE_DISTANCE", "1.0f"), String.class));
    }

    public static void settHyperParameter(Context ctx, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString("FACE_DISTANCE", value);
        editor.commit();
    }

    public static void setBranchDetails(Context ctx, BranchData branchData) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString("BranchData", new Gson().toJson(branchData));
        editor.commit();
    }

    public static BranchData getBranchDetails(Context ctx) {
        return new Gson().fromJson(getSharedPreferences(ctx).getString("BranchData", null), BranchData.class);
    }

    public static void setBranchServerDataList(Context ctx, JSONArray jsonArray) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString("BRANCH_LIST_DATA", jsonArray.toString());
        editor.commit();
    }

    public static JSONArray getBranchServerDataList(Context ctx) {
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = new JSONArray(getSharedPreferences(ctx).getString("BRANCH_LIST_DATA", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }
        return jsonArray;
    /*      Type listType = new TypeToken<JSONArray>() {
        }.getType();


        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }
        return jsonArray;
    }
    */
    }
}