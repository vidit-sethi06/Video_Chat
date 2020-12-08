package com.example.videochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends AppCompatActivity {
    private CountryCodePicker ccp;
    private EditText phoneText;
    private EditText codeText;
    private Button continueAndNextBtn;
    private String checker= "", phoneNumber="";
    private RelativeLayout relativeLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        continueAndNextBtn = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);

        continueAndNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (continueAndNextBtn.getText().equals("Submit") ||  checker.equals("Code Sent"))
                {

            }
                else {
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if (!phoneNumber.equals("")) {
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("Please Wait, while we are verifying your Phone Number");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthOptions options =
                                PhoneAuthOptions.newBuilder(mAuth)
                                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                        .setActivity(this)                 // Activity (for callback binding)
                                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                        .build();
                        PhoneAuthProvider.verifyPhoneNumber(options);
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Please write valid Phone Numbe", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
          mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
              @Override
              public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

              }

              @Override
              public void onVerificationFailed(@NonNull FirebaseException e) {

              }

              @Override
              public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                  super.onCodeSent(s, forceResendingToken);
              }
          };


    }
}
