package com.sushant.whatsapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    public GroupChatAdapter(ArrayList<Messages> messageModel, ArrayList<Users> users, Context context) {
        this.messageModel = messageModel;
        this.users = users;
        this.context = context;
    }

    public GroupChatAdapter(ArrayList<Messages> messageModel, Context context, String recId) {
        this.messageModel = messageModel;
        this.context = context;
        this.recId = recId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_group_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_group_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages message = messageModel.get(position);
        if (holder.getClass() == SenderViewHolder.class) {
            ((SenderViewHolder) holder).txtSender.setText(message.getMessage());
            SimpleDateFormat dateFormat= new SimpleDateFormat("hh:mm a");
            ((SenderViewHolder) holder).txtSenderTime.setText(dateFormat.format(new Date(message.getTimestamp())));
        } else {
            ((ReceiverViewHolder) holder).txtReceiver.setText(message.getMessage());
            SimpleDateFormat dateFormat= new SimpleDateFormat("hh:mm a");
            ((ReceiverViewHolder) holder).txtReceiverTime.setText(dateFormat.format(new Date(message.getTimestamp())));
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
                                database.getReference().child("Chats").child(senderRoom).child(message.getMessageId()).setValue(null);
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

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        private TextView txtReceiver, txtReceiverTime;
        private CircleImageView receiver_profile_image;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReceiver = itemView.findViewById(R.id.txtGroupReceiver);
            txtReceiverTime = itemView.findViewById(R.id.txtGroupReceiverTime);
            receiver_profile_image=itemView.findViewById(R.id.group_profile_image);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        private TextView txtSender, txtSenderTime;
        private CircleImageView sender_profile_image;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSender = itemView.findViewById(R.id.txtGroupSender);
            txtSenderTime = itemView.findViewById(R.id.txtGroupSenderTime);
            sender_profile_image=itemView.findViewById(R.id.group_profile_image);
        }
    }
}
