package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import static android.widget.Toast.LENGTH_SHORT;

public class Login extends AppCompatActivity {
    private Button loginbutton, signupbutton;
    private EditText email, passsword;
    private TextView forgetPassword, phoneLogin;
    private FirebaseAuth mAuth;
    private ProgressDialog lodingBar,recoverylodingBar;
    private DatabaseReference userref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        userref = FirebaseDatabase.getInstance().getReference().child("User");
        recoverylodingBar = new ProgressDialog(this);
        implementaion();
      Sendusertoregister();
      Userlogin();
      forgetpassword();
    }

    private void forgetpassword() {
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showrecoverydialog();
            }
        });
    }

    private void showrecoverydialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recovery Password");
        builder.setMessage("Enter your Email i'd. We will just you a recovery link on your Email i'd");

        LinearLayout linearLayout = new LinearLayout(this);
        EditText editText = new EditText(this);
        editText.setHint("Enter your Email i'd");
        editText.setMinEms(16);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(editText);
        linearLayout.setPadding(10, 10, 10, 10 );
        builder.setView(linearLayout);
        builder.setPositiveButton("Recovery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = editText.getText().toString().trim();
                beginerecovery(email);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginerecovery(String email) {
        recoverylodingBar.setMessage("Sending Email...");
        recoverylodingBar.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
             if(task.isSuccessful()){
                 recoverylodingBar.dismiss();
                 Toast.makeText(getApplicationContext(),"Recovery Email is been send", Toast.LENGTH_SHORT).show();

             }else{
                 recoverylodingBar.dismiss();
                 Toast.makeText(getApplicationContext(),"Sorry your email is incorrect or may not be linked with us.", Toast.LENGTH_SHORT).show();
             }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                recoverylodingBar.dismiss();
                Toast.makeText(getApplicationContext(),""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void Userlogin() {
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email = email.getText().toString();
                String Password = passsword.getText().toString();

                if (TextUtils.isEmpty(Email)){
                    Toast.makeText(Login.this,"Please enter E-mail i'd", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(Password)){
                    Toast.makeText(Login.this,"Please enter Password", Toast.LENGTH_SHORT).show();
                }
               else{
                    lodingBar.setTitle("Login to your Account");
                    lodingBar.setMessage("Pleased wait....");
                    lodingBar.setCanceledOnTouchOutside(true);
                    lodingBar.show();
                   mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           if (task.isSuccessful()){
                               String currentuserId = mAuth.getCurrentUser().getUid();
                               String deviceToken = FirebaseInstanceId.getInstance().getToken();
                               userref.child(currentuserId).child("Device_Token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                           sendusertomainactivity();
                                           Toast.makeText(Login.this,"Login successful", LENGTH_SHORT).show();
                                           lodingBar.dismiss();
                                       }else{
                                           Toast.makeText(Login.this,"it seems that your email is not verified", LENGTH_SHORT).show();
                                           lodingBar.dismiss();
                                       }

                                   }
                               });

                           }else{
                               String Loginerror = task.getException().toString();
                               Toast.makeText(Login.this,"Error" + Loginerror, LENGTH_SHORT).show();
                               lodingBar.dismiss();
                           }
                       }
                   }).addOnFailureListener(new OnFailureListener() {
                       public void onFailure(@NonNull Exception e) {
                           Log.e("exception", e.getMessage());
                       }
                   });
                }
            }
        });
        phoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneIntent = new Intent(Login.this,phoneLogin.class);
                startActivity(phoneIntent);
            }
        });
    }

    private void Sendusertoregister() {
        signupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent singup = new Intent(Login.this,Register.class);
                startActivity(singup);
                finish();
            }
        });
    }

    private void implementaion() {
        loginbutton = (Button) findViewById(R.id.loginButton);
        signupbutton = (Button) findViewById(R.id.SignUp);
        email = (EditText) findViewById(R.id.email);
        passsword = (EditText) findViewById(R.id.password);
        forgetPassword = (TextView) findViewById(R.id.forgetpassword);
        phoneLogin = (TextView) findViewById(R.id.loginusingphone);
        lodingBar = new ProgressDialog(this);

    }

    private void sendusertomainactivity() {
        Intent Senduser = new Intent(Login.this, MainActivity.class);
        Senduser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Senduser);
        finish();
    }
}
