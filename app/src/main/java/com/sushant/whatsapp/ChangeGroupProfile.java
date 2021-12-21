package com.sushant.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.sushant.whatsapp.Fragments.GroupNameFragment;
import com.sushant.whatsapp.Fragments.GroupPicFragment;
import com.sushant.whatsapp.Fragments.GroupUsersFragment;
import com.sushant.whatsapp.databinding.ActivityChangeGroupProfileBinding;

public class ChangeGroupProfile extends AppCompatActivity {

    ActivityChangeGroupProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChangeGroupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        String Gid=getIntent().getStringExtra("Gid");
        String GName=getIntent().getStringExtra("GName");
        binding.txtGroupName.setText(GName);

        binding.btnChangeGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupNameFragment groupNameFragment= new GroupNameFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, groupNameFragment).commit();
            }
        });

        binding.btnChangeGroupPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupPicFragment groupPicFragment=new GroupPicFragment();
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
}