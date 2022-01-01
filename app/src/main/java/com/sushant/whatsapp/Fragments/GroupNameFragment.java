package com.sushant.whatsapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

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

        String text=binding.editNewGroupName.getEditText().getText().toString();
        binding.btnRenameGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!text.isEmpty()){

                }
            }
        });



        return binding.getRoot();
    }
}