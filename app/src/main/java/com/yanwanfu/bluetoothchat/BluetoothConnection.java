package com.yanwanfu.bluetoothchat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 处理连接
 */
public class BluetoothConnection {

    private static final String NAME = "BluetoothChat";
    private static final UUID MY_UUID = UUID.fromString("840edf1d-1a7f-4758-9b62-bb1237f4550a");
    private final Activity context;
    private AcceptThread acceptThread =null;
    private ManageConnectionThread manageConnectionThread = null;
    private OnReadLineListener onReadLineListener = null;

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

    public void manageConnection(BluetoothSocket socket){
        manageConnectionThread = new ManageConnectionThread(socket);
        new ManageConnectionThread(socket).start();
    }

    //get 监听器
    public OnReadLineListener getOnReadLineListener() {
        return onReadLineListener;
    }
    //set 监听器
    public void setOnReadLineListener(OnReadLineListener onReadLineListener) {
        this.onReadLineListener = onReadLineListener;
    }

    class ManageConnectionThread extends Thread{

        private BluetoothSocket socket;
        private InputStream in;
        private OutputStream out;

        public ManageConnectionThread( BluetoothSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            try {
                out = socket.getOutputStream();
                in = socket.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                String line = null;

                while((line=br.readLine())!=null){
                    if (getOnReadLineListener() != null){
                        getOnReadLineListener().onRead(line);//传出数据
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            cancel();
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,"已经断开连接",Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                    manageConnection(socket);
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

    //监听器
    interface OnReadLineListener{
        void onRead(String line);
    }
}
