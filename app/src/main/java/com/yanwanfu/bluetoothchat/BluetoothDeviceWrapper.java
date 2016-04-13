package com.yanwanfu.bluetoothchat;

import android.bluetooth.BluetoothDevice;

/**
 * 蓝牙实体类.
 */
public class BluetoothDeviceWrapper {

    private final BluetoothDevice device;

    public BluetoothDeviceWrapper(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    @Override
    public boolean equals(Object o) {
        if (o!=null){
            //执行一个断言，o的实例必须是 BluetoothDeviceWrapper 类型
            assert o instanceof BluetoothDeviceWrapper;

            //通过mac地址来判断两个设备是不是同一个设备
            return ((BluetoothDeviceWrapper) o).getDevice().getAddress().equals(getDevice().getAddress());
        }else{
            return false;
        }

    }

    @Override
    public String toString() {
        return String.format("%s\n%s",getDevice().getName(),getDevice().getAddress());
    }
}
