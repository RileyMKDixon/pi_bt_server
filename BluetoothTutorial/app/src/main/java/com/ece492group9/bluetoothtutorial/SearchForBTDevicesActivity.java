package com.ece492group9.bluetoothtutorial;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

public class SearchForBTDevicesActivity extends AppCompatActivity {

    public final static String activityID = "com.ece492group9.bluetoothTutorial.SearchForBTDevicesActivity";

    private ArrayList<BTDeviceData> btDevices;
    private ArrayAdapter<BTDeviceData> btDeviceListAdapter;
    private BluetoothAdapter bluetoothAdapter;

    private ListView btDeviceList;


    @Override
    protected void onStart(){
        super.onStart();
        discoverBTDevices();
        btDeviceListAdapter = new ArrayAdapter<BTDeviceData>(this, R.layout.bt_device_list_layout, btDevices);
        btDeviceList.setAdapter(btDeviceListAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_device);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar toolbarActions = getSupportActionBar();
        toolbarActions.setDisplayHomeAsUpEnabled(true);
        toolbarActions.setTitle("Searching For Smart AVL Device");


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btDeviceList = findViewById(R.id.BTList);
        btDevices = new ArrayList<BTDeviceData>();

        btDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BTDeviceData selectedDevice = btDevices.get(position);
                Log.i("BT_SELECTED_NAME", "Selected: " + selectedDevice.getBTDeviceName());
                Log.i("BT_SELECTED_ADDRESS", "Selected: " + selectedDevice.getBTDeviceAddress());

                //ATTEMPT CONNECTION WITH SELECTED DEVICE HERE

                Intent returnToMainIntent = new Intent();
                returnToMainIntent.putExtra(MainActivity.btDeviceDataID, selectedDevice);
                setResult(RESULT_OK, returnToMainIntent);
                finish();

            }
        });




    }





    public void discoverBTDevices(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        bluetoothAdapter.startDiscovery();
        registerReceiver(receiver, filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem selection){
        switch(selection.getItemId()){
            case android.R.id.home:
                Log.i("SearchForBTDevices", "Back Button Pressed");
                setResult(Activity.RESULT_CANCELED);
                finish();
        }
        return true;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                //Device Found!
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = btDevice.getName();
                String deviceHardwareAddress = btDevice.getAddress();
                Log.i("BT_DEVICE_FOUND", "Device Name: " + deviceName);
                Log.i("BT_DEVICE_FOUND", "Device Address: " + deviceHardwareAddress);


                BTDeviceData newDevice = new BTDeviceData(btDevice);
                if(!btDevices.contains(newDevice)) {
                    btDevices.add(newDevice);
                    btDeviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(receiver);
    }
}
