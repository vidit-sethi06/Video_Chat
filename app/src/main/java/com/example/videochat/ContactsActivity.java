package com.example.videochat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ContactsActivity extends AppCompatActivity {
    BottomNavigationView bottomNav;
    RecyclerView myContactsList;
    ImageView findPeopleBtn;
    private DatabaseReference contactsRef,usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String userName="",profileImage="",calledBy="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contacts);

        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        bottomNav = findViewById(R.id.bottom_nav);
         bottomNav.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        findPeopleBtn = findViewById(R.id.find_people_btn);
        myContactsList = findViewById(R.id.contact_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        findPeopleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findPeopleIntent = new Intent(ContactsActivity.this, FindPeopleActivity.class);
                startActivity(findPeopleIntent);
            }
        });

    }

    private  BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.navigation_home:
                    Intent mainIntent = new Intent(ContactsActivity.this, ContactsActivity.class);
                    startActivity(mainIntent);
                    break;
                case R.id.navigation_notifications:
                    Intent notificationsIntent = new Intent(ContactsActivity.this,NotificationsActivity.class);
                    startActivity(notificationsIntent);
                    break;
                case R.id.navigation_settings:
                    Intent settingsIntent = new Intent(ContactsActivity.this,SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
                case R.id.navigation_logout:
                    FirebaseAuth.getInstance().signOut();
                    Intent logoutIntent = new Intent(ContactsActivity.this,RegistrationActivity.class);
                    startActivity(logoutIntent);
                    finish();
                    break;

            }

            return true;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        
        checkForReceivingCall();

        validateUser();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(currentUserID),Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull Contacts contacts) {
                final String listUserID= getRef(i).getKey();
                usersRef.child(listUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            userName=snapshot.child("name").getValue().toString();
                            profileImage=snapshot.child("image").getValue().toString();

                            contactsViewHolder.userNameTxt.setText(userName);
                            Picasso.get().load(profileImage).into(contactsViewHolder.profileImageView);
                        }
                        contactsViewHolder.callBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent callingIntent = new Intent(ContactsActivity.this,CallingActivity.class);
                                callingIntent.putExtra("visit_user_id",listUserID);
                                startActivity(callingIntent);
                                finish();

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
               ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        myContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }

    private void checkForReceivingCall() {
        usersRef.child(currentUserID).child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild("ringing")){
                            calledBy = snapshot.child("ringing").getValue().toString();
                            Intent callingIntent = new Intent(ContactsActivity.this,CallingActivity.class);
                            callingIntent.putExtra("visit_user_id",calledBy);
                            startActivity(callingIntent);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTxt;
        Button callBtn;
        ImageView profileImageView;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTxt = itemView.findViewById(R.id.name_contact);
            callBtn = itemView.findViewById(R.id.call_btn);
            profileImageView = itemView.findViewById(R.id.image_contact);
        }
    }
    private void validateUser() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Intent settingIntent = new Intent(ContactsActivity.this,SettingsActivity.class);
                    startActivity(settingIntent);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}