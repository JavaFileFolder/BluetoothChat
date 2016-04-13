package com.yanwanfu.bluetoothchat;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, BluetoothConnection.OnReadNewLineListener {

    private static final int REQUEST_ENABLE_BT = 1000;
    private BluetoothAdapter bluetoothAdapter;
    private ListView lvDevices;
    private DevicesListAdapter devicesListAdapter;
    private boolean scanning = false;
    private View viewProgress;
    private BluetoothConnection connection;
    private EditText etInput;
    private TextView tvOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connection = new BluetoothConnection(this);
        connection.setOnReadNewLineListener(this);

        viewProgress = findViewById(R.id.viewProgress);

        lvDevices = (ListView) findViewById(R.id.lvDevices);
        devicesListAdapter = new DevicesListAdapter(this, android.R.layout.simple_list_item_1);
        lvDevices.setAdapter(devicesListAdapter);
        //列表监听
        lvDevices.setOnItemClickListener(this);

        etInput = (EditText) findViewById(R.id.etChatInput);
        tvOutput = (TextView) findViewById(R.id.tvChatOutput);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();// 蓝牙对象
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "当前设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                //启用蓝牙
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i, REQUEST_ENABLE_BT);
            } else {
                //绑定蓝牙
                loadBondedDevices();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                switch (resultCode) {
                    case RESULT_OK:

                        loadBondedDevices();
                        break;
                    default:
                        new AlertDialog.Builder(this)
                                .setTitle("提醒")
                                .setMessage("拒绝启用蓝牙")
                                .setPositiveButton("再次启用", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestEnableBluetooth();
                                    }
                                }).setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setCancelable(false).show();

                        break;
                }
        }
    }

    /**
     * 再次请求打开蓝牙
     */
    private void requestEnableBluetooth() {
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(i, REQUEST_ENABLE_BT);
    }

    /**
     * 加载已经绑定的蓝牙设备
     */
    private void loadBondedDevices() {
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            //显示所有已经绑定的蓝牙设备
            devicesListAdapter.add(new BluetoothDeviceWrapper(device));
        }
    }

    /**
     * 启用被发现
     */
    public void btnDiscoverableClicked(View view) {
        //启用被发现
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //被发现持续时间300秒
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        connection.startServerSocket();
    }

    @Override
    protected void onPause() {
        super.onPause();

        checkToStopScanDevices();
        connection.stopServerSocket();
    }

    public void btnStartScanClicked(View view) {
        checkToScanDevices();
    }

    public void btnStopScanClicked(View view) {
        checkToStopScanDevices();
    }

    private void checkToScanDevices() {
        if (!scanning) {
            registerReceiver(deviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            bluetoothAdapter.startDiscovery(); //添加权限
            showProgress();
            scanning = true;
        }
    }


    private void checkToStopScanDevices() {
        if (scanning) {
            unregisterReceiver(deviceFoundReceiver);

            bluetoothAdapter.startDiscovery();
            hideProgress();
            scanning = false;
        }
    }

    void showProgress() {
        viewProgress.setVisibility(View.VISIBLE);
    }

    void hideProgress() {
        viewProgress.setVisibility(View.GONE);
    }


    private BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devicesListAdapter.add(new BluetoothDeviceWrapper(device));
            }

        }
    };

    public void btnSendLineClicked(View view) {
        //非空
        if (!TextUtils.isEmpty(etInput.getText())){
            //发送
            connection.sendLine(etInput.getText().toString());
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //连接另一个设备
        connection.connect(devicesListAdapter.getItem(position).getDevice());
    }

    @Override
    public void onRead(final String line, final BluetoothDevice remoteDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvOutput.append(remoteDevice.getName()+":"+line+"\n");
            }
        });
    }
}
