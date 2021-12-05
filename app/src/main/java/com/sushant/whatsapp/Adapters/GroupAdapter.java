package com.sushant.whatsapp.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sushant.whatsapp.Models.GroupChat;
import com.sushant.whatsapp.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.viewHolder>{

    ArrayList<GroupChat> list;
    Context context;

    public GroupAdapter(ArrayList<GroupChat> list, Context context) {
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
        GroupChat groupChat = list.get(position);
        Glide.with(context).load(groupChat.getGroupPP()).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image);
        holder.groupName.setText(groupChat.getGroupName());
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
