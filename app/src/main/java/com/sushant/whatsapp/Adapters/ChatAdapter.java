package com.sushant.whatsapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.whatsapp.FullScreenImage;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter {

    ArrayList<Messages> messageModel;
    Context context;
    String recId;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    private MediaPlayer player = null;
    boolean isPlaying=false;

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
            if ("photo".equals(message.getType())){ //yoda condition solves unsafe null behaviour
                ((SenderViewHolder) holder).imgSender.setImageBitmap(null);
                ((SenderViewHolder) holder).imgSender.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).txtSender.setVisibility(View.GONE);
                ((SenderViewHolder) holder).imgSender.layout(0,0,0,0);
                Glide.with(((SenderViewHolder) holder).imgSender.getContext()).load(message.getImageUrl()).placeholder(R.drawable.placeholder).
                        diskCacheStrategy(DiskCacheStrategy.ALL).into(((SenderViewHolder) holder).imgSender);
            }else if ("text".equals(message.getType())){
                ((SenderViewHolder) holder).txtSender.setText(message.getMessage());
            }else {
                if (message.getAudioFile()!=null){
                    ((SenderViewHolder) holder).layoutAudio.setVisibility(View.VISIBLE);
                    ((SenderViewHolder) holder).txtSender.setVisibility(View.GONE);
                }
            }
            SimpleDateFormat dateFormat= new SimpleDateFormat("hh:mm a");
            ((SenderViewHolder) holder).txtSenderTime.setText(dateFormat.format(new Date(message.getTimestamp())));
        } else {
            if ("photo".equals(message.getType())){
                ((ReceiverViewHolder) holder).imgReceiver.setImageBitmap(null);
                ((ReceiverViewHolder) holder).imgReceiver.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder) holder).txtReceiver.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).imgReceiver.layout(0,0,0,0);
                Glide.with(((ReceiverViewHolder) holder).imgReceiver.getContext()).load(message.getImageUrl()).placeholder(R.drawable.placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(((ReceiverViewHolder) holder).imgReceiver);
            }else if ("text".equals(message.getType())){
                ((ReceiverViewHolder) holder).txtReceiver.setText(message.getMessage());
            }else {
                if (message.getAudioFile()!=null){
                    ((ReceiverViewHolder) holder).txtReceiver.setVisibility(View.GONE);
                    ((ReceiverViewHolder) holder).layoutAudio.setVisibility(View.VISIBLE);
                }
            }
            SimpleDateFormat dateFormat= new SimpleDateFormat("hh:mm a");
            ((ReceiverViewHolder) holder).txtReceiverTime.setText(dateFormat.format(new Date(message.getTimestamp())));
            Glide.with(context).load(message.getProfilePic()).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((ReceiverViewHolder) holder).profilepic);
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("photo".equals(message.getType())){
                    Intent fullScreenImage=new Intent(context, FullScreenImage.class);
                    fullScreenImage.putExtra("messageImage",message.getImageUrl());
                    context.startActivity(fullScreenImage);
                }else if ("audio".equals(message.getType())){
                    if (!isPlaying){
                        startPlaying(message.getAudioFile());
                        if (holder.getClass()==SenderViewHolder.class){
                            ((SenderViewHolder) holder).imgAudio.setImageResource(R.drawable.ic_stop_circle);
                            ((SenderViewHolder) holder).txtAudio.setText("Playing..");
                        }else {
                            ((ReceiverViewHolder) holder).imgAudio.setImageResource(R.drawable.ic_stop_circle);
                            ((ReceiverViewHolder) holder).txtAudio.setText("Playing..");
                        }
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                mediaPlayer.release();
                                if (holder.getClass()==SenderViewHolder.class){
                                    ((SenderViewHolder) holder).imgAudio.setImageResource(R.drawable.ic_play);
                                    ((SenderViewHolder) holder).txtAudio.setText("Play Recording");
                                }else {
                                    ((ReceiverViewHolder) holder).imgAudio.setImageResource(R.drawable.ic_play);
                                    ((ReceiverViewHolder) holder).txtAudio.setText("Play Recording");
                                }
                            }
                        });
                        isPlaying=true;
                    }else {
                         stopPlaying();
                        if (holder.getClass()==SenderViewHolder.class){
                            ((SenderViewHolder) holder).imgAudio.setImageResource(R.drawable.ic_play);
                            ((SenderViewHolder) holder).txtAudio.setText("Play Recording");
                        }else {
                            ((ReceiverViewHolder) holder).imgAudio.setImageResource(R.drawable.ic_play);
                            ((ReceiverViewHolder) holder).txtAudio.setText("Play Recording");
                        }
                        isPlaying=false;
                    }

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
        private final TextView txtReceiver;
        private final TextView txtReceiverTime,txtAudio;
        private final CircleImageView profilepic;
        private final ImageView imgReceiver,imgAudio;
        private final LinearLayout layoutAudio;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReceiver = itemView.findViewById(R.id.txtGroupReceiver);
            txtReceiverTime = itemView.findViewById(R.id.txtGroupReceiverTime);
            profilepic=itemView.findViewById(R.id.group_profile_image);
            imgReceiver=itemView.findViewById(R.id.imgReceiver);
            layoutAudio=itemView.findViewById(R.id.layout_audio);
            txtAudio=itemView.findViewById(R.id.txt_audio);
            imgAudio=itemView.findViewById(R.id.img_play_record);
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtSender;
        private final TextView txtSenderTime,txtAudio;
        private final ImageView imgSender,imgAudio;
        private final LinearLayout layoutAudio;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSender = itemView.findViewById(R.id.txtSender);
            txtSenderTime = itemView.findViewById(R.id.txtSenderTime);
            imgSender=itemView.findViewById(R.id.imgSender);
            layoutAudio=itemView.findViewById(R.id.layout_audio);
            txtAudio=itemView.findViewById(R.id.txt_audio);
            imgAudio=itemView.findViewById(R.id.img_play_record);
        }
    }

    private void startPlaying(String audio) {
        player = new MediaPlayer();
        try {
            player.setDataSource(audio);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e("Audio", "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }
}
