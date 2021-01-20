package com.android.woundmonitoringsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

public class WoundProfile extends Fragment implements View.OnClickListener{

    AutoCompleteTextView typeField , colorField;
    TextView sizeField;
    ArrayList<String> typeList , colorList;
    Button backBtn , addBtn;
    Helpers helpers;
    private RequestQueue mQueue;
    RelativeLayout loadingDialog;
    String type , size , color;
    JSONObject jsonBody;
    public static WoundProfile newInstance() {
        return new WoundProfile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.wound_profile_fragment, container, false);
        typeField = view.findViewById(R.id.type);
        colorField = view.findViewById(R.id.color);
        backBtn = view.findViewById(R.id.back);
        addBtn = view.findViewById(R.id.add);
        sizeField = view.findViewById(R.id.size);
        loadingDialog = view.findViewById(R.id.loading);
        helpers = new Helpers(getActivity());
        mQueue = Volley.newRequestQueue(getActivity());
        typeList = new ArrayList();
        colorList = new ArrayList();
        Collections.addAll(typeList , "Open" , "Closed");
        Collections.addAll(colorList , "Greenish" , "Blueish" , "Pinkish" , "Redish" , "Black");
        typeField.setThreshold(1);
        colorField.setThreshold(1);
        ArrayAdapter typeAdapter = new ArrayAdapter(getActivity() , android.R.layout.simple_list_item_1 , typeList);
        ArrayAdapter colorAdapter = new ArrayAdapter(getActivity() , android.R.layout.simple_list_item_1 , colorList);
        colorField.setAdapter(colorAdapter);
        typeField.setAdapter(typeAdapter);
        backBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.add:
                size = sizeField.getText().toString().trim();
                type = typeField.getText().toString().trim();
                color = colorField.getText().toString().trim();
                if(type.equals("")){
                    typeField.setError("Type is Required");
                    typeField.setFocusable(true);
                }else if(size.equals("")){
                    sizeField.setError("Size is Required");
                    sizeField.setFocusable(true);
                }else if(color.equals("")){
                    colorField.setError("Color is Required");
                    colorField.setFocusable(true);
                }else{
                    saveWound();
                    FragmentTransaction t = this.getFragmentManager().beginTransaction();
                    Fragment mFrag = new AllWounds();
                    t.replace(R.id.nav_host_fragment, mFrag);
                    t.commit();
                }
                break;
            case R.id.back:
                FragmentTransaction t = this.getFragmentManager().beginTransaction();
                Fragment mFrag = new AllWounds();
                t.replace(R.id.nav_host_fragment, mFrag);
                t.commit();
                break;
        }
    }
    public void saveWound(){
        String id = helpers.getPreferences("shared_pref" , "uid");
        try {
            jsonBody = new JSONObject();
            jsonBody.put("type", type);
            jsonBody.put("size", size);
            jsonBody.put("color", color);
            jsonBody.put("uid", id);
            jsonBody.put("wound", "done");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = getResources().getString(R.string.url);
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String res = response.getString("res");
                    if(res.equals("1")){
                        Toast.makeText(getActivity() , "Wound Added Successfully" , Toast.LENGTH_LONG).show();
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


    public void retryDialog(){
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

        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                saveWound();
            }
        });
        AlertDialog dialog = builder.create(); // calling builder.create after adding buttons
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.third_matching_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.error));
    }
}