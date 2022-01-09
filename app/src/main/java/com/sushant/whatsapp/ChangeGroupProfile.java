package com.sushant.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Fragments.GroupNameFragment;
import com.sushant.whatsapp.Fragments.GroupPicFragment;
import com.sushant.whatsapp.Fragments.GroupUsersFragment;
import com.sushant.whatsapp.databinding.ActivityChangeGroupProfileBinding;

import java.util.Objects;

public class ChangeGroupProfile extends AppCompatActivity {

    ActivityChangeGroupProfileBinding binding;
    FirebaseDatabase database;
    ValueEventListener eventListener;
    DatabaseReference reference;
    Query query;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChangeGroupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        database=FirebaseDatabase.getInstance();

        String Gid=getIntent().getStringExtra("Gid");
        String GName=getIntent().getStringExtra("GName");
        binding.txtGroupName.setText(GName);

        reference=database.getReference().child("Groups").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(Gid).child("participant");
        query=reference.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid());

      eventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    String role= snapshot1.child("role").getValue(String.class);
                    if ("Admin".equals(role)){
                        binding.btnRemoveUser.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        query.addValueEventListener(eventListener);


        binding.btnChangeGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("GroupId",Gid);
                GroupNameFragment groupNameFragment= new GroupNameFragment();
                groupNameFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, groupNameFragment).commit();
            }
        });

        binding.btnChangeGroupPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("GroupId",Gid);
                GroupPicFragment groupPicFragment=new GroupPicFragment();
                groupPicFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, groupPicFragment).commit();
            }
        });

        binding.btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("GroupId",Gid);
                GroupUsersFragment groupUsersFragment= new GroupUsersFragment();
                groupUsersFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, groupUsersFragment).commit();
            }
        });

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reference!=null){
            query.removeEventListener(eventListener);
        }
    }
}