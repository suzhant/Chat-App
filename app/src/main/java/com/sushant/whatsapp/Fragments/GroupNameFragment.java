package com.sushant.whatsapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.MainActivity;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.FragmentGroupNameBinding;

import java.util.HashMap;
import java.util.Objects;


public class GroupNameFragment extends Fragment {

    FragmentGroupNameBinding binding;
    FirebaseDatabase database;
    ValueEventListener eventListener;
    DatabaseReference reference1;
    String id;

    public GroupNameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentGroupNameBinding.inflate(getLayoutInflater(),container,false);

        database=FirebaseDatabase.getInstance();
        assert getArguments() != null;
        id = getArguments().getString("GroupId");

        binding.btnRenameGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupName= Objects.requireNonNull(binding.editNewGroupName.getEditText()).getText().toString();
                if (groupName.isEmpty()){
                    Toast.makeText(getContext(), "Field cannot be Empty!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                database.getReference().child("Groups").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(id).child("groupName").setValue(groupName);
                reference1=database.getReference().child("Groups").child(FirebaseAuth.getInstance().getUid()).child(id).child("participant");
                eventListener= new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Users users = snapshot1.getValue(Users.class);
                                assert users != null;
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(users.getUserId()).child(id);
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("groupName", groupName);
                                reference.updateChildren(map);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                reference1.addValueEventListener(eventListener);
                binding.etNewGroupName.setText("");
                binding.editNewGroupName.clearFocus();
                Intent intent= new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                Toast.makeText(getContext(), "Group Name Changed", Toast.LENGTH_SHORT).show();
            }
        });



        return binding.getRoot();
    }
}