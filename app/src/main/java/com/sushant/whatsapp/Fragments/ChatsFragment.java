package com.sushant.whatsapp.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sushant.whatsapp.Adapters.StoryAdapter;
import com.sushant.whatsapp.Adapters.UsersAdapter;
import com.sushant.whatsapp.FindFriendActivity;
import com.sushant.whatsapp.Models.Story;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.R;
import com.sushant.whatsapp.Utils.Encryption;
import com.sushant.whatsapp.Utils.ImageUtils;
import com.sushant.whatsapp.databinding.FragmentChatsBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class ChatsFragment extends Fragment {

    FragmentChatsBinding binding;
    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    LinearLayoutManager layoutManager, storyLayoutManager;
    UsersAdapter adapter;
    DatabaseReference d1, storyRef;
    ValueEventListener eventListener, storyListener;
    ArrayList<Users> oldlist = new ArrayList<>();
    Query query;
    ArrayList<String> stories = new ArrayList<>();
    ArrayList<Users> selectedUsers = new ArrayList<>();
    StoryAdapter storyAdapter;
    FirebaseAuth auth;
    String pp, name;
    ActivityResultLauncher<Intent> imageLauncher;
    ProgressDialog dialog;
    FirebaseStorage storage;
    Users admin;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(getContext());
        storage = FirebaseStorage.getInstance();
        //chat adapter
        adapter = new UsersAdapter(oldlist, getContext());
        binding.chatRecyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getContext());
        binding.chatRecyclerView.setLayoutManager(layoutManager);


        //getting pic from the db
        getUserDetails();

        //story adapter
        storyAdapter = new StoryAdapter(selectedUsers, getContext());
        binding.storyRecycler.setAdapter(storyAdapter);
        storyLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.storyRecycler.setLayoutManager(storyLayoutManager);


        d1 = database.getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("Friends");

        binding.chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    binding.fab.collapse(true);
                } else {
                    binding.fab.expand(true);
                }
            }
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FindFriendActivity.class);
                startActivity(intent);
            }
        });

        database.getReference().child("Stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    stories.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Users users = snapshot1.getValue(Users.class);
                        assert users != null;
                        if (snapshot1.hasChild("Info")) {
                            stories.add(users.getUserId());
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        eventListener = new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    assert users != null;
                    users.setUserId(dataSnapshot.getKey());
                    if (users.getUserId() != null && !users.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                        if ("Accepted".equals(users.getRequest())) {
                            if (users.getNickName() != null) {
                                users.setUserName(users.getNickName());
                            }
                            list.add(users);
                        }
                    }
                }
                Collections.reverse(list);
                //  adapter.notifyDataSetChanged();
                adapter.updateUserList(list);
                oldlist.clear();
                oldlist.addAll(list);

                selectedUsers.clear();
                for (int i = 0; i < stories.size(); i++) {
                    String users1 = stories.get(i);
                    if (users1.equals(auth.getUid())) {
                        selectedUsers.add(admin);
                    }
                    for (int j = 0; j < oldlist.size(); j++) {
                        Users users = oldlist.get(j);
                        if (users1.equals(users.getUserId())) {
                            selectedUsers.add(users);
                            break;
                        }
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       };
        query = d1.orderByChild("timestamp");
        query.addValueEventListener(eventListener);

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                query.addValueEventListener(eventListener);
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        //  createStory;
        binding.imgCreateStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                imageLauncher.launch(intent);
            }
        });

        imageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            if (result.getData().getClipData() != null) {
                                ClipData clipData = result.getData().getClipData();
                                int count = clipData.getItemCount();
                                for (int i = 0; i < count; i++) {
                                    Uri imageUrl = clipData.getItemAt(i).getUri();
                                    createImageBitmap(imageUrl);
                                }

                            } else if (result.getData().getData() != null) {
                                Uri selectedImage = result.getData().getData();
                                createImageBitmap(selectedImage);
                            }
                        }
                    }
                });
        deleteMessage();
        return binding.getRoot();
    }

    private void getUserDetails() {
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                admin = snapshot.getValue(Users.class);
                assert admin != null;
                pp = admin.getProfilePic();
                name = admin.getUserName();
                if (getActivity() != null) {
                    Glide.with(getActivity()).load(pp).placeholder(R.drawable.avatar)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(binding.imgCreateStory);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createImageBitmap(Uri imageUrl) {
        Bitmap bitmap = null;
        try {
            bitmap = ImageUtils.handleSamplingAndRotationBitmap(requireContext(), imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert bitmap != null;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        int length = bytes.length / 1024;
        uploadImageToFirebase(bytes, length);
    }

    private void uploadImageToFirebase(byte[] uri, int length) {
        Calendar calendar = Calendar.getInstance();
        final StorageReference reference = storage.getReference().child("Stories").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(calendar.getTimeInMillis() + "");
        if (length > 256) {
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        dialog.setProgress(0);
        dialog.setMessage("Uploading Image");
        dialog.show();
        UploadTask uploadTask = reference.putBytes(uri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                // Uri fdelete = Uri.fromFile(new File(uri.toString()));
                // File fdelete= new File(uri.toString());
                //File fdelete = new File(Objects.requireNonNull(getFilePath(uri)));

                if (task.isSuccessful()) {
                    dialog.dismiss();
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void onSuccess(Uri uri) {
                            String filePath = uri.toString();
                            String key = database.getReference().push().getKey();
                            Date date = new Date();
                            final Story story = new Story(key, Encryption.encryptMessage(filePath), date.getTime());
                            story.setType("image");

                            assert key != null;

                            HashMap<String, Object> user = new HashMap<>();
                            user.put("userName", name);
                            user.put("profilePic", pp);
                            user.put("userId", auth.getUid());
                            database.getReference().child("Stories").child(Objects.requireNonNull(auth.getUid())).child("Info").child(key).setValue(story).
                                    addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference().child("Stories").child(Objects.requireNonNull(auth.getUid())).updateChildren(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(getContext(), "Added Story Successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                //only works if image size is greater than 256kb!
                if (length > 256) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    int currentProgress = (int) progress;
                    dialog.setProgress(currentProgress);
                }
            }
        });
    }

    private void deleteMessage() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS); //1 day old
                DatabaseReference ttlRef = database.getReference().child("Stories");
                ttlRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Users users = snapshot1.getValue(Users.class);
                            DatabaseReference infoRef = database.getReference().child("Stories").child(users.getUserId()).child("Info");
                            Query oldItems = infoRef.orderByChild("timestamp").endAt(cutoff);
                            oldItems.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                        itemSnapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    throw databaseError.toException();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        };
        handler.postDelayed(runnable, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        query.removeEventListener(eventListener);
        d1.keepSynced(false);
    }
}