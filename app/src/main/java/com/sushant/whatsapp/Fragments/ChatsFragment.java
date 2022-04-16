package com.sushant.whatsapp.Fragments;

import static com.sushant.whatsapp.R.color.amp_transparent;
import static com.sushant.whatsapp.R.color.red;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.github.dhaval2404.imagepicker.ImagePicker;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sushant.whatsapp.Adapters.StoryAdapter;
import com.sushant.whatsapp.Adapters.UsersAdapter;
import com.sushant.whatsapp.CheckConnection;
import com.sushant.whatsapp.FindFriendActivity;
import com.sushant.whatsapp.Models.Story;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.R;
import com.sushant.whatsapp.Utils.Encryption;
import com.sushant.whatsapp.Utils.ImageUtils;
import com.sushant.whatsapp.databinding.FragmentChatsBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class ChatsFragment extends Fragment {

    public static final int REQUEST_IMAGE_CAPTURE = 23;
    FragmentChatsBinding binding;
    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    LinearLayoutManager layoutManager, storyLayoutManager;
    UsersAdapter adapter;
    DatabaseReference d1, storyRef;
    ValueEventListener eventListener, storyListener, mediaListener;
    ArrayList<Users> oldlist = new ArrayList<>();
    Query query;
    ArrayList<Users> selectedUsers = new ArrayList<>();
    StoryAdapter storyAdapter;
    FirebaseAuth auth;
    String pp, name;
    ActivityResultLauncher<Intent> imageLauncher, cameraLauncher, editResultLauncher;
    ProgressDialog dialog;
    FirebaseStorage storage;
    Users admin;
    ArrayList<Users> oldStoryList = new ArrayList<>();
    android.app.AlertDialog.Builder alertDialog;
    Uri selectedImage;

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
        storyAdapter = new StoryAdapter(oldStoryList, getContext());
        binding.storyRecycler.setAdapter(storyAdapter);
        storyLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.storyRecycler.setLayoutManager(storyLayoutManager);


        d1 = database.getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("Friends");

//        binding.chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (dy > 0) {
//                    binding.fab.collapse(true);
//                } else {
//                    binding.fab.expand(true);
//                }
//            }
//        });

        binding.chatScroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                if (i1 > 0) {
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

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        query = d1.orderByChild("timestamp");
        query.addValueEventListener(eventListener);

        storyRef = database.getReference().child("Stories").child(Objects.requireNonNull(auth.getUid()));
        storyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                selectedUsers.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Users users = snapshot1.getValue(Users.class);
                        assert users != null;
                        if (users.getUserId() != null) {
                            if (users.getLastStory() != null) {
                                try {
                                    users.setLastStory(Encryption.decryptMessage(users.getLastStory()));
                                } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }

                            storyRef.child(users.getUserId()).child("medias").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int childCount = (int) snapshot.getChildrenCount();
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("storiesCount", childCount);
                                    storyRef.child(users.getUserId()).updateChildren(map);
                                    int unSeenCount = 0;
                                    for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                                        Story story = snapshot2.getValue(Story.class);
                                        assert story != null;
                                        if (story.getSid() != null) {
                                            if (story.getSeen().equals("false")) {
                                                unSeenCount++;
                                            }
                                        }
                                    }
                                    HashMap<String, Object> user = new HashMap<>();
                                    user.put("unseenCount", unSeenCount);
                                    FirebaseDatabase.getInstance().getReference().child("Stories").child(FirebaseAuth.getInstance().getUid())
                                            .child(users.userId).updateChildren(user);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            selectedUsers.add(users);
                            Collections.reverse(selectedUsers);
                            storyAdapter.updateStoryList(selectedUsers);
                            oldStoryList.clear();
                            oldStoryList.addAll(selectedUsers);
                        }
                    }
                    //    storyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        Query story = storyRef.orderByChild("unseenCount");
        story.addValueEventListener(storyListener);

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                story.addValueEventListener(storyListener);
                query.addValueEventListener(eventListener);
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        mediaListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Users users = snapshot1.getValue(Users.class);
                    assert users != null;
                    if (users.getUserId() != null) {
                        storyRef.child(users.getUserId()).child("medias").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int count = 0;
                                for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                                    Story story = snapshot2.getValue(Story.class);
                                    assert story != null;
                                    if (story.getSid() != null) {
                                        if (snapshot.getChildrenCount() == 1) {
                                            HashMap<String, Object> user = new HashMap<>();
                                            user.put("lastStory", story.getUrl());
                                            storyRef.child(users.getUserId()).updateChildren(user);
                                            break;
                                        } else if (story.getSeen().equals("false")) {
                                            HashMap<String, Object> user = new HashMap<>();
                                            user.put("lastStory", story.getUrl());
                                            storyRef.child(users.getUserId()).updateChildren(user);
                                            break;
                                        } else if (story.getSeen().equals("true")) {
                                            count = (int) ((count + 1) % snapshot.getChildrenCount());
                                        }
                                    }
                                }
                                HashMap<String, Object> user = new HashMap<>();
                                user.put("seenCount", count);
                                storyRef.child(users.getUserId()).updateChildren(user);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        storyRef.addValueEventListener(mediaListener);

        //  createStory;
        binding.createStoryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        showCaptureDialog();
                    }
                };
                handler.postDelayed(runnable, 100);
            }
        });

        alertDialog = new android.app.AlertDialog.Builder(getContext()).setTitle("Image")
                .setMessage("Do you want to edit the image?")
                .setCancelable(false)
                .setIcon(R.drawable.ic_gallery)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkPermission(selectedImage);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createImageBitmap(selectedImage);
                        dialogInterface.dismiss();
                    }
                });

        editResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            if (result.getData().getData() != null) {
                                Uri editedImage = result.getData().getData();
                                createImageBitmap(editedImage);
                            }
                        }
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
                                selectedImage = result.getData().getData();
                                alertDialog.show();
//                                createImageBitmap(selectedImage);
                            }
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            if (result.getData().getData() != null) {
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
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(binding.imgPp);
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
                            story.setSid(key);
                            story.setSeen("false");

                            assert key != null;

                            HashMap<String, Object> user = new HashMap<>();
                            user.put("userName", name);
                            user.put("profilePic", pp);
                            user.put("userId", auth.getUid());
                            database.getReference().child("Stories").child(Objects.requireNonNull(auth.getUid())).child(auth.getUid()).updateChildren(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference().child("Stories").child(Objects.requireNonNull(auth.getUid())).child(auth.getUid()).child("medias")
                                                    .child(key).setValue(story);
                                            for (int i = 0; i < oldlist.size(); i++) {
                                                Users users = oldlist.get(i);
                                                database.getReference().child("Stories").child(Objects.requireNonNull(users.getUserId())).child(auth.getUid()).updateChildren(user)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                database.getReference().child("Stories").child(users.getUserId()).child(auth.getUid()).child("medias")
                                                                        .child(key).setValue(story);
                                                            }
                                                        });
                                            }
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
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS); //1 day old
                DatabaseReference ttlRef = database.getReference().child("Stories").child(Objects.requireNonNull(auth.getUid()));
                ttlRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Users users = snapshot1.getValue(Users.class);
                            assert users != null;
                            if (users.getUserId() != null) {
                                DatabaseReference infoRef = database.getReference().child("Stories").child(auth.getUid()).child(users.getUserId()).child("medias");
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        };
        handler.postDelayed(runnable, 2000);
    }

    private void showCaptureDialog() {

        if (getActivity() != null) {
            CheckConnection checkConnection = new CheckConnection();
            if (checkConnection.isConnected(getActivity()) || !checkConnection.isInternet()) {
                Toast.makeText(getActivity(), "Please connect to the internet", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
        builder.setCancelable(true);
        builder.setTitle("Choose");


        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.story_item_dialog, getActivity().findViewById(android.R.id.content), false);

        final ImageView imgCapture = viewInflated.findViewById(R.id.imgCapture);
        final ImageView imgGallery = viewInflated.findViewById(R.id.imgGallery);
        builder.setView(viewInflated);

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // Title
        TextView titleView = new TextView(getActivity());
        titleView.setText("Choose");
        titleView.setTextSize(20F);
        titleView.setPadding(20, 20, 20, 20);
        //    titleView.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));

        AlertDialog alertDialog = builder.create();
        // dialog.setCustomTitle(titleView);
        alertDialog.show();

        imgCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(ChatsFragment.this)
                        .cameraOnly()
                        .crop()
                        .start(REQUEST_IMAGE_CAPTURE);
                alertDialog.dismiss();
            }
        });

        imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                imageLauncher.launch(intent);
                alertDialog.dismiss();
            }
        });


        //buttons
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), red));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(ContextCompat.getColor(getActivity(), amp_transparent));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    if (data.getData() != null) {
                        selectedImage = data.getData();
                        alertDialog.show();
//                        createImageBitmap(selectedImage);
                    }
                }
            }
        }
    }

    private void startEditIntent(Uri data) {
        Intent dsPhotoEditorIntent = new Intent(getContext(), DsPhotoEditorActivity.class);
        dsPhotoEditorIntent.setData(data);
        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Edited Pics");
        int[] toolsToHide = {DsPhotoEditorActivity.TOOL_ORIENTATION, DsPhotoEditorActivity.TOOL_CROP};
        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE, toolsToHide);
        editResultLauncher.launch(dsPhotoEditorIntent);
    }

    private void checkPermission(Uri data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startEditIntent(data);
        } else {
            Dexter.withContext(getContext())
                    .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            startEditIntent(data);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Toast.makeText(getContext(), "Please accept permissions", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                    }).check();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (storyRef != null) {
            storyRef.removeEventListener(storyListener);
            storyRef.removeEventListener(mediaListener);
        }
        query.removeEventListener(eventListener);
        d1.keepSynced(false);
    }
}