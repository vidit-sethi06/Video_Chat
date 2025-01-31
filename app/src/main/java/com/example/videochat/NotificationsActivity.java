package com.example.videochat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView notifications_list;
    private DatabaseReference friendRequestRef,contactsRef,usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        friendRequestRef= FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        notifications_list = findViewById(R.id.notifications_list);
        notifications_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().
                setQuery(friendRequestRef.child(currentUserID),Contacts.class).
                build();

        FirebaseRecyclerAdapter<Contacts,NotificationsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final NotificationsViewHolder notificationsViewHolder, int i, @NonNull Contacts contacts) {
                        notificationsViewHolder.acceptBtn.setVisibility(View.VISIBLE);
                        notificationsViewHolder.cancelBtn.setVisibility(View.VISIBLE);

                        final String listUserID= getRef(i).getKey();

                        DatabaseReference requestTypeRef =getRef(i).child("request_type").getRef();
                        requestTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    String type= snapshot.getValue().toString();
                                    if(type.equals("received")){
                                        notificationsViewHolder.cardView.setVisibility(View.VISIBLE);
                                        usersRef.child(listUserID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.hasChild("image")){
                                                    final String imageStr = snapshot.child("image").getValue().toString();
                                                    Picasso.get().load(imageStr).into(notificationsViewHolder.profileImageView);

                                                }

                                                    final String nameStr = snapshot.child("name").getValue().toString();
                                                    notificationsViewHolder.userNameTxt.setText(nameStr);

                                                notificationsViewHolder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        contactsRef.child(currentUserID).child(listUserID)
                                                                .child("Contacts").setValue("Saved")
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            contactsRef.child(listUserID).child(currentUserID)
                                                                                    .child("Contacts").setValue("Saved")
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()){
                                                                                                friendRequestRef.child(currentUserID).child(listUserID)
                                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful()){

                                                                                                            friendRequestRef.child(listUserID).child(currentUserID)
                                                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                    if(task.isSuccessful()){
                                                                                                                        Toast.makeText(NotificationsActivity.this, "Contact Added", Toast.LENGTH_SHORT).show();
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
                                                });
                                                notificationsViewHolder.cancelBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        friendRequestRef.child(currentUserID).child(listUserID)
                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){

                                                                    friendRequestRef.child(listUserID).child(currentUserID)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                Toast.makeText(NotificationsActivity.this, "Friend Request Removed", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }
                                    else {
                                        notificationsViewHolder.cardView.setVisibility(View.GONE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_design,parent,false);
                        NotificationsViewHolder viewHolder = new NotificationsViewHolder(view);
                        return viewHolder;
                    }
                };
        notifications_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTxt;
        Button acceptBtn,cancelBtn;
        ImageView profileImageView;
        RelativeLayout cardView;

        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTxt = itemView.findViewById(R.id.name_notification);
            acceptBtn = itemView.findViewById(R.id.request_accept_btn);
            cancelBtn = itemView.findViewById(R.id.request_decline_btn);
            profileImageView = itemView.findViewById(R.id.image_notification);
            cardView  = itemView.findViewById(R.id.card_view);
        }
    }
    private void AcceptFriendRequest() {
    }

    private void CancelFriendRequest() {

    }

}