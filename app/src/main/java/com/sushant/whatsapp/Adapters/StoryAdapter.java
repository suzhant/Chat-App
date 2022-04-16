package com.sushant.whatsapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.devlomi.circularstatusview.CircularStatusView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.ChatDetailsActivity;
import com.sushant.whatsapp.MainActivity;
import com.sushant.whatsapp.Models.Story;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.R;
import com.sushant.whatsapp.Utils.Encryption;
import com.sushant.whatsapp.Utils.StoriesDiffUtil;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.OnStoryChangedCallback;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.viewHolder> {
    ArrayList<Users> stories;
    Context context;

    public StoryAdapter(ArrayList<Users> stories, Context context) {
        this.stories = stories;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story_layout, parent, false);
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
                        holder.txtName.setText(bundle.getString("newUserName"));
                        break;
                    case "newPic":
                        Glide.with(context).load(bundle.getString("newPic")).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(holder.imgStory);
                        break;
                    case "newLastStory":
                        Glide.with(context).load(bundle.getString("newLastStory"))
                                .placeholder(R.drawable.placeholder).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(holder.storyImg);
                        break;
                    case "newStoriesCount":
                        holder.circularStatusView.setPortionsCount(bundle.getInt("newStoriesCount"));
                        break;
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Users users = stories.get(position);
        Glide.with(context).load(users.getProfilePic())
                .placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(holder.imgStory);
        Glide.with(context).load(users.getLastStory())
                .placeholder(R.drawable.placeholder).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(holder.storyImg);
        holder.txtName.setText(users.getUserName());
        holder.circularStatusView.setPortionsCount(users.getStoriesCount());
        holder.circularStatusView.setPortionsColor(ContextCompat.getColor(context, R.color.fcm_test_color));
        ArrayList<MyStory> myStories = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Stories").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(users.userId).child("medias")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myStories.clear();
                        int index = 0;
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Story story = snapshot1.getValue(Story.class);
                            assert story != null;
                            if (story.getUrl() != null) {
                                try {
                                    myStories.add(new MyStory(
                                            Encryption.decryptMessage(story.getUrl()),
                                            new Date(story.getTimestamp())));
                                } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                                if (story.getSeen().equals("true")) {
                                    holder.circularStatusView.setPortionColorForIndex(index, ContextCompat.getColor(context, R.color.grayBackground));
                                    index++;
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.storyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new StoryView.Builder(((MainActivity) context).getSupportFragmentManager())
                        .setStoriesList(myStories) // Required
                        .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                        .setTitleText(users.getUserName()) // Default is Hidden
                        .setSubtitleText("") // Default is Hidden
                        .setTitleLogoUrl(users.getProfilePic()) // Default is Hidden
                        .setStartingIndex(users.getSeenCount())
                        .setStoryClickListeners(new StoryClickListeners() {
                            @Override
                            public void onDescriptionClickListener(int position) {
                                //your action
                            }

                            @Override
                            public void onTitleIconClickListener(int position) {
                                //your action
                                if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                                    Intent intent = new Intent(context, ChatDetailsActivity.class);
                                    intent.putExtra("UserId", users.getUserId());
                                    intent.putExtra("ProfilePic", users.getProfilePic());
                                    intent.putExtra("UserName", users.getUserName());
                                    intent.putExtra("userEmail", users.getMail());
                                    context.startActivity(intent);
                                }
                            }
                        }) // Optional Listeners
                        .setOnStoryChangedCallback(new OnStoryChangedCallback() {
                            @Override
                            public void storyChanged(int position) {
                                FirebaseDatabase.getInstance().getReference().child("Stories").child(FirebaseAuth.getInstance().getUid())
                                        .child(users.getUserId()).child("medias").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                            Story story = snapshot1.getValue(Story.class);
                                            assert story != null;
                                            if (story.getUrl() != null) {
                                                try {
                                                    story.setUrl(Encryption.decryptMessage(story.getUrl()));
                                                } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if (story.getSid() != null && myStories.get(position).getUrl().equals(story.getUrl())) {
                                                HashMap<String, Object> seen = new HashMap<>();
                                                seen.put("seen", "true");
                                                FirebaseDatabase.getInstance().getReference().child("Stories").child(FirebaseAuth.getInstance().getUid())
                                                        .child(users.getUserId()).child("medias").child(Objects.requireNonNull(snapshot1.getKey())).updateChildren(seen);
                                            }
                                            if (position == myStories.size() - 1) {
                                                HashMap<String, Object> seen = new HashMap<>();
                                                seen.put("lastStory", Encryption.encryptMessage(myStories.get(0).getUrl()));
                                                FirebaseDatabase.getInstance().getReference().child("Stories").child(FirebaseAuth.getInstance().getUid())
                                                        .child(users.getUserId()).updateChildren(seen);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        })
                        .build() // Must be called before calling show method
                        .show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgStory, storyImg;
        private final CircularStatusView circularStatusView;
        private final CardView storyCard;
        private final TextView txtName;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgStory = itemView.findViewById(R.id.imgStory);
            circularStatusView = itemView.findViewById(R.id.circular_status_view);
            storyImg = itemView.findViewById(R.id.storyImg);
            storyCard = itemView.findViewById(R.id.storyCard);
            txtName = itemView.findViewById(R.id.name);
        }
    }

    public void updateStoryList(ArrayList<Users> users) {
        final StoriesDiffUtil diffCallback = new StoriesDiffUtil(this.stories, users);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.stories.clear();
        this.stories.addAll(users);
        diffResult.dispatchUpdatesTo(this);
    }
}
