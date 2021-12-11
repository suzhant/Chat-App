package com.sushant.whatsapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.sushant.whatsapp.Interface.isClicked;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.ProfileActivity;
import com.sushant.whatsapp.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.viewHolder> {

    ArrayList<Users> list;
    Context context;
    String Gid;

    public MemberAdapter(ArrayList<Users> list,String Gid, Context context) {
        this.list = list;
        this.Gid=Gid;
        this.context = context;
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_member, parent, false);
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
                if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid())){
                    Intent intent= new Intent(context, ProfileActivity.class);
                    intent.putExtra("UserIdPA",users.getUserId());
                    intent.putExtra("EmailPA",users.getMail());
                    intent.putExtra("UserNamePA",users.getUserName());
                    intent.putExtra("ProfilePicPA",users.getProfilePic());
                    intent.putExtra("StatusPA",users.getStatus());
                    context.startActivity(intent);
                }
            }
        });

        FirebaseDatabase.getInstance().getReference().child("Groups").child(users.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role=snapshot.child(Gid).child("participant").child(users.getUserId()).child("role").getValue(String.class);
                if ("Admin".equals(role)){
                    holder.memberIndicator.setText(role);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query checkStatus = reference.orderByChild("userId").equalTo(users.getUserId());
        checkStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String StatusFromDB = snapshot.child(users.getUserId()).child("Connection").child("Status").getValue(String.class);
                    assert StatusFromDB != null;
                    if ("online".equals(StatusFromDB)){
                        holder.image.setBorderWidth(4);
                        holder.blackCircle.setVisibility(View.VISIBLE);
                        holder.blackCircle.setColorFilter(Color.parseColor("#7C4DFF"));
                        holder.image.setBorderColor(Color.parseColor("#7C4DFF"));
                    }else {
                        holder.blackCircle.setVisibility(View.GONE);
                        holder.image.setBorderWidth(0);
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

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        public CircleImageView image;
        public ImageView blackCircle,checkbox;
        public TextView userName, lastMessage, memberIndicator;


        public viewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            blackCircle=itemView.findViewById(R.id.black_circle);
            checkbox=itemView.findViewById(R.id.checkbox);
            memberIndicator=itemView.findViewById(R.id.txtMemberIndicator);

        }
    }


}