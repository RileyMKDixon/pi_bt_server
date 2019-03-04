package com.ece492group9.bluetoothtutorial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Set;


public class MainActivity extends AppCompatActivity {

    public final static String activityID = "com.ece492group9.bluetoothTutorial.mainActivity";
    public final static String btDeviceDataID = "mainActivity.bluetoothAdapter";
    public final static String btDeviceToConnect = "mainActivity.bluetoothDevice";
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int CONNECT_TO_BT = 2;
    private final static int BASE_COMM_BT = 3;


    private BluetoothAdapter bluetoothAdapter;
    private int numPairedDevices;
    private BTDeviceData connectedDevice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button connectToRaspPi = findViewById(R.id.connectToRaspPi);
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        ActionBar toolbarActions = getSupportActionBar();
        toolbarActions.setTitle("Main Page");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        numPairedDevices = 0;
        connectedDevice = null;




        if(bluetoothAdapter == null){
            Log.e("BT_NULL", "Bluetooth is not available on this platform.");
            finish();
        }
        if(!bluetoothAdapter.isEnabled()){
            //Request Bluetooth be turned on.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        connectToRaspPi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent connectToDeviceIntent = new Intent(MainActivity.this, SearchForBTDevicesActivity.class);
                startActivityForResult(connectToDeviceIntent, CONNECT_TO_BT);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == RESULT_OK){
                Log.i("BT_ENABLE", "Bluetooth Enabled");
            }else if(resultCode == RESULT_CANCELED){
                Log.e("BT_ENABLE", "Bluetooth NOT Enabled");
            }
        }else if(requestCode == CONNECT_TO_BT){
            if(resultCode == RESULT_OK){
                connectedDevice = data.getParcelableExtra(btDeviceDataID);
                //Log.i("CONNECT_BT", "Successful Connection Made. In MainActivity.");
                //Log.i("CONNECT_BT", "Name: " + connectedDevice.getBTDeviceName());
                //Log.i("CONNECT_BT", "Address: " + connectedDevice.getBTDeviceAddress());


                Intent baseCommActivityIntent = new Intent(MainActivity.this, BaseCommActivity.class);
                //TODO:Put bluetooth thing or whatever
                baseCommActivityIntent.putExtra(btDeviceToConnect, connectedDevice);
                startActivityForResult(baseCommActivityIntent, BASE_COMM_BT);

            }else if(resultCode == RESULT_CANCELED){
                AlertDialog.Builder cancelledConnectionAlert = new AlertDialog.Builder(this);
                cancelledConnectionAlert.setTitle("Connection Cancelled.");
                cancelledConnectionAlert.setMessage("Device connection required to proceed.");
                cancelledConnectionAlert.show();
            }
        }else if(requestCode == BASE_COMM_BT){
            if(resultCode == RESULT_OK){

            }else if(resultCode == RESULT_CANCELED){
                String errMsg = data.getStringExtra(BaseCommActivity.BaseCommActivityID);
                Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getPairedDevices(){
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        if(pairedDevice.size() > 0){
            for(BluetoothDevice device : pairedDevice){
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();

                Log.i("BT_DEVICE", "Paired Device Name: " + deviceName);
                Log.i("BT_DEVICE", "Paired Device Address: " + deviceHardwareAddress);
            }
        }
    }




    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
