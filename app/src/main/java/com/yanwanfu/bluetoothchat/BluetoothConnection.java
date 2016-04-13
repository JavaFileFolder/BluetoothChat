package com.yanwanfu.bluetoothchat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
 * 处理连接类
 */
public class BluetoothConnection {

    private static final String NAME = "BluetoothChat";
//    private static final UUID MY_UUID = UUID.fromString("840edf1d-1a7f-4758-9b62-bb1237f4550a");
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final Activity context;
    private AcceptThread acceptThread = null;
    private ManageConnectionThread manageConnectionThread = null;
    private OnReadNewLineListener onReadNewLineListener = null;

    public BluetoothConnection(Activity context) {
        this.context = context;
    }

    //开始服务
    public void startServerSocket() {
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    //停止服务
    public void stopServerSocket() {
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
    }

    //管理连接
    public void manageConnection(BluetoothSocket socket) {
        manageConnectionThread = new ManageConnectionThread(socket);
        manageConnectionThread.start();
    }

    //get 监听器
    public OnReadNewLineListener getOnReadNewLineListener() {
        return onReadNewLineListener;
    }

    //set 监听器
    public void setOnReadNewLineListener(OnReadNewLineListener onReadNewLineListener) {
        this.onReadNewLineListener = onReadNewLineListener;
    }

    //发送消息
    public void sendLine(String line) {
        if (manageConnectionThread != null) {
            manageConnectionThread.sendLine(line);
        }
    }

    /**
     * 连接对象
     */
    public void connect(BluetoothDevice device) {
        stopServerSocket();//关闭服务
        new ConnectThread(device).start(); //匿名连接对象
    }

    /**
     * 连接类
     */
    class ConnectThread extends Thread {
        private BluetoothDevice device = null;
        private BluetoothSocket socket=null;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            try {
                //连接一个服务器
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            if (socket==null){
                return;
            }
            try {
                socket.connect();           //执行连接
                manageConnection(socket);   //管理连接
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,"成功连接设备",Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,"无法创建连接",Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }

    /**
     * 管理连接
     */
    class ManageConnectionThread extends Thread {

        private BluetoothSocket socket;
        private InputStream in;
        private OutputStream out;

        public ManageConnectionThread(BluetoothSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            try {
                out = socket.getOutputStream();
                in = socket.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String line = null;

                while ((line = br.readLine()) != null) {
                    if (getOnReadNewLineListener() != null) {
                        getOnReadNewLineListener().onRead(line,socket.getRemoteDevice());//传出数据
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            cancel();
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "已经断开连接", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void cancel() {
            try {
                socket.close();
                manageConnectionThread = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendLine(String line) {
            try {
                line += "\n";
                out.write(line.getBytes("UTF-8"));
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 接受配对
     */
    class AcceptThread extends Thread {

        private BluetoothAdapter bluetoothAdapter;
        private BluetoothServerSocket serverSocket = null;
        private boolean listenning = true;

        //AcceptThread构造函数
        public AcceptThread() {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            try {
                listenning = true;
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(BluetoothConnection.NAME, BluetoothConnection.MY_UUID);
                System.out.println("Success to listen");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();

            if (serverSocket == null) {
                return;
            }
            BluetoothSocket socket = null;
            while (listenning) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (listenning) {
                        //UI线程
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "无法接受连接", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
                if (socket != null) {
                    cancel();
                    manageConnection(socket);
                }
            }
        }

        public void cancel() {
            if (serverSocket == null) {
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

    /**
     * 监听器
     */
    interface OnReadNewLineListener {
        void onRead(String line,BluetoothDevice remoteDevice);
    }
}
