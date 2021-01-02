package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Contactus extends AppCompatActivity {
    private Toolbar stoolbar;
    private ImageView imageView,add;
    private EditText editText;
    private Button send;
    private Uri imagefile,imageuriaftercompress;
    private String myUrl = "",imagefileName;
    private FirebaseAuth mauth;
    private StorageTask uploadTask;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactus);

        initailization();
        addmethod();
        sendmessage();
    }

    private void sendmessage() {
        String messsage = editText.getText().toString();
       send.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(messsage.isEmpty()){
                   Toast.makeText(getApplicationContext(),"Please Explain your problem",Toast.LENGTH_SHORT).show();
               }else{
                   imagefilemessage(imagefile,imagefileName);
               }
           }
       });
    }

    private void addmethod() {
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imageIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(imageIntent,"Select Image"),438);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 438 && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            imagefile = data.getData();
            String filePath = this.getRealPathFromURI(imagefile);
            Long Filelength = this.getfilesize(filePath);
            if(checckfilesize(Filelength).equals("ok")) {
                String imagefilepath = compressImage(filePath);
                imageuriaftercompress = Uri.fromFile(new File(imagefilepath));
                //Toast.makeText(personalChat.this, imagefilepath, Toast.LENGTH_SHORT).show();
                imagefilepath = imageuriaftercompress.getLastPathSegment();
                Picasso.get().load(imageuriaftercompress).into(imageView);

            }
        }
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
    private void imagefilemessage(final Uri imagefile, final String filename) {
        StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Contact_Help");

        DatabaseReference usermsgKeyRef = FirebaseDatabase.getInstance().getReference().child("Conatct_Help").child(mauth.getCurrentUser().getUid()).push();

        final String msgPushId = usermsgKeyRef.getKey();
        final StorageReference filePath = imagereference.child(msgPushId+"."+"jpg");
        uploadTask = filePath.putFile(imagefile);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    myUrl = downloadUri.toString();

                    uploadfiletotheserver(myUrl,msgPushId);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                notificationbar(progress);
            }
        });
    }


    private  void uploadfiletotheserver(String myUrl,String msgpushid){
        Map msgpicbody = new HashMap();
        msgpicbody.put("Screensorts",myUrl);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Conatct_Help").child(mauth.getCurrentUser().getUid());
        reference.updateChildren(msgpicbody).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Toast.makeText(getApplicationContext(),"Your message is been recived we will responce as soon as possible.",Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void notificationbar(double progress){
        final int value = (int) progress;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Contact Us")
                .setContentText("Uploading request")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //.setSmallIcon(R.drawable.download);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                mBuilder.setProgress(100,value,false);
                if(value == 100){
                    mBuilder.setProgress(0,0,false)
                            .setContentText("Message send");
                }
                int id = 1;
                notificationManager.notify(id,mBuilder.build());
            }
        }).start();
    }
    public Long getfilesize(String filepath){
        File filedata = new File(filepath);
        Long fileLength = filedata.length();
        fileLength = fileLength/1024;
        return fileLength;
    }
    public String checckfilesize(Long Filelength){
        String checkstate;
        if(Filelength < 16000 ){
            checkstate = "ok";
        }else{
            checkstate = "file large than 16mb.";
        }
        return checkstate;
    }
    public String compressImage(String filePath) {

        String compressfilePath = filePath;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(compressfilePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {               imgRatio = maxHeight / actualHeight;                actualWidth = (int) (imgRatio * actualWidth);               actualHeight = (int) maxHeight;             } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(compressfilePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(compressfilePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename(filePath);
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    private String getFilename(String filePath) {
        File file = new File(filePath);
        String filename = file.getName();
        return filename;
    }


    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {
                MediaStore.Audio.Media.DATA
        };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
//        Toast.makeText(personalChat.this, Long.toString(cursor.getLong(sizeIndex)),Toast.LENGTH_SHORT).show();
        return cursor.getString(column_index);
    }
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      }       final float totalPixels = width * height;       final float totalReqPixelsCap = reqWidth * reqHeight * 2;       while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private void initailization() {
        stoolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(stoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Contact us");

        send = findViewById(R.id.button);
        imageView = findViewById(R.id.screenimageview);
        add = findViewById(R.id.add);
        editText = findViewById(R.id.edit);
    }
}
