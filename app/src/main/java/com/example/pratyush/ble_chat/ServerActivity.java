package com.example.pratyush.ble_chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.pratyush.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerActivity extends AppCompatActivity {

    private List<BluetoothDevice> mDevices;
    private BluetoothGattServer mGattServer;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    Button restart;
    TextView DeviceInfoTextView;
    //public static String SERVICE_STRING = "18902a9a-1f4a-44fe-936f-14c8eea41800";
    public static UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

    //public static String CHARACTERISTIC_STRING = "18902a9a-1f4a-44fe-936f-14c8eea41801";
    public static UUID CHARACTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private int REQUEST_ENABLE_BT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        restart = (Button) findViewById(R.id.restart_server_button);
        mDevices = new ArrayList<>();

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        DeviceInfoTextView = (TextView) findViewById(R.id.server_device_info_text_view);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartServer();
            }
        });
        /*GattServerCallback gattServerCallback = new GattServerCallback();
        mGattServer = mBluetoothManager.openGattServer(this, gattServerCallback);

        setupServer();
        startAdvertising();*/

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            finish();
            return;
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        GattServerCallback gattServerCallback = new GattServerCallback();
        mGattServer = mBluetoothManager.openGattServer(this, gattServerCallback);

        @SuppressLint("HardwareIds")
        String deviceInfo = "Device Info" + "\nName: " + mBluetoothAdapter.getName() + "\nAddress: " + mBluetoothAdapter.getAddress();
        DeviceInfoTextView.setText(deviceInfo);

        setupServer();
        startAdvertising();

    }

    @Override
    protected void onPause() {
        super.onPause();
       // stopServer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            finish();
            return;
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        GattServerCallback gattServerCallback = new GattServerCallback();
        mGattServer = mBluetoothManager.openGattServer(this, gattServerCallback);

        @SuppressLint("HardwareIds")
        String deviceInfo = "Device Info" + "\nName: " + mBluetoothAdapter.getName() + "\nAddress: " + mBluetoothAdapter.getAddress();
        DeviceInfoTextView.setText(deviceInfo);

        setupServer();
        startAdvertising();*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {

                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupServer() {
        /*BluetoothGattService service = new BluetoothGattService(SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mGattServer.addService(service);*/
        BluetoothGattService service1 = new BluetoothGattService(SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
                CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        service1.addCharacteristic(writeCharacteristic);

        mGattServer.addService(service1);
        mGattServer.getService(SERVICE_UUID);
    }

    private void stopServer() {
        if (mGattServer != null) {

            mGattServer.close();
        }
    }

    private void restartServer() {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1);
        startActivity(discoverableIntent);
        stopServer();
        setupServer();
        startAdvertising();
    }

    private void startAdvertising() {
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 20);
        startActivity(discoverableIntent);
        Log.i("discoverable", "Is Discoverable");
    }

    private void notifyCharacteristic(byte[] value, UUID uuid) {
            BluetoothGattService service = mGattServer.getService(SERVICE_UUID);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid);
            Log.i("notify_char", "Notifying characteristic " + characteristic.getUuid().toString()
                    + ", new value: " + StringUtils.byteArrayInHexFormat(value));

            characteristic.setValue(value);
            boolean confirm = BluetoothUtils.requiresConfirmation(characteristic);
            for(BluetoothDevice device : mDevices) {
                mGattServer.notifyCharacteristicChanged(device, characteristic, confirm);
            }
    }

    public void sendResponse(BluetoothDevice device, int requestId, int status, int offset, byte[] value) {
        mGattServer.sendResponse(device, requestId, status, 0, null);
    }

    private void sendReverseMessage(byte[] message) {
        byte[] response = ByteUtils.reverse(message);
        Log.i("rev_send", "Sending: " + StringUtils.byteArrayInHexFormat(response));
        notifyCharacteristicEcho(response);

    }

    public void notifyCharacteristicEcho(byte[] value) {
        notifyCharacteristic(value, CHARACTERISTIC_UUID);
    }



    private class GattServerCallback extends BluetoothGattServerCallback {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                addDevice(device);
                Log.i("Connect", "Connected to "+device);
                /*Intent intent = new Intent(ServerActivity.this, ChatActivity.class);
                startActivity(intent);*/
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                removeDevice(device);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device,
                                                int requestId,
                                                int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

            Log.i("char_read", "onCharacteristicReadRequest "
                    + characteristic.getUuid().toString());

            if (BluetoothUtils.requiresResponse(characteristic)) {
                sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
            }
        }
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device,
                                                 int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite,
                                                 boolean responseNeeded,
                                                 int offset,
                                                 byte[] value) {
            super.onCharacteristicWriteRequest(device,
                    requestId,
                    characteristic,
                    preparedWrite,
                    responseNeeded,
                    offset,
                    value);
            Log.i("char_write", "onCharacteristicWriteRequest" + characteristic.getUuid().toString()
                    + "\nReceived: " + StringUtils.byteArrayInHexFormat(value));

            if (CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
                sendReverseMessage(value);
            }
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.i("send_notific", "onNotificationSent");
        }
    }

    public void addDevice(BluetoothDevice device)
    {
        mDevices.add(device);
    }

    public void removeDevice(BluetoothDevice device)
    {
        mDevices.remove(device);
    }



}
