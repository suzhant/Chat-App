package com.sushant.whatsapp.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sushant.whatsapp.MainActivity;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.Utils.ImageUtils;
import com.sushant.whatsapp.databinding.FragmentGroupPicBinding;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;


public class GroupPicFragment extends Fragment {

    FragmentGroupPicBinding binding;
    String id,image;
    FirebaseStorage storage;
    ProgressDialog dialog,progressDialog;
    FirebaseDatabase database;
    DatabaseReference reference1;
    ValueEventListener eventListener;
    ActivityResultLauncher<Intent> someActivityResultLauncher;

    public GroupPicFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGroupPicBinding.inflate(getLayoutInflater(), container, false);

        assert getArguments() != null;
        id = getArguments().getString("GroupId");

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Uploading Image");
        dialog.setCancelable(false);
        progressDialog= new ProgressDialog(getContext());
        progressDialog.setMessage("Saving Image");
        progressDialog.setCancelable(false);

        binding.imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                someActivityResultLauncher.launch(intent);
            }
        });

        binding.btnSaveGp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadToFirebase();
            }
        });

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            Uri sFile = result.getData().getData();
                            Bitmap bitmap=null;
                            try{
                                bitmap = ImageUtils.handleSamplingAndRotationBitmap(requireContext(), sFile);
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            assert bitmap != null;
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                            byte[] img = baos.toByteArray();
                            binding.imgProfile.setImageBitmap(bitmap);
                            binding.btnSaveGp.setEnabled(true);

                            final StorageReference reference = storage.getReference().child("Group Pictures").child(id);
                            dialog.show();

                            reference.putBytes(img).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            dialog.dismiss();
                                            image=uri.toString();
                                            Toast.makeText(getContext(), "Pic Uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
        return binding.getRoot();
    }

    private void uploadToFirebase() {
        database.getReference().child("Groups").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(id).child("groupPP").setValue(image);
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
                        map.put("groupPP", image);
                        reference.updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        reference1.addValueEventListener(eventListener);
        Intent intent= new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        Toast.makeText(getContext(), "Group Pic Saved", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (reference1!=null){
            reference1.removeEventListener(eventListener);
        }
    }
}