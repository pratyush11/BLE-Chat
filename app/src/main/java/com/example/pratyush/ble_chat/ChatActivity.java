package com.example.pratyush.ble_chat;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ChatActivity extends AppCompatActivity {
    Button send = (Button) findViewById(R.id.button_chatbox_send);
    EditText chatbox = (EditText) findViewById(R.id.edittext_chatbox);
    public String message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = chatbox.getText().toString();
                broadcastUpdate(message);
            }
        });


    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);


    /*public static BluetoothGattService createTimeService() {
        BluetoothGattService service = new BluetoothGattService(TIME_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // Current Time characteristic
        BluetoothGattCharacteristic currentTime = new BluetoothGattCharacteristic(CURRENT_TIME,
                //Read-only characteristic, supports notifications
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattDescriptor configDescriptor = new BluetoothGattDescriptor(CLIENT_CONFIG,
                //Read/write descriptor
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
        currentTime.addDescriptor(configDescriptor);

        // Local Time Information characteristic
        BluetoothGattCharacteristic localTime = new BluetoothGattCharacteristic(LOCAL_TIME_INFO,
                //Read-only characteristic
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        service.addCharacteristic(currentTime);
        service.addCharacteristic(localTime);

        return service;
    }*/

    }
}

