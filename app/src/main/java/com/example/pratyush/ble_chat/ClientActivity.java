package com.example.pratyush.ble_chat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private BluetoothGatt mGatt;
    TextView DeviceInfoTextView;
    Button startScanning;
    Button stopScanning;
    Button disconnect;
    public ListView lv;
    public List<BluetoothDevice> mDevices;
    boolean mEchoInitialized;
    public static String SERVICE_STRING = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    public static String CHARACTERISTIC_ECHO_STRING = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        mDevices = new ArrayList<BluetoothDevice>();
        DeviceInfoTextView = (TextView) findViewById(R.id.client_device_info_text_view);
        startScanning = (Button) findViewById(R.id.start_scanning_button);
        stopScanning = (Button) findViewById(R.id.stop_scanning_button);
        disconnect = (Button) findViewById(R.id.disconnect_button);
        EditText messageEditText = (EditText) findViewById(R.id.message_edit_text);
        Button send = (Button) findViewById(R.id.send_message_button);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        startScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });
        stopScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopScan();
            }
        });
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //disconnectGattServer();
            }
        });
        lv = (ListView) findViewById(R.id.device_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object o = lv.getItemAtPosition(i);
                String device = o.toString();
                connect(mDevices.get(i));
                /*Intent intent = new Intent(ClientActivity.this, ChatActivity.class);
                startActivity(intent);*/
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver3);
        mBluetoothAdapter.cancelDiscovery();
        mGatt.disconnect();
        mGatt.close();
    }

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(device!=null)
                    mDevices.add(device);

                lv.setAdapter(new ArrayAdapter<BluetoothDevice>(ClientActivity.this, android.R.layout.simple_list_item_1, mDevices));
                //ArrayAdapter<BluetoothDevice> arrayAdapter = new ArrayAdapter(ClientActivity.this, android.R.layout.simple_list_item_1, mDevices);
            }
        }
    };


    private void startScan() {
        mBluetoothAdapter.startDiscovery();
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
    }

    private void stopScan() {
        mBluetoothAdapter.cancelDiscovery();
    }

    /*private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }*/

    private void connect(BluetoothDevice device) {
        //mGatt = device.connectGatt(this, true, mCallback);
        final BluetoothDevice device1 = device;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (device1 != null) {
                    mGatt = device1.connectGatt(getApplicationContext(), false, mCallback);
                    stopScan();
                }
            }
        });
    }



    private void sendMessage() {
        EditText messageEditText = (EditText) findViewById(R.id.message_edit_text);
        BluetoothGattCharacteristic characteristic = BluetoothUtils.findEchoCharacteristic(mGatt);
        if (characteristic == null) {
            Log.i("Characteristic", "Unable to find echo characteristic.");
            try
            {
                mGatt.disconnect();
                mGatt.close();
            }
            catch(Exception e)
            {
                Log.i("Disconnect", "Problem disconnecting.");
            }
            return;
        }

        String message = messageEditText.getText().toString();
        Log.i("send", "Sending message: " + message);

        byte[] messageBytes = StringUtils.bytesFromString(message);
        if (messageBytes.length == 0) {
            //logError("Unable to convert message to bytes");
            return;
        }

        characteristic.setValue(messageBytes);

        boolean success = mGatt.writeCharacteristic(characteristic);
        if (success) {
            Log.i("write", "Wrote: " + StringUtils.byteArrayInHexFormat(messageBytes));
        } else {
            Log.i("write", "Failed to write data");
        }
    }

    public void initializeEcho() {
        mEchoInitialized = true;
    }

    public BluetoothGattCallback mCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    final BluetoothGatt mGatt = gatt;
                    Handler handler;
                    handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mGatt.discoverServices();
                        }
                    });
                    //gatt.discoverServices();

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    try
                    {
                        //onDestroy();
                        Log.i("no_conn", "Connection unsuccessful with status"+status);
                        //mGatt.disconnect();
                        mGatt.close();
                    }
                    catch(Exception e)
                    {

                    }
                }
            }
            /*else {
                //final int finalStatus = status;
                //Toast.makeText(ClientActivity.this, "Error!", Toast.LENGTH_SHORT).show();

            }*/
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.i("Not success", "Device service discovery unsuccessful, status " + status);
                return;
            }
            //List<BluetoothGattService> matchingServices = gatt.getServices();
            gatt.getService(UUID.fromString(SERVICE_STRING));
            List<BluetoothGattCharacteristic> matchingCharacteristics = BluetoothUtils.findCharacteristics(gatt);
            if (matchingCharacteristics.isEmpty()) {
                Log.i("No characteristics", "Unable to find characteristics.");
                return;
            }

            //log("Initializing: setting write type and enabling notification");
            for (BluetoothGattCharacteristic characteristic : matchingCharacteristics) {
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                enableCharacteristicNotification(gatt, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
               Log.i("Write successful", "Characteristic written successfully");
            } else {
                Log.i("Unsuccessful", "Characteristic write unsuccessful, status: " + status);
                //mGatt.disconnect();
                mGatt.close();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("Read success", "Characteristic read successfully");
                readCharacteristic(characteristic);
            } else {
                Log.i("Read unsuccessful", "Characteristic read unsuccessful, status: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i("Characteristic change", "Characteristic changed, " + characteristic.getUuid().toString());
            readCharacteristic(characteristic);
        }

        private void enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            boolean characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true);
            if (characteristicWriteSuccess) {
                Log.i("Notific success", "Characteristic notification set successfully for " + characteristic.getUuid().toString());
                if (BluetoothUtils.isEchoCharacteristic(characteristic)) {
                    initializeEcho();
                }
            } else {
                Log.i("Notification failure.", "Characteristic notification set failure for " + characteristic.getUuid().toString());
            }
        }

        private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
            byte[] messageBytes = characteristic.getValue();
            Log.i("read", "Read: " + StringUtils.byteArrayInHexFormat(messageBytes));
            String message = StringUtils.stringFromBytes(messageBytes);
            if (message == null) {
                //logError("Unable to convert bytes to string");
                return;
            }

            Log.i("Received", "Received message: " + message);
        }
    };


}
