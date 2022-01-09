package com.sushant.whatsapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sushant.whatsapp.Models.Groups;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityFinalCreateGroupBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class FinalCreateGroup extends AppCompatActivity{

    ActivityFinalCreateGroupBinding binding;
    ProgressDialog dialog;
    FirebaseDatabase database;
    ArrayList<Users> list;
    String uid,name,profilePic,mail;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    FirebaseStorage storage;
    String id,image;
    ValueEventListener eventListener;
    DatabaseReference reference1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityFinalCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database= FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        dialog= new ProgressDialog(this);
        dialog.setMessage("Uploading Image");
        dialog.setCancelable(false);

        list= (ArrayList<Users>) getIntent().getSerializableExtra("participantList");

        database.getReference().child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user= snapshot.getValue(Users.class);
                uid=user.getUserId();
                name=user.getUserName();
                profilePic= user.getProfilePic();
                mail=user.getMail();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Generating group id
        UUID uuid= UUID.randomUUID();
        id=uuid+"";

        binding.btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.editName.getEditText().getText().toString().isEmpty()){
                    binding.editName.setError("Field cannot be empty");
                    return;
                }
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
                Groups groups = new Groups();
                String GroupName= binding.editName.getEditText().getText().toString();
                groups.setGroupName(GroupName);
                groups.setGroupId(id);
                groups.setCreatedBy(uid);
                groups.setCreatedOn(timeStamp);
                database.getReference().child("GroupList").child(id).setValue(groups);
                createLastMessage();

                for (int i=0;i<list.size();i++){
                    Users users= list.get(i);
                    DatabaseReference reference= database.getReference().child("Groups").child(users.getUserId()).child(id);
                    reference.setValue(groups).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            for (int i=0;i<list.size();i++){
                                Users users= list.get(i);
                                users.setJoinedGroupOn(timeStamp);
                                if (FirebaseAuth.getInstance().getUid()!=null){
                                    if (FirebaseAuth.getInstance().getUid().equals(users.getUserId())){
                                        users.setRole("Admin");
                                    }else{
                                        users.setRole("normal");
                                    }
                                    reference.child("participant").child(users.getUserId()).setValue(users);
                                }
                            }
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Users admin= new Users();
                            admin.setUserId(uid);
                            admin.setUserName(name);
                            admin.setProfilePic(profilePic);
                            admin.setMail(mail);
                            admin.setJoinedGroupOn(timeStamp);
                            admin.setRole("Admin");
                            reference.child("participant").child(admin.getUserId()).setValue(admin);
                        }
                    });
                }

                DatabaseReference reference= database.getReference().child("Groups").child(user.getUid()).child(id);
                reference.setValue(groups).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        for (int i=0;i<list.size();i++){
                            Users users= list.get(i);
                            users.setJoinedGroupOn(timeStamp);
                            if (FirebaseAuth.getInstance().getUid()!=null){
                                if (FirebaseAuth.getInstance().getUid().equals(users.getUserId())){
                                    users.setRole("Admin");
                                }else{
                                    users.setRole("normal");
                                }
                                reference.child("participant").child(users.getUserId()).setValue(users);
                            }
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Users admin= new Users();
                        admin.setUserId(uid);
                        admin.setUserName(name);
                        admin.setProfilePic(profilePic);
                        admin.setMail(mail);
                        admin.setRole("Admin");
                        admin.setJoinedGroupOn(timeStamp);
                        reference.child("participant").child(admin.getUserId()).setValue(admin);
                    }
                });

                database.getReference().child("Groups").child(FirebaseAuth.getInstance().getUid()).child(id).child("groupPP").setValue(image);
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

                Intent intent= new Intent(FinalCreateGroup.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                Toast.makeText(getApplicationContext(), "Group Created Successfully", Toast.LENGTH_SHORT).show();
            }
        });

        binding.imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,20);
            }
        });
    }

    private void createLastMessage(){
        HashMap<String,Object> map= new HashMap<>();
        map.put("lastMessage","Say Hi!!");
        map.put("senderName","name");
        map.put("senderId","uid");
        database.getReference().child("Group Chat").child("Last Messages").child(id).setValue(map);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            if (data.getData()!=null) {
                Uri sFile = data.getData();
                Bitmap bitmap=null;
                try{
                    bitmap = handleSamplingAndRotationBitmap(FinalCreateGroup.this,sFile);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] img = baos.toByteArray();
                binding.imgProfile.setImageBitmap(bitmap);

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
                                Toast.makeText(FinalCreateGroup.this, "Profile Pic Updated", Toast.LENGTH_SHORT).show();
                                binding.btnCreate.setEnabled(true);
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reference1!=null){
            Log.d("REF", "onDestroy: Removed Event");
            reference1.removeEventListener(eventListener);
        }
    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = Math.min(heightRatio, widthRatio);

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

}