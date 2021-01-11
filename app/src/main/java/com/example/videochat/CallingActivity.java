package com.example.videochat;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {
    private TextView nameContact;
    private ImageView profileImage,cancelCallBtn,acceptCallBtn;

    private String receiverUserID="",receiverUserName="",receiverUserImage="";
    private String senderUserID="",senderUserName="",senderUserImage="",checker="";
    private String callingID="",ringingID="";
    private MediaPlayer mediaPlayer;
    DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        senderUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mediaPlayer = MediaPlayer.create(this,R.raw.ringing);

        nameContact = findViewById(R.id.name_calling);
        profileImage = findViewById(R.id.profile_image_calling);
        cancelCallBtn = findViewById(R.id.cancel_call);
        acceptCallBtn = findViewById(R.id.make_call);

        cancelCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                checker = "clicked" ;
                cancelCallingUser();
            }
        });
        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();

                final HashMap <String,Object> callPickUpMap = new HashMap<>();
                callPickUpMap.put("picked","picked");
                userRef.child(senderUserID).child("Ringing")
                        .updateChildren(callPickUpMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isComplete()){
                                    Intent intent = new Intent(CallingActivity.this,VideoChatActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
            }
        });

        getAndSetUserProfile();
    }
    private void getAndSetUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(receiverUserID).exists()){
                    receiverUserImage = snapshot.child(receiverUserID).child("image").getValue().toString();
                    receiverUserName = snapshot.child(receiverUserID).child("name").getValue().toString();

                    nameContact.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImage);
                }
                if (snapshot.child(senderUserID).exists()){
                    senderUserImage = snapshot.child(senderUserID).child("image").getValue().toString();
                    senderUserName = snapshot.child(senderUserID).child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.start();
        userRef.child(receiverUserID)
                 .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if( !checker.equals("clicked") && !snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing"))
                {

                    final HashMap<String,Object > callingInfo  = new HashMap<>();

                    callingInfo.put("calling",receiverUserID);

                    userRef.child(senderUserID)
                            .child("Calling")
                            .updateChildren(callingInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        final HashMap<String,Object > ringingInfo  = new HashMap<>();
                                        ringingInfo.put("ringing",senderUserID);

                                        userRef
                                                .child(receiverUserID)
                                                .child("Ringing")
                                                .updateChildren(ringingInfo);
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(senderUserID).hasChild("Ringing") && !snapshot.child(senderUserID).hasChild("Calling")  )
                {
                 acceptCallBtn.setVisibility(View.VISIBLE);
                }
                if(snapshot.child(receiverUserID).child("Ringing").hasChild("picked")){
                    mediaPlayer.stop();
                    Intent intent = new Intent(CallingActivity.this,VideoChatActivity.class);
                    startActivity(intent);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void cancelCallingUser() {
        mediaPlayer.stop();
        //Sender Side
        userRef.child(senderUserID).child("Calling")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChild("calling")) {
                    callingID = snapshot.child("calling").getValue().toString();
                    userRef.child(callingID)
                            .child("Ringing")
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        userRef.child(senderUserID)
                                                .child("Calling")
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        startActivity(new Intent(CallingActivity.this, RegistrationActivity.class));
                                                        finish();
                                                    }
                                                });
                                    }
                                }
                            });
                }
                else {
                    startActivity(new Intent(CallingActivity.this, RegistrationActivity.class));
                    finish();
                }            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //receiver Side
        userRef.child(senderUserID)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChild("ringing")) {
                    ringingID = snapshot.child("ringing").getValue().toString();
                    userRef.child(ringingID)
                            .child("Calling")
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        userRef.child(senderUserID)
                                                .child("Ringing")
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        mediaPlayer.stop();
                                                        startActivity(new Intent(CallingActivity.this, RegistrationActivity.class));
                                                        finish();
                                                    }
                                                });
                                    }
                                }
                            });
                }
                else {
                    mediaPlayer.stop();
                    startActivity(new Intent(CallingActivity.this, RegistrationActivity.class));
                    finish();
                }            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}