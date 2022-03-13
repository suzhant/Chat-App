package com.sushant.whatsapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.ChatDetailsActivity;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.R;
import com.sushant.whatsapp.Utils.ChatDiffCallback;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.viewHolder> {

    ArrayList<Users> list;
    Context context;
    String lastMsg;

    public UsersAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_users, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            Bundle bundle = (Bundle) payloads.get(0);
            for (String key : bundle.keySet()) {
                switch (key) {
                    case "newUserName":
                        holder.userName.setText(bundle.getString("newUserName"));
                        break;
                    case "newPic":
                        Glide.with(context).load(bundle.getString("newPic")).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(holder.image);
                        break;
                    case "newLastMessage":
                        holder.lastMessage.setText(bundle.getString("newLastMessage"));
                        break;
                }
            }
        }

    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.viewHolder holder, @SuppressLint("RecyclerView") int position) {
        Users users = list.get(position);
//        Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.avatar).into(holder.image);
        Glide.with(context).load(users.getProfilePic()).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image);
        holder.userName.setText(users.getUserName());
        if (users.getLastMessage() != null) {
            holder.lastMessage.setText(users.getLastMessage());
        }

//        DatabaseReference reference2=FirebaseDatabase.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getUid() + users.getUserId());
//        Query message=reference2.orderByChild("timestamp").limitToLast(1);
//        message.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.hasChildren()) {
//                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
//                                lastMsg=snapshot1.child("message").getValue(String.class);
//                                holder.lastMessage.setText(lastMsg);
//                                holder.lastMessage.setTypeface(null, Typeface.NORMAL);
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

        if (users.getSeen() != null) {
            if (users.getSeen().equals("false")) {
                holder.userName.setTextColor(Color.BLACK);
                holder.userName.setTypeface(null, Typeface.BOLD);
                holder.lastMessage.setTypeface(null, Typeface.BOLD);
                holder.lastMessage.setTextColor(Color.BLACK);
            } else {
                holder.userName.setTextColor(Color.parseColor("#757575"));
                holder.lastMessage.setTextColor(Color.parseColor("#757575"));
                holder.userName.setTypeface(null, Typeface.NORMAL);
                holder.lastMessage.setTypeface(null, Typeface.NORMAL);
            }
        }


//        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
//                .child("Friends");
//        Query checkStatus1 = reference1.orderByChild("userId").equalTo(users.getUserId());
//        checkStatus1.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    String presence = snapshot.child(users.getUserId()).child("Typing").getValue(String.class);
//                    if ("Typing...".equals(presence)) {
//                        holder.lastMessage.setText("Typing...");
//                        holder.lastMessage.setTypeface(null, Typeface.ITALIC);
//                    }
////                    else {
////                        holder.lastMessage.setText(lastMsg);
////                        Typeface typeface = ResourcesCompat.getFont(context, R.font.alice);
////                        holder.lastMessage.setTypeface(typeface);
////                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query checkStatus = reference.orderByChild("userId").equalTo(users.getUserId());
        checkStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String StatusFromDB = snapshot.child(users.getUserId()).child("Connection").child("Status").getValue(String.class);
                    assert StatusFromDB != null;
                    if ("online".equals(StatusFromDB)) {
                        holder.blackCircle.setVisibility(View.VISIBLE);
                        holder.blackCircle.setColorFilter(Color.parseColor("#7C4DFF"));
                        holder.image.setBorderColor(Color.parseColor("#7C4DFF"));
                    } else {
                        holder.blackCircle.setVisibility(View.GONE);
                        holder.image.setBorderColor(Color.GRAY);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                Query checkStatus = reference.orderByChild("userId").equalTo(users.getUserId());
                checkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String StatusFromDB = snapshot.child(users.getUserId()).child("Connection").child("Status").getValue(String.class);
                            String status = snapshot.child(users.getUserId()).child("status").getValue(String.class);
                            Intent intent = new Intent(context, ChatDetailsActivity.class);
                            intent.putExtra("UserId", users.getUserId());
                            intent.putExtra("ProfilePic", users.getProfilePic());
                            intent.putExtra("UserName", users.getUserName());
                            intent.putExtra("userEmail", users.getMail());
                            intent.putExtra("Status", StatusFromDB);
                            intent.putExtra("UserStatus", status);
                            context.startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class viewHolder extends RecyclerView.ViewHolder {

        public CircleImageView image;
        public ImageView blackCircle;
        public TextView userName, lastMessage;


        public viewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            blackCircle = itemView.findViewById(R.id.black_circle);

        }
    }


    public void updateUserList(ArrayList<Users> users) {
        final ChatDiffCallback diffCallback = new ChatDiffCallback(this.list, users);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.list.clear();
        this.list.addAll(users);
        diffResult.dispatchUpdatesTo(this);
    }


}
