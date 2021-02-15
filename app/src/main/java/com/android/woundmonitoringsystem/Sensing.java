package com.android.woundmonitoringsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Sensing extends Fragment implements View.OnClickListener {

    Socket echoSocket = null;
    String deviceAddress, deviceName, feas;
    DataOutputStream os = null;
    LinearLayout inWidget, outWidget;
    private boolean isBtConnected = false;
    private boolean isExternal = false;
    private boolean isInternal = false;
    private boolean isReset = false;
    DataInputStream is = null;
    RelativeLayout loading;
    JSONObject jsonBody;
    AlertDialog alert;
    private RequestQueue mQueue;
    Helpers helpers;
    Button bluetoothBtn, wifiBtn, feasibilityButton, resetButton, saveButton, chartButton;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DevicesListAdapter mDeviceListAdapter;
    BluetoothAdapter mBluetoothAdapter;
    ListView lvNewDevices;
    TextView externalRes, internalRes, fesRes;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();
    Handler handler;
    private ConnectedThread mConnectedThread;
    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    StringBuilder messages;
    SwitchMaterial internalSwitch, externalSwitch;
    Float btmp, tmp, hmid, oxy, qual, con;

    public static Sensing newInstance() {
        return new Sensing();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensing_fragment, container, false);
        bluetoothBtn = view.findViewById(R.id.bluBtn);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        messages = new StringBuilder();
        mQueue = Volley.newRequestQueue(getActivity());

//        Getting elements
        inWidget = view.findViewById(R.id.inWidget);
        outWidget = view.findViewById(R.id.exWidget);
        loading = view.findViewById(R.id.loading);
        internalSwitch = view.findViewById(R.id.internalSwitch);
        externalSwitch = view.findViewById(R.id.externalSwitch);
        externalRes = view.findViewById(R.id.externalRes);
        internalRes = view.findViewById(R.id.internalRes);
        fesRes = view.findViewById(R.id.fesRes);
        resetButton = view.findViewById(R.id.reset);
        feasibilityButton = view.findViewById(R.id.fes);
        saveButton = view.findViewById(R.id.save);
        wifiBtn = view.findViewById(R.id.wifiBtn);
        chartButton = view.findViewById(R.id.chart);
        helpers = new Helpers(getActivity());
//        Setting on clik
        chartButton.setOnClickListener(this);
        bluetoothBtn.setOnClickListener(this);
        feasibilityButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);
        wifiBtn.setOnClickListener(this);

        internalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    if (isBtConnected) {
                        try {
                            mConnectedThread.write("cmd1");
                            isInternal = true;
                        } catch (Exception e) {

                        }
                    } else {
                        try {
                            mConnectedThread.write("cmd1");
                            os.write("cmd1".getBytes(Charset.forName("UTF-8")));
                        } catch (Exception e) {
                            System.err.println("Can't send the data");
                        }
                    }
                } else {
                    isInternal = false;
                }
            }
        });

        externalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isBtConnected) {
                        try {
                            mConnectedThread.write("cmd2");
                            isExternal = true;
                        } catch (Exception e) {
//                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        try {
                            mConnectedThread.write("cmd2");
                            os.write("cmd2".getBytes(Charset.forName("UTF-8")));
                        } catch (Exception e) {
                            System.err.println("Can't send the data");
                        }
                    }
                } else {
                    isExternal = false;
                }
            }

            ;
        });

        handler = new Handler(new Handler.Callback() {
            public boolean handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:
                        // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        // create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\r\n");
                        // determine the end-of-line
                        if (endOfLineIndex > 0) {                                            // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);               // extract string
                            sb.delete(0, sb.length());                                      // and clear
//                            Toast.makeText(getActivity(), sbprint , Toast.LENGTH_LONG).show();            // update TextView
                            try {
                                JSONObject jsonObject = new JSONObject(sbprint);
                                if (isExternal) {
                                    tmp = Float.parseFloat(jsonObject.getString("temp"));
                                    qual = Float.parseFloat(jsonObject.getString("dden"));
                                    con = Float.parseFloat(jsonObject.getString("gcon"));
                                    hmid = Float.parseFloat(jsonObject.getString("hmid"));
                                    String temp = "Temperature: " + jsonObject.getString("temp");
                                    String humidity = "Humidity: " + jsonObject.getString("hmid");
                                    String dustDensity = "Dust Density: " + jsonObject.getString("dden");
                                    String voltage = "Voltage: " + jsonObject.getString("volt");
                                    String gasCon = "Gas Concentration: " + jsonObject.getString("gcon");
                                    Spanned res = Html.fromHtml(temp + "<br>" + humidity + "<br>" + dustDensity + "<br>" + voltage + "<br>" + gasCon);
                                    externalRes.setText(res);
                                    externalSwitch.setChecked(false);
                                    inWidget.setVisibility(View.VISIBLE);
                                    outWidget.setVisibility(View.VISIBLE);
                                    loading.setVisibility(View.GONE);
                                    isExternal = false;
                                }
                                if (isInternal) {
                                    oxy = Float.parseFloat(jsonObject.getString("spo2"));
                                    btmp = Float.parseFloat(jsonObject.getString("btmp"));
                                    String btemp = "Body Temperature: " + jsonObject.getString("btmp");
                                    String hrtb = "HeartBeat: " + jsonObject.getString("hrtb");
                                    String spoTwo = "Oxygen: " + jsonObject.getString("spo2");
                                    Spanned res = Html.fromHtml(btemp + "<br>" + hrtb + "<br>" + spoTwo);
                                    internalRes.setText(res);
                                    internalSwitch.setChecked(false);
                                    inWidget.setVisibility(View.VISIBLE);
                                    outWidget.setVisibility(View.VISIBLE);
                                    loading.setVisibility(View.GONE);
                                    isInternal = false;
                                }
                                if (isReset) {
                                    loading.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), "Reset Successfully", Toast.LENGTH_LONG).show();
                                    bluetoothBtn.setClickable(true);
                                    bluetoothBtn.setAlpha(1);
                                }
                            } catch (JSONException e) {
                                if (isInternal) {
                                    mConnectedThread.write("cmd1");
                                }
                                if (isExternal) {
                                    mConnectedThread.write("cmd2");
                                }
                                if (isReset) {
                                    mConnectedThread.write("cmd3");
                                    Toast.makeText(getActivity(), "Reset Successfully", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        break;
                }
                return true;
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void serialCommunication() {
        try {
            echoSocket = new Socket("192.168.4.1", 80);
            os = new DataOutputStream(echoSocket.getOutputStream());
            is = new DataInputStream(echoSocket.getInputStream());
        } catch (UnknownHostException e) {
            Toast.makeText(getActivity(), "Host Not found", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection");
            Toast.makeText(getActivity(), "Couldn't get Input/Output for Connection", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.bluBtn:
                inWidget.setVisibility(View.GONE);
                outWidget.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                checkState();
                showBluetoothDevices();
                break;
            case R.id.wifiBtn:
                serialCommunication();
                break;
            case R.id.reset:
                reset();
                break;
            case R.id.save:
                saveFeasibility();
                break;
            case R.id.fes:
                checkFeasibility();
                break;
            case R.id.chart:
                showChart();
                break;
        }
    }

    public void reset() {
        try {
            mConnectedThread.write("cmd3");
            isReset = true;
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            System.err.println("Can't send the data");
        }
    }

    public void showChart() {

        FragmentTransaction t = this.getFragmentManager().beginTransaction();
        Fragment mFrag = new Chart();
        Bundle args = new Bundle();
        args.putString("feas", feas);
        mFrag.setArguments(args);
        t.replace(R.id.nav_host_fragment, mFrag);
        t.commit();
    }

    public void retryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setTitle("No Internet");
        builder.setMessage("Connection TimeOut! Please check your internet connection..");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getActivity().finish();
            }
        });

        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                saveFeasibility();
            }
        });
        AlertDialog dialog = builder.create(); // calling builder.create after adding buttons
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.third_matching_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.error));
    }

    public void saveFeasibility() {
        try {
            jsonBody = new JSONObject();
            jsonBody.put("hmid", hmid);
            jsonBody.put("tmp", tmp);
            jsonBody.put("btmp", btmp);
            jsonBody.put("oxy", oxy);
            jsonBody.put("gcon", con);
            jsonBody.put("qual", qual);
            jsonBody.put("feas", "done");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = getResources().getString(R.string.url);
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String res = response.getString("res");
                    if (res.equals("1")) {
                        Toast.makeText(getActivity(), "Added Successfully", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (volleyError instanceof NetworkError) {
                    retryDialog();
                } else if (volleyError instanceof ServerError) {
                    try {
                        NetworkResponse response = volleyError.networkResponse;
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // Now you can use any deserializer to make sense of data
                        JSONObject obj = new JSONObject(res);
                        System.out.println(obj);
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                    } catch (JSONException e2) {
                        // returned data is not JSONObject?
                        e2.printStackTrace();
                    }
                } else if (volleyError instanceof AuthFailureError) {
                    retryDialog();
                } else if (volleyError instanceof ParseError) {
                    retryDialog();
                } else if (volleyError instanceof NoConnectionError) {
                    retryDialog();
                } else if (volleyError instanceof TimeoutError) {
                    retryDialog();
                }
            }
        });
        mQueue.add(request);
    }

    public void checkFeasibility() {
        String internalResText = internalRes.getText().toString().trim();
        String externalResText = externalRes.getText().toString().trim();

        if (internalResText.equals("")) {
            Toast.makeText(getActivity(), "There is no values for Internal Body", Toast.LENGTH_LONG).show();

        } else if (externalResText.equals("")) {
            Toast.makeText(getActivity(), "There is no values for External Environment", Toast.LENGTH_LONG).show();
        } else {
            try {
                feas = helpers.FuzzyEngine(35.5, 60, 90, 30, 9, 100);
                feasibilityButton.setClickable(false);
                feasibilityButton.setAlpha((float) 0.3);
                fesRes.setText(feas);
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
    public void checkState(){
    if(mBluetoothAdapter ==null) {
        Toast.makeText(getActivity() , "No Bluetooth" , Toast.LENGTH_LONG).show();
    }

    if(!mBluetoothAdapter.isEnabled()) {
        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBTIntent);
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        requireActivity().registerReceiver(deviceOnOff , BTIntent);
    }
    showBluetoothDevices();
}
    //Bluetooth Devices
    public void showBluetoothDevices(){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);

            //check BT permissions in manifest

            mBluetoothAdapter.startDiscovery();

        }
        if(!mBluetoothAdapter.isDiscovering()){
//                    Toast.makeText(getBaseContext()  , "Discovering" , Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
            //check BT permissions in manifest
            mBluetoothAdapter.startDiscovery();
        }
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        requireActivity().registerReceiver(getDevices, discoverDevicesIntent);
    }
    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private BroadcastReceiver getDevices = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            View devList = getLayoutInflater().inflate(R.layout.devices_list, null);
            lvNewDevices = devList.findViewById(R.id.devices);
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                    BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                    mBTDevices.add(device);
                    mDeviceListAdapter = new DevicesListAdapter(context, R.layout.activity_discover, mBTDevices);
                    lvNewDevices.setAdapter(mDeviceListAdapter);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getResources().getString(R.string.bd));
                    builder.setAdapter(mDeviceListAdapter ,new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            BluetoothDevice obj = mDeviceListAdapter.getItem(i);
                            Toast.makeText(getActivity() , deviceAddress , Toast.LENGTH_LONG).show();
                            mBluetoothAdapter.cancelDiscovery();
                            deviceName = mBTDevices.get(i).getName();
                            deviceAddress = obj.getAddress();
                            mBTDevices.get(i).createBond();
                            alert.dismiss();
                            startBtConnection();
                        }
                    });

                    alert = builder.create();
    //                alert
                    alert.show();
                    loading.setVisibility(View.GONE);
                    inWidget.setVisibility(View.VISIBLE);
                    outWidget.setVisibility(View.VISIBLE);
                }
        }
    };
    private final BroadcastReceiver deviceOnOff = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isBtConnected) {
            requireActivity().unregisterReceiver(getDevices);
            requireActivity().unregisterReceiver(deviceOnOff);
        }
    }

    public void startBtConnection(){
        new ConnectBT().execute();
    }

//    Creating bluettoth connection
    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            Toast.makeText(getActivity(),"Connecting....",Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.BLUETOOTH},1);
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.BLUETOOTH_ADMIN},1);
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.BLUETOOTH_PRIVILEGED},1);

                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device

                    BluetoothDevice dispositivo = mBluetoothAdapter.getRemoteDevice(deviceAddress);

                    //connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                Toast.makeText(getActivity(),"Connection Failed",Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
            else
            {
                Toast.makeText(getActivity(),"Connection Successful",Toast.LENGTH_SHORT).show();
                isBtConnected = true;
                mConnectedThread = new ConnectedThread(btSocket);
                mConnectedThread.start();
                internalSwitch.setClickable(true);
                externalSwitch.setClickable(true);
                externalSwitch.setAlpha(1);
                internalSwitch.setAlpha(1);
                wifiBtn.setClickable(false);
                bluetoothBtn.setClickable(false);
                wifiBtn.setAlpha((float) 0.7);
                bluetoothBtn.setAlpha((float) 0.7);
            }
        }
    }

//    Thread for reading and writing over Bluetooth module
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
//                Toast.makeText(get)
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

//            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    handler.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            inWidget.setVisibility(View.GONE);
            outWidget.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
            Log.d("Data", "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d("Error", "...Error data send: " + e.getMessage() + "...");
            }
        }
    }
}
