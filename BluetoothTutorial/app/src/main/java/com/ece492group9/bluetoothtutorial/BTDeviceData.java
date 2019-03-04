package com.ece492group9.bluetoothtutorial;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;
import java.util.UUID;

public class BTDeviceData implements Parcelable {
    private String BTDeviceName;
    private String BTDeviceAddress;
    private BluetoothDevice BTDevice;

    private final UUID raspberryPiUUID = UUID.fromString("efd4d135-d043-4fca-b99e-c2ae5ece6471");

    public BTDeviceData(BluetoothDevice BTDevice){
        this.BTDevice = BTDevice;
        this.BTDeviceName = BTDevice.getName();
        this.BTDeviceAddress = BTDevice.getAddress();
    }

    public BTDeviceData(Parcel parcel){
        String[] parcelItems = new String[2];
        parcel.readStringArray(parcelItems);
        BTDeviceName = parcelItems[0];
        BTDeviceAddress = parcelItems[1];
        BTDevice = parcel.readParcelable(BluetoothDevice.class.getClassLoader());
    }

    public UUID getRaspberryPiUUID() {
        return raspberryPiUUID;
    }

    public String getBTDeviceName() {
        return BTDeviceName;
    }

    public void setBTDeviceName(String BTDeviceName) {
        this.BTDeviceName = BTDeviceName;
    }

    public String getBTDeviceAddress() {
        return BTDeviceAddress;
    }

    public void setBTDeviceAddress(String BTDeviceAddress) {
        this.BTDeviceAddress = BTDeviceAddress;
    }

    public BluetoothDevice getBTDevice() {
        return BTDevice;
    }

    public void setBTDevice(BluetoothDevice BTDevice) {
        this.BTDevice = BTDevice;
    }

    //BEGIN Parcelable Implementation
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.BTDeviceName, this.BTDeviceAddress});
        dest.writeParcelable(this.BTDevice, flags);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public BTDeviceData createFromParcel(Parcel source) {
            return new BTDeviceData(source);
        }

        @Override
        public BTDeviceData[] newArray(int size) {
            return new BTDeviceData[size];
        }
    };
    //END Parcelable Implementation

    @Override
    public String toString(){
        return "Device Name: " + BTDeviceName + "\nDevice Address: " + BTDeviceAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BTDeviceData that = (BTDeviceData) o;
        return Objects.equals(BTDeviceName, that.BTDeviceName) &&
                Objects.equals(BTDeviceAddress, that.BTDeviceAddress) &&
                Objects.equals(BTDevice, that.BTDevice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(BTDeviceName, BTDeviceAddress, raspberryPiUUID, BTDevice);
    }
}
