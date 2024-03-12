package com.one.above.fitness.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ApplicationService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("------------------------- Service started by user -------------------");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        try {

            super.onTaskRemoved(rootIntent);
            System.out.println("------------------------- onTaskRemoved  Remove Broadcasters ----------------------");
/*
            if (BluetoothCommunication.mBroadcastReceiver1 != null && BluetoothCommunication.mBroadcastReceiver2 != null
                    && BluetoothCommunication.mBroadcastReceiver3 != null && BluetoothCommunication.mBroadcastReceiver4 != null) {
                LocalBroadcastManager.getInstance(BluetoothCommunication.this).unregisterReceiver(mBroadcastReceiver1);
                LocalBroadcastManager.getInstance(BluetoothCommunication.this).unregisterReceiver(mBroadcastReceiver2);
                LocalBroadcastManager.getInstance(BluetoothCommunication.this).unregisterReceiver(mBroadcastReceiver3);
                LocalBroadcastManager.getInstance(BluetoothCommunication.this).unregisterReceiver(mBroadcastReceiver4);
            }
*/
        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}