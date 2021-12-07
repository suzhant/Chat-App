package com.sushant.whatsapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatAdapter extends RecyclerView.Adapter {

    ArrayList<Messages> messageModel;
    ArrayList<Users> users;

    Context context;
    String recId;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;

    public GroupChatAdapter(ArrayList<Messages> messageModel, Context context) {
        this.messageModel = messageModel;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_group_receivers, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages message = messageModel.get(position);
        if (holder.getClass() == GroupChatAdapter.SenderViewHolder.class) {
            if ("photo".equals(message.getType())){ //yoda condition solves unsafe null behaviour
                ((SenderViewHolder) holder).imgSender.setImageBitmap(null);
                ((GroupChatAdapter.SenderViewHolder) holder).imgSender.setVisibility(View.VISIBLE);
                ((GroupChatAdapter.SenderViewHolder) holder).txtSender.setVisibility(View.GONE);
                ((GroupChatAdapter.SenderViewHolder) holder).imgSender.layout(0,0,0,0);
                Glide.with(((GroupChatAdapter.SenderViewHolder) holder).imgSender.getContext()).load(message.getImageUrl()).placeholder(R.drawable.placeholder).
                        diskCacheStrategy(DiskCacheStrategy.ALL).into(((GroupChatAdapter.SenderViewHolder) holder).imgSender);
            }else{
                ((GroupChatAdapter.SenderViewHolder) holder).txtSender.setText(message.getMessage());
            }
            SimpleDateFormat dateFormat= new SimpleDateFormat("hh:mm a");
            ((GroupChatAdapter.SenderViewHolder) holder).txtSenderTime.setText(dateFormat.format(new Date(message.getTimestamp())));
        } else {
            if ("photo".equals(message.getType())){
                ((GroupChatAdapter.ReceiverViewHolder) holder).imgReceiver.setImageBitmap(null);
                ((GroupChatAdapter.ReceiverViewHolder) holder).imgReceiver.setVisibility(View.VISIBLE);
                ((GroupChatAdapter.ReceiverViewHolder) holder).txtReceiver.setVisibility(View.GONE);
                ((GroupChatAdapter.ReceiverViewHolder) holder).imgReceiver.layout(0,0,0,0);
                Glide.with(((GroupChatAdapter.ReceiverViewHolder) holder).imgReceiver.getContext()).load(message.getImageUrl()).placeholder(R.drawable.placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(((GroupChatAdapter.ReceiverViewHolder) holder).imgReceiver);
            }else{
                ((GroupChatAdapter.ReceiverViewHolder) holder).txtReceiver.setText(message.getMessage());
                ((ReceiverViewHolder) holder).txtSenderName.setText(message.getSenderName());
            }
            SimpleDateFormat dateFormat= new SimpleDateFormat("hh:mm a");
            ((GroupChatAdapter.ReceiverViewHolder) holder).txtReceiverTime.setText(dateFormat.format(new Date(message.getTimestamp())));
            Glide.with(context).load(message.getProfilePic()).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((GroupChatAdapter.ReceiverViewHolder) holder).profilepic);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((ReceiverViewHolder) holder).txtSenderName.getVisibility()==View.VISIBLE){
                        ((ReceiverViewHolder) holder).txtSenderName.setVisibility(View.GONE);
                    }else {
                        ((ReceiverViewHolder) holder).txtSenderName.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(context).setTitle("Delete")
                        .setMessage("Do you want to delete this message?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseDatabase database= FirebaseDatabase.getInstance();
                                String senderRoom= FirebaseAuth.getInstance().getUid() + recId;
                                database.getReference().child("Group Chat").child(senderRoom).child(message.getMessageId()).setValue(null);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        return messageModel.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messageModel.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        final private TextView txtReceiver, txtReceiverTime, txtSenderName;
        final private CircleImageView profilepic;
        final private ImageView imgReceiver;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReceiver = itemView.findViewById(R.id.txtGroupReceiver);
            txtReceiverTime = itemView.findViewById(R.id.txtGroupReceiverTime);
            profilepic=itemView.findViewById(R.id.group_profile_image);
            imgReceiver=itemView.findViewById(R.id.imgReceiver);
            txtSenderName= itemView.findViewById(R.id.txtSenderName);
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        final private TextView txtSender, txtSenderTime;
        final private ImageView imgSender;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSender = itemView.findViewById(R.id.txtSender);
            txtSenderTime = itemView.findViewById(R.id.txtSenderTime);
            imgSender=itemView.findViewById(R.id.imgSender);
        }
    }
}
