package com.sushant.whatsapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Adapters.ProfileAdapter;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityFindFriendBinding;

import java.util.ArrayList;

public class FindFriendActivity extends AppCompatActivity {
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
        findFriendBinding = ActivityFindFriendBinding.inflate(getLayoutInflater());
        setContentView(findFriendBinding.getRoot());
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
//        ActionBar ab = getSupportActionBar();
//        assert ab != null;
//        ab.setDisplayHomeAsUpEnabled(true);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPurple));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.mainNavColor));

        database = FirebaseDatabase.getInstance();

        adapter = new ProfileAdapter(list, this);
        findFriendBinding.findFriendRecycleView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(this);
        findFriendBinding.findFriendRecycleView.setLayoutManager(layoutManager);
        getAllUsers();

        findFriendBinding.topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Type here to search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                if (!TextUtils.isEmpty(query.trim())) {
//                    searchUser(query);
//                } else {
//                    getAllUsers();
//                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchUser(newText);
                } else {
                    getAllUsers();
                }
                return false;
            }
        });
        return true;
    }

    private void getAllUsers() {
        valueEventListener1 = new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    assert users != null;
                    users.setUserId(dataSnapshot.getKey());
                    if (users.getUserId() != null && !users.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                        list.add(users);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addListenerForSingleValueEvent(valueEventListener1);
    }

    private void searchUser(String query) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    assert users != null;
                    if (users.getUserId() != null) {
                        assert user != null;
                        if (!users.getUserId().equals(user.getUid())) {
                            if (users.getUserName().toLowerCase().contains(query.toLowerCase().trim()) || users.getMail().toLowerCase().equals(query.toLowerCase().trim())) {
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
    public void onBackPressed() {
        finishAfterTransition();
        super.onBackPressed();
    }
}