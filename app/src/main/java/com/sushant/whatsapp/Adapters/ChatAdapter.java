package com.sushant.whatsapp.Adapters;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.ProfileActivity;
import com.sushant.whatsapp.R;
import com.sushant.whatsapp.ShareActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.jvm.functions.Function1;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

public class ChatAdapter extends RecyclerView.Adapter {

    ArrayList<Messages> messageModel;
    Context context;
    String recId;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    String receiverName, profilePic, email, Status;
    int reaction;

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
            View view = LayoutInflater.from(context).inflate(R.layout.sample, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages message = messageModel.get(position);
        holder.setIsRecyclable(false);
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(new int[]{
                        R.drawable.ic_fb_like,
                        R.drawable.ic_fb_love,
                        R.drawable.ic_fb_laugh,
                        R.drawable.ic_fb_wow,
                        R.drawable.ic_fb_sad,
                        R.drawable.ic_fb_angry,
                        R.drawable.ic_delete
                })
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            return true; // true is closing popup, false is requesting a new selection
        });
        String senderRoom = FirebaseAuth.getInstance().getUid() + recId;
        String receiverRoom = recId + FirebaseAuth.getInstance().getUid();
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

                    if (message.getMessage().contains("tiktok")) {
                        Glide.with(context).load(R.drawable.tiktok4).placeholder(R.drawable.placeholder).
                                into(((SenderViewHolder) holder).imgLink);
                    } else if (message.getMessage().contains("youtu.be") && message.getImageUrl() != null) {
                        RequestOptions options = new RequestOptions();
                        Glide.with(context).load(message.getImageUrl())
                                .thumbnail(Glide.with(context).load(message.getImageUrl()))
                                .apply(options)
                                .placeholder(R.drawable.placeholder).
                                into(((SenderViewHolder) holder).imgLink);

                    } else if (message.getMessage().contains("instagram")) {
                        Glide.with(context).load(R.drawable.instagram_round_logo).placeholder(R.drawable.placeholder).
                                into(((SenderViewHolder) holder).imgLink);
                    } else if (message.getMessage().contains("facebook") || message.getMessage().contains("fb")) {
                        Glide.with(context).load(R.drawable.fb_logo).placeholder(R.drawable.placeholder).
                                into(((SenderViewHolder) holder).imgLink);
                    }
                }
                ((SenderViewHolder) holder).txtSender.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("text", ((SenderViewHolder) holder).txtSender.getText());
                        manager.setPrimaryClip(clipData);
                    }
                });
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
                            CheckConnection checkConnection = new CheckConnection();
                            if (checkConnection.isConnected(context) || !checkConnection.isInternet()) {
                                Toast.makeText(context, "Please connect to the internet", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ((SenderViewHolder) holder).voicePlayerView.setAudio(message.getAudioFile());
                            ((SenderViewHolder) holder).voicePlayerView.setSeekBarStyle(R.color.colorPurple, R.color.colorPurple);
                        }
                    });

                }
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            ((SenderViewHolder) holder).txtSenderTime.setText(dateFormat.format(new Date(message.getTimestamp())));

            if (message.getReaction() >= 0 && message.getReaction() <= 5) {
                ((SenderViewHolder) holder).imgReact.setVisibility(View.VISIBLE);
                switch (message.getReaction()) {
                    case 0:
                        reaction = R.drawable.ic_fb_like;
                        break;
                    case 1:
                        reaction = R.drawable.ic_fb_love;
                        break;
                    case 2:
                        reaction = R.drawable.ic_fb_laugh;
                        break;
                    case 3:
                        reaction = R.drawable.ic_fb_wow;
                        break;
                    case 4:
                        reaction = R.drawable.ic_fb_sad;
                        break;
                    case 5:
                        reaction = R.drawable.ic_fb_angry;
                        break;
                    default:
                        ((SenderViewHolder) holder).imgReact.setVisibility(View.GONE);
                        break;
                }
                Glide.with(context).load(reaction)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(((SenderViewHolder) holder).imgReact);
            }
            ((SenderViewHolder) holder).imgReact.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view, motionEvent);
                    return false;
                }
            });

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

                    if (message.getMessage().contains("tiktok")) {
                        Glide.with(context).load(R.drawable.tiktok4).placeholder(R.drawable.placeholder).
                                into(((ReceiverViewHolder) holder).imgLink);
                    } else if (message.getMessage().contains("youtu.be") && message.getImageUrl() != null) {
                        RequestOptions options = new RequestOptions();
                        Glide.with(context).load(message.getImageUrl())
                                .thumbnail(Glide.with(context).load(message.getImageUrl()))
                                .apply(options)
                                .placeholder(R.drawable.placeholder).
                                into(((ReceiverViewHolder) holder).imgLink);
                    } else if (message.getMessage().contains("instagram")) {
                        Glide.with(context).load(R.drawable.instagram_round_logo).placeholder(R.drawable.placeholder).
                                into(((ReceiverViewHolder) holder).imgLink);
                    } else if (message.getMessage().contains("facebook") || message.getMessage().contains("fb")) {
                        Glide.with(context).load(R.drawable.fb_logo).placeholder(R.drawable.placeholder).
                                into(((ReceiverViewHolder) holder).imgLink);
                    }
                }
                ((ReceiverViewHolder) holder).txtReceiver.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("text", ((ReceiverViewHolder) holder).txtReceiver.getText());
                        manager.setPrimaryClip(clipData);
                    }
                });
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
                    ((ReceiverViewHolder) holder).txtReceiver.setVisibility(View.GONE);
                    ((ReceiverViewHolder) holder).voicePlayerView.setVisibility(View.VISIBLE);
                    ((ReceiverViewHolder) holder).voicePlayerView.getImgPlay().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CheckConnection checkConnection = new CheckConnection();
                            if (checkConnection.isConnected(context) || !checkConnection.isInternet()) {
                                Toast.makeText(context, "Please connect to the internet", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ((ReceiverViewHolder) holder).voicePlayerView.setAudio(message.getAudioFile());
                            ((ReceiverViewHolder) holder).voicePlayerView.setSeekBarStyle(R.color.colorPurple, R.color.colorPurple);
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

            if (message.getReaction() >= 0 && message.getReaction() <= 5) {
                ((ReceiverViewHolder) holder).imgReact.setVisibility(View.VISIBLE);

                switch (message.getReaction()) {
                    case 0:
                        reaction = R.drawable.ic_fb_like;
                        break;
                    case 1:
                        reaction = R.drawable.ic_fb_love;
                        break;
                    case 2:
                        reaction = R.drawable.ic_fb_laugh;
                        break;
                    case 3:
                        reaction = R.drawable.ic_fb_wow;
                        break;
                    case 4:
                        reaction = R.drawable.ic_fb_sad;
                        break;
                    case 5:
                        reaction = R.drawable.ic_fb_angry;
                        break;
                    default:
                        ((ReceiverViewHolder) holder).imgReact.setVisibility(View.GONE);
                        break;
                }
                Glide.with(context).load(reaction).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(((ReceiverViewHolder) holder).imgReact);
            }

            ((ReceiverViewHolder) holder).imgReact.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view, motionEvent);
                    return false;
                }
            });
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Dialog shareDialog = new Dialog(context);
                shareDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                shareDialog.setContentView(R.layout.fragment_bottom_sheet);
                TextView txtShareInside = shareDialog.findViewById(R.id.txtShare);
                TextView txtRemove = shareDialog.findViewById(R.id.txtRemove);
                TextView txtShareOutside = shareDialog.findViewById(R.id.txtShareOutSide);
                TextView txtReact = shareDialog.findViewById(R.id.txtReact);
                TextView txtCopy = shareDialog.findViewById(R.id.txtCopy);

                shareDialog.show();
                shareDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                shareDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                shareDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                shareDialog.getWindow().setGravity(Gravity.BOTTOM);
                shareDialog.getWindow().getAttributes().windowAnimations = R.style.NoAnimation;

                txtShareInside.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (message.getType()) {
                            case "text":
                                if (message.getMessage().contains("instagram") || message.getMessage().contains("tiktok") || message.getMessage().contains("youtu.be")
                                        || message.getMessage().contains("facebook") || message.getMessage().contains("fb")) {
                                    Intent intent = new Intent(context, ShareActivity.class);
                                    intent.putExtra("link", message.getMessage());
                                    if (message.getMessage().contains("youtu.be") && message.getImageUrl() != null) {
                                        intent.putExtra("thumbnail", message.getImageUrl());
                                    }
                                    intent.setAction("SEND_TEXT");
                                    intent.setType("chat_txt");
                                    context.startActivity(intent);
                                } else {
                                    Toast.makeText(context, "select link or image only", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case "photo": {
                                Intent intent = new Intent(context, ShareActivity.class);
                                intent.putExtra("image", message.getImageUrl());
                                intent.setAction("SEND_IMAGE");
                                intent.setType("chat_img");
                                context.startActivity(intent);
                                break;
                            }
                            case "audio": {
                                Intent intent = new Intent(context, ShareActivity.class);
                                intent.putExtra("audio", message.getAudioFile());
                                intent.setAction("SEND_AUDIO");
                                intent.setType("chat_audio");
                                context.startActivity(intent);
                                break;
                            }
                        }
                    }
                });

                txtShareOutside.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (message.getType()) {
                            case "text":
                                if (message.getMessage().contains("instagram") || message.getMessage().contains("tiktok") || message.getMessage().contains("youtu.be")
                                        || message.getMessage().contains("facebook") || message.getMessage().contains("fb")) {
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, message.getMessage());
                                    context.startActivity(Intent.createChooser(shareIntent, "Share via"));
                                } else {
                                    Toast.makeText(context, "select link or image only", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case "photo": {
                                ImageView imageView;
                                if (holder.getClass() == SenderViewHolder.class) {
                                    imageView = ((SenderViewHolder) holder).imgSender;
                                } else {
                                    imageView = ((ReceiverViewHolder) holder).imgReceiver;
                                }
                                Uri bmpUri = getLocalBitmapUri(imageView);
                                if (bmpUri != null) {
                                    Intent shareIntent = new Intent();
                                    shareIntent.setAction(Intent.ACTION_SEND);
                                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                    shareIntent.setType("image/*");
                                    context.startActivity(Intent.createChooser(shareIntent, "Share via"));
                                }
                                break;
                            }
                        }

                    }
                });

                txtReact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        shareDialog.dismiss();
                        if (holder.getClass() == SenderViewHolder.class) {
                            ((SenderViewHolder) holder).imgReact.setVisibility(View.VISIBLE);
                        } else {
                            ((ReceiverViewHolder) holder).imgReact.setVisibility(View.VISIBLE);
                        }
                    }
                });

                txtRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "remove", Toast.LENGTH_SHORT).show();
                    }
                });

                txtCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData;
                        if (holder.getClass() == SenderViewHolder.class) {
                            clipData = ClipData.newPlainText("text", ((SenderViewHolder) holder).txtSender.getText());
                        } else {
                            clipData = ClipData.newPlainText("text", ((ReceiverViewHolder) holder).txtReceiver.getText());
                        }
                        manager.setPrimaryClip(clipData);
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show();
                        shareDialog.dismiss();
                    }
                });

//                new AlertDialog.Builder(context).setTitle("Delete")
//                        .setMessage("Do you want to delete this message?")
//                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
////                                FirebaseDatabase database = FirebaseDatabase.getInstance();
////                                String senderRoom = FirebaseAuth.getInstance().getUid() + recId;
////                                database.getReference().child("Chats").child(senderRoom).child(message.getMessageId()).setValue(null);
//                            }
//                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                }).show();
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

        popup.setReactionSelectedListener(new Function1<Integer, Boolean>() {
            @Override
            public Boolean invoke(Integer integer) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("reaction", integer);

                FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child(message.getMessageId()).updateChildren(map)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverRoom).child(message.getMessageId()).updateChildren(map);
                            }
                        });
                return null;
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
        private final TextView txtReceiver, txtLink;
        private final TextView txtReceiverTime, txtVideoCall;
        private final CircleImageView profilepic;
        private final ImageView imgReceiver, imgLink, imgReact;
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
            cardLink = itemView.findViewById(R.id.receiverCardLink);
            txtLink = itemView.findViewById(R.id.receiverTxtLink);
            imgLink = itemView.findViewById(R.id.receiverImgThumbnail);
            imgReact = itemView.findViewById(R.id.img_receiver_react);
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtSender, txtLink;
        private final TextView txtSenderTime, txtVideoCall;
        private final ImageView imgSender, imgLink, imgReact;
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
            txtLink = itemView.findViewById(R.id.txtLink);
            imgLink = itemView.findViewById(R.id.imgThumbnail);
            cardLink = itemView.findViewById(R.id.cardLink);
            imgReact = itemView.findViewById(R.id.img_react);
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

    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // This way, you don't need to request external read/write permission.
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");

            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(context, "com.sushant.whatsapp.fileprovider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

}
