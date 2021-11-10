package com.sushant.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Adapters.ProfileAdapter;
import com.sushant.whatsapp.Adapters.UsersAdapter;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityFindFriendBinding;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class FindFriendActivity extends AppCompatActivity{
    MaterialToolbar toolbar;
    ActivityFindFriendBinding findFriendBinding;
    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    LinearLayoutManager layoutManager;
    ProfileAdapter adapter;
    DatabaseReference ref;
    ValueEventListener valueEventListener1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findFriendBinding=ActivityFindFriendBinding.inflate(getLayoutInflater());
        setContentView(findFriendBinding.getRoot());
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        ActionBar ab= getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();

        adapter = new ProfileAdapter(list, this);
        findFriendBinding.findFriendRecycleView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(this);
        findFriendBinding.findFriendRecycleView.setLayoutManager(layoutManager);
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
                        list.add(users);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref =FirebaseDatabase.getInstance().getReference("Users");
        ref.addListenerForSingleValueEvent(valueEventListener1);
    }

    private void searchUser(String query) {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    assert users != null;
                    if (!users.getUserId().equals(user.getUid())){
                        if (users.getUserName().contains(query.toLowerCase())|| users.getMail().contains(query.toLowerCase())){
                            list.add(users);
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
}