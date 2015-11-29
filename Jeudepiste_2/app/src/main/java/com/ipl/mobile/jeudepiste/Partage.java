/*
 *
 *  * Copyright (c) <2015> <Institut Paul Lambin>
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *  *
 *  *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  *  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  *  Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

/*
 * Android Code
 * Copyright 2005-2008 The Android Open Source Project
 * This product includes software developed as part of
 * The Android Open Source Project (http://source.android.com).
 */

package com.ipl.mobile.jeudepiste;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.ipl.mobile.jeudepiste.R;
import com.ipl.mobile.jeudepiste.util.Util;
public class Partage extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;
    private TextView text;
    private BluetoothAdapter myBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<BluetoothDevice> devices;
    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;
    private String tag = "debugging";
    protected static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    private SharedPreferences settings;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(tag, "in handler");
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCESS_CONNECT:
                    // DO something
                    ConnectedThread connectedThread = new ConnectedThread(
                            (BluetoothSocket) msg.obj);
                    Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_SHORT).show();
                    String s = "successfully connected";
                    connectedThread.write(s.getBytes());
                    Log.i(tag, "connected");
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partage);

        // take an instance of BluetoothAdapter - Bluetooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            text.setText("Status: not supported");

            Toast.makeText(getApplicationContext(),
                    "Your device does not support Bluetooth", Toast.LENGTH_LONG)
                    .show();
        } else {
            myListView = (ListView) findViewById(R.id.listDiscoverDeviceBlue);
            BTArrayAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1);
            devices = new ArrayList<BluetoothDevice>();
            myListView.setAdapter(BTArrayAdapter);
            text = (TextView) findViewById(R.id.textPartage);
            onBtn = (Button) findViewById(R.id.turnOn);
            onBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    on(v);
                }
            });

            offBtn = (Button) findViewById(R.id.turnOff);
            offBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    off(v);
                }
            });

            listBtn = (Button) findViewById(R.id.paired);
            listBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    list(v);
                }
            });

            findBtn = (Button) findViewById(R.id.search);
            findBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    find(v);
                }
            });

            // create the arrayAdapter that contains the BTDevices, and set it
            // to the ListView

            myListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    if (myBluetoothAdapter.isDiscovering()) {
                        myBluetoothAdapter.cancelDiscovery();
                    }

                    BluetoothDevice selectedDevice = devices.get(position);
                    if (pairedDevices.contains(selectedDevice)) {

                        if (unpairDevice(selectedDevice))
                            Toast.makeText(getApplicationContext(),
                                    "Device unpaired", Toast.LENGTH_LONG)
                                    .show();
                        else
                            Toast.makeText(getApplicationContext(),
                                    "Problem while unpairing device",
                                    Toast.LENGTH_LONG).show();
                    } else {
                        if (pairDevice(selectedDevice)) {
                            Toast.makeText(getApplicationContext(),
                                    "Device paired", Toast.LENGTH_LONG).show();
                            ConnectThread connect = new ConnectThread(
                                    selectedDevice);
                            connect.start();
                        } else
                            Toast.makeText(getApplicationContext(),
                                    "Problem while pairing device",
                                    Toast.LENGTH_LONG).show();
                    }
                }

            });

        }
    }

    // For Pairing
    private boolean pairDevice(BluetoothDevice device) {
        try {
            Log.d("pairDevice()", "Start Pairing...");
            Method m = device.getClass()
                    .getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // For UnPairing
    private boolean unpairDevice(BluetoothDevice device) {
        try {
            Log.d("unpairDevice()", "Start Un-Pairing...");
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void on(View view) {
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);



            Toast.makeText(getApplicationContext(), "Bluetooth turned on",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is already on",
                    Toast.LENGTH_LONG).show();
        }

        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (myBluetoothAdapter.isEnabled()) {
                text.setText("Status: Enabled");
            } else {
                text.setText("Status: Disabled");
            }
        }
    }

    public void list(View view) {
        BTArrayAdapter.clear();
        devices.clear();
        // get paired devices
        // pairedDevices.clear();
        pairedDevices = myBluetoothAdapter.getBondedDevices();

        // put it's one to the adapter
        for (BluetoothDevice myDevice : pairedDevices) {
            BTArrayAdapter.add(myDevice.getName() + "\n" + myDevice.getAddress());
            devices.add(myDevice);
        }



        Toast.makeText(getApplicationContext(), "Show Paired Devices",
                Toast.LENGTH_SHORT).show();

    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the
                // arrayAdapter
                devices.add(device);
                BTArrayAdapter.add(device.getName() + "\n"
                        + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find(View view) {
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            Toast.makeText(getApplicationContext(), "Discovery cancelled",
                    Toast.LENGTH_SHORT).show();
            myBluetoothAdapter.cancelDiscovery();
        } else {
            Toast.makeText(getApplicationContext(), "Discovering new devices",
                    Toast.LENGTH_SHORT).show();
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();
            registerReceiver(bReceiver, new IntentFilter(
                    BluetoothDevice.ACTION_FOUND));
        }
    }

    public void off(View view) {
        myBluetoothAdapter.disable();
        text.setText("Status: Disconnected");

        Toast.makeText(getApplicationContext(), "Bluetooth turned off",
                Toast.LENGTH_LONG).show();
    }


    private class ConnectThread extends Thread { // client

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.i(tag, "construct");
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server
                // code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.i(tag, "get socket failed");

            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            myBluetoothAdapter.cancelDiscovery();
            Log.i(tag, "connect - run");
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                Log.i(tag, "connect - succeeded");
            } catch (IOException connectException) {
                Log.i(tag, "connect failed");
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)

            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {

        settings = getSharedPreferences(Util.preferences, MODE_PRIVATE);
        SharedPreferences.Editor ed = settings.edit();
        try {


            InputStream mInputStream = mmSocket.getInputStream();
            OutputStream mOutputStream = mmSocket.getOutputStream();



            byte[] bytes = { };
            mInputStream.read(bytes);
            float f = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();


            mOutputStream.write(Byte.parseByte(""+settings.getInt(Util.bestScore,0)));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectedThread extends Thread { //server
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer; // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

}