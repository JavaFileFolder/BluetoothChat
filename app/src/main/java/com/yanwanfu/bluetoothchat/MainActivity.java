package com.yanwanfu.bluetoothchat;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1000;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();// 蓝牙对象
        if (bluetoothAdapter ==null){
            Toast.makeText(MainActivity.this,"当前设备不支持蓝牙",Toast.LENGTH_SHORT).show();
            finish();
        }else{
            if (!bluetoothAdapter.isEnabled()){ //启用蓝牙
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i, REQUEST_ENABLE_BT);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                switch (resultCode){
                    case RESULT_OK:
                        // TODO: 2016/4/7
                        break;
                    default:
                        new AlertDialog.Builder(this)
                                .setTitle("提醒")
                                .setMessage("拒绝启用蓝牙")
                                .setPositiveButton("再次启用", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).setNegativeButton("关闭");

                        break;
                }
        }
    }
}
