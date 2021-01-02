package com.nanb.alpha;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.Environment.getExternalStorageDirectory;

public class message_adapter extends RecyclerView.Adapter<message_adapter.MessageViewHolder> {
    private List<message> usermsgList;
    private FirebaseAuth mAuth;
    private DatabaseReference userref;
    public message_adapter(List<message> usermsgList){
        this.usermsgList = usermsgList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.coustommsglayout,parent,false);
        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        String msgsenderid = mAuth.getCurrentUser().getUid();
        final message Message = usermsgList.get(position);
        String fromuserid = Message.getFfrom();
        String touserid = Message.getTo();
        final String fromMessageType = Message.getType();

        userref = FirebaseDatabase.getInstance().getReference().child("User").child(fromuserid);
        userref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if (dataSnapshot.hasChild("image")){
                  String reciverimage = dataSnapshot.child("image").getValue().toString();
                  Picasso.get().load(reciverimage).into(holder.reciverProfileImage);
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.recivermsgtext.setVisibility(View.GONE);
        holder.reciverProfileImage.setVisibility(View.GONE);
        holder.sendermsgtext.setVisibility(View.GONE);
        holder.sendermsgImage.setVisibility(View.GONE);
        holder.recivermsgImage.setVisibility(View.GONE);
        holder.senderTime.setVisibility(View.GONE);
        holder.reciverTime.setVisibility(View.GONE);
        holder.senderimageTime.setVisibility(View.GONE);
        holder.reciverimageTime.setVisibility(View.GONE);
        holder.downloadButton.setVisibility(View.GONE);
        if (fromMessageType.equals("Text")){
           String Messagetime = Message.getTime();
           String Messagetext = Message.getMessage();
          textmsgmethod(msgsenderid,fromuserid,holder,Messagetime,Messagetext);

        }else if(fromMessageType.equals("image")){
            String Messagetime = Message.getTime();
            String Messagetext = Message.getMessage();
            String Messagename = Message.getname();
            String Messageid = Message.getMessageId();
            imagemmessagemethod(fromuserid,msgsenderid,holder,Messagetime,Messagetext,touserid,Messagename,Messageid);

        }else if(fromMessageType.equals("pdf")){

            String Messagetime = Message.getTime();
            String Messagetext = Message.getMessage();
            String Messagename = "Doc file ...";
            String Messageid = Message.getMessageId();
            String Messagetemp = Message.gettemp();
            pdfmessagemethod(fromuserid,msgsenderid,holder,Messagename,Messagetext,Messageid,Messagetime,touserid,Messagetemp);

        }else if(fromMessageType.equals("video")){
            String Messagetime = Message.getTime();
            String Messagetext = Message.getMessage();
            String Messagename = Message.getname();
            String Messageid = Message.getMessageId();
            String Messagetemp = Message.gettemp();
            videomethod(fromuserid,msgsenderid,holder,Messagename,Messagetext,Messageid,Messagetime,touserid,Messagetemp);
        }
        else if(fromMessageType.equals("audio")){
            String Messagetime = Message.getTime();
            String Messagetext = Message.getMessage();
            String Messagename = Message.getname();
            String Messageid = Message.getMessageId();
            audiomethod(fromuserid,msgsenderid,holder,Messagename,Messagetext,Messageid,Messagetime,touserid);
        }
      if(fromuserid.equals(msgsenderid)){
          holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
              @Override
              public boolean onLongClick(View view) {
                      if (usermsgList.get(position).getType().equals("pdf")
                              || usermsgList.get(position).getType().equals("video")
                              || usermsgList.get(position).getType().equals("audio")
                              || usermsgList.get(position).getType().equals("image")){
                          CharSequence options[] = new CharSequence[]{
                                  "Delete for me",
                                  "Download and view",
                                  "Delete for Everyone",
                                  "Cancel"
                          };
                          AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                          builder.setTitle("Delete message");
                          builder.setItems(options, new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which) {
                               if(position == 0){
                                   deletesentmesssage(position,holder);
                               }else if(position == 1){

                               }
                               else if(position == 2){
                                   deletemesssageforeveryone(position,holder);

                               }else if(position == 3){


                               }
                              }
                          });
                          builder.show();
                      }
                      else if (usermsgList.get(position).getType().equals("Text")){
                          CharSequence options[] = new CharSequence[]{
                                  "Delete for me",
                                  "Delete for Everyone",
                                  "Cancel"

                          };
                          AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                          builder.setTitle("Delete message");
                          builder.setItems(options, new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which) {
                                  if(position == 0){
                                      deletesentmesssage(position,holder);
                                  }else if(position == 1){
                                      deletemesssageforeveryone(position,holder);
                                  }
                                  else if(position == 2){

                                  }
                              }
                          });
                          builder.show();
                      }


                  return true;
              }
          });
      }else{
              holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                  @Override
                  public boolean onLongClick(View view) {
                      if (usermsgList.get(position).getType().equals("pdf")
                              || usermsgList.get(position).getType().equals("video")
                              || usermsgList.get(position).getType().equals("audio")
                              || usermsgList.get(position).getType().equals("image")){
                          CharSequence options[] = new CharSequence[]{
                                  "Delete for me",
                                  "Download and view",
                                  "Delete for Everyone",
                                  "Cancel"
                          };
                          AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                          builder.setTitle("Delete message");
                          builder.setItems(options, new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which) {
                                  if(position == 0){
                                      deleterecivemesssage(position,holder);
                                  }else if(position == 1){

                                  }
                                  else if(position == 2){
                                      deletemesssageforeveryone(position,holder);
                                  }else if(position == 3){

                                  }
                              }
                          });
                          builder.show();
                      }
                      else if (usermsgList.get(position).getType().equals("Text")){
                          CharSequence options[] = new CharSequence[]{
                                  "Delete for me",
                                  "Delete for Everyone",
                                  "Cancel"
                          };
                          AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                          builder.setTitle("Delete message");
                          builder.setItems(options, new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which) {
                                  if(position == 0){
                                      deleterecivemesssage(position,holder);
                                  }else if(position == 1){
                                      deletemesssageforeveryone(position,holder);
                                  }
                                  else if(position == 2){

                                  }
                              }
                          });
                          builder.show();
                      }

                      return false;
                  }
              });

      }
    }




    private void deletesentmesssage(final int position, final MessageViewHolder messageHolder){
        DatabaseReference rootdataref = FirebaseDatabase.getInstance().getReference();
        rootdataref.child("Message")
                .child(usermsgList.get(position).getFfrom())
                .child(usermsgList.get(position).getTo())
                .child(usermsgList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              if (task.isSuccessful()){
                  Toast.makeText(messageHolder.itemView.getContext(),"Message is been deleted",Toast.LENGTH_SHORT).show();
              }else{
                  Toast.makeText(messageHolder.itemView.getContext(),"Error while deleting messsage",Toast.LENGTH_SHORT).show();

              }
            }
        });
    }
    private void deleterecivemesssage(final int position, final MessageViewHolder messageHolder){
        DatabaseReference rootdataref = FirebaseDatabase.getInstance().getReference();
        rootdataref.child("Message")
                .child(usermsgList.get(position).getTo())
                .child(usermsgList.get(position).getFfrom())
                .child(usermsgList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(messageHolder.itemView.getContext(),"Message is been deleted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(messageHolder.itemView.getContext(),"Error while deleting messsage",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void deletemesssageforeveryone(final int position, final MessageViewHolder messageHolder){
        final DatabaseReference rootdataref = FirebaseDatabase.getInstance().getReference();
        rootdataref.child("Message")
                .child(usermsgList.get(position).getFfrom())
                .child(usermsgList.get(position).getTo())
                .child(usermsgList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootdataref.child("Message")
                            .child(usermsgList.get(position).getTo())
                            .child(usermsgList.get(position).getFfrom())
                            .child(usermsgList.get(position).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(messageHolder.itemView.getContext(),"Message is been deleted",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(messageHolder.itemView.getContext(),"Error while deleting messsage",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return usermsgList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView sendermsgtext,recivermsgtext,reciverTime,senderTime,reciverimageTime,senderimageTime,senderpdfinfo,reciverpdfinfo,senderaudiotext,reciveraudiotext;
        public CircleImageView reciverProfileImage;
        public ImageView recivermsgImage,sendermsgImage;
        public ImageButton downloadButton,audiodownload,senerplay;
        public MediaPlayer player;
        public boolean isplaying = false;

        public MessageViewHolder(@NonNull View itemView){
            super(itemView);
            sendermsgtext = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            recivermsgtext = (TextView) itemView.findViewById(R.id.receiver_message_text);
            reciverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            recivermsgImage = (ImageView) itemView.findViewById(R.id.reciverImage);
            sendermsgImage = (ImageView) itemView.findViewById(R.id.senderImage);
            reciverTime = (TextView) itemView.findViewById(R.id.time);
            senderTime = (TextView) itemView.findViewById(R.id.sendertime);
            reciverimageTime = (TextView) itemView.findViewById(R.id.reciverImagetime);
            senderimageTime = (TextView) itemView.findViewById(R.id.senderimagetime);
            downloadButton = (ImageButton) itemView.findViewById(R.id.download);
            senderpdfinfo = (TextView) itemView.findViewById(R.id.senderpdfinfo);
            reciverpdfinfo = (TextView) itemView.findViewById(R.id.reciverpdfinfo);
            senderaudiotext = (TextView) itemView.findViewById(R.id.senderaudio);
            reciveraudiotext = (TextView) itemView.findViewById(R.id.receiveraudio);
            audiodownload = (ImageButton) itemView.findViewById(R.id.audiodownload);
            senerplay = (ImageButton) itemView.findViewById(R.id.senderaudiodownload);

        }
    }
    public void textmsgmethod(String msgsenderid, String fromuserid,MessageViewHolder holder,String Messagetime, String MessageText){
        if (fromuserid.equals(msgsenderid)){
            holder.sendermsgtext.setVisibility(View.VISIBLE);
            //holder.sendermsgtext.setBackgroundResource(R.drawable.sendermsglayout);
            holder.sendermsgtext.setText(MessageText);
            holder.senderTime.setText(Messagetime);
            holder.senderTime.setVisibility(View.VISIBLE);

        }else{

            holder.recivermsgtext.setVisibility(View.VISIBLE);
            holder.reciverProfileImage.setVisibility(View.VISIBLE);
            // holder.recivermsgtext.setBackgroundResource(R.drawable.recivermsglayout);
            holder.recivermsgtext.setText(MessageText);
            holder.reciverTime.setVisibility(View.VISIBLE);
            holder.reciverTime.setText(Messagetime);
        }
    }
    public void imagemmessagemethod(String msgsenderid, String fromuserid,final MessageViewHolder holder, String Messagetime, final String MessageText, final String touserid,final String Messagename,final String Messageid){
        if (fromuserid.equals(msgsenderid)){
            holder.sendermsgImage.setVisibility(View.VISIBLE);
            //holder.sendermsgtext.setBackgroundResource(R.drawable.sendermsglayout);
            Picasso.get().load(MessageText).into(holder.sendermsgImage);
            holder.senderimageTime.setText(Messagetime);
            holder.senderimageTime.setVisibility(View.VISIBLE);
            holder.sendermsgImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   File imageFile = new File(Environment.getExternalStorageDirectory().getPath()+"/Alpha/Photos/"+Messagename);
                   if (imageFile.exists()){
                       Intent imagereview = new Intent(holder.itemView.getContext().getApplicationContext(), imagereview.class);
                       imagereview.putExtra("userIds",touserid);
                       imagereview.putExtra("imagestring",Messagename);
                       imagereview.putExtra("imageid",Messageid);
                       imagereview.putExtra("imagetype","sender");
                       holder.itemView.getContext().startActivities(new Intent[]{imagereview});
                   }else{
                       Toast.makeText(holder.itemView.getContext().getApplicationContext(),"The file doesn't exists anymore ",Toast.LENGTH_SHORT).show();
                   }

                }
            });

        }else{
            File filechecker = new File(getExternalStorageDirectory()+"/Alpha/Photos/reciver/"+Messageid+".jpg");
            if(filechecker.exists()){
                holder.recivermsgImage.setVisibility(View.VISIBLE);
                Picasso.get().load(filechecker).into(holder.recivermsgImage);
                holder.reciverTime.setVisibility(View.VISIBLE);
                holder.reciverTime.setText(Messagetime);
                holder.recivermsgImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent imagereview = new Intent(holder.itemView.getContext().getApplicationContext(), imagereview.class);
                        imagereview.putExtra("userIds",touserid);
                        imagereview.putExtra("imageid",Messageid);
                        imagereview.putExtra("imagestring",Messagename);
                        imagereview.putExtra("imagetype","reciver");
                        holder.itemView.getContext().startActivities(new Intent[]{imagereview});
                    }
                });
            }else {
                holder.downloadButton.setVisibility(View.VISIBLE);
                holder.reciverTime.setVisibility(View.VISIBLE);
                holder.reciverTime.setText(Messagetime);
                holder.downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StorageReference downloadfilepath = FirebaseStorage.getInstance().getReference().child("image").child(Messageid + ".jpg");
                        Toast.makeText(holder.itemView.getContext(), downloadfilepath.toString(), Toast.LENGTH_SHORT).show();
                        File fileNameOnDevice = new File(Environment.getExternalStorageDirectory().getPath()+"/Alpha/Photos/recived/");
                        Toast.makeText(holder.itemView.getContext(), fileNameOnDevice.toString(), Toast.LENGTH_SHORT).show();
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(Messageid));
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setTitle("Download");
                        request.setDescription("Downloading image..");
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir("/Alpha/Photos/recived", Messageid + ".jpg");
                        DownloadManager manager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                    }
                });
            }
        }

    }

    private void pdfmessagemethod(String fromuserid, String msgsenderid, final MessageViewHolder holder, String messagename, String messagetext, final String messageid, String messagetime, final String touserid, String messagetemp) {
        if (fromuserid.equals(msgsenderid)){
            holder.senderpdfinfo.setVisibility(View.VISIBLE);
            holder.sendermsgImage.setVisibility(View.VISIBLE);
            holder.senderpdfinfo.setText(messagename);
            //holder.sendermsgtext.setBackgroundResource(R.drawable.sendermsglayout);
            Picasso.get().load(messagetemp).into(holder.sendermsgImage);
            holder.senderimageTime.setText(messagetime);
            holder.senderimageTime.setVisibility(View.VISIBLE);
            holder.sendermsgImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File imageFile = new File(Environment.getExternalStorageDirectory().getPath()+"/Alpha/pdf/Send/"+messageid+".pdf");
                    if (imageFile.exists()){
                        Intent imagereview = new Intent(holder.itemView.getContext().getApplicationContext(), pdfViewer.class);
                        imagereview.putExtra("userIds",touserid);
                        imagereview.putExtra("pdfid",messageid);
                        holder.itemView.getContext().startActivities(new Intent[]{imagereview});

                    }else{
                        Toast.makeText(holder.itemView.getContext().getApplicationContext(),"The file doesn't exists anymore ",Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }else{
            File filechecker = new File(getExternalStorageDirectory()+"/Alpha/pdf/reciver/"+messageid+".pdf");
            if(filechecker.exists()){
                holder.reciverpdfinfo.setVisibility(View.VISIBLE);
                holder.reciverpdfinfo.setText(messagename);
                holder.recivermsgImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messagetemp).into(holder.recivermsgImage);
                holder.reciverTime.setVisibility(View.VISIBLE);
                holder.reciverTime.setText(messagetime);
                holder.recivermsgImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent imagereview = new Intent(holder.itemView.getContext().getApplicationContext(), pdfViewer.class);
                        imagereview.putExtra("userIds",touserid);
                        imagereview.putExtra("pdfid",messageid);
                        holder.itemView.getContext().startActivities(new Intent[]{imagereview});
                    }
                });
            }else {
                holder.downloadButton.setVisibility(View.VISIBLE);
                holder.reciverTime.setVisibility(View.VISIBLE);
                holder.reciverpdfinfo.setVisibility(View.VISIBLE);
                holder.reciverpdfinfo.setText(messagename);
                holder.reciverTime.setText(messagetime);
                holder.downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StorageReference downloadfilepath = FirebaseStorage.getInstance().getReference().child("Doc").child(messageid + ".pdf");
                        Toast.makeText(holder.itemView.getContext(), downloadfilepath.toString(), Toast.LENGTH_SHORT).show();
                        File fileNameOnDevice = new File(Environment.getExternalStorageDirectory().getPath()+"/Alpha/pdf/recived/");
                        Toast.makeText(holder.itemView.getContext(), fileNameOnDevice.toString(), Toast.LENGTH_SHORT).show();
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(messageid));
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setTitle("Download");
                        request.setDescription("Downloading image..");
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir("/Alpha/pdf/recived", messageid + ".pdf");
                        DownloadManager manager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                    }
                });
            }

        }
    }
    private void videomethod(String fromuserid, String msgsenderid, final MessageViewHolder holder, String messagename, String messagetext, final String messageid, String messagetime, final String touserid, String messagetemp) {
        if (fromuserid.equals(msgsenderid)){

            holder.sendermsgImage.setVisibility(View.VISIBLE);
            Picasso.get().load(messagetemp).into(holder.sendermsgImage);
            holder.senderimageTime.setText(messagetime);
            holder.senderimageTime.setVisibility(View.VISIBLE);
            holder.sendermsgImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File imageFile = new File(Environment.getExternalStorageDirectory().getPath()+"/Alpha/Video/Send/"+messageid+".mp4");
                    if (imageFile.exists()){
                        Intent imagereview = new Intent(holder.itemView.getContext().getApplicationContext(), videoViewer.class);
                        imagereview.putExtra("userIds",touserid);
                        imagereview.putExtra("videoid",messageid);
                        holder.itemView.getContext().startActivities(new Intent[]{imagereview});

                    }else{
                        Toast.makeText(holder.itemView.getContext().getApplicationContext(),"The file doesn't exists anymore ",Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }else{
            File filechecker = new File(getExternalStorageDirectory()+"/Alpha/Video/reciver/"+messageid+".mp4");
            if(filechecker.exists()){
                holder.recivermsgImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messagetemp).into(holder.recivermsgImage);
                holder.reciverTime.setVisibility(View.VISIBLE);
                holder.reciverTime.setText(messagetime);
                holder.recivermsgImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent imagereview = new Intent(holder.itemView.getContext().getApplicationContext(), videoViewer.class);
                        imagereview.putExtra("userIds",touserid);
                        imagereview.putExtra("pdfid",messageid);
                        holder.itemView.getContext().startActivities(new Intent[]{imagereview});
                    }
                });
            }else {
                holder.downloadButton.setVisibility(View.VISIBLE);
                holder.reciverTime.setVisibility(View.VISIBLE);
                holder.reciverTime.setText(messagetime);
                holder.downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StorageReference downloadfilepath = FirebaseStorage.getInstance().getReference().child("Video").child(messageid + ".mp4");
                        Toast.makeText(holder.itemView.getContext(), downloadfilepath.toString(), Toast.LENGTH_SHORT).show();
                        File fileNameOnDevice = new File(Environment.getExternalStorageDirectory().getPath()+"/Alpha/Video/recived/");
                        Toast.makeText(holder.itemView.getContext(), fileNameOnDevice.toString(), Toast.LENGTH_SHORT).show();
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(messageid));
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setTitle("Download");
                        request.setDescription("Downloading image..");
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir("/Alpha/Video/recived", messageid + ".mp4");
                        DownloadManager manager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                    }
                });
            }

        }
    }
    private void audiomethod(String fromuserid, String msgsenderid, final MessageViewHolder holder, String messagename, String messagetext, final String messageid, String messagetime, String touserid){

        if (fromuserid.equals(msgsenderid)){
            holder.senderaudiotext.setVisibility(View.VISIBLE);
            holder.senderaudiotext.setText(messagetext);
            holder.senderTime.setText(messagetime);
            holder.senderTime.setVisibility(View.VISIBLE);
            holder.senerplay.setVisibility(View.VISIBLE);
           if(!holder.isplaying){
               holder.senerplay.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       String path = Environment.getExternalStorageDirectory().getPath()+"/Alpha//Audio/Send/"+messageid+".mp3";

                       if(holder.player == null){
                           try{
                               holder.player.setDataSource(path);
                               holder.player.prepare();
                               holder.player.start();
                               holder.senerplay.setImageResource(R.mipmap.stop);
                               holder.isplaying = true;
                           }catch (Exception e){
                               Toast.makeText(holder.itemView.getContext().getApplicationContext(),"Exception of type : " + e.toString(),Toast.LENGTH_SHORT).show();
                               e.printStackTrace();
                           }
                       }
                   }
               });
           }else{
               holder.senerplay.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       if (holder.player != null){
                           try{
                               holder.player.release();
                               holder.isplaying = false;
                               holder.senerplay.setImageResource(R.mipmap.play);
                           }catch (Exception e){
                               Toast.makeText(holder.itemView.getContext().getApplicationContext(),"Exception of type : " + e.toString(),Toast.LENGTH_SHORT).show();
                               e.printStackTrace();
                           }
                       }
                   }
               });
           }


        }else {
            File filechecker = new File(getExternalStorageDirectory()+"/Alpha/Audio/reciver/"+messageid+".mp3");
            if(filechecker.exists()){
                holder.reciveraudiotext.setText(messagetext);
                holder.reciveraudiotext.setVisibility(View.VISIBLE);
                holder.reciverTime.setVisibility(View.VISIBLE);
                holder.reciverTime.setText(messagetime);
                holder.audiodownload.setVisibility(View.VISIBLE);
                holder.audiodownload.setImageResource(R.mipmap.play);
               if(!holder.isplaying){
                   holder.audiodownload.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           String path = Environment.getExternalStorageDirectory().getPath()+"/Alpha//Audio/Send/"+messageid+".mp3";

                           if(holder.player == null){
                               try{
                                   holder.player.setDataSource(path);
                                   holder.player.prepare();
                                   holder.player.start();
                                   holder.senerplay.setImageResource(R.mipmap.stop);
                                   holder.isplaying = true;
                               }catch (Exception e){
                                   Toast.makeText(holder.itemView.getContext().getApplicationContext(),"Exception of type : " + e.toString(),Toast.LENGTH_SHORT).show();
                                   e.printStackTrace();
                               }
                           }
                       }
                   });
               }else{
                   holder.audiodownload.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           if (holder.player != null){
                               try{
                                   holder.player.release();
                                   holder.isplaying = false;
                                   holder.senerplay.setImageResource(R.mipmap.play);
                               }catch (Exception e){
                                   Toast.makeText(holder.itemView.getContext().getApplicationContext(),"Exception of type : " + e.toString(),Toast.LENGTH_SHORT).show();
                                   e.printStackTrace();
                               }
                           }
                       }
                   });
               }
            }else{
                holder.reciveraudiotext.setText(messagetext);
                holder.reciveraudiotext.setVisibility(View.VISIBLE);
                holder.reciverTime.setVisibility(View.VISIBLE);
                holder.reciverTime.setText(messagetime);
                holder.audiodownload.setVisibility(View.VISIBLE);
                holder.audiodownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StorageReference downloadfilepath = FirebaseStorage.getInstance().getReference().child("Audio").child(messageid + ".mp3");
                        Toast.makeText(holder.itemView.getContext(), downloadfilepath.toString(), Toast.LENGTH_SHORT).show();
                        File fileNameOnDevice = new File(Environment.getExternalStorageDirectory().getPath()+"/Alpha/Audio/recived/");
                        Toast.makeText(holder.itemView.getContext(), fileNameOnDevice.toString(), Toast.LENGTH_SHORT).show();
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(messageid));
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setTitle("Download");
                        request.setDescription("Downloading image..");
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir("/Alpha/Audio/recived", messageid + ".mp3");
                        DownloadManager manager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                    }
                });
            }

        }
    }

}
