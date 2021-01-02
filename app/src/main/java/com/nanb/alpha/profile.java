package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class profile extends AppCompatActivity {
    private CircleImageView img;
    private EditText name, username, status, email,phone;
    private Button update;
    private FirebaseAuth mauth;
    private DatabaseReference rootref;
    private String currentUserid;
    private Toolbar mtoolbar;
    private  static  final int galleryPick = 1;
    private  StorageReference userprofileImage;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        incialition();
        updateuser();
         mauth = FirebaseAuth.getInstance();
         rootref = FirebaseDatabase.getInstance().getReference();
         currentUserid = mauth.getCurrentUser().getUid();
         userprofileImage = FirebaseStorage.getInstance().getReference().child("Profile image");
         petriveData();
         userprofileimage();
    }

    private void userprofileimage() {
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),galleryPick);

            }
        });
    }

    private void upadateimagetodatabase(String downloadUrl) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User").child(currentUserid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("image")){
                    reference.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(profile.this, "Image Updated, Successfully...", Toast.LENGTH_SHORT).show();
                            Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(img);
                        }
                    });
                }else{
                    Map imagemap = new HashMap<>();
                    imagemap.put("image",downloadUrl);
                    reference.updateChildren(imagemap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            Toast.makeText(profile.this, "Image Uploaded, Successfully...", Toast.LENGTH_SHORT).show();
                            Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(img);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(profile.this, databaseError.toString()+" Please report", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void petriveData() {
        rootref.child("User").child(currentUserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("Username") && (dataSnapshot.hasChild("image"))
               && (dataSnapshot.hasChild("status")) && (dataSnapshot.hasChild("Email")) && (dataSnapshot.hasChild("name")) )){
                   String profileUsername = dataSnapshot.child("Username").getValue().toString();
                   String profilestatus = dataSnapshot.child("status").getValue().toString();
                   String profilename = dataSnapshot.child("name").getValue().toString();
                   String profileEmail = dataSnapshot.child("Email").getValue().toString();
                   String profileImage = dataSnapshot.child("image").getValue().toString();
                   String profilephone = dataSnapshot.child("Phone_no").getValue().toString();
                  username.setText(profileUsername);
                  name.setText(profilename);
                  status.setText(profilestatus);
                  email.setText(profileEmail);
                   email.setEnabled(false);
                  phone.setText(profilephone);
                  Picasso.get().load(profileImage).into(img);
               }
               else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("Username")) ){
                   String profileUsername = dataSnapshot.child("Username").getValue().toString();
                   String profilestatus = dataSnapshot.child("status").getValue().toString();
                   String profilename = dataSnapshot.child("name").getValue().toString();
                   String profileEmail = dataSnapshot.child("Email").getValue().toString();
                   String profilephone = dataSnapshot.child("Phone_no").getValue().toString();
                   Picasso.get().load(R.mipmap.defaultdp).into(img);

                   username.setText(profileUsername);
                   name.setText(profilename);
                   status.setText(profilestatus);
                   email.setText(profileEmail);
                   email.setEnabled(false);
                   phone.setText(profilephone);
               }else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("Email"))){
                  // String profileUsername = dataSnapshot.child("Username").getValue().toString();
                   //String profilestatus = dataSnapshot.child("status").getValue().toString();
                   //String profilename = dataSnapshot.child("name").getValue().toString();
                   String profileEmail = dataSnapshot.child("Email").getValue().toString();
                   Picasso.get().load(R.mipmap.defaultdp).into(img);

                   username.setText("");
                   name.setText("");
                   status.setText("");
                   email.setText(profileEmail);
                   email.setEnabled(false);
                   phone.setText("");
               }
               else{
                   Toast.makeText(profile.this,"Please update your profile",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateuser() {
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullname = name.getText().toString();
                String Username = username.getText().toString();
                String statusInput = status.getText().toString();
                String emailid = email.getText().toString();
                String userphone = phone.getText().toString();
                if (TextUtils.isEmpty(fullname)){
                    Toast.makeText(profile.this,"Please enter your full name", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(Username)){
                    Toast.makeText(profile.this,"Please enter Username", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(statusInput)){
                    Toast.makeText(profile.this,"Please enter Status", Toast.LENGTH_SHORT).show();
                }if (TextUtils.isEmpty(emailid)) {
                    Toast.makeText(profile.this, "Please enter your Email id", Toast.LENGTH_SHORT).show();
                }
                 if (TextUtils.isEmpty(userphone)){
                    Toast.makeText(profile.this,"Please enter your Phonenumber", Toast.LENGTH_SHORT).show();
                }
                else {
                    HashMap<String, Object>ProfileMap = new HashMap<>();
                    //HashMap<String, Long>ProfilePhone = new HashMap<>();
                    ProfileMap.put("uid",currentUserid);
                    ProfileMap.put("name",fullname);
                    ProfileMap.put("Username",Username);
                    ProfileMap.put("Email",emailid);
                    ProfileMap.put("status",statusInput);
                    ProfileMap.put("Phone_no",userphone);
                    rootref.child("User").child(currentUserid).updateChildren(ProfileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(String.valueOf(username))
                                        .build();

                                mauth.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            sendusertomainactivity();
                                            Toast.makeText(profile.this,"Profile updated",Toast.LENGTH_SHORT).show();
                                            Log.d("profileupdate", "User profile updated.");
                                        }
                                    }
                                });

                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(profile.this,"Error: "+message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }


            }
        });
    }

    private void incialition() {
        img = (CircleImageView) findViewById(R.id.profile_image);
        name = (EditText) findViewById(R.id.nameInput);
        username = (EditText) findViewById(R.id.usernameInput);
        email = (EditText) findViewById(R.id.emailInput);
        status = (EditText) findViewById(R.id.stausInput);
        phone = (EditText) findViewById(R.id.phoneInput);
        update = (Button) findViewById(R.id.edit);
        mtoolbar = (Toolbar) findViewById(R.id.group_chat_bar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");

        loadingBar = new ProgressDialog(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==galleryPick && resultCode==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(profile.this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultUri = result.getUri();
                //Toast.makeText(this,resultUri.toString(),Toast.LENGTH_SHORT).show();
                StorageReference filepath = userprofileImage.child(currentUserid + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                      if(task.isSuccessful()){

                          final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
                          rootref.child("User").child(currentUserid).child("image")
                                  .setValue(downloadUrl)
                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task)
                                      {
                                          if (task.isSuccessful())
                                          {

                                              upadateimagetodatabase(downloadUrl);
                                              loadingBar.dismiss();

                                          }
                                          else
                                          {
                                              String message = task.getException().toString();
                                              Toast.makeText(profile.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                              loadingBar.dismiss();
                                          }
                                      }
                                  });

                      }else {
                          String errmsg = task.getException().toString();
                          Toast.makeText(profile.this,"Error:"+ errmsg,Toast.LENGTH_SHORT).show();
                          loadingBar.dismiss();
                      }
                    }
                });
            }
        }
    }



    private void sendusertomainactivity() {
        Intent Senduser = new Intent(profile.this, MainActivity.class);
        Senduser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Senduser);
        finish();
    }
}
