package com.ece492group9.bluetoothtutorial;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class CommHandler extends Thread {

    private final static int bufferSize = 2048;
    public final static int MESSAGE_READ = 0;
    public final static int MESSAGE_WRITE = 1;
    public final static int CONNECTION_FAILED = 2;


    private BTDeviceData connectedDevice;
    private BluetoothSocket serverSocket;
    private InputStream readStream;
    private OutputStream writeStream;

    private Handler handler; //facilitate communication between the entity and view classes

    public CommHandler(BTDeviceData connectedDevice, Handler handler){
        this.connectedDevice = connectedDevice;
        try {
            this.serverSocket = connectedDevice.getBTDevice()
                    .createRfcommSocketToServiceRecord(connectedDevice.getRaspberryPiUUID());
            this.readStream = serverSocket.getInputStream();
            this.writeStream = serverSocket.getOutputStream();

            this.handler = handler;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        byte[] readerBuffer = new byte[bufferSize];

        try {
            this.serverSocket.connect();
            while(true){
                int numBytesRead = readStream.read(readerBuffer);
                byte[] readerBufferSliced = Arrays.copyOfRange(readerBuffer,0, numBytesRead-1);
                if(numBytesRead != 0) {
                    String stringRead = new String(readerBufferSliced);
                    Log.i("CommHandler", "Msg_Read: " + stringRead);
                    Message messageRead = handler.obtainMessage(MESSAGE_READ, stringRead);
                    messageRead.sendToTarget();
                }
            }
        } catch (IOException e) {
            Message connectionFailed = handler.obtainMessage(CONNECTION_FAILED);
            connectionFailed.sendToTarget();
            e.printStackTrace();
        }


    }

    public void write(String stringToSend){
        byte[] writerBuffer = stringToSend.getBytes();
        try{
            Log.i("CommHandler", "Msg_Write: " + stringToSend);
            writeStream.write(writerBuffer);
        }catch(IOException e){
            Message messageWrite = handler.obtainMessage(MESSAGE_WRITE, stringToSend);
            messageWrite.sendToTarget();
            e.printStackTrace();
        }

    }

    public void cancel(){
        try{
            serverSocket.close();
        }catch(IOException ioe){
            Log.e("CommHandler", "Socket failed to close.");
        }

    }

}
