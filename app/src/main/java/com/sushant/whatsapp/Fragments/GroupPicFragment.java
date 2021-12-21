package com.sushant.whatsapp.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sushant.whatsapp.R;
import com.sushant.whatsapp.databinding.FragmentGroupPicBinding;


public class GroupPicFragment extends Fragment {

    FragmentGroupPicBinding binding;

    public GroupPicFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentGroupPicBinding.inflate(getLayoutInflater(),container,false);
        return binding.getRoot();
    }
}