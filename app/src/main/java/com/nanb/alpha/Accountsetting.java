package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Accountsetting extends AppCompatActivity {
    private Toolbar stoolbar;
    private FirebaseAuth mAuth;
    private String currentuserid;
    private LinearLayout privacylinear,request,delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsetting);
        mAuth = FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();
        intializzation();
        privacymethod();
        requestinfomethod();
        deletemyaccountmethod();
    }
    private void deletemyaccountmethod(){
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(),R.style.AlertDialogbox);
                LinearLayout linearLayout = new LinearLayout(getApplicationContext());
                linearLayout.setPadding(5, 5, 5, 5);
                builder.setTitle("Verify");
                builder.setMessage("Please enter your Login Email or Phone number.");
                EditText editText = new EditText(getApplicationContext());
                editText.setHint("Email or phone number");
                linearLayout.addView(editText);
                builder.setView(linearLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String Email = editText.getText().toString();
                        getpasswordmethod(Email);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                     dialog.dismiss();
                    }
                });
            }
        });
    }

    private void getpasswordmethod(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(),R.style.AlertDialogbox);
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        linearLayout.setPadding(5, 5, 5, 5);
        builder.setTitle("Verify password");
        builder.setMessage("Please enter your password.");
        EditText editText = new EditText(getApplicationContext());
        editText.setHint("password");
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = editText.getText().toString();
                deleteusermethod(email,password);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    private void deleteusermethod(String email, String password) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        AuthCredential credential = EmailAuthProvider
                .getCredential(email,password);


        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("userAccountdelete", "User account deleted.");
                                        }
                                    }
                                });

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentuserid != null){

            updateuserStatus("online");

        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (currentuserid != null){
            updateuserStatus("offline");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentuserid != null){
            updateuserStatus("offline");
        }
    }

    private void updateuserStatus(String state){
        String savecurrentDate,savecurrentTime;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        savecurrentTime = currenttime.format(calendar.getTime());
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        savecurrentDate = currentDate.format(calendar.getTime());
        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("Time",savecurrentTime);
        onlineState.put("Date",savecurrentDate);
        onlineState.put("State",state);
        String currentUser = mAuth.getCurrentUser().getUid();
        DatabaseReference rootref = FirebaseDatabase.getInstance().getReference();
        rootref.child("User").child(currentUser).child("userState").updateChildren(onlineState);
    }

    private void requestinfomethod() {
       request.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User").child(currentuserid);
               reference.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       String username = dataSnapshot.child("Username").getValue().toString();
                       String Email = dataSnapshot.child("Email").getValue().toString();
                       String name = dataSnapshot.child("name").getValue().toString();
                       String phone_no = dataSnapshot.child("Phone_no").getValue().toString();
                       String status = dataSnapshot.child("status").getValue().toString();
                       FileOutputStream fos = null;

                       try{
                           fos = openFileOutput(username+".txt",MODE_PRIVATE);
                           String data = "Username: "+username+"\n"+"Name:"+name+"\n"+"Email:"+Email+"\n"+"Phone no:"+phone_no+"\n"+"Status:"+status;
                           fos.write(data.getBytes());
                           fos.close();
                           //Toast.makeText(getApplicationContext(), getFilesDir().getAbsolutePath(),Toast.LENGTH_SHORT).show();
                       }catch (FileNotFoundException e){
                           e.printStackTrace();
                       }catch (IOException e){
                           e.printStackTrace();
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {

                   }
               });
           }
       });
    }

    private void privacymethod() {
        privacylinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),privacy.class);
                startActivity(intent);
            }
        });
    }

    private void intializzation() {
        stoolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(stoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account Setting");
        privacylinear = findViewById(R.id.Account);
        request = findViewById(R.id.Request);
        delete = findViewById(R.id.Delete_my_Account);
    }
}
