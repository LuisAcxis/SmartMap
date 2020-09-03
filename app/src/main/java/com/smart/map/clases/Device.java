package com.smart.map.clases;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class Device {
    private static Device instancia;

    public BluetoothSPP bt;
    public Activity bluetoothActivity;
    public Intent bluetoothIntent;
    public Class bluetoothFragment = DeviceList.class;

    private Device() {

    }

    public static Device getInstancia() {
        return instancia == null ? instancia = new Device() : instancia;
    }

    public void loadBluetooth(Activity context) {
        this.bluetoothActivity = context;
        this._loadBluetooth();
    }
    public void loadBluetooth(Activity context, Class fragment) {
        this.bluetoothActivity = context;
        this.bluetoothFragment = fragment;
        this._loadBluetooth();
    }

    public Activity getBluetoothActivity() {
        return this.bluetoothActivity;
    }

    private void _loadBluetooth() {
        this.bt = new BluetoothSPP(this.bluetoothActivity);
        bluetoothIntent = new Intent(this.bluetoothActivity, bluetoothFragment);
        bluetoothIntent.putExtra("bluetooth_devices", "Dispositivos Bluetooth");
        bluetoothIntent.putExtra("no_devices_found", "Dispositivos no disponibles");
        bluetoothIntent.putExtra("scanning", "Escaneando");
        bluetoothIntent.putExtra("scan_for_devices", "Buscar");
        bluetoothIntent.putExtra("select_device", "Seleccionar");
    }
}