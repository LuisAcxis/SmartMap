package com.smart.map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.map.clases.Device;

import org.json.JSONException;
import org.json.JSONObject;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class MainActivity extends AppCompatActivity {

    Device device;

    boolean isBluetoothConected = false;

    JSONObject jsonReceived;

    public TextView
            tvPuntos,
            tvNivel,
            tvDeviceNombre;

    CardView
            cardNivel
            ,cardDispositivo
            ,cardReiniciar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPuntos = this.findViewById(R.id.tvPuntos);
        tvNivel = this.findViewById(R.id.tvNivel);
        tvDeviceNombre = this.findViewById(R.id.tvDeviceNombre);

        final CharSequence[] items = {"Nivel 1", "Nivel 2", "Nivel 3"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona un nivel");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        sendData("{\"nivel\":1}"); break;
                    case 1:
                        sendData("{\"nivel\":2}"); break;
                    case 2:
                        sendData("{\"nivel\":3}"); break;
                }
            }
        });
        final AlertDialog alert = builder.create();
        cardNivel = this.findViewById(R.id.cardNivel);
        cardNivel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alert.show();
            }
        });

        cardDispositivo = this.findViewById(R.id.cardDispositivo);
        cardDispositivo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDeviceSelect();
            }
        });

        cardReiniciar = this.findViewById(R.id.cardReiniciar);
        cardReiniciar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("{\"reset\":\"true\"}");
            }
        });

        device = Device.getInstancia();
        device.loadBluetooth(this);

        showDeviceSelect();

        if(!device.bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Tu dispositivo no cuenta con conexión Bluetooth"
                    , Toast.LENGTH_LONG).show();
            finish();
        }

        device.bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
            public void onServiceStateChanged(int state) {
                if(state == BluetoothState.STATE_CONNECTED) {
                    isBluetoothConected = true;
                }
                else if(state == BluetoothState.STATE_CONNECTING) {
                    tvDeviceNombre.setText("Conectando...");
                    isBluetoothConected = false;
                }
                else if(state == BluetoothState.STATE_LISTEN) {
                    isBluetoothConected = false;
                }
                else if(state == BluetoothState.STATE_NONE) {
                    isBluetoothConected = false;
                }
            }
        });

        device.bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String nombre, String direccion) {
                tvDeviceNombre.setText(nombre);
                isBluetoothConected = true;
            }
            public void onDeviceDisconnected() {
                tvDeviceNombre.setText("----");
                isBluetoothConected = false;
            }
            public void onDeviceConnectionFailed() {
                tvDeviceNombre.setText("Conexión Fallida");
                isBluetoothConected = false;
            }
        });

        device.bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] dataByte, String data) {
                try {
                    jsonReceived = new JSONObject(data);
                    JSONObject status = jsonReceived.getJSONObject("status");
                    if(status.getInt("code") == 2) {
                        tvPuntos.setText(jsonReceived.getString("puntos"));
                        tvNivel.setText(jsonReceived.getString("nivel"));
                    } else {
                        Toast.makeText(getApplicationContext(), status.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {

                }
            }
        });

    }

    public void onStart() {
        super.onStart();

        boolean enabling = false;
        if(!device.bt.isBluetoothEnabled()) {
            device.bt.enable();
            enabling = true;
        }

        if(!device.bt.isServiceAvailable()) {
            setupDeviceService(enabling);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        device.bt.stopService();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {

        } else {
            if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
                if (resultCode == Activity.RESULT_OK) {
                    if(isBluetoothConected) {
                        device.bt.disconnect();
                    }
                    device.bt.connect(data);
                }
            }
        }
    }

    public void setupDeviceService(boolean sleep) {
        if(sleep) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    device.bt.setupService();
                    device.bt.startService(BluetoothState.DEVICE_OTHER);
                }
            }, 4000);
        } else {
            device.bt.setupService();
            device.bt.startService(BluetoothState.DEVICE_OTHER);
        }
    }

    public void showDeviceSelect() {
        startActivityForResult(device.bluetoothIntent, BluetoothState.REQUEST_CONNECT_DEVICE);
    }

    public void sendData(String data) {
        if(isBluetoothConected) {
            device.bt.send(data, true);
        } else {
            showDeviceSelect();
        }
    }
}