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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class PatientProfile extends Fragment implements View.OnClickListener{

    TextView nameView ,emailView , genderView , ageView , regionView;
    Helpers helpers;
    private RequestQueue mQueue;
    UserProfile user;
    RelativeLayout loadingDialog;
    Button updateProfile , showWounds;
    public static PatientProfile newInstance() {
        return new PatientProfile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patient_profile_fragment, container, false);
        nameView = view.findViewById(R.id.name);
        emailView = view.findViewById(R.id.email);
        ageView = view.findViewById(R.id.age);
        genderView = view.findViewById(R.id.gender);
        regionView = view.findViewById(R.id.region);
        loadingDialog = view.findViewById(R.id.loading);
        updateProfile = view.findViewById(R.id.updateBtn);
        showWounds = view.findViewById(R.id.showWounds);
        updateProfile.setOnClickListener(this);
        showWounds.setOnClickListener(this);
        helpers = new Helpers(getActivity());
        mQueue = Volley.newRequestQueue(getActivity());
        System.out.println("in on create view");
        getProfile();
        return view;
    }

    //validating user for login
    public void getProfile(){
        String id = helpers.getPreferences("shared_pref" , "uid");
        String url = getResources().getString(R.string.url)+"?uid="+id;
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                        JSONObject jsonObject = response.getJSONObject("res");
                        String name = helpers.capitalizeIstLetter(jsonObject.getString("name"));
                        String email = jsonObject.getString("email");
                        String age = helpers.capitalizeIstLetter(jsonObject.getString("age"));
                        String gender = helpers.capitalizeIstLetter(jsonObject.getString("gender"));
                        String region = helpers.capitalizeIstLetter(jsonObject.getString("region"));
                        user = new UserProfile(name , email , age , gender , region);
                        nameView.setText(name);
                        emailView.setText(email);
                        ageView.setText(age);
                        genderView.setText(gender);
                        regionView.setText(region);
                        loadingDialog.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println("Error is "+e.toString());
                    Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        user = mViewModel.getUser();
//        System.out.println(user);
    }

    public void update(View view){
        System.out.println("Coming");
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
                getProfile();
            }
        });
        AlertDialog dialog = builder.create(); // calling builder.create after adding buttons
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.third_matching_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.error));

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.updateBtn:
                FragmentTransaction t = this.getFragmentManager().beginTransaction();
                Fragment mFrag = new UpdateProfile();
                t.replace(R.id.nav_host_fragment, mFrag);
                t.commit();
                break;
            case R.id.showWounds:
                FragmentTransaction tr = this.getFragmentManager().beginTransaction();
                Fragment mFragg = new AllWounds();
                tr.replace(R.id.nav_host_fragment, mFragg);
                tr.commit();
                break;
        }
    }
}