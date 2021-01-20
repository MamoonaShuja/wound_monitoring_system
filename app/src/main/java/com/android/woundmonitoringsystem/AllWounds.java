package com.android.woundmonitoringsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ArrayList;

public class AllWounds extends Fragment implements View.OnClickListener{
    Button addBtn;
    ArrayList<WoundList> lists;
    Helpers helpers;
    private RequestQueue mQueue;
    RelativeLayout loadingDialog , noRecordView;
    WoundList wound;
    RecyclerView recyclerView;
    public AllWounds() {
        // Required empty public constructor
    }

    public static AllWounds newInstance(String param1, String param2) {
        AllWounds fragment = new AllWounds();
        Bundle args = new Bundle();
        fragment.setArguments(args);
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
        View view = inflater.inflate(R.layout.fragment_all_wounds, container, false);
        addBtn = view.findViewById(R.id.add_wound);
        recyclerView = view.findViewById(R.id.recycler);
        noRecordView = view.findViewById(R.id.no_record);
        loadingDialog = view.findViewById(R.id.loading);
        helpers = new Helpers(getActivity());
        lists = new ArrayList<>();
        mQueue = Volley.newRequestQueue(getActivity());
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        addBtn.setOnClickListener(this);
        getWounds();
        return view;
    }

    @Override
    public void onClick(View view) {
        FragmentTransaction t = this.getFragmentManager().beginTransaction();
        Fragment mFrag = new WoundProfile();
        t.replace(R.id.nav_host_fragment, mFrag);
        t.commit();
    }

    public void getWounds(){
        String id = helpers.getPreferences("shared_pref" , "uid");
        String url = getResources().getString(R.string.url)+"?wound_uid="+id;
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("res");
                    if(jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String type = helpers.capitalizeIstLetter(jsonObject.getString("type"));
                            String size = jsonObject.getString("size");
                            String color = helpers.capitalizeIstLetter(jsonObject.getString("color"));
                            wound = new WoundList(type, size, color);
                            lists.add(wound);
                        }
                        loadingDialog.setVisibility(View.GONE);
                        noRecordView.setVisibility(View.GONE);
                        WoundRecyclerAdapter adapter = new WoundRecyclerAdapter(lists);
                        recyclerView.setAdapter(adapter);
                    }else{
                        loadingDialog.setVisibility(View.GONE);
                    }
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
                getWounds();
            }
        });
        AlertDialog dialog = builder.create(); // calling builder.create after adding buttons
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.third_matching_color));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.error));

    }
}