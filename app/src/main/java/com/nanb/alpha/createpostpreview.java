package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class createpostpreview extends AppCompatActivity {

    private ImageView postpreview;
    private Button Doenbutton;
    private ImageButton cancelbutton,tagbutton;
    private String postimage,currentuserid,currenttime,currentdate;
    private FirebaseAuth mAuth;
    private DatabaseReference rootref,contactRef,usersref;
    private StorageTask uploadTask;
    private Uri pathuri;
    private FrameLayout tagframe;
    private ImageButton Tagback;
    private Button tagnext;
    private RecyclerView tagrecycle;
    private boolean isSelect = false;
    ArrayList<String> userids = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createpostpreview);

        mAuth = FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();
        rootref = FirebaseDatabase.getInstance().getReference();
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contact").child(currentuserid);
        usersref = FirebaseDatabase.getInstance().getReference().child("User");
        SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm");
        currenttime = sdf.format(new Date());
        SimpleDateFormat sdformat = new SimpleDateFormat(" yyyy.MM.dd");
        currentdate = sdformat.format(new Date());
        postimage = getIntent().getExtras().get("Path").toString();
        intialization();
        pathuri = Uri.fromFile(new File(postimage));
        Picasso.get().load(pathuri).into(postpreview);

       doenmethod();
       tagbuttonmethod();
        tagdonemethod();
        recyclerviewtagfriend();
        tagbackmethod();
        cancelmethod();

    }

    private void cancelmethod() {
        cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(postimage);
                if(file.exists()){
                    file.delete();
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void tagbackmethod() {
       Tagback.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Doenbutton.setVisibility(View.VISIBLE);
               tagframe.setVisibility(View.INVISIBLE);
           }
       });
    }

    private void recyclerviewtagfriend() {
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        tagrecycle.setLayoutManager(manager);
        mainrecycle();

    }

    private void mainrecycle() {
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<tagfriendmodelclass>()
                .setQuery(contactRef, tagfriendmodelclass.class)
                .build();
        FirebaseRecyclerAdapter<tagfriendmodelclass,tagfriendViewholder> adapter = new FirebaseRecyclerAdapter<tagfriendmodelclass, tagfriendViewholder>(options) {
            @NonNull
            @Override
            public tagfriendViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tagfriendsdisplaylayout, parent, false);
                return new tagfriendViewholder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final tagfriendViewholder tagfriendViewholder, int i, @NonNull final tagfriendmodelclass tagfriendmodelclass) {
                final String usersId = getRef(i).getKey();
                usersref.child(usersId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     if(dataSnapshot.hasChild("image")){
                         final String profileimage = dataSnapshot.child("image").getValue().toString();
                         final String profilename = dataSnapshot.child("name").getValue().toString();
                         String ustatus = dataSnapshot.child("status").getValue().toString();

                         tagfriendViewholder.userName.setText(profilename);
                         tagfriendViewholder.userstatus.setText(ustatus);
                         Picasso.get().load(profileimage).into(tagfriendViewholder.profilepic);

                         tagfriendViewholder.itemView.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 if(isSelect == false){
                                     isSelect = true;
                                     tagfriendViewholder.selectimageview.setVisibility(View.VISIBLE);
                                     userids.add(usersId);
                                 }else{
                                     isSelect = false;
                                     tagfriendViewholder.selectimageview.setVisibility(View.INVISIBLE);
                                     userids.remove(usersId);
                                 }
                             }
                         });
                     }else{
                         final String profilename = dataSnapshot.child("name").getValue().toString();
                         String ustatus = dataSnapshot.child("status").getValue().toString();

                         tagfriendViewholder.userName.setText(profilename);
                         tagfriendViewholder.userstatus.setText(ustatus);
                         Picasso.get().load(R.mipmap.defaultdp).into(tagfriendViewholder.profilepic);

                         tagfriendViewholder.itemView.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 if(isSelect == false){
                                     isSelect = true;
                                     tagfriendViewholder.selectimageview.setVisibility(View.VISIBLE);
                                     userids.add(usersId);
                                 }else{
                                     isSelect = false;
                                     tagfriendViewholder.selectimageview.setVisibility(View.INVISIBLE);
                                     userids.remove(usersId);
                                 }
                             }
                         });
                     }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        tagrecycle.setAdapter(adapter);
        adapter.startListening();
    }

    private void tagbuttonmethod() {
        tagbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagframe.setVisibility(View.VISIBLE);
                Doenbutton.setVisibility(View.GONE);
            }
        });
    }

    private void doenmethod() {
        Doenbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userids.isEmpty()){
                    StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Post");
                    DatabaseReference textpostref = rootref.child("Post").push();
                    final String msgPushId = textpostref.getKey();
                    final StorageReference filePath = imagereference.child(msgPushId+"."+"jpg");
                    uploadTask = filePath.putFile(pathuri);
                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isSuccessful()){
                                throw task.getException();
                            }
                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = (Uri) task.getResult();
                                String myuri = downloadUri.toString();
                                //int tagcounter = userids.size();
                                uploadtotheservernotag(myuri,currentuserid,msgPushId);
                            }
                        }
                    });
                }else{
                    StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Post");
                    DatabaseReference textpostref = rootref.child("Post").push();
                    final String msgPushId = textpostref.getKey();
                    final StorageReference filePath = imagereference.child(msgPushId+"."+"jpg");
                    uploadTask = filePath.putFile(pathuri);
                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isSuccessful()){
                                throw task.getException();
                            }
                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = (Uri) task.getResult();
                                String myuri = downloadUri.toString();
                                int tagcounter = userids.size();
                                uploadtotheserverwithtag(myuri,currentuserid,tagcounter,msgPushId);
                            }
                        }
                    });
                }
            }
        });
    }

    private void uploadtotheserverwithtag(String myuri, String currentuser, final int tagcounter, final String msgpussid) {
        Map postimage = new HashMap();
        postimage.put("PostAdmin",currentuser);
        postimage.put("tag",tagcounter);
        postimage.put("type","image");
        postimage.put("url",myuri);
        postimage.put("date",currentdate);
        postimage.put("time",currenttime);
        rootref.child("Post").child(msgpussid).updateChildren(postimage).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                rootref.child("Tag friends").child(msgpussid).push();
                for (String usertagedid: userids){
                    String usertagids = usertagedid;
                    Map tagids = new HashMap();
                    tagids.put("user id",usertagids);
                    rootref.child("Tag friends").child(msgpussid).updateChildren(tagids).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            Intent postintent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(postintent);
                        }
                    });
                }
            }
        });
    }

    private void uploadtotheservernotag(String myuri, String currentuser, String msgPushId) {
        Map postimage = new HashMap();
        postimage.put("PostAdmin",currentuser);
        postimage.put("type","image");
        postimage.put("url",myuri);
        postimage.put("date",currentdate);
        postimage.put("time",currenttime);
        rootref.child("Post").child(msgPushId).updateChildren(postimage).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Intent postintent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(postintent);
            }
        });

    }
    private void tagdonemethod() {
        tagnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Doenbutton.setVisibility(View.VISIBLE);
                tagframe.setVisibility(View.INVISIBLE);

            }
        });
    }
    private void intialization() {
        postpreview = findViewById(R.id.postcreatepreview);
        Doenbutton = findViewById(R.id.doen);
        cancelbutton = findViewById(R.id.closepostpreview);
        tagbutton = findViewById(R.id.tagbutton);
        tagframe = findViewById(R.id.tagfriendframe);
        tagnext =  findViewById(R.id.tagnextbutton);
        Tagback = findViewById(R.id.tagbackbutton);
        tagrecycle = findViewById(R.id.tagfriendrecycleview);
    }
}
class tagfriendViewholder extends RecyclerView.ViewHolder{

    TextView userName, userstatus ;
    CircleImageView profilepic;
    ImageView selectimageview;
    public tagfriendViewholder(@NonNull View itemView) {
        super(itemView);
        userName = itemView.findViewById(R.id.username);
        selectimageview = itemView.findViewById(R.id.selectview);
        userstatus = itemView.findViewById(R.id.status);
        profilepic = itemView.findViewById(R.id.userprofileImage);
    }
}