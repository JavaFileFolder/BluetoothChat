package com.yanwanfu.bluetoothchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示已经绑定的蓝牙设备
 */
public class DevicesListAdapter extends BaseAdapter {

    private final Context context;
    private final int cellResId;
    private List<BluetoothDeviceWrapper> items = new ArrayList<>();


    public DevicesListAdapter(Context context, int cellResId) {
        this.context = context;
        this.cellResId = cellResId;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public BluetoothDeviceWrapper getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(cellResId,null);
        }
        //断言
        assert convertView instanceof TextView;
        ((TextView) convertView).setText(getItem(position).toString());
        return convertView;
    }

    public void add(BluetoothDeviceWrapper bluetoothDeviceWrapper) {
        //列表内是否包含了同一个蓝牙设备,包含就不再添加
        if (!items.contains(bluetoothDeviceWrapper)){
            items.add(bluetoothDeviceWrapper);
            notifyDataSetChanged();
        }
    }
}
