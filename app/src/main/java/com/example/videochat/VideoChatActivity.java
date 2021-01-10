package com.example.videochat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity
        implements Session.SessionListener,
        Publisher.PublisherListener
{
    private static String API_Key="",SESSION_Id="",TOKEN ="";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private FrameLayout mPublisherView,mSubscriberView;
    private Session mSession;
    private Publisher mPublisher;


    private ImageView closeVideoChatBtn;
    private DatabaseReference usersRef;
    private String userId="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(userId).hasChild("Ringing")){
                            usersRef.child(userId).child("Ringing").removeValue();
                            startActivity(new Intent(VideoChatActivity.this,RegistrationActivity.class));
                            finish();
                        }
                        if(snapshot.child(userId).hasChild("Calling")){
                            usersRef.child(userId).child("Calling").removeValue();
                            startActivity(new Intent(VideoChatActivity.this,RegistrationActivity.class));
                            finish();
                        }
                        else {
                            startActivity(new Intent(VideoChatActivity.this,RegistrationActivity.class));
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);
    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions(){
        String [] perms = {Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        if(EasyPermissions.hasPermissions(this,perms)){
            mPublisherView = findViewById(R.id.publisher_container);
            mSubscriberView = findViewById(R.id.subscriber_container);

            //intializing and connecting the controller
            mSession = new Session.Builder(this,API_Key, SESSION_Id).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);
        }
        else {
            EasyPermissions.requestPermissions(this,"Please allow the Camera and Mic Permissions to Start Video Chatting",RC_VIDEO_APP_PERM,perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }
    // Publishing stream to the session
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG,"Session Connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);

        
    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}