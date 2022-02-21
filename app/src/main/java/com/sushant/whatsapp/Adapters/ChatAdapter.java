package com.sushant.whatsapp.Adapters;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.sushant.whatsapp.CheckConnection;
import com.sushant.whatsapp.ConnectingActivity;
import com.sushant.whatsapp.FullScreenImage;
import com.sushant.whatsapp.InternetCheckServices;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.ProfileActivity;
import com.sushant.whatsapp.R;
import com.sushant.whatsapp.ShareActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import a.a.b.b.e;
import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

public class ChatAdapter extends RecyclerView.Adapter {

    ArrayList<Messages> messageModel;
    Context context;
    String recId;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    private final MediaPlayer player = null;
    boolean isPlaying = false;
    String receiverName, profilePic, email, Status;
    BroadcastReceiver broadcastReceiver;

    public ChatAdapter(ArrayList<Messages> messageModel, Context context) {
        this.messageModel = messageModel;
        this.context = context;
    }

    public ChatAdapter(ArrayList<Messages> messageModel, Context context, String recId) {
        this.messageModel = messageModel;
        this.context = context;
        this.recId = recId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
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
        holder.setIsRecyclable(false);
        if (holder.getClass() == SenderViewHolder.class) {
            if ("photo".equals(message.getType())) { //yoda condition solves unsafe null behaviour
                if (message.getImageUrl() != null) {
                    ((SenderViewHolder) holder).imgSender.setImageBitmap(null);
                    ((SenderViewHolder) holder).imgSender.setVisibility(View.VISIBLE);
                    ((SenderViewHolder) holder).txtSender.setVisibility(View.GONE);
                    ((SenderViewHolder) holder).imgSender.layout(0, 0, 0, 0);
                    Glide.with(((SenderViewHolder) holder).imgSender.getContext()).load(message.getImageUrl()).placeholder(R.drawable.placeholder).
                            diskCacheStrategy(DiskCacheStrategy.ALL).into(((SenderViewHolder) holder).imgSender);
                }
            } else if ("text".equals(message.getType())) {
                ((SenderViewHolder) holder).txtSender.setText(message.getMessage());
                if (message.getMessage().contains("https://")) {
                 //   ((SenderViewHolder) holder).txtSender.setSingleLine();
                    ((SenderViewHolder) holder).cardLink.setVisibility(View.VISIBLE);
                    ((SenderViewHolder) holder).txtSender.setVisibility(View.GONE);
                    ((SenderViewHolder) holder).txtLink.setText(message.getMessage());

                    Link link = new Link(message.getMessage())
                            .setTextColor(Color.parseColor("#FFFFFF"))                  // optional, defaults to holo blue
                            .setTextColorOfHighlightedLink(Color.parseColor("#0D3D0C")) // optional, defaults to holo blue
                            .setHighlightAlpha(.4f)                                     // optional, defaults to .15f
                            .setUnderlined(true)                                       // optional, defaults to true
                            .setBold(true)// optional, defaults to false
                            .setOnLongClickListener(new Link.OnLongClickListener() {
                                @Override
                                public void onLongClick(String clickedText) {
                                    // long clicked
                                }
                            })
                            .setOnClickListener(new Link.OnClickListener() {
                                @Override
                                public void onClick(@NonNull String clickedText) {
                                    // single clicked
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickedText));
                                    context.startActivity(browserIntent);
                                }
                            });


                    // create the link builder object add the link rule
                    LinkBuilder.on(((SenderViewHolder) holder).txtLink)
                            .addLink(link)
                            .build(); //

                    if (message.getMessage().contains("tiktok")){
                        Glide.with(context).load(R.drawable.tiktok4).placeholder(R.drawable.placeholder).
                                into(((SenderViewHolder) holder).imgLink);
                    }else if (message.getMessage().contains("youtu.be")){
                        String thumbnail="https://img.youtube.com/vi/" + getYouTubeId(message.getMessage()) + "/0.jpg";
                        RequestOptions options= new RequestOptions();
                        Glide.with(context).load(thumbnail)
                                .thumbnail(Glide.with(context).load(thumbnail))
                                .apply(options)
                                .placeholder(R.drawable.placeholder).
                                into(((SenderViewHolder) holder).imgLink);
                    }else if (message.getMessage().contains("instagram")){
                        Glide.with(context).load(R.drawable.instagram_round_logo).placeholder(R.drawable.placeholder).
                                into(((SenderViewHolder) holder).imgLink);
                    }
                }
            } else if ("videoCall".equals(message.getType())) {
                ((SenderViewHolder) holder).txtSender.setVisibility(View.GONE);
                ((SenderViewHolder) holder).layoutVideoCall.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).txtVideoCall.setText("The video chat ended.");

                ((SenderViewHolder) holder).btnCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkResponse();
                    }
                });
            } else {
                if (message.getAudioFile() != null) {

                    ((SenderViewHolder) holder).txtSender.setVisibility(View.GONE);
                    ((SenderViewHolder) holder).voicePlayerView.setVisibility(View.VISIBLE);
                    ((SenderViewHolder) holder).voicePlayerView.getImgPlay().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            broadcastReceiver = new InternetCheckServices();
                            registerBroadcastReceiver();
                            CheckConnection checkConnection = new CheckConnection();
                            if (checkConnection.isConnected(context)) {
                                return;
                            }
                            ((SenderViewHolder) holder).voicePlayerView.setAudio(message.getAudioFile());
                            ((SenderViewHolder) holder).voicePlayerView.setSeekBarStyle(R.color.colorPurple, R.color.colorPurple);
                            ((SenderViewHolder) holder).voicePlayerView.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    unregisterNetwork();
                                }
                            });
                        }
                    });

                }
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            ((SenderViewHolder) holder).txtSenderTime.setText(dateFormat.format(new Date(message.getTimestamp())));
        } else {
            if ("photo".equals(message.getType())) {
                if (message.getImageUrl() != null) {
                    ((ReceiverViewHolder) holder).imgReceiver.setImageBitmap(null);
                    ((ReceiverViewHolder) holder).imgReceiver.setVisibility(View.VISIBLE);
                    ((ReceiverViewHolder) holder).txtReceiver.setVisibility(View.GONE);
                    ((ReceiverViewHolder) holder).imgReceiver.layout(0, 0, 0, 0);
                    Glide.with(((ReceiverViewHolder) holder).imgReceiver.getContext()).load(message.getImageUrl()).placeholder(R.drawable.placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL).into(((ReceiverViewHolder) holder).imgReceiver);
                }
            } else if ("text".equals(message.getType())) {
                ((ReceiverViewHolder) holder).txtReceiver.setText(message.getMessage());
                if (message.getMessage().contains("https://")) {
                   // ((ReceiverViewHolder) holder).txtReceiver.setSingleLine();
                    ((ReceiverViewHolder) holder).cardLink.setVisibility(View.VISIBLE);
                    ((ReceiverViewHolder) holder).txtReceiver.setVisibility(View.GONE);
                    ((ReceiverViewHolder) holder).txtLink.setText(message.getMessage());
                    Link link = new Link(message.getMessage())
                            .setTextColor(Color.parseColor("#259B24"))                  // optional, defaults to holo blue
                            .setTextColorOfHighlightedLink(Color.parseColor("#0D3D0C")) // optional, defaults to holo blue
                            .setHighlightAlpha(.4f)                                     // optional, defaults to .15f
                            .setUnderlined(true)                                       // optional, defaults to true
                            .setBold(true)                                              // optional, defaults to false
                            .setOnLongClickListener(new Link.OnLongClickListener() {
                                @Override
                                public void onLongClick(String clickedText) {
                                    // long clicked
                                }
                            })
                            .setOnClickListener(new Link.OnClickListener() {
                                @Override
                                public void onClick(@NonNull String clickedText) {
                                    // single clicked
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickedText));
                                    context.startActivity(browserIntent);

                                }
                            });


                    // create the link builder object add the link rule
                    LinkBuilder.on(((ReceiverViewHolder) holder).txtLink)
                            .addLink(link)
                            .build();

                    if (message.getMessage().contains("tiktok")){
                        Glide.with(context).load(R.drawable.tiktok4).placeholder(R.drawable.placeholder).
                                into(((ReceiverViewHolder) holder).imgLink);
                    }else if (message.getMessage().contains("youtu.be")){
                        String thumbnail="https://img.youtube.com/vi/" + getYouTubeId(message.getMessage()) + "/0.jpg";
                        RequestOptions options= new RequestOptions();
                        Glide.with(context).load(thumbnail)
                                .thumbnail(Glide.with(context).load(thumbnail))
                                .apply(options)
                                .placeholder(R.drawable.placeholder).
                                into(((ReceiverViewHolder) holder).imgLink);
                    }else if (message.getMessage().contains("instagram")){
                        Glide.with(context).load(R.drawable.instagram_logo).placeholder(R.drawable.placeholder).
                                into(((ReceiverViewHolder) holder).imgLink);
                    }
                }
            } else if ("videoCall".equals(message.getType())) {
                ((ReceiverViewHolder) holder).txtReceiver.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).layoutVideoCall.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder) holder).txtVideoCall.setText("You missed a call.");

                ((ReceiverViewHolder) holder).btnCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkResponse();
                    }
                });
            } else {
                if (message.getAudioFile() != null) {
                    broadcastReceiver = new InternetCheckServices();
                    registerBroadcastReceiver();
                    CheckConnection checkConnection = new CheckConnection();
                    if (checkConnection.isConnected(context)) {
                        return;
                    }
                    ((ReceiverViewHolder) holder).txtReceiver.setVisibility(View.GONE);
                    ((ReceiverViewHolder) holder).voicePlayerView.setVisibility(View.VISIBLE);
                    ((ReceiverViewHolder) holder).voicePlayerView.getImgPlay().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((ReceiverViewHolder) holder).voicePlayerView.setAudio(message.getAudioFile());
                            ((ReceiverViewHolder) holder).voicePlayerView.setSeekBarStyle(R.color.colorPurple, R.color.colorPurple);
                        }
                    });
                    ((ReceiverViewHolder) holder).voicePlayerView.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            unregisterNetwork();
                        }
                    });
                }
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            ((ReceiverViewHolder) holder).txtReceiverTime.setText(dateFormat.format(new Date(message.getTimestamp())));
            Glide.with(context).load(message.getProfilePic()).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((ReceiverViewHolder) holder).profilepic);

            FirebaseDatabase.getInstance().getReference().child("Users").child(recId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Users users = snapshot.getValue(Users.class);
                    assert users != null;
                    receiverName = users.getUserName();
                    profilePic = users.getProfilePic();
                    email = users.getMail();
                    Status = users.getStatus();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            ((ReceiverViewHolder) holder).profilepic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("UserIdPA", recId);
                    intent.putExtra("UserNamePA", receiverName);
                    intent.putExtra("ProfilePicPA", profilePic);
                    intent.putExtra("EmailPA", email);
                    intent.putExtra("StatusPA", Status);
                    context.startActivity(intent);
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
                                String senderRoom = FirebaseAuth.getInstance().getUid() + recId;
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("photo".equals(message.getType())) {
                    Intent fullScreenImage = new Intent(context, FullScreenImage.class);
                    fullScreenImage.putExtra("UserId", recId);
                    fullScreenImage.putExtra("messageImage", message.getImageUrl());
                    fullScreenImage.putExtra("ProfilePic", profilePic);
                    fullScreenImage.putExtra("userEmail", email);
                    fullScreenImage.putExtra("UserName", receiverName);
                    context.startActivity(fullScreenImage);
                }
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
        private final TextView txtReceiver,txtLink;
        private final TextView txtReceiverTime, txtVideoCall;
        private final CircleImageView profilepic;
        private final ImageView imgReceiver,imgLink;
        private final VoicePlayerView voicePlayerView;
        private final LinearLayout layoutVideoCall;
        private final Button btnCall;
        private final MaterialCardView cardLink;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReceiver = itemView.findViewById(R.id.txtGroupReceiver);
            txtReceiverTime = itemView.findViewById(R.id.txtGroupReceiverTime);
            profilepic = itemView.findViewById(R.id.group_profile_image);
            imgReceiver = itemView.findViewById(R.id.imgReceiver);
            voicePlayerView = itemView.findViewById(R.id.voicePlayerView);
            txtVideoCall = itemView.findViewById(R.id.txtReceiverVideoCall);
            layoutVideoCall = itemView.findViewById(R.id.receiverLayoutVideoCall);
            btnCall = itemView.findViewById(R.id.btnReceiverCall);
            cardLink=itemView.findViewById(R.id.receiverCardLink);
            txtLink=itemView.findViewById(R.id.receiverTxtLink);
            imgLink=itemView.findViewById(R.id.receiverImgThumbnail);
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtSender,txtLink;
        private final TextView txtSenderTime, txtVideoCall;
        private final ImageView imgSender,imgLink;
        private final VoicePlayerView voicePlayerView;
        private final LinearLayout layoutVideoCall;
        private final Button btnCall;
        private final MaterialCardView cardLink;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSender = itemView.findViewById(R.id.txtSender);
            txtSenderTime = itemView.findViewById(R.id.txtSenderTime);
            imgSender = itemView.findViewById(R.id.imgSender);
            voicePlayerView = itemView.findViewById(R.id.voicePlayerView);
            layoutVideoCall = itemView.findViewById(R.id.layoutVideoCall);
            txtVideoCall = itemView.findViewById(R.id.txtVideoCall);
            btnCall = itemView.findViewById(R.id.btnCall);
            txtLink=itemView.findViewById(R.id.txtLink);
            imgLink=itemView.findViewById(R.id.imgThumbnail);
            cardLink=itemView.findViewById(R.id.cardLink);
        }
    }

    private void checkResponse() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(recId).child("VideoCall").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String onCall = snapshot.child("onCall").getValue(String.class);
                    if ("false".equals(onCall)) {
                        Intent intent = new Intent(context, ConnectingActivity.class);
                        intent.putExtra("ProfilePic", profilePic);
                        intent.putExtra("UserName", receiverName);
                        intent.putExtra("UserId", recId);
                        intent.putExtra("userEmail", email);
                        intent.putExtra("type", "video");
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Users is on Another Call", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("onCall", "false");
                    map.put("response", "idle");
                    map.put("key", FirebaseAuth.getInstance().getUid() + recId);
                    FirebaseDatabase.getInstance().getReference().child("Users").child(recId).child("VideoCall").updateChildren(map);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private String getYouTubeId (String youTubeUrl) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if(matcher.find()){
            return matcher.group();
        } else {
            return "error";
        }
    }

    private void registerBroadcastReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            IntentFilter intentFilter= new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
           context.registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    private void unregisterNetwork() {
        try {
            context.unregisterReceiver(broadcastReceiver);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        unregisterNetwork();
    }

}
