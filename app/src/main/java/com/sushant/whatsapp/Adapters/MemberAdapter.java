package com.sushant.whatsapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sushant.whatsapp.Interface.isClicked;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.ProfileActivity;
import com.sushant.whatsapp.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.viewHolder> {

    ArrayList<Users> list;
    Context context;

    public MemberAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_participant, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberAdapter.viewHolder holder, @SuppressLint("RecyclerView") int position) {
        Users users = list.get(position);
        Glide.with(context).load(users.getProfilePic()).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image);
        holder.userName.setText(users.getUserName());



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(context, ProfileActivity.class);
                intent.putExtra("UserIdPA",users.getUserId());
                intent.putExtra("EmailPA",users.getMail());
                intent.putExtra("UserNamePA",users.getUserName());
                intent.putExtra("ProfilePicPA",users.getProfilePic());
                intent.putExtra("StatusPA",users.getStatus());
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        public CircleImageView image;
        public ImageView blackCircle,checkbox;
        public TextView userName, lastMessage;


        public viewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            blackCircle=itemView.findViewById(R.id.black_circle);
            checkbox=itemView.findViewById(R.id.checkbox);

        }
    }


}