package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class profileView extends AppCompatActivity {
    private String userid,currentState,currentuserId;
    private CircleImageView profileDp,onlieStatus;
    private TextView fullname, ustatus, uUsername,bio;
    private DatabaseReference userRef;
    private Button ufollow,rCancel,rAdd,sendmsg;
    private Toolbar mtoolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference rootref,Contactref,notificationref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        userid = getIntent().getExtras().get("userId").toString();
        mAuth = FirebaseAuth.getInstance();
        currentuserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("User");
        rootref = FirebaseDatabase.getInstance().getReference();
        Contactref = FirebaseDatabase.getInstance().getReference().child("Contact");
        notificationref = FirebaseDatabase.getInstance().getReference().child("Notification");
        initialition();
        retriveUserInfo();
    }

    private void retriveUserInfo() {
        userRef.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && (dataSnapshot.hasChild("image"))){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String username = dataSnapshot.child("Username").getValue().toString();
                    String userfullname = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();
                 //   String useremail = dataSnapshot.child("Email").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.im).into(profileDp);
                    fullname.setText(userfullname);
                    ustatus.setText(userstatus);
                    uUsername.setText(username);
                    userRef.child(userid).child("userState").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                          if(dataSnapshot.exists()){
                              String state = dataSnapshot.child("State").getValue().toString();
                              if(state.equals("Online")){
                                  onlieStatus.setVisibility(View.VISIBLE);
                              }
                          }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    manageChat();
                }else{
                    String username = dataSnapshot.child("Username").getValue().toString();
                    String userfullname = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();
            //        String useremail = dataSnapshot.child("Email").getValue().toString();

                    fullname.setText(userfullname);
                    ustatus.setText(userstatus);
                    uUsername.setText(username);
          //          uEmail.setText(useremail);

                    manageChat();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void manageChat() {
        rootref.child("Friend").child(currentuserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userid)){
                    String request_type = dataSnapshot.child(userid).child("Request_type").getValue().toString();
                    if (request_type.equals("Sent")){
                        currentState = "request_sent";
                        ufollow.setText("Cancel");
                    }else if(request_type.equals("Recived")){
                        currentState = "request_Recived";
                        ufollow.setVisibility(View.INVISIBLE);
                        ufollow.setEnabled(false);
                        rAdd.setVisibility(View.VISIBLE);
                        rAdd.setEnabled(true);
                        rAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                addToContact();
                            }
                        });
                        rCancel.setVisibility(View.VISIBLE);
                        rCancel.setEnabled(true);
                        rCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancleRequest();

                            }
                        });
                    }
                }
                else{
                    Contactref.child(currentuserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(userid)){
                                currentState = "Friends";
                                ufollow.setEnabled(false);
                                ufollow.setVisibility(View.INVISIBLE);
                                rCancel.setEnabled(true);
                                rCancel.setVisibility(View.VISIBLE);
                                rCancel.setText("Remove");
                                rCancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        remove_Contact();
                                    }
                                });
                                rAdd.setEnabled(false);
                                rAdd.setVisibility(View.VISIBLE);
                                rAdd.setText("Added");
                                sendmsg.setEnabled(true);
                                sendmsg.setVisibility(View.VISIBLE);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (!currentuserId.equals(userid)){
            ufollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ufollow.setEnabled(false);
                    if (currentState.equals("New")){
                        chatrequest();
                    }
                    if (currentState.equals("request_sent")){
                        cancleRequest();
                    }
                    if (currentState.equals("request_Recived")){
                        rAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                addToContact();
                            }
                        });
                    }
                    if (currentState.equals("Friends")){
                        rCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                remove_Contact();
                            }
                        });
                    }
                }
            });
        }else {
            ufollow.setVisibility(View.INVISIBLE);
        }
    }

    private void remove_Contact() {
        Contactref.child(currentuserId).child(userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Contactref.child(userid).child(currentuserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                ufollow.setEnabled(true);
                                currentState = "New";
                                ufollow.setText("Follow");
                                rAdd.setEnabled(false);
                                rAdd.setVisibility(View.INVISIBLE);
                                rCancel.setEnabled(false);
                                rCancel.setVisibility(View.INVISIBLE);
                                ufollow.setVisibility(View.VISIBLE);
                                sendmsg.setEnabled(false);
                                sendmsg.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void addToContact() {
        Contactref.child(currentuserId).child(userid).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              if (task.isSuccessful()){
                  Contactref.child(userid).child(currentuserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull Task<Void> task) {
                          if (task.isSuccessful()){
                              rootref.child("Friend").child(userid).child(currentuserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                  @Override
                                  public void onComplete(@NonNull Task<Void> task) {
                                     if (task.isSuccessful()){
                                         rootref.child("Friend").child(currentuserId).child(userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                               ufollow.setEnabled(false);
                                               ufollow.setVisibility(View.INVISIBLE);
                                                 rCancel.setEnabled(true);
                                                 rCancel.setVisibility(View.VISIBLE);
                                                 rCancel.setText("Remove");
                                                 rAdd.setEnabled(false);
                                                 rAdd.setVisibility(View.VISIBLE);
                                                 rAdd.setText("Added");
                                                 sendmsg.setEnabled(true);
                                                 sendmsg.setVisibility(View.VISIBLE);
                                                 currentState = "Friends";

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

    private void cancleRequest() {
        rootref.child("Friend").child(currentuserId).child(userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               if (task.isSuccessful()){
                   rootref.child("Friend").child(userid).child(currentuserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()){
                               ufollow.setEnabled(true);
                               currentState = "New";
                               ufollow.setText("Follow");
                               rAdd.setEnabled(false);
                               rAdd.setVisibility(View.INVISIBLE);
                               rCancel.setEnabled(false);
                               rCancel.setVisibility(View.INVISIBLE);
                               ufollow.setVisibility(View.VISIBLE);

                           }
                       }
                   });
               }
            }
        });
    }

    private void chatrequest() {
        rootref.child("Friend").child(currentuserId).child(userid).child("Request_type").setValue("Sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
             if (task.isSuccessful()) {
                 rootref.child("Friend").child(userid).child(currentuserId).child("Request_type").setValue("Recived").addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {

                         HashMap<String,String> chatnotification = new HashMap<>();
                         chatnotification.put("from",currentuserId);
                         chatnotification.put("type","Request");
                         notificationref.child(userid).push().setValue(chatnotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                             @Override
                             public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    ufollow.setEnabled(true);
                                    currentState = "request_sent";
                                    ufollow.setText("Cancel");
                                }
                             }
                         });
                     }
                 });
             }
            }
        });

    }

    private void initialition() {
        profileDp = (CircleImageView) findViewById(R.id.profileImage);
        fullname = (TextView) findViewById(R.id.name);
        ustatus = (TextView) findViewById(R.id.status);
        uUsername = (TextView) findViewById(R.id.username);
        ufollow = (Button) findViewById(R.id.follow);
        rAdd = (Button) findViewById(R.id.Add);
        rCancel = (Button) findViewById(R.id.Cancel);
        sendmsg = (Button) findViewById(R.id.msg);
        onlieStatus = (CircleImageView) findViewById(R.id.OnlineStatus);
        currentState = "New";
        mtoolbar = (Toolbar) findViewById(R.id.maintoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");
    }
}
