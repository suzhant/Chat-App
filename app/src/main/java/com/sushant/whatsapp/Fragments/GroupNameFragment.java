package com.sushant.whatsapp.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sushant.whatsapp.R;
import com.sushant.whatsapp.databinding.FragmentGroupNameBinding;


public class GroupNameFragment extends Fragment {

    FragmentGroupNameBinding binding;

    public GroupNameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentGroupNameBinding.inflate(getLayoutInflater(),container,false);



        return binding.getRoot();
    }
}