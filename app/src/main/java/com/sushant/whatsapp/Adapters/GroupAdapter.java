package com.sushant.whatsapp.Adapters;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.GroupChatActivity;
import com.sushant.whatsapp.Models.Groups;
import com.sushant.whatsapp.R;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.viewHolder>{

    ArrayList<Groups> list;
    Context context;
    String lastMsg;

    public GroupAdapter(ArrayList<Groups> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_group, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Groups groups = list.get(position);
        Glide.with(context).load(groups.getGroupPP()).placeholder(R.drawable.avatar).into(holder.image);
        holder.groupName.setText(groups.getGroupName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(context,GroupChatActivity.class);
                intent.putExtra("GId", groups.getGroupId());
                intent.putExtra("GPic", groups.getGroupPP());
                intent.putExtra("GName", groups.getGroupName());
                intent.putExtra("CreatedOn",groups.getCreatedOn());
                intent.putExtra("CreatedBy",groups.getCreatedBy());
                context.startActivity(intent);
            }
        });

//        DatabaseReference reference2=FirebaseDatabase.getInstance().getReference().child("Group Chat").child(groups.getGroupId());
//        Query message=reference2.orderByChild("timestamp").limitToLast(1);
//        message.addValueEventListener(new ValueEventListener() {
//            @SuppressLint("SetTextI18n")
//            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.hasChildren()) {
//                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
//                        lastMsg=snapshot1.child("message").getValue(String.class);
//                        String senderName=snapshot1.child("senderName").getValue(String.class);
//                        if (Objects.equals(FirebaseAuth.getInstance().getUid(), snapshot1.child("uId").getValue(String.class))){
//                            holder.lastMessage.setText("You: "+ lastMsg);
//                        }else {
//                            holder.lastMessage.setText(senderName +": "+ lastMsg);
//                        }
//
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        FirebaseDatabase.getInstance().getReference().child("Group Chat").child("Last Messages").child(groups.getGroupId())
                .addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lastMsg=snapshot.child("lastMessage").getValue(String.class);
                String senderName=snapshot.child("senderName").getValue(String.class);
                String senderId=snapshot.child("senderId").getValue(String.class);
                holder.lastMessage.setText(lastMsg);
                if (Objects.equals(FirebaseAuth.getInstance().getUid(), senderId)){
                    holder.lastMessage.setText("You: "+ lastMsg);
                }else if ("Say Hi!!".equals(lastMsg)){
                    holder.lastMessage.setText(lastMsg);
                }else {
                    holder.lastMessage.setText(senderName +": "+ lastMsg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class viewHolder extends RecyclerView.ViewHolder {

        public CircleImageView image;
        public TextView groupName,lastMessage;


        public viewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            groupName = itemView.findViewById(R.id.groupName);
            lastMessage=itemView.findViewById(R.id.lastMessage);

        }
    }
}
