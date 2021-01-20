package com.android.woundmonitoringsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

public class Signup extends AppCompatActivity {
//    Data members
    EditText nameField , emailField , pwdField , ageField;
    AutoCompleteTextView genderField , regionField;
    ArrayList<String> regionsList , genderList;
    ArrayAdapter regionAdapter , genderAdapter;
    String name , email , pwd , age , gender , region;
    Helpers helpers;
    JSONObject jsonBody;
    //    Moving to Login
    private RequestQueue mQueue;
    public void login(View view){
        startActivity(new Intent(Signup.this , LoginActivity.class));
    }
//    Signup Method
    public void signUp(View view){
        name = nameField.getText().toString().trim();
        email = emailField.getText().toString().trim();
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
        }else if(email.equals("")){
            emailField.setFocusable(true);
            emailField.setError("Email is required");
        }else if(pwd.equals("")){
            pwdField.setFocusable(true);
            pwdField.setError("Password is required");
        }else if(region.equals("")){
            regionField.setFocusable(true);
            regionField.setError("Region is required");
        }else{
            helpers.addLoadingDialog("Sign Up" , "Creating Your Account");
            addUser();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
//        queue obj
        mQueue = Volley.newRequestQueue(getBaseContext());
        helpers = new Helpers(Signup.this);
        regionsList = new ArrayList<>();
        genderList = new ArrayList<>();
        Collections.addAll(regionsList , "Africa" , "Asia" ,"The Caribbean",
        "Central America" , "Europe" , "North America" , "Oceania" , "South America"
        );
        Collections.addAll(genderList , "Male" , "Female" , "Others");
//        EditText Fields
        nameField = findViewById(R.id.name);
        emailField = findViewById(R.id.email);
        pwdField = findViewById(R.id.pwd);
        ageField = findViewById(R.id.age);
        genderField = findViewById(R.id.gender);
        regionField = findViewById(R.id.region);
        //Adapters
        regionAdapter = new ArrayAdapter(this , android.R.layout.simple_list_item_1 , regionsList);
        genderAdapter = new ArrayAdapter(this , android.R.layout.simple_list_item_1 , genderList);
        //Setting ThreshHold
        regionField.setThreshold(1);
        genderField.setThreshold(1);

        //Setting adapters
        regionField.setAdapter(regionAdapter);
        genderField.setAdapter(genderAdapter);

    }

    public void addUser(){
        try {
            jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("pwd", pwd);
            jsonBody.put("name", name);
            jsonBody.put("gender", gender);
            jsonBody.put("age", age);
            jsonBody.put("region", region);
            jsonBody.put("signup", "done");

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
                        Toast.makeText(getBaseContext() , "Sign up Successfully" , Toast.LENGTH_LONG).show();
                        helpers.dismissLoadingDialog();
                        startActivity(new Intent(Signup.this , LoginActivity.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Signup.this, e.toString(), Toast.LENGTH_SHORT).show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(Signup.this);
        builder.setCancelable(false);
        builder.setTitle("No Internet");
        builder.setMessage("Connection TimeOut! Please check your internet connection..");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Signup.this.finish();
            }
        });

        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                addUser();
            }
        });
        AlertDialog dialog = builder.create(); // calling builder.create after adding buttons
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.third_matching_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.error));
    }
}