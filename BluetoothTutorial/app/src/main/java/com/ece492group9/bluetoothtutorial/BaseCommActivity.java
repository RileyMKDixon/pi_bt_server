package com.ece492group9.bluetoothtutorial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BaseCommActivity extends AppCompatActivity {

    public final static String BaseCommActivityID = "bluetoothTutorial.BaseCommActivity";

    private CommHandler commHandler;
    private Handler handler;

    @Override
    protected  void onStart(){
        super.onStart();
        BTDeviceData connectedDevice = getIntent().getParcelableExtra(MainActivity.btDeviceToConnect);
        commHandler = new CommHandler(connectedDevice, handler);
        commHandler.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_comm);

        Toolbar baseCommToolbar = findViewById(R.id.baseCommToolbar);
        setSupportActionBar(baseCommToolbar);
        ActionBar toolbarActions = getSupportActionBar();
        toolbarActions.setTitle("SMART AVL DEVICE ID");

        final TextView receivedTextView = findViewById(R.id.receivedBody);
        final EditText sentTextView = findViewById(R.id.sentBody);
        Button closeButton = findViewById(R.id.closeButton);
        Button sendButton = findViewById(R.id.sendButton);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent closeConnectionIntent = new Intent();
                closeConnectionIntent.putExtra(BaseCommActivityID, "Closed Normally.");
                Log.i("BaseCommActivity", "Closing socket.");
                setResult(RESULT_OK);
                finish();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stringToSend = sentTextView.getText().toString();
                commHandler.write(stringToSend);
                Log.d("BaseCommActivity", "Attempting to send message.");
                sentTextView.setText("");
            }
        });


        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message inputMessage){
                int whatHappened = inputMessage.what;
                switch (whatHappened){
                    case CommHandler.MESSAGE_READ:
                        String receivedMessage = (String) inputMessage.obj;
                        receivedTextView.setText(receivedMessage);
                        break;
                    case CommHandler.MESSAGE_WRITE:
                        Toast.makeText(getApplicationContext(), "Failed to send message", Toast.LENGTH_LONG).show();
                        break;
                    case CommHandler.CONNECTION_FAILED:
                        Intent connectionFailedIntent = new Intent();
                        connectionFailedIntent.putExtra(BaseCommActivityID, "Connection Failed");
                        setResult(RESULT_CANCELED, connectionFailedIntent);
                        finish();
                }

            }
        };

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        commHandler.cancel();
    }

}
