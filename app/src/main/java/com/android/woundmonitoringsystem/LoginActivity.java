package com.android.woundmonitoringsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class LoginActivity extends AppCompatActivity {
    //Data Members
    EditText emailField , pwdField;
    String email , pwd ;
    Helpers helpers;
    JSONObject jsonBody;
    private RequestQueue mQueue;
//    On create default
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pwdField = findViewById(R.id.pwd);
        emailField = findViewById(R.id.email);
        helpers = new Helpers(LoginActivity.this);
        mQueue = Volley.newRequestQueue(getBaseContext());
    }
    //    Moving to SignUp
    public void signUp(View view){
       startActivity(new Intent(LoginActivity.this , Signup.class));
    }

//    login validate
    public void login(View view){
        email = emailField.getText().toString().trim();
        pwd = pwdField.getText().toString().trim();
        if(email.equals("")){
            emailField.setFocusable(true);
            emailField.setError("Email is required");
        }else if(pwd.equals("")){
            pwdField.setFocusable(true);
            pwdField.setError("Password is required");
        }else{
            helpers.addLoadingDialog("Validating" , "Validating Your Details");
            checkUser();
        }
    }
    //validating user for login
    public void checkUser(){
        try {
            jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("pwd", pwd);
            jsonBody.put("login", "done");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = getResources().getString(R.string.url);
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Integer id = Integer.parseInt(response.getString("res"));
                    if(id > 0){
                        helpers.savePreferences("shared_pref" , "uid" , String.valueOf(id));
                        helpers.dismissLoadingDialog();
                        startActivity(new Intent(LoginActivity.this , MainActivity.class));
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setCancelable(false);
        builder.setTitle("No Internet");
        builder.setMessage("Connection TimeOut! Please check your internet connection..");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                LoginActivity.this.finish();
            }
        });

        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                checkUser();
            }
        });
        AlertDialog dialog = builder.create(); // calling builder.create after adding buttons
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.third_matching_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.error));

    }
}