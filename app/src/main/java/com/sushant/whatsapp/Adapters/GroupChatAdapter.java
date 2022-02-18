package com.sushant.whatsapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatAdapter extends RecyclerView.Adapter {

    ArrayList<Messages> messageModel;
    Context context;
    String Gid;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;

    public GroupChatAdapter(ArrayList<Messages> messageModel, Context context, String Gid) {
        this.messageModel = messageModel;
        this.context = context;
        this.Gid = Gid;
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
            if ("photo".equals(message.getType())) { //yoda condition solves unsafe null behaviour
                holder.setIsRecyclable(false);
                ((SenderViewHolder) holder).imgSender.setImageBitmap(null);
                ((SenderViewHolder) holder).imgSender.setVisibility(View.VISIBLE);
                ((GroupChatAdapter.SenderViewHolder) holder).txtSender.setVisibility(View.GONE);
                ((GroupChatAdapter.SenderViewHolder) holder).imgSender.layout(0, 0, 0, 0);
                if (message.getImageUrl() != null) {
                    Glide.with(((GroupChatAdapter.SenderViewHolder) holder).imgSender.getContext()).load(message.getImageUrl()).placeholder(R.drawable.placeholder).
                            diskCacheStrategy(DiskCacheStrategy.ALL).into(((GroupChatAdapter.SenderViewHolder) holder).imgSender);
                }
            } else {
                ((GroupChatAdapter.SenderViewHolder) holder).txtSender.setText(message.getMessage());
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            ((GroupChatAdapter.SenderViewHolder) holder).txtSenderTime.setText(dateFormat.format(new Date(message.getTimestamp())));
        } else {
            if ("photo".equals(message.getType())) {
                holder.setIsRecyclable(false);
                ((GroupChatAdapter.ReceiverViewHolder) holder).imgReceiver.setImageBitmap(null);
                ((ReceiverViewHolder) holder).imgReceiver.setVisibility(View.VISIBLE);
                ((GroupChatAdapter.ReceiverViewHolder) holder).txtReceiver.setVisibility(View.GONE);
                ((GroupChatAdapter.ReceiverViewHolder) holder).imgReceiver.layout(0, 0, 0, 0);
                if (message.getImageUrl() != null) {
                    Glide.with(((GroupChatAdapter.ReceiverViewHolder) holder).imgReceiver.getContext()).load(message.getImageUrl()).placeholder(R.drawable.placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL).into(((GroupChatAdapter.ReceiverViewHolder) holder).imgReceiver);
                }
            } else {
                ((GroupChatAdapter.ReceiverViewHolder) holder).txtReceiver.setText(message.getMessage());
                String firstWord = getFirstWord(message.getSenderName());
                ((ReceiverViewHolder) holder).txtSenderName.setText(firstWord);
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            ((GroupChatAdapter.ReceiverViewHolder) holder).txtReceiverTime.setText(dateFormat.format(new Date(message.getTimestamp())));
            Glide.with(context).load(message.getProfilePic()).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((GroupChatAdapter.ReceiverViewHolder) holder).profilepic);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((ReceiverViewHolder) holder).txtReceiverTime.getVisibility() == View.VISIBLE) {
                        ((ReceiverViewHolder) holder).txtReceiverTime.setVisibility(View.GONE);
                    } else {
                        ((ReceiverViewHolder) holder).txtReceiverTime.setVisibility(View.VISIBLE);
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
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                database.getReference().child("Group Chat").child(Gid).child(message.getMessageId()).setValue(null);
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
        final private MaterialCardView cardViewReceiver;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReceiver = itemView.findViewById(R.id.txtGroupReceiver);
            txtReceiverTime = itemView.findViewById(R.id.txtGroupReceiverTime);
            profilepic = itemView.findViewById(R.id.group_profile_image);
            imgReceiver = itemView.findViewById(R.id.imgReceiver);
            txtSenderName = itemView.findViewById(R.id.txtSenderName);
            cardViewReceiver = itemView.findViewById(R.id.cardViewReceiver);
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        final private TextView txtSender, txtSenderTime;
        final private ImageView imgSender;
        final private MaterialCardView cardViewSender;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSender = itemView.findViewById(R.id.txtSender);
            txtSenderTime = itemView.findViewById(R.id.txtSenderTime);
            imgSender = itemView.findViewById(R.id.imgSender);
            cardViewSender = itemView.findViewById(R.id.cardViewSender);
        }
    }

    private String getFirstWord(String text) {

        int index = text.indexOf(' ');

        if (index > -1) { // Check if there is more than one word.

            return text.substring(0, index).trim(); // Extract first word.

        } else {

            return text; // Text is the first word itself.
        }
    }
}
