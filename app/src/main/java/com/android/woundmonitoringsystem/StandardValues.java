package com.android.woundmonitoringsystem;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StandardValues extends Fragment {
    TextView ninetyFive , hun;
    public StandardValues() {
        // Required empty public constructor
    }

    public static StandardValues newInstance(String param1, String param2) {
        StandardValues fragment = new StandardValues();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_standard_values, container, false);
        ninetyFive = view.findViewById(R.id.ninetyFive);
        hun = view.findViewById(R.id.hun);
        ninetyFive.setText(">95%");
        hun.setText(">100%");
        return view;
    }
}