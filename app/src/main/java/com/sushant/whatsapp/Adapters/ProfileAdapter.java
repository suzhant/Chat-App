package com.sushant.whatsapp.Adapters;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.sushant.whatsapp.ChatDetailsActivity;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.ProfileActivity;
import com.sushant.whatsapp.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.viewHolder>{

    ArrayList<Users> list;
    Context context;

    public ProfileAdapter(ArrayList<Users> list, Context context) {
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
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Users users = list.get(position);
        Glide.with(context).load(users.getProfilePic()).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image);
        holder.userName.setText(users.getUserName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                Query checkStatus = reference.orderByChild("userId").equalTo(users.getUserId());
                checkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String StatusFromDB = snapshot.child(users.getUserId()).child("status").getValue(String.class);
                            Intent intent = new Intent(context, ProfileActivity.class);
                            intent.putExtra("UserIdPA", users.getUserId());
                            intent.putExtra("ProfilePicPA", users.getProfilePic());
                            intent.putExtra("UserNamePA", users.getUserName());
                            intent.putExtra("StatusPA", StatusFromDB);
                            intent.putExtra("EmailPA",users.getMail());
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
        public TextView userName;


        public viewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userName);
            blackCircle=itemView.findViewById(R.id.black_circle);

        }
    }
}
