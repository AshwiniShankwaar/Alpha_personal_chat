package com.nanb.alpha;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactView extends Fragment {
    private View contact_view;
    private RecyclerView myContactList;
    private DatabaseReference contactRef,usersref;
    private FirebaseAuth mAuth;
    private String currentuserId;



    public ContactView() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contact_view =  inflater.inflate(R.layout.fragment_contact_view, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentuserId = mAuth.getCurrentUser().getUid();
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contact").child(currentuserId);
        usersref = FirebaseDatabase.getInstance().getReference().child("User");
        initialization();
        return contact_view;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){
            updateuserStatus("offline");
        }
    }


    @Override
    public void onDestroy() {
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
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){

            updateuserStatus("online");

        }
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<contact>()
                .setQuery(contactRef,contact.class)
                .build();

        FirebaseRecyclerAdapter<contact,contacts_viewHolder> adapter = new FirebaseRecyclerAdapter<contact, contacts_viewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final contacts_viewHolder contacts_viewHolder, int i, @NonNull contact contact) {
             final String usersId = getRef(i).getKey();
              usersref.child(usersId).addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("image")){
                        final String profileimage = dataSnapshot.child("image").getValue().toString();
                        final String profilename = dataSnapshot.child("name").getValue().toString();
                        String ustatus = dataSnapshot.child("status").getValue().toString();

                        contacts_viewHolder.userName.setText(profilename);
                        contacts_viewHolder.userstatus.setText(ustatus);
                        Picasso.get().load(profileimage).into(contacts_viewHolder.profilepic);
                        usersref.child(usersId).child("userState").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String state = dataSnapshot.child("State").getValue().toString();
                                if (state.equals("Online")){
                                    contacts_viewHolder.onlineStatus.setVisibility(View.VISIBLE);
                                }else{
                                    contacts_viewHolder.onlineStatus.setVisibility(View.INVISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        contacts_viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent personalChatIntent = new Intent(getContext(),personalChat.class);
                                personalChatIntent.putExtra("userIds",usersId);
                                personalChatIntent.putExtra("fullname",profilename);
                                personalChatIntent.putExtra("image",profileimage);
                                startActivity(personalChatIntent);
                            }
                        });

                    }else{
                        final String profilename = dataSnapshot.child("name").getValue().toString();
                        String ustatus = dataSnapshot.child("status").getValue().toString();

                        contacts_viewHolder.userName.setText(profilename);
                        contacts_viewHolder.userstatus.setText(ustatus);

                        contacts_viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent personalChatIntent = new Intent(getContext(),personalChat.class);
                                personalChatIntent.putExtra("userIds",usersId);
                                personalChatIntent.putExtra("fullname",profilename);
                                //personalChatIntent.putExtra("image",profileimage);
                                startActivity(personalChatIntent);
                            }
                        });
                    }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {

                  }
              });
            }

            @NonNull
            @Override
            public contacts_viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usersdisplaylayout, parent, false);
                contacts_viewHolder viewHolder = new contacts_viewHolder(view);
                return viewHolder;
            }
        };
        myContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class contacts_viewHolder extends RecyclerView.ViewHolder{
        TextView userName, userstatus ;
        CircleImageView profilepic,onlineStatus;
        public contacts_viewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.status);
            profilepic = itemView.findViewById(R.id.userprofileImage);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);
        }
    }

    private void initialization() {
        myContactList = (RecyclerView) contact_view.findViewById(R.id.contact_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));


    }

}
