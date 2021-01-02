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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import static android.widget.Toast.LENGTH_SHORT;

public class Register extends AppCompatActivity {
    private EditText Email, Password, Repassword;
    private Button Signup, Login;
    private FirebaseAuth mAuth;
    private DatabaseReference rootref;
    private ProgressDialog lodingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        rootref = FirebaseDatabase.getInstance().getReference();
        rimplemantaion();
        sendusertoLogin();
        createAccount();
    }

    private void createAccount() {
        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createnewAccount();
            }
        });
    }

    private void createnewAccount() {
        String email = Email.getText().toString();
        String password = Password.getText().toString();
        String rpassword = Repassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email i'd ", LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter Password ", LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(rpassword)){
            Toast.makeText(this, "Please enter Re-Password ", LENGTH_SHORT).show();
        }
        if (rpassword.equals(password) ){
            lodingBar.setTitle("Creating new account");
            lodingBar.setMessage("Pleased wait till we creat your account");
            lodingBar.setCanceledOnTouchOutside(true);
            lodingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String device_token = FirebaseInstanceId.getInstance().getToken();
                        String currenrtuserid = mAuth.getCurrentUser().getUid();
                        String email = Email.getText().toString();
                        HashMap<String, String>ProfileDetail = new HashMap<>();
                        ProfileDetail.put("Email",email);
                        ProfileDetail.put("Device_Token",device_token);

                        rootref.child("User").child(currenrtuserid).setValue(ProfileDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(Register.this,"Please verify your email.",Toast.LENGTH_SHORT).show();
                                }else{
                                    String message = task.getException().toString();
                                    Toast.makeText(Register.this,"Error: "+message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        senduseraverificationemail();
                        sendusertoLogin();
                        Toast.makeText(Register.this,"Account created successfully", Toast.LENGTH_SHORT).show();
                        lodingBar.dismiss();
                        Intent loginIntent = new Intent(Register.this, Login.class);
                        startActivity(loginIntent);
                        finish();
                    }else{
                        String Signuperror = task.getException().toString();
                        Toast.makeText(Register.this,"Error" + Signuperror, LENGTH_SHORT).show();
                        lodingBar.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                public void onFailure(@NonNull Exception e) {
                    Log.e("exception", e.getMessage());
                }
            });
        }
        else{
            Toast.makeText(this, "Password and Re-enter password are not same ", LENGTH_SHORT).show();

        }

    }

    private void senduseraverificationemail() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("emailverification", "Email sent.");
                        }
                    }
                });
    }

    private void sendusertoLogin() {
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(Register.this, Login.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }

    private void rimplemantaion() {
        Email = (EditText) findViewById(R.id.email);
        Password = (EditText) findViewById(R.id.password);
        Repassword = (EditText) findViewById(R.id.Rpassword);
        Signup = (Button) findViewById(R.id.SignUp);
        Login = (Button) findViewById(R.id.LogIn);
        lodingBar = new ProgressDialog(this);
    }
}
