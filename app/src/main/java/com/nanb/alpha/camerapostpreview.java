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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class camerapostpreview extends AppCompatActivity {
    private ImageButton backbutton;
    private Button Nextbutton;
    private ImageView imageView,selectimageview;
    private String filepath,currentuser,savecurrentTime,savecurrentDate,captiontext;
    private CircleImageView profiledp;
    private EditText caption;
    private TextView tagpeople,tagcount;
    private FirebaseAuth mAuth;
    private DatabaseReference rootref;
    private FrameLayout tagframelayout;
    private ImageButton Tagback;
    private Button tagnext;
    private RecyclerView tagrecycle;
    private StorageTask uploadTask;
    private DatabaseReference contactRef,usersref;
    private Uri fileuri;
    private boolean isSelect = false;
    ArrayList<String> userids = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerapostpreview);

        filepath = getIntent().getExtras().get("fileAddress").toString();
        intialization();
        //Toast.makeText(getApplicationContext(),filepath,Toast.LENGTH_SHORT).show();
        fileuri = Uri.fromFile(new File(filepath));
        imageView.setImageURI(fileuri);

        rootref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser().getUid();

        contactRef = FirebaseDatabase.getInstance().getReference().child("Contact").child(currentuser);
        usersref = FirebaseDatabase.getInstance().getReference().child("User");
        backbuttonmethod();
        captiontext = catptionmethod();
        profilemethod();
        tagpeoplemethod();
        tagdonemethod();
        recyclerviewtagfriend();
        nextmethod();
        //Toast.makeText(getApplicationContext(),String.valueOf(userids.size()),Toast.LENGTH_SHORT).show();
    }

    private void nextmethod() {
        Nextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userids.isEmpty()){
                    //Toast.makeText(getApplicationContext(),"Arraylist is empty",Toast.LENGTH_SHORT).show();
                    StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Post");
                    DatabaseReference usermsgKeyRef = rootref.child("Post").push();
                    final String msgPushId = usermsgKeyRef.getKey();
                    final StorageReference filePath = imagereference.child(msgPushId+"."+"jpg");
                    uploadTask = filePath.putFile(fileuri);
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
                                uploadfiletoserver(myuri,currentuser,captiontext,msgPushId);
                            }
                        }
                    });
                }else{
                   StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Post");
                    DatabaseReference usermsgKeyRef = rootref.child("Post").push();
                    final String msgPushId = usermsgKeyRef.getKey();
                    final StorageReference filePath = imagereference.child(msgPushId+"."+"jpg");
                    uploadTask = filePath.putFile(fileuri);
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
                                uploadtoserver(myuri,currentuser,tagcounter,captiontext,msgPushId);
                            }
                        }
                    });
                    //Toast.makeText(getApplicationContext(),"Array has"+userids.size()+"tag people",Toast.LENGTH_SHORT).show();
                    //for(String usertagedid: userids){
                      //  Toast.makeText(getApplicationContext(),usertagedid,Toast.LENGTH_SHORT).show();
                    //}
                }

            }
        });
    }

    private void uploadfiletoserver(String myuri, String currentuser, String captiontext, String msgPushId) {
        Map postimage = new HashMap();
        postimage.put("PostAdmin",currentuser);
        postimage.put("type","image");
        postimage.put("url",myuri);
        postimage.put("caption",captiontext);
        postimage.put("date",savecurrentDate);
        postimage.put("time",savecurrentTime);
        rootref.child("Post").child(msgPushId).updateChildren(postimage).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Intent postintent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(postintent);
            }
        });
    }

    private void uploadtoserver(String myuri, String currentuser, final int tagcounter, String caption, final String msgpussid) {
        Map postimage = new HashMap();
        postimage.put("PostAdmin",currentuser);
        postimage.put("tag",tagcounter);
        postimage.put("type","image");
        postimage.put("url",myuri);
        postimage.put("caption",caption);
        postimage.put("date",savecurrentDate);
        postimage.put("time",savecurrentTime);
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

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser().getUid() != null){

            updateuserStatus("online");

        }
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
    private void recyclerviewtagfriend() {
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        tagrecycle.setLayoutManager(manager);
        mainrecyclermethod();
    }

    private void mainrecyclermethod() {
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<tagfriendmodelclass>()
                .setQuery(contactRef, tagfriendmodelclass.class)
                .build();
        FirebaseRecyclerAdapter<tagfriendmodelclass, tagfriend_viewHolder> adapter = new FirebaseRecyclerAdapter<tagfriendmodelclass, tagfriend_viewHolder>(options) {

            @NonNull
            @Override
            public tagfriend_viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tagfriendsdisplaylayout, parent, false);
                return new tagfriend_viewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final tagfriend_viewHolder tagfriend_viewHolder, int i, @NonNull final tagfriendmodelclass tagfriendmodelclass) {
                final String usersId = getRef(i).getKey();
                usersref.child(usersId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("image")) {
                            final String profileimage = dataSnapshot.child("image").getValue().toString();
                            final String profilename = dataSnapshot.child("name").getValue().toString();
                            String ustatus = dataSnapshot.child("status").getValue().toString();

                            tagfriend_viewHolder.userName.setText(profilename);
                            tagfriend_viewHolder.userstatus.setText(ustatus);
                            Picasso.get().load(profileimage).into(tagfriend_viewHolder.profilepic);

                            tagfriend_viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (isSelect == false){
                                        isSelect = true;
                                        tagfriend_viewHolder.selectimageview.setVisibility(View.VISIBLE);
                                        userids.add(usersId);
                                    }else{
                                        isSelect = false;
                                        tagfriend_viewHolder.selectimageview.setVisibility(View.INVISIBLE);
                                        userids.remove(usersId);
                                    }
                                }
                            });


                        }else{
                            final String profilename = dataSnapshot.child("name").getValue().toString();
                            String ustatus = dataSnapshot.child("status").getValue().toString();

                            tagfriend_viewHolder.userName.setText(profilename);
                            tagfriend_viewHolder.userstatus.setText(ustatus);
                            Picasso.get().load(R.mipmap.defaultdp).into(tagfriend_viewHolder.profilepic);

                            tagfriend_viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (isSelect == false){
                                        isSelect = true;
                                        tagfriend_viewHolder.selectimageview.setVisibility(View.VISIBLE);
                                        userids.add(usersId);
                                    }else{
                                        isSelect = false;
                                        tagfriend_viewHolder.selectimageview.setVisibility(View.INVISIBLE);
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

    private void tagdonemethod() {
        tagnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Nextbutton.setVisibility(View.VISIBLE);
                tagframelayout.setVisibility(View.INVISIBLE);
                String tagcountnumber = String.valueOf(userids.size());
                //Toast.makeText(getApplicationContext(),tagcountnumber,Toast.LENGTH_SHORT).show();
                tagcount.setText(tagcountnumber);
            }
        });
    }

    private void tagpeoplemethod() {
        tagpeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagframelayout.setVisibility(View.VISIBLE);
                Nextbutton.setVisibility(View.GONE);
            }
        });
    }

    private void profilemethod() {

         rootref.child("User").child(currentuser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("image").exists()){
                    String profileimage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(profileimage).into(profiledp);
                }else{
                    Picasso.get().load(R.mipmap.defaultdp).into(profiledp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String catptionmethod() {
        String captionstringtext = caption.getText().toString();
        return captionstringtext;
    }

    private void backbuttonmethod() {
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Nextbutton.setVisibility(View.VISIBLE);
                Intent mainIntent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(mainIntent);
            }
        });
    }


    private void intialization() {
        backbutton = (ImageButton) findViewById(R.id.backbutton);
        Nextbutton = (Button) findViewById(R.id.nextbutton);
        imageView = (ImageView) findViewById(R.id.imagepreview);
        caption = (EditText) findViewById(R.id.caption);
        tagpeople = (TextView) findViewById(R.id.tagfriends);
        profiledp = (CircleImageView) findViewById(R.id.profiledp);
        tagframelayout = (FrameLayout) findViewById(R.id.tagfriendframe);
        tagnext = (Button) findViewById(R.id.tagnextbutton);
        Tagback = (ImageButton) findViewById(R.id.tagbackbutton);
        tagrecycle = (RecyclerView) findViewById(R.id.tagfriendrecycleview);
        //selectimageview = (ImageView) findViewById(R.id.selectview);
        tagcount = (TextView) findViewById(R.id.tagcount);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        savecurrentTime = currenttime.format(calendar.getTime());
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        savecurrentDate = currentDate.format(calendar.getTime());
    }

}
    class tagfriend_viewHolder extends RecyclerView.ViewHolder{
        TextView userName, userstatus ;
        CircleImageView profilepic;
        ImageView selectimageview;
        public tagfriend_viewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.username);
            selectimageview = itemView.findViewById(R.id.selectview);
            userstatus = itemView.findViewById(R.id.status);
            profilepic = itemView.findViewById(R.id.userprofileImage);
        }
    }
