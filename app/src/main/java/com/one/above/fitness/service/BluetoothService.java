package com.one.above.fitness.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.one.above.fitness.database.SharedPreference;
import com.one.above.fitness.utility.Utility;

import org.json.JSONArray;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class BluetoothService {
    public BluetoothService(Context context) {
        this.mContext = context;
    }

    public static Timer timer = null;
    private String BluetoothDeviceName = "Microcub";
    private Context mContext;
    public static DataOutputStream outputStream;
    private final String TAG = "BluetoothService";
    private String Msg = "TEST";
    public static boolean isDataSend = false;
    public static boolean isFlashDataSend = false;
    private String memberNo = "";
    private clientSock clientSock = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BluetoothSocket clientSocket;
    private void sendDataViaBluetooth() {
        try {
            BluetoothDeviceName = SharedPreference.getBluetoothDeviceName(mContext);
            if (clientSocket == null)
                clientSocket = getBluetoothSocket();

            BluetoothDevice device = clientSocket.getRemoteDevice();

            Log.d(TAG, ">>> device name >>>" + device.getName() + ">>> device address >>>" + device.getAddress());

            if (device != null && device.getName() != null && !device.getName().toLowerCase().contains(BluetoothDeviceName.toLowerCase())) {
                Utility.showToast(mContext, BluetoothDeviceName + "device not found in bluetooth history !!");
                return;
            }
            if (!clientSocket.isConnected()) {
                try {
                    clientSocket.connect();
                    Log.e(TAG, "Socket connect in 1 attempt !!!");
                    outputStream = new DataOutputStream(clientSocket.getOutputStream());

                } catch (Exception e) {
                    try {
                        clientSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                        if (!clientSocket.isConnected()) {
                            clientSocket.connect();
                            Log.e(TAG, "Socket connected 2 attempt !!");
                            outputStream = new DataOutputStream(clientSocket.getOutputStream());
                        }
                    } catch (Exception e1) {
                        try {
                            Method m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
                            clientSocket = (BluetoothSocket) m.invoke(device, 1);
                            if (!clientSocket.isConnected()) {
                                clientSocket.connect();
                                Log.e(TAG, "Socket connected 3 attempt !!");
                                outputStream = new DataOutputStream(clientSocket.getOutputStream());
                            }
                        } catch (Exception e2) {
                            Log.e(TAG, "Socket Failed to connect !!!" + e2.getMessage());
                        }
                    }
                }
            }

            if (clientSocket.isConnected()) {
                clientSock.write();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startBluetoothService(String message, String no) {
        try {

            BluetoothDeviceName = SharedPreference.getBluetoothDeviceName(mContext);
            clientSock = new clientSock();

            if (!bluetoothAdapter.isEnabled()) {
                Utility.showToast(mContext, "Please turn on bluetooth to connect !!\n and connect to device " + BluetoothDeviceName);
                return;
            } else {

                Msg = message;
                memberNo = no;
                sendDataViaBluetooth();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BluetoothSocket getBluetoothSocket() {
        BluetoothSocket mBSocket = null;

        // inside doInBackground() function
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            try {
                for (BluetoothDevice bt : bluetoothAdapter.getBondedDevices()) {
                    if (bt.getName().toLowerCase().contains(BluetoothDeviceName.toLowerCase())) {
                        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(bt.getAddress());
                        bluetoothAdapter.cancelDiscovery();

                        mBSocket = device.createInsecureRfcommSocketToServiceRecord(Utility.MY_UUID_INSECURE);
                        return mBSocket;
                    }
                }
            } catch (IOException e) {
                if (mBSocket != null) {
                    try {
                        mBSocket.close();
                    } catch (Exception e1) {
                        Log.d(TAG, ">>> error >>>");
                        e1.printStackTrace();
                    }
                    mBSocket = null;
                }
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public class clientSock {
        public void write() {
            try {
                Log.d(TAG, ">>> clientSock >>> write >>>");
                //  if (!isDataSend && !isFlashDataSend) {
                outputStream.writeBytes(Msg); // anything you want
                outputStream.flush();
                Log.d(TAG, "Write success !! >" + Msg);
                if (Msg.equalsIgnoreCase("ON")) {
                    new AsyncCall().execute();
                }
                //discardRequestForSpecificTime();
                isFlashDataSend = true;
                isDataSend = true;
                //  }
            } catch (Exception e1) {
                Utility.showToast(mContext, "Something went wrong !!\nTry to reconnect Bluetooth Device !!");
                Log.d("Bluetooth", "Error >>>" + e1.getMessage());
                e1.printStackTrace();
                return;
            }
        }

    }

    private void discardRequestForSpecificTime() {

        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (isDataSend) isDataSend = false;
                    if (isFlashDataSend) isFlashDataSend = false;
                    Log.d(TAG, ">>>> discardRequestForSpecificTime >>>" + isDataSend);
                }
            }, 0, 1000);
        } else {
            //timer.cancel();
        }
    }

    private class AsyncCall extends AsyncTask<Void, Void, Void> {
        JSONArray jsonArray = null;

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "memberNo  >>>" + memberNo);
            jsonArray = WebService.saveAttendance(memberNo);
            Log.d(TAG, "Response >>>" + new Gson().toJson(jsonArray));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (jsonArray != null && jsonArray.length() > 0) {
                Log.d(TAG, ">>>> server response >>>" + jsonArray.toString());
                Utility.showToast(mContext, "Attendance saved successfully !!");
                Msg = "FOFF";
                clientSock.write();
            }
        }
    }
}
