package com.yanwanfu.bluetoothchat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

/**
 * 处理连接
 */
public class BluetoothConnection {

    private static final String NAME = "BluetoothChat";
    private static final UUID MY_UUID = UUID.fromString("840edf1d-1a7f-4758-9b62-bb1237f4550a");
    private final Activity context;
    private AcceptThread acceptThread =null;

    public BluetoothConnection(Activity context){
        this.context = context;
    }

    public void startServerSocket(){
        if (acceptThread ==null){
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public void stopServerSocket() {
        if (acceptThread!=null){
            acceptThread.cancel();
            acceptThread=null;
        }
    }

    class ManagerConnectionThread extends Thread{

        private BluetoothSocket socket;

        public ManagerConnectionThread( BluetoothSocket socket) {
            this.socket = socket;

        }

        @Override
        public void run() {
            super.run();

            // TODO: 2016/4/12  
        }
    }

    class AcceptThread extends Thread{

        private BluetoothAdapter bluetoothAdapter;
        private BluetoothServerSocket serverSocket = null;
        private boolean listenning = true;
        //AcceptThread构造函数
        public AcceptThread() {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            try {
                listenning = true;
                serverSocket =  bluetoothAdapter.listenUsingRfcommWithServiceRecord(BluetoothConnection.NAME,BluetoothConnection.MY_UUID);
                System.out.println("Success to listen");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();

            if (serverSocket == null){
                return;
            }
            BluetoothSocket socket = null;
            while (listenning){
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (listenning){
                        //UI线程
                       context.runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               Toast.makeText(context,"无法接受连接",Toast.LENGTH_SHORT).show();
                           }
                       });
                    }

                }
                if (socket!=null){
                    new ManagerConnectionThread(socket).start();
                    cancel();
                }
            }
        }
        public void cancel(){
            if (serverSocket == null){
                return;
            }
            listenning = false;

            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
