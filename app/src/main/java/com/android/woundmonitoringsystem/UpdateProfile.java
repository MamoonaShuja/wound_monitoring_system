package com.android.woundmonitoringsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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

public class UpdateProfile extends Fragment implements View.OnClickListener{

    EditText nameField  , pwdField , ageField;
    AutoCompleteTextView genderField , regionField;
    ArrayList<String> regionsList , genderList;
    ArrayAdapter regionAdapter , genderAdapter;
    String name  , pwd , age , gender , region;
    Helpers helpers;
    Button updateBtn;
    private RequestQueue mQueue;
    JSONObject jsonBody;
    //    Signup Method
    public void update(){
        name = nameField.getText().toString().trim();
        pwd = pwdField.getText().toString().trim();
        age = ageField.getText().toString().trim();
        gender = genderField.getText().toString().trim();
        region = regionField.getText().toString().trim();
        if(name.equals("")){
            nameField.setFocusable(true);
            nameField.setError("Name is required");
        }else if(age.equals("")){
            ageField.setFocusable(true);
            ageField.setError("Age is required");
        }else if(gender.equals("")){
            genderField.setFocusable(true);
            genderField.setError("Gender is required");
        }else if(pwd.equals("")){
            pwdField.setFocusable(true);
            pwdField.setError("Password is required");
        }else if(region.equals("")){
            regionField.setFocusable(true);
            regionField.setError("Region is required");
        }else{
            helpers.addLoadingDialog("Updating" , "Updating Your Account");
            updateUser();
            FragmentTransaction t = this.getFragmentManager().beginTransaction();
            Fragment mFrag = new PatientProfile();
            t.replace(R.id.nav_host_fragment, mFrag);
            t.commit();
        }
    }

    public void updateUser(){
        String id = helpers.getPreferences("shared_pref" , "uid");
        try {
            jsonBody = new JSONObject();
            jsonBody.put("uid", id);
            jsonBody.put("pwd", pwd);
            jsonBody.put("name", name);
            jsonBody.put("gender", gender);
            jsonBody.put("age", age);
            jsonBody.put("region", region);
            jsonBody.put("update", "done");
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
                        Toast.makeText(getActivity() , "Updated Successfully" , Toast.LENGTH_LONG).show();
                        helpers.dismissLoadingDialog();
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
            }
        });

        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                updateUser();
            }
        });
        AlertDialog dialog = builder.create(); // calling builder.create after adding buttons
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.third_matching_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.error));
    }
    public static UpdateProfile newInstance(String param1, String param2) {
        UpdateProfile fragment = new UpdateProfile();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_update_profile, container, false);
        mQueue = Volley.newRequestQueue(getActivity());
        helpers = new Helpers(getActivity());
        nameField = view.findViewById(R.id.name);
        pwdField = view.findViewById(R.id.pwd);
        ageField = view.findViewById(R.id.age);
        genderField = view.findViewById(R.id.gender);
        regionField = view.findViewById(R.id.region);
        updateBtn = view.findViewById(R.id.updateBtn);
        updateBtn.setOnClickListener(this);
        //Adapters
        regionAdapter = new ArrayAdapter(getActivity() , android.R.layout.simple_list_item_1 , regionsList);
        genderAdapter = new ArrayAdapter(getActivity() , android.R.layout.simple_list_item_1 , genderList);
        //Setting ThreshHold
        regionField.setThreshold(1);
        genderField.setThreshold(1);
        return view;
    }

    @Override
    public void onClick(View view) {
        update();
    }
}