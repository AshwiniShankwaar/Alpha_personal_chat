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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
public class chat extends Fragment {
    private View chat;
    private RecyclerView chatlist;
    private DatabaseReference chatRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserid,profiledp;
    private ImageButton groupicon;

    public chat() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chat =  inflater.inflate(R.layout.fragment_chat, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserid = mAuth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contact").child(currentUserid);
        userRef = FirebaseDatabase.getInstance().getReference().child("User");
        Incialition();
        grooupbuttonmethod();
        return chat;
    }

    private void grooupbuttonmethod() {
        groupicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupintent = new Intent(getContext(),group.class);
                groupintent.putExtra("Txt","passed");
                startActivity(groupintent);
            }
        });
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
        FirebaseRecyclerOptions<contact> options = new FirebaseRecyclerOptions.Builder<contact>().setQuery(chatRef,contact.class).build();
        FirebaseRecyclerAdapter<contact,chatViewHolder> adapter = new FirebaseRecyclerAdapter<contact, chatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatViewHolder chatViewHolder, int i, @NonNull contact contact) {
                final String userIds = getRef(i).getKey();

                userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")){
                                profiledp = dataSnapshot.child("image").getValue().toString();

                                Picasso.get().load(profiledp).into(chatViewHolder.profileImg);
                            }
                            final String fullName = dataSnapshot.child("name").getValue().toString();
                            chatViewHolder.username.setText(fullName);
                            chatViewHolder.msg.setText("message here");
                            if(dataSnapshot.child("userState").hasChild("State")){
                                String state = dataSnapshot.child("userState").child("State").getValue().toString();
                                String date = dataSnapshot.child("userState").child("Date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("Time").getValue().toString();

                                if (state.equals("online")){
                                    chatViewHolder.useronlineStatus.setVisibility(View.VISIBLE);
                                }else{
                                    chatViewHolder.useronlineStatus.setVisibility(View.INVISIBLE);
                                }
                            }
                            chatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent personalChatIntent = new Intent(getContext(),personalChat.class);
                                    personalChatIntent.putExtra("userIds",userIds);
                                    personalChatIntent.putExtra("fullname",fullName);
                                    personalChatIntent.putExtra("image",profiledp);
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
            public chatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usersdisplaylayout,parent,false);
                return  new chatViewHolder(view);
            }
        };
        chatlist.setAdapter(adapter);
        adapter.startListening();
    }

    public static class chatViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImg,useronlineStatus;
        TextView username,msg;

        public chatViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImg = itemView.findViewById(R.id.userprofileImage);
            useronlineStatus = itemView.findViewById(R.id.onlineStatus);
            username = itemView.findViewById(R.id.username);
            msg = itemView.findViewById(R.id.status);
        }
    }

    private void Incialition() {
        chatlist = (RecyclerView) chat.findViewById(R.id.chatList);
        chatlist.setLayoutManager(new LinearLayoutManager(getContext()));
        groupicon = (ImageButton) chat.findViewById(R.id.fbutton);
    }


}
