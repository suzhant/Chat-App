package com.sushant.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Adapters.FriendRequestAdapter;
import com.sushant.whatsapp.Adapters.ProfileAdapter;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityFindFriendBinding;
import com.sushant.whatsapp.databinding.ActivityFriendRequestBinding;

import java.util.ArrayList;

public class FriendRequestActivity extends AppCompatActivity {

    ActivityFriendRequestBinding binding;
    MaterialToolbar toolbar;
    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    LinearLayoutManager layoutManager;
    FriendRequestAdapter adapter;
    DatabaseReference ref;
    ValueEventListener valueEventListener1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityFriendRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        ActionBar ab= getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();

        adapter = new FriendRequestAdapter(list, this);
        binding.friendRequestRecycleView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(this);
        binding.friendRequestRecycleView.setLayoutManager(layoutManager);
        getAllUsers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.top_app_bar,menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Type here to search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())){
                    ref.removeEventListener(valueEventListener1);
                    searchUser(query);
                }
                else {
                    getAllUsers();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())){
                    ref.removeEventListener(valueEventListener1);
                    searchUser(newText);
                }
                else {
                    getAllUsers();
                }
                return false;
            }
        });
        return true;
    }

    FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
    private void getAllUsers() {
        valueEventListener1= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    assert users != null;
                    users.setUserId(dataSnapshot.getKey());
                    if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                        if(users.getRequest().equals("Req_Pending")){
                            list.add(users);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref =FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("Friends");
        ref.addValueEventListener(valueEventListener1);
    }

    private void searchUser(String query) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("Friends");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    assert users != null;
                    if (!users.getUserId().equals(user.getUid())){
                        if (users.getRequest().equals("Req_Pending")){
                            if (users.getUserName().equalsIgnoreCase(query.toLowerCase())|| users.getMail().equalsIgnoreCase(query.toLowerCase())){
                                list.add(users);
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        ref.removeEventListener(valueEventListener1);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        ref.removeEventListener(valueEventListener1);
        super.onStop();
    }

    @Override
    protected void onRestart() {
        ref.addValueEventListener(valueEventListener1);
        super.onRestart();
    }
}