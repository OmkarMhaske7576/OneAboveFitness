package com.one.above.fitness.bluetooth_communication;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.one.above.fitness.utility.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionService";
    private static final String appName = "ONE_ABOVE_FITNESS";
    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    Dialog mProgressDialog;
    private ConnectedThread mConnectedThread;
    private TextView connectedDeviceTxt;

    public BluetoothConnectionService(Context context, TextView connectedDeviceTxt) {
        mContext = context;
        this.connectedDeviceTxt = connectedDeviceTxt;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {

        // The local server socket
        private BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, Utility.MY_UUID_INSECURE);

                //  tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(appName, Utility.MY_UUID_INSECURE);
                Log.d(TAG, "AcceptThread: Setting up Server using: " + Utility.MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start.....");

                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }
            //talk about this is in the 3rd
            if (socket != null) {
                connected(socket, mmDevice);
            }

            Log.i(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        int sdk = Build.VERSION.SDK_INT;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: " + Utility.MY_UUID_INSECURE);
                if (sdk >= 10) {
                    tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);
                } else {
                    tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
                }
                //  tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;
            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exceptionRa
                if (!mmSocket.isConnected()) {
                    mmSocket.connect();
                    Log.e(TAG, "SOCKET connected 1 attempt !!");
                    Log.d(TAG, "run: SOCKET connected.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Close the socket
                Log.d(TAG, "IOException >>>>" + e.getMessage());
                try {
                    //mmSocket.close();
                    mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
                    if (!mmSocket.isConnected()) {
                        mmSocket.connect();
                        Log.e(TAG, "SOCKET connected  2 attempt !!");
                    }
                } catch (Exception e1) {
                    try {
                        Method m = mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                        mmSocket = (BluetoothSocket) m.invoke(mmDevice, 1);
                        if (!mmSocket.isConnected()) {
                            mmSocket.connect();
                            Log.e(TAG, "SOCKET connected 3 attempt!!");
                        }
                    } catch (Exception e2) {
                        Log.d(TAG, "run: Closed Socket." + e.getMessage());
                        Log.e(TAG, "mConnectThread: run: Unable to close connection in socket ");
                        Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + Utility.MY_UUID_INSECURE);
                    }
                }
                connected(mmSocket, mmDevice);
            }
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**
     * AcceptThread starts and sits waiting for a connection.
     * Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     **/
    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: Started.");

        //initprogress dialog
       /* mProgressDialog = new ProgressDialog(mContext, R.style.MyTheme);
        mProgressDialog.show(mContext, "Connecting Bluetooth"
                , "Please Wait...", true);*/

        mProgressDialog = Utility.showLoadingDialog("Connecting Bluetooth...", mContext);
        mProgressDialog.show();

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    /**
     * Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     * receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try {
                mProgressDialog.dismiss();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    Log.d(TAG, "InputStream: ");
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                connectedDeviceTxt.setVisibility(View.VISIBLE);
                connectedDeviceTxt.setText("Write Failed !!" + mmDevice.getName());
                mmOutStream.write(bytes);
                Log.d(TAG, "Write successfully !!" + text);
                connectedDeviceTxt.setText("Write Success !!" + mmDevice.getName());
            } catch (IOException e) {
                if (mContext != null && !mmSocket.isConnected()) {
                    Utility.showToast(mContext, "Socket not established !!\nTry to reconnect to device !!");
                    Utility.showToast(mContext, "Please click on start connection to start again");
                }
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());

            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        try {
            // Create temporary object
            ConnectedThread r;

            // Synchronize a copy of the ConnectedThread
            Log.d(TAG, "write: Write Called.");
            //perform the write
            mConnectedThread.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
























