package com.example.mehmetcan.admcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class BluetoothList extends FragmentActivity {

    public static String EXTRA_ADDRESS = "device_address";
    private TextView text;
    private ListView listOfDevices;
    private Button btn_list;
    private BluetoothAdapter phoneBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_liste);

        this.initializeComponents();
        this.bluetoothControl();
        this.initializeListeners();

    }

    /**
     * Control the bluetooth whether it exists or not.
     */
    private void bluetoothControl(){
        // Take our phone's bluetooth
        phoneBluetooth = BluetoothAdapter.getDefaultAdapter();

        // Control, if your phone has bluetooth or not
        if (phoneBluetooth == null) {
            // If bluetooth is not available, give warning message and close application
            message("Your phone does not support bluetooth");
            finish();
        } else if (!phoneBluetooth.isEnabled()) {
            // If you have a bluetooth but it is not open, request to connect.
            Intent BTac = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(BTac, 1);
        }
    }

    /**
     * Initialize the components
     */
    private void initializeComponents(){
        // Describing XML Widgets
        text = (TextView) findViewById(R.id.textView);
        listOfDevices = (ListView) findViewById(R.id.listView);
        btn_list = (Button) findViewById(R.id.ButtonTest);
    }

    /**
     * Initialize the listeners
     */
    private void initializeListeners(){
        btn_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPairedDevices();
            }
        });
    }

    /**
     * This method allows us to show paired devices and
     * open new intent(CarActivity) by clicking anyone(list items).
     */
    private void showPairedDevices() {
        // Take paired devices
        pairedDevices = phoneBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                // Add the name and address of the Bluetooth device to the list.
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            message("Paired devices not found, Be sure bluetooth connection is open.. ");
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, list);

        listOfDevices.setAdapter(adapter);

        // Method that allows us to select desired devices to connect.
        listOfDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // We get the mac address, the last 17 characters in the view.
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                // We define an intent to start a new activity.
                Intent i = new Intent(BluetoothList.this, CarActivity.class);

                // Start the activity.
                i.putExtra(EXTRA_ADDRESS, address); // this will be received from CarActivity class
                startActivity(i);
            }
        });
    }

    private void message(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}