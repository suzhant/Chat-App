package com.sushant.whatsapp.Adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.sushant.whatsapp.GroupChatActivity;
import com.sushant.whatsapp.Models.Groups;
import com.sushant.whatsapp.R;

import java.util.ArrayList;

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
        Glide.with(context).load(groups.getGroupPP()).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image);
        holder.groupName.setText(groups.getGroupName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(context,GroupChatActivity.class);
                intent.putExtra("GId", groups.getGroupId());
                intent.putExtra("GPic", groups.getGroupPP());
                intent.putExtra("GName", groups.getGroupName());
                context.startActivity(intent);
            }
        });

        DatabaseReference reference2=FirebaseDatabase.getInstance().getReference().child("Group Chat").child(groups.getGroupId());
        Query message=reference2.orderByChild("timestamp").limitToLast(1);
        message.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        lastMsg=snapshot1.child("message").getValue(String.class);
                        holder.lastMessage.setText(lastMsg);
                        holder.lastMessage.setTypeface(null, Typeface.NORMAL);
                    }
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


    public class viewHolder extends RecyclerView.ViewHolder {

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
