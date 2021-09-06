
package com.sushant.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.viewpager.widget.ViewPager;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.sushant.whatsapp.Adapters.FragmentsAdapter;

import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityMainBinding;

import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LifecycleObserver {

    ActivityMainBinding binding;

    Toolbar toolbar;
    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    TabLayout tabLayout;
    ViewPager viewPager;
    TextView nav_username, nav_email,nav_verify,nav_delete;
    CircleImageView nav_profile;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ValueEventListener eventListener;
    DatabaseReference infoConnected;
    BroadcastReceiver broadcastReceiver;
    String token;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        database= FirebaseDatabase.getInstance();;
        auth = FirebaseAuth.getInstance();

        broadcastReceiver=new InternetCheckServices();
        registerBroadcastReceiver();

        token=getIntent().getStringExtra("googleToken");


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        navigationView.setNavigationItemSelectedListener(this);


        viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        View header = navigationView.getHeaderView(0);
        nav_email = header.findViewById(R.id.nav_email);
        nav_username = header.findViewById(R.id.nav_username);
        nav_profile = header.findViewById(R.id.nav_profilePic);
        nav_verify=header.findViewById(R.id.nav_verify);

        //retrieving logged in user data from real time database into the nav header views
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users user = snapshot.getValue(Users.class);
                        assert user != null;
                        Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.avatar).into(nav_profile);
                        nav_email.setText(user.getMail());
                        nav_username.setText(user.getUserName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //check email verification
        FirebaseUser user=auth.getCurrentUser();
        assert user != null;
        if (!user.isEmailVerified()){
            //checking users presence
            manageConnection();
//            showErrorDialog();
            nav_verify.setVisibility(View.VISIBLE);
            Menu nav_Menu = navigationView.getMenu();
            nav_Menu.findItem(R.id.nav_link).setVisible(false);
            nav_verify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(), "Verification link sent to your email " +user.getEmail(), Toast.LENGTH_SHORT).show();
                            auth.signOut();
                            Intent intent= new Intent(getApplicationContext(),SignInActivity.class);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Email couldn't be sent", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }else {
            nav_verify.setVisibility(View.GONE);
        }


        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()){
                    return;
                }
                String token=task.getResult();
                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
                String uid=user.getUid();
                HashMap<String,Object> obj= new HashMap<>();
                obj.put("Token",token);
                database.getReference().child("Users").child(uid).updateChildren(obj);
            }
        });


        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        // app moved to foreground
        if (auth.getCurrentUser()!=null){
            database.goOnline();
            if (!auth.getCurrentUser().isEmailVerified()){
                showErrorDialog();
            }
        }
//        updateStatus("online");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        // app moved to background
        if (auth.getCurrentUser()!=null){
            database.goOffline();
            if (!auth.getCurrentUser().isEmailVerified()){
                showErrorDialog();
            }
        }
//        updateStatus("offline");
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu, menu);
//        return super.onCreateOptionsMenu(menu);
//
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.settings:
//                Intent setting= new Intent(MainActivity.this,SettingsActivity.class);
//                startActivity(setting);
//                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
//                break;
//
//            case R.id.groupChat:
//                Intent intent= new Intent(MainActivity.this,GroupChatActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.logout:
//                auth.signOut();
//                Intent intentLogout = new Intent(MainActivity.this, SignInActivity.class);
//                startActivity(intentLogout);
//                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_setting:
                Intent setting = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(setting);
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_groupChat:
                Intent intent = new Intent(MainActivity.this, GroupChatActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_link:
                Intent intent1 = new Intent(MainActivity.this,LinkAccount.class);
                startActivity(intent1);
                break;

            case R.id.nav_delete:
                showDeleteDialog();
                break;


            case R.id.nav_logout:
                Log.d("TAG", "onSuccess: logout started");
                updateStatus("offline");
                CheckConnection checkConnection= new CheckConnection();
                if (!checkConnection.isConnected(getApplicationContext())){
                    showCustomDialog();
                    return false;
                }
                deleteToken();
                infoConnected.removeEventListener(eventListener);
                database.goOffline();
                auth.signOut();

                Intent intentLogout = new Intent(getApplicationContext(), SignInActivity.class);
                intentLogout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentLogout);
                GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("SingOut", "onSuccess: ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("SingOut",e.getMessage());
                        Toast.makeText(MainActivity.this, "SignOut failed", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void manageConnection() {
       final DatabaseReference status = database.getReference().child("Users").child(auth.getUid()).child("Connection").child("Status");
       final DatabaseReference lastOnlineRef = database.getReference().child("Users").child(auth.getUid()).child("Connection").child("lastOnline");
        infoConnected = database.getReference(".info/connected");

       eventListener=infoConnected.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    status.setValue("online");
                    status.onDisconnect().setValue("offline");
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                }else{
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteToken(){
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        String uid=user.getUid();
        HashMap<String,Object> obj= new HashMap<>();
        obj.put("Token",null);
        database.getReference().child("Users").child(uid).updateChildren(obj);
    }

    //check connection
    private void registerBroadcastReceiver(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            registerReceiver(broadcastReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            registerReceiver(broadcastReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

    }

    private void unregisterNetwork(){
        try {
            unregisterReceiver(broadcastReceiver);

        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetwork();
    }


    private void showCustomDialog() {
        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Please connect to the internet to proceed forward")
                .setTitle("No Connection")
                .setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void showErrorDialog() {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Please verify your account in your email "+user.getEmail())
                .setTitle("Verify your account")
                .setCancelable(false)
                .setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), "Verification mail sent to your mail", Toast.LENGTH_SHORT).show();
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                                    startActivity(browserIntent);
                                }else {
                                    Toast.makeText(getApplicationContext(), "Something went wrong.Try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).setNegativeButton("Go to Sign In Page", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                database.goOffline();
                updateStatus("offline");
                auth.signOut();
                startActivity(new Intent(MainActivity.this,SignInActivity.class));
                finish();
            }
        }).show();
    }

    private void showDeleteDialog() {
        final EditText edittext = new EditText(MainActivity.this);
        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Enter Password")
                .setTitle("Delete Account")
                .setCancelable(false)
                .setView(edittext)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       final FirebaseUser user=auth.getCurrentUser();
                        assert user != null;
                        AuthCredential credential;
                        if (token == null) {
                            credential = EmailAuthProvider.getCredential(user.getEmail(), edittext.getText().toString());
                        } else {
                            //Doesn't matter if it was Facebook Sign-in or others. It will always work using GoogleAuthProvider for whatever the provider.
                            credential = GoogleAuthProvider.getCredential(token, null);
                        }

                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            deleteUser(user.getUid());
//                                            database.getReference().child("Users").child(auth.getUid()).removeValue();
                                            Toast.makeText(getApplicationContext(), "Re-authenticated", Toast.LENGTH_SHORT).show();
                                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Intent intent= new Intent(MainActivity.this,SignInActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(intent);
                                                        finish();
                                                        Log.d("TAG", "onComplete: User deleted"+user.getEmail());
                                                        Toast.makeText(getApplicationContext(), "User Account has been Deleted", Toast.LENGTH_SHORT).show();
                                                    }else {
                                                        Toast.makeText(getApplicationContext(), "Account couldn't be deleted", Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                            });
                                        } else {
                                    Toast.makeText(getApplicationContext(),  task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }


        void updateStatus(String status){
        HashMap<String,Object> obj= new HashMap<>();
        obj.put("Status",status);
        database.getReference().child("Users").child(auth.getUid()).child("Connection").updateChildren(obj);
    }

    void deleteUser(String userid){
        HashMap<String,Object> obj= new HashMap<>();
        obj.put(userid,null);
        database.getReference().child("Users").updateChildren(obj);
    }




}