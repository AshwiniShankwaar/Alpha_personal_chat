package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class phoneLogin extends AppCompatActivity {
    private EditText phoneNumberIn, verification_code;
    private Button sendVerificationCode, verifyCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        mAuth = FirebaseAuth.getInstance();
        Initialization();
        sendVerificationcodeButton();
        verifycodeButton();
    }

    private void verifycodeButton() {
        verifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCode.setVisibility(View.INVISIBLE);
                phoneNumberIn.setVisibility(View.INVISIBLE);

                String verificationCodeInput = verification_code.getText().toString();
                if (TextUtils.isEmpty(verificationCodeInput)){
                    Toast.makeText(phoneLogin.this,"Please enter the verification code", Toast.LENGTH_SHORT).show();
                }else{
                    loadingBar.setTitle("Verification code");
                    loadingBar.setMessage("Please wait...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCodeInput);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void sendVerificationcodeButton() {
       sendVerificationCode.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               String phnbr = phoneNumberIn.getText().toString();
               if(TextUtils.isEmpty(phnbr)){
                   Toast.makeText(phoneLogin.this,"Please enter phone number",Toast.LENGTH_SHORT).show();
               }else{
                   loadingBar.setTitle("Phone verification");
                   loadingBar.setMessage("Please wait till we verify your account");
                   loadingBar.setCanceledOnTouchOutside(false);
                   loadingBar.show();

                   PhoneAuthProvider.getInstance().verifyPhoneNumber(
                           phnbr,
                           60,
                           TimeUnit.SECONDS,
                           phoneLogin.this,
                           mCallbacks
                   );
               }
           }
       });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
               signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
              Toast.makeText(phoneLogin.this,"Please enter the correct phone number", Toast.LENGTH_SHORT).show();
                sendVerificationCode.setVisibility(View.VISIBLE);
                phoneNumberIn.setVisibility(View.VISIBLE);
                verifyCode.setVisibility(View.INVISIBLE);
                verification_code.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                loadingBar.dismiss();

                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(phoneLogin.this,"Verification code is been send", Toast.LENGTH_SHORT).show();
                sendVerificationCode.setVisibility(View.INVISIBLE);
                phoneNumberIn.setVisibility(View.INVISIBLE);
                verifyCode.setVisibility(View.VISIBLE);
                verification_code.setVisibility(View.VISIBLE);
            }
        };
    }

    private void Initialization() {
        phoneNumberIn = (EditText) findViewById(R.id.phoneNumberInput);
        verification_code = (EditText) findViewById(R.id.verificationInput);
        sendVerificationCode = (Button) findViewById(R.id.send_code);
        verifyCode = (Button) findViewById(R.id.verification);
        loadingBar = new ProgressDialog(this);
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            sendusertomainActivity();
                            Toast.makeText(phoneLogin.this,"welcome",Toast.LENGTH_SHORT).show();
                        } else {
                            String msg = task.getException().toString();
                            Toast.makeText(phoneLogin.this,"Error: "+ msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendusertomainActivity() {
        Intent logintent = new Intent(phoneLogin.this,MainActivity.class);
        startActivity(logintent);
    }
}
