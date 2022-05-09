package com.sushant.whatsapp.Adapters;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.ortiz.touchview.TouchImageView;
import com.sushant.whatsapp.Interface.VpInterface;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.R;

import java.util.ArrayList;

public class VPAdapter extends RecyclerView.Adapter<VPAdapter.viewHolder> {
    public ArrayList<Messages> messages;
    Context context;
    VpInterface vpInterface;
    String image;

    public VPAdapter(ArrayList<Messages> messages, Context context, VpInterface vpInterface, String image) {
        this.messages = messages;
        this.context = context;
        this.vpInterface = vpInterface;
        this.image = image;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_pager_items, parent, false);
        return new VPAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Messages model = messages.get(position);
        if (model.getImageUrl() != null && model.getImageUrl().equals(image)) {
            Glide.with(context).load(model.getImageUrl()).placeholder(R.drawable.placeholder).into(holder.touchImageView);
        }
        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    vpInterface.onEditListener(model.getImageUrl());
                } else {
                    Dexter.withContext(context.getApplicationContext())
                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    vpInterface.onEditListener(model.getImageUrl());
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    Toast.makeText(context, "Please accept permissions", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                            }).check();
                }
            }
        });
        holder.imgDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    vpInterface.onDownloadListener(model.getImageUrl());
                } else {
                    Dexter.withContext(context.getApplicationContext())
                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    vpInterface.onDownloadListener(model.getImageUrl());
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    Toast.makeText(context, "Please accept permissions", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                            }).check();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        public TouchImageView touchImageView;
        public ImageView imgEdit;
        public ImageView imgDownload;


        public viewHolder(@NonNull View itemView) {
            super(itemView);
            touchImageView = itemView.findViewById(R.id.fullScreenImage);
            imgEdit = itemView.findViewById(R.id.img_edit);
            imgDownload = itemView.findViewById(R.id.img_download);

        }
    }

}
