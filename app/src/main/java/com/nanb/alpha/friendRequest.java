package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class friendRequest extends AppCompatActivity {
    private RecyclerView myContactList;
    private DatabaseReference frequestref,userRef,Contactref;
    private FirebaseAuth mAuth;
    private String currentuserId;
    private Toolbar mtoolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        mAuth = FirebaseAuth.getInstance();
        currentuserId = mAuth.getCurrentUser().getUid();
        frequestref = FirebaseDatabase.getInstance().getReference().child("Friend");
        userRef = FirebaseDatabase.getInstance().getReference().child("User");
        Contactref = FirebaseDatabase.getInstance().getReference().child("Contact");
        Initialization();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){
            updateuserStatus("offline");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){
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
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootref = FirebaseDatabase.getInstance().getReference();
        rootref.child("User").child(currentUser).child("userState").updateChildren(onlineState);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){

            updateuserStatus("online");

        }
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<contact>().setQuery(frequestref.child(currentuserId),contact.class).build();

        FirebaseRecyclerAdapter<contact,frequest_ViewHolder> adapter = new FirebaseRecyclerAdapter<contact, frequest_ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final frequest_ViewHolder frequest_viewHolder, int i, @NonNull contact contact) {
                frequest_viewHolder.requestlayout.setVisibility(View.VISIBLE);

                final String listuser_id = getRef(i).getKey();
                DatabaseReference getTyperef = getRef(i).child("Request_type").getRef();
                getTyperef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      if (dataSnapshot.exists()){
                          String type = dataSnapshot.getValue().toString();
                          if (type.equals("Recived")){
                              userRef.child(listuser_id).addValueEventListener(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild("image")){
                                        String profileimage = dataSnapshot.child("image").getValue().toString();
                                        String profilename = dataSnapshot.child("name").getValue().toString();
                                        String ustatus = dataSnapshot.child("status").getValue().toString();

                                        frequest_viewHolder.userName.setText(profilename);
                                        frequest_viewHolder.userstatus.setText(ustatus);
                                        Picasso.get().load(profileimage).into(frequest_viewHolder.profilepic);

                                        frequest_viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent profileViewIntent = new Intent(friendRequest.this,profileView.class);
                                                profileViewIntent.putExtra("userId",listuser_id);
                                                startActivity(profileViewIntent);
                                            }
                                        });

                                        frequest_viewHolder.uadd.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                addcontact(listuser_id );
                                            }
                                        });
                                        frequest_viewHolder.uremove.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                removeRequest(listuser_id);
                                            }
                                        });

                                    }else{
                                        String profilename = dataSnapshot.child("name").getValue().toString();
                                        String ustatus = dataSnapshot.child("status").getValue().toString();

                                        frequest_viewHolder.userName.setText(profilename);
                                        frequest_viewHolder.userstatus.setText(ustatus);

                                        frequest_viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent profileViewIntent = new Intent(friendRequest.this,profileView.class);
                                                profileViewIntent.putExtra("userId",listuser_id);
                                                startActivity(profileViewIntent);
                                            }
                                        });
                                        frequest_viewHolder.uadd.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                addcontact(listuser_id );
                                            }
                                        });
                                        frequest_viewHolder.uremove.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                removeRequest(listuser_id);
                                            }
                                        });

                                    }

                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError databaseError) {

                                  }
                              });
                          }
                      }else{
                          Toast.makeText(friendRequest.this,"No friend request to show",Toast.LENGTH_SHORT).show();
                      }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public frequest_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usersdisplaylayout, parent, false);
                frequest_ViewHolder viewHolder = new frequest_ViewHolder(view);
                return viewHolder;
            }
        };
        myContactList.setAdapter(adapter);
        adapter.startListening();
    }

    private void removeRequest(final String listuser_id) {
        frequestref.child(currentuserId).child(listuser_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    frequestref.child(listuser_id).child(currentuserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                              Toast.makeText(friendRequest.this,"Friend Request removed",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }
        });
    }

    private void addcontact(final String listuser_id) {
        Contactref.child(currentuserId).child(listuser_id ).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Contactref.child(listuser_id ).child(currentuserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                frequestref.child(listuser_id ).child(currentuserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            frequestref.child(currentuserId).child(listuser_id ).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                   Toast.makeText(friendRequest.this,"New contact Added",Toast.LENGTH_SHORT).show();

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

    public static class frequest_ViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userstatus ;
        CircleImageView profilepic;
        Button uadd,uremove;
        LinearLayout requestlayout;
        public frequest_ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.status);
            profilepic = itemView.findViewById(R.id.userprofileImage);
            uadd = itemView.findViewById(R.id.faccept);
            uremove = itemView.findViewById(R.id.fremove);
            requestlayout = itemView.findViewById(R.id.requestlayout);
        }
    }

    private void Initialization() {
    myContactList = (RecyclerView) findViewById(R.id.frequestlist);
    myContactList.setLayoutManager(new LinearLayoutManager(this));
    mtoolbar = (Toolbar) findViewById(R.id.mainbar);
    setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Friend Request");

    }
}
