package com.sushant.whatsapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sushant.whatsapp.Adapters.FragmentsAdapter;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityMainBinding;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DefaultLifecycleObserver {

    ActivityMainBinding binding;

    Toolbar toolbar;
    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    TabLayout tabLayout;
    ViewPager viewPager;
    TextView nav_username, nav_email, nav_verify, txtUserName;
    CircleImageView nav_profile;
    FirebaseAuth auth;
    FirebaseDatabase database;
    BroadcastReceiver broadcastReceiver;
    DatabaseReference reference, infoConnected, NavDrawer, friendReqRef;
    ValueEventListener eventListener1, eventListener, friendReqListener;
    SharedPreferences sharedPreferences;
    TextView txtConnection, txtFriendReq;
    Boolean conn;
    int friendCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        txtConnection = findViewById(R.id.txtNoConnection);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPurple));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.mainNavColor));

        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("Users");
        auth = FirebaseAuth.getInstance();
        manageConnection();

        broadcastReceiver = new InternetCheckServices();
        registerBroadcastReceiver();
        CheckConnection checkConnection = new CheckConnection();
        conn = !checkConnection.isConnected(getApplicationContext());
        if (!conn) {
            txtConnection.setVisibility(View.VISIBLE);
        }


        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        boolean Notification = sharedPreferences.getBoolean("Notification", true);
        if (Notification) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    String token = task.getResult();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    assert user != null;
                    String uid = user.getUid();
                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("Token", token);
                    reference.child(uid).updateChildren(obj);
                }
            });
        }

        //updating Email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String uid = user.getUid();

            reference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Users users = snapshot.getValue(Users.class);
                    assert users != null;
                    if (!users.getMail().equals(email)) {
                        updateEmailInFriend(uid, email);
                        updateEmail(email, uid);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        navigationView.setNavigationItemSelectedListener(this);
        txtUserName = findViewById(R.id.txtUserName);
        MenuItem menu = navigationView.getMenu().findItem(R.id.nav_friendRequest);
        txtFriendReq = (TextView) menu.getActionView();


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
        nav_verify = header.findViewById(R.id.nav_verify);


        //retrieving logged in user data from real time database into the nav header views
        eventListener1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                assert user != null;
                Glide.with(getApplicationContext()).load(user.getProfilePic()).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(nav_profile);
                nav_email.setText(user.getMail());
                nav_username.setText(user.getUserName());
                txtUserName.setText(user.getUserName().toUpperCase(Locale.ROOT));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        NavDrawer = reference.child(Objects.requireNonNull(auth.getUid()));
        NavDrawer.addValueEventListener(eventListener1);

        //retrieving friend request info from db
        friendReqListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendCounter = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Users user = snapshot1.getValue(Users.class);
                    assert user != null;
                    if (user.getRequest() != null) {
                        if ("Req_Pending".equals(user.getRequest())) {
                            friendCounter++;
                        }
                    }

                }
                initializeCountDrawer();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        friendReqRef = reference.child(Objects.requireNonNull(auth.getUid())).child("Friends");
        friendReqRef.addValueEventListener(friendReqListener);


        //check email verification
        assert user != null;
        if (!user.isEmailVerified()) {
            //checking users presence
            showErrorDialog();
            nav_verify.setVisibility(View.VISIBLE);
            Menu nav_Menu = navigationView.getMenu();
            nav_Menu.findItem(R.id.nav_link).setVisible(false);
            nav_verify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(), "Verification link sent to your email " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            auth.signOut();
                            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
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
        } else {
            nav_verify.setVisibility(View.GONE);
//            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
//                @Override
//                public void onComplete(@NonNull Task<String> task) {
//                    if (!task.isSuccessful()){
//                        return;
//                    }
//                    String token=task.getResult();
//                    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
//                    assert user != null;
//                    String uid=user.getUid();
//                    HashMap<String,Object> obj= new HashMap<>();
//                    obj.put("Token",token);
//                    reference.child(uid).updateChildren(obj);
//                }
//            });
        }


        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

    }

    private void initializeCountDrawer() {
        //Gravity property aligns the text
        txtFriendReq.setGravity(Gravity.CENTER_VERTICAL);
        txtFriendReq.setTypeface(null, Typeface.BOLD);
        txtFriendReq.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPurple));
        if (friendCounter == 0) {
            txtFriendReq.setText("");
        } else {
            txtFriendReq.setText(String.valueOf(friendCounter));
        }

    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        // app moved to foreground
        CheckConnection checkConnection = new CheckConnection();
        if (checkConnection.isConnected(getApplicationContext())) {
            txtConnection.setVisibility(View.VISIBLE);
        } else {
            txtConnection.setVisibility(View.GONE);
        }
        if (auth.getCurrentUser() != null) {
            NavDrawer.addValueEventListener(eventListener1);
            friendReqRef.addValueEventListener(friendReqListener);
//            database.goOnline();
            updateStatus("online");
            if (!auth.getCurrentUser().isEmailVerified()) {
                showErrorDialog();
            }
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        // app moved to background
        if (auth.getCurrentUser() != null) {
            NavDrawer.removeEventListener(eventListener1);
            friendReqRef.removeEventListener(friendReqListener);
//            database.goOffline();
            updateStatus("offline");
            if (!auth.getCurrentUser().isEmailVerified()) {
                showErrorDialog();
            }
        }
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

//            case R.id.nav_groupChat:
//                Intent intent = new Intent(MainActivity.this, GroupChatActivity.class);
//                startActivity(intent);
//                break;

            case R.id.nav_findFriend:
                Intent intent2 = new Intent(MainActivity.this, FindFriendActivity.class);
                startActivity(intent2);
                break;

            case R.id.nav_friendRequest:
                Intent intent3 = new Intent(MainActivity.this, FriendRequestActivity.class);
                startActivity(intent3);
                break;

//            case R.id.nav_delete:
//                showDeleteDialog();
//                break;

            case R.id.nav_logout:
                Log.d("TAG", "onSuccess: logout started");
                Log.d("TAG", "onNavigationItemSelected: destroyed");
                updateStatus("offline");
                CheckConnection checkConnection = new CheckConnection();
                if (checkConnection.isConnected(getApplicationContext())) {
                    showCustomDialog();
                    return false;
                }
                deleteToken();
//                database.goOffline();
                auth.signOut();

                Intent intentLogout = new Intent(getApplicationContext(), SignInActivity.class);
                intentLogout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentLogout);
                GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("SingOut", "onSuccess: ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("SingOut", e.getMessage());
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



    private void deleteToken() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        String uid = user.getUid();
        HashMap<String, Object> obj = new HashMap<>();
        obj.put("Token", null);
        reference.child(uid).updateChildren(obj);
    }

    //check connection
    private void registerBroadcastReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unregisterNetwork() {
        try {
            unregisterReceiver(broadcastReceiver);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetwork();
        infoConnected.removeEventListener(eventListener);
        friendReqRef.removeEventListener(friendReqListener);
        NavDrawer.removeEventListener(eventListener1);
        Log.d("Drawer", "onDestroy: eventDeleted");
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
        final DatabaseReference status = database.getReference().child("Users").child((auth.getUid())).child("Connection").child("Status");
        final DatabaseReference lastOnlineRef = database.getReference().child("Users").child(auth.getUid()).child("Connection").child("lastOnline");
        infoConnected = database.getReference(".info/connected");

        eventListener = infoConnected.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    status.setValue("online");
                    lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                } else {
                    status.onDisconnect().setValue("offline");
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Please verify your account in your email " + user.getEmail())
                .setTitle("Verify your account")
                .setCancelable(false)
                .setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Verification mail sent to your mail", Toast.LENGTH_SHORT).show();
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                                    startActivity(browserIntent);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Something went wrong.Try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).setNegativeButton("Go to Sign In Page", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                database.goOffline();
                deleteToken();
                updateStatus("offline");
                auth.signOut();
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                finish();
            }
        }).show();
    }

    void updateStatus(String status) {
        HashMap<String, Object> obj = new HashMap<>();
        obj.put("Status", status);
        reference.child(auth.getUid()).child("Connection").updateChildren(obj);
    }

    void updateEmail(String email, String uid) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("mail", email);
        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).updateChildren(map);
    }

    void updateEmailInFriend(String userid, String email) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Users").child(userid).child("Friends");
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Users users = snapshot1.getValue(Users.class);
                    DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference().child("Users").child(users.getUserId()).child("Friends");
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("mail", email);
                    reference2.child(userid).updateChildren(map);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}