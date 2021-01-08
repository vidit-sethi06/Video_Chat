package com.example.videochat;

import android.os.Bundle;
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
    private ImageView profileImage,cancelCallBtn,makeCallBtn;

    private String receiverUserID="",receiverUserName="",receiverUserImage="";
    private String senderUserID="",senderUserName="",senderUserImage="";
    DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        senderUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        nameContact = findViewById(R.id.name_calling);
        profileImage = findViewById(R.id.profile_image_calling);
        cancelCallBtn = findViewById(R.id.cancel_call);
        makeCallBtn = findViewById(R.id.make_call);

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
        userRef.child(receiverUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing"))
                {
                    final HashMap<String,Object > callingInfo  = new HashMap<>();
                    callingInfo.put("uid",senderUserID);
                    callingInfo.put("name",senderUserName);
                    callingInfo.put("image",senderUserImage);
                    callingInfo.put("calling",receiverUserID);

                    userRef.child(senderUserID).child("Calling")
                            .updateChildren(callingInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        final HashMap<String,Object > callingInfo  = new HashMap<>();
                                        callingInfo.put("uid",senderUserID);
                                        callingInfo.put("name",senderUserName);
                                        callingInfo.put("image",senderUserImage);
                                        callingInfo.put("calling",receiverUserID);

                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}