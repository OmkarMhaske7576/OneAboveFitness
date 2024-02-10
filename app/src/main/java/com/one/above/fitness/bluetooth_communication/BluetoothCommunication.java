package com.one.above.fitness.bluetooth_communication;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.one.above.fitness.R;
import com.one.above.fitness.utility.Utility;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class BluetoothCommunication extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "BluetoothCommunication";
    public static BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discoverable;
    public static BluetoothConnectionService mBluetoothConnection = null;
    Button btnStartConnection;
    Button btnSend;
    EditText etSend;
    Context context;
    TextView connectedDeviceTxt;
    public static HashMap<String, BluetoothDevice> deviceHashMap = new HashMap<>();
    public static BluetoothDevice mBTDevice;
    public static ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    //    HashMap<String, BluetoothDevice> devicesMap = new HashMap<>();
    public static DeviceListAdapter mDeviceListAdapter;
    public static ListView lvNewDevices;

    // Create a BroadcastReceiver for ACTION_FOUND
    public static final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    public static final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    public static BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceHashMap.put(device.getAddress(), device);
                mBTDevices = new ArrayList<BluetoothDevice>(deviceHashMap.values());

                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);

                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    public static final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
       /* LocalBroadcastManager.getInstance(BluetoothCommunication.this).unregisterReceiver(mBroadcastReceiver1);
        LocalBroadcastManager.getInstance(BluetoothCommunication.this).unregisterReceiver(mBroadcastReceiver2);
        LocalBroadcastManager.getInstance(BluetoothCommunication.this).unregisterReceiver(mBroadcastReceiver3);
        LocalBroadcastManager.getInstance(BluetoothCommunication.this).unregisterReceiver(mBroadcastReceiver4);
*/
        /*unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);*/
        //mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_connection_layout);
        context = getApplicationContext();
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);
        btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        connectedDeviceTxt = (TextView) findViewById(R.id.connectedDevice);
        mBTDevices = new ArrayList<>();
        deviceHashMap = new HashMap<>();

        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        btnSend = (Button) findViewById(R.id.btnSend);
        etSend = (EditText) findViewById(R.id.editText);

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        lvNewDevices.setOnItemClickListener(BluetoothCommunication.this);

        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                enableDisableBT();
            }
        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBTDevice != null) {
                    try {
                        //Utility.MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                        Utility.MY_UUID_INSECURE = getDeviceUid(mBTDevice)[0].getUuid();
                        Log.e(TAG, "Utility.MY_UUID_INSECURE >>>" + Utility.MY_UUID_INSECURE.toString());
                        startConnection();
                    } catch (Exception e) {

                    }
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                if (mBluetoothConnection != null)
                    mBluetoothConnection.write(bytes);
            }
        });

    }

    //create method for starting connection
//***remember the connection will fail and app will crash if you haven't paired first
    public void startConnection() {
        startBTConnection(mBTDevice, Utility.MY_UUID_INSECURE);
    }

    /**
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        if (mBluetoothConnection != null)
            mBluetoothConnection.startClient(device, uuid);
    }

    public void enableDisableBT() {
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: disabling BT.");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "permission denied !!");
            }
            boolean flag = mBluetoothAdapter.disable();
            Log.e(TAG, "flag >>>!!" + flag);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }

    }


    public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, intentFilter);

    }

    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()) {

            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.e(TAG, " >>> Discovering devices !!!" + isGpsEnabled);
            if (!isGpsEnabled) {
                Utility.showToast(context, "Please Enabled location permission to discover devices !!");
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
            }
            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     * <p>
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Utility.showToast(context, "Android version is not supported for bluetooth !!");
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        try {

            String deviceName = mBTDevices.get(i).getName();
            String deviceAddress = mBTDevices.get(i).getAddress();

            Log.d(TAG, "onItemClick: deviceName = " + deviceName);
            Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

            AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothCommunication.this, R.style.MyAlertDialogTheme);

            builder.setMessage("Are you sure you want to connect to device " + deviceName + " ?")
                    .setTitle("Connect device")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //first cancel discovery because its very memory intensive.
                            mBluetoothAdapter.cancelDiscovery();
                            //create the bond.
                            //NOTE: Requires API 17+? I think this is JellyBean
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                Log.d(TAG, "Trying to pair with " + deviceName);
                                boolean isBondCreate = mBTDevices.get(i).createBond();
                                boolean isAlreadyPaired = checkDeviceIsAlreadyPaired(mBluetoothAdapter, mBTDevices.get(i));
//                                if (!isAlreadyPaired) {
//                                    boolean isBondCreate = mBTDevices.get(i).createBond();
//                                }
////                                boolean isBondCreate = mBTDevices.get(i).createBond();
//                                boolean isBondCreate = createBond(mBTDevices.get(i));
//                                Log.e(TAG, isAlreadyPaired + " >>>  isAlreadyPaired() >>>> isBondCreate " + isBondCreate);
                                mBTDevice = mBTDevices.get(i);
                                mBluetoothConnection = new BluetoothConnectionService(BluetoothCommunication.this, connectedDeviceTxt);
                            } else {
                                Utility.showToast(context, "Android version is not supported for bluetooth !!");
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // CANCEL
                        }
                    });

            builder.create();
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean createBond(BluetoothDevice btDevice) {
        try {
            Log.d("pairDevice()", "Start Pairing...");
            Method m = btDevice.getClass().getMethod("createBond", (Class[]) null);
            Boolean returnValue = (Boolean) m.invoke(btDevice, (Object[]) null);
            Log.d("pairDevice()", "Pairing finished.");
            return returnValue;

        } catch (Exception e) {
            Log.e("pairDevice()", e.getMessage());
        }
        return false;
    }

    private boolean checkDeviceIsAlreadyPaired(BluetoothAdapter bluetoothAdapter, BluetoothDevice device1) {
        boolean flag = false;
        Set<BluetoothDevice> set_pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.e(TAG, "set_pairedDevices  >>>" + new Gson().toJson(set_pairedDevices));
        for (BluetoothDevice device : set_pairedDevices) {
            if (device.getAddress().equalsIgnoreCase(device1.getAddress())) {
                flag = true;
                return flag;
            }
        }
        return flag;
    }

    public ParcelUuid[] getDeviceUid(BluetoothDevice device) {
        int deviceAPiVer = Integer.valueOf(android.os.Build.VERSION.SDK);
        ParcelUuid[] supportedUuids = new ParcelUuid[5];
        boolean result = device.fetchUuidsWithSdp();
        Log.w(TAG, " >>> fetchUuidsWithSdp >>>" + result);
        if (deviceAPiVer >= 15) {
            supportedUuids = device.getUuids();
        } else {
            try {
                Class cl = Class.forName("android.bluetooth.BluetoothDevice");
                Class[] params = {};
                Method method = cl.getMethod("getUuids", params);
                Object[] args = {};
                supportedUuids = (ParcelUuid[]) method.invoke(device, args);
            } catch (Exception e) {
                // no op
                Log.e("uuids", "Activation of getUuids() via reflection failed: " + e);
            }
        }
        return supportedUuids;
    }

}
