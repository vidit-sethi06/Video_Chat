package com.example.videochat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class profileActivity extends AppCompatActivity {
    private String receiverUserID="",receiverUserName="",receiverUserImage="";
    private ImageView background_profile_view;
    private TextView name_profile;
    private Button add_friend,decline_friend_request;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String currentState = "new";
    private DatabaseReference friendRequestRef,contactsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        friendRequestRef= FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");


        receiverUserID= getIntent().getExtras().get("visit_user_id").toString();
        receiverUserName= getIntent().getExtras().get("profile_name").toString();
        receiverUserImage= getIntent().getExtras().get("profile_image").toString();

        background_profile_view = findViewById(R.id.background_profile_view);
        name_profile = findViewById(R.id.name_profile);
        add_friend= findViewById(R.id.add_friend);
        decline_friend_request = findViewById(R.id.decline_friend_request);

        Picasso.get().load(receiverUserImage).into(background_profile_view);
        name_profile.setText(receiverUserName);

        manageClickEvents();

    }

    private void manageClickEvents() {
        friendRequestRef.child(currentUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(receiverUserID)){
                            String requestType = snapshot.child(receiverUserID).child("request_type").getValue().toString();
                            if(requestType.equals("sent")){
                                currentState ="request_sent";
                                add_friend.setText("Cancel Friend Request");

                            }
                            else if(requestType.equals("received")){
                                currentState ="request_received";
                                add_friend.setText("Accept Friend Request");

                                decline_friend_request.setVisibility(View.VISIBLE);
                                decline_friend_request.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelFriendRequest();
                                    }
                                });

                            }
                        }
                        else {
                            contactsRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(receiverUserID)){
                                        currentState = "friends";
                                        add_friend.setText("Delete Contact");
                                    }
                                    else {
                                        currentState ="new";
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if(receiverUserID.equals(currentUserID)){
            add_friend.setVisibility(View.GONE);
        }
        else {
            add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (currentState.equals("new")){
                        SendFriendRequest();
                    }
                    if(currentState.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    if(currentState.equals("request_received")){
                        AcceptFriendRequest();

                    }
                    if(currentState.equals("request_sent")){
                        CancelFriendRequest();

                    }

                }
            });
        }
    }

    private void AcceptFriendRequest() {
        contactsRef.child(currentUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(receiverUserID).child(currentUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                friendRequestRef.child(currentUserID).child(receiverUserID)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){

                                                            friendRequestRef.child(receiverUserID).child(currentUserID)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        currentState= "Friend";
                                                                        add_friend.setText("Remove Friend");
                                                                        decline_friend_request.setVisibility(View.GONE);
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest() {
        friendRequestRef.child(currentUserID).child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    friendRequestRef.child(receiverUserID).child(currentUserID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                currentState= "new";
                                add_friend.setText("Add Friend");
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendFriendRequest() {
        friendRequestRef.child(currentUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(receiverUserID).child(currentUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                currentState ="request_sent";
                                                add_friend.setText("Cancel Friend Request");
                                                Toast.makeText(profileActivity.this, "Friend Request Sent ", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                        }
                    }
                });
    }
}