package com.nanb.alpha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class imageViewer extends AppCompatActivity {
    private ImageButton send;
    private TextView retry;
    private ImageView imageView;
    private String intentimagepath,reciveruserid,CurrentUserId,savecurrenttime,savecurrentdate,myurl="",fullName,checker;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageTask uploadTask;
    private Uri imagefile;
    private String imageaftercompress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        intentimagepath = getIntent().getExtras().get("imagepath").toString();
        reciveruserid = getIntent().getExtras().get("userIds").toString();
        fullName = getIntent().getExtras().get("fullname").toString();

       // Toast.makeText(imageViewer.this,imageaftercompress,Toast.LENGTH_SHORT).show();
        //Toast.makeText(imageViewer.this,intentimagepath,Toast.LENGTH_SHORT).show();

        intialization();
        imageViewmethod();
        sendimagemethod();
        retryimagemethod();
    }

    private void retryimagemethod() {
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    File deleteoriginalfile = new File(intentimagepath);
                    if(deleteoriginalfile.exists()){
                        Boolean delete = deleteoriginalfile.delete();
                        if(delete){
                           // Toast.makeText(imageViewer.this,"Image is been deleted",Toast.LENGTH_SHORT).show();
                            camera();
                        }else{
                            camera();
                            Toast.makeText(imageViewer.this,"Error: Capture Image not deleted",Toast.LENGTH_SHORT).show();
                        }

                    }


            }
        });
    }

    private void camera(){
        boolean data = checkCameraHardware(imageViewer.this);
        if (data == true){
            checker = "imageCapture";
            //Toast.makeText(personalChat.this,"Camera exits",Toast.LENGTH_SHORT).show();
            cameraopen(checker);
        }else{
            Toast.makeText(imageViewer.this,"Camera doesn't exits",Toast.LENGTH_SHORT).show();
        }
    }
    private void cameraopen(String checker) {
        Camera c = null;
        if(c == null) {
            try {
                Intent cameraIntent = new Intent(this,cameraintent.class);
                cameraIntent.putExtra("Checker",checker);
                cameraIntent.putExtra("userIds",reciveruserid);
                cameraIntent.putExtra("fullname",fullName);
                startActivity(cameraIntent);
                // attempt to get a Camera instance
            } catch (Exception e) {
                // Camera is not available (in use or does not exist)
                c.release();
                c = null;
                Toast.makeText(imageViewer.this, "Error:" + e, Toast.LENGTH_SHORT).show();
            }
        }else{
            c.release();
            c = null;
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void sendimagemethod() {
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageaftercompress = compressImage(intentimagepath);
                imagefile = Uri.fromFile(new File(imageaftercompress));
                uploadfile(imagefile);
            }
        });
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
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "Alpha/Photos");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

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

    private void uploadfile(Uri imagefile) {
        StorageReference imagereference = FirebaseStorage.getInstance().getReference().child("Image");
        final String msgSenderref = "Message/" + CurrentUserId + "/" + reciveruserid;
        final String msgReciverref = "Message/" + reciveruserid + "/" + CurrentUserId;
        DatabaseReference usermsgKeyRef = rootRef.child("Message").child(CurrentUserId).child(reciveruserid).push();
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
                    myurl = downloadUri.toString();

                    uploadfiletotheserver(myurl,msgPushId,msgSenderref,msgReciverref);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(imageViewer.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                //notificationbar(progress,checker);
            }
        });
    }

    private  void uploadfiletotheserver(String myUrl, final String msgPushId, final String msgSenderref, final String msgReciverref){
        Map msgpicbody = new HashMap();
        msgpicbody.put("message",myUrl);
        msgpicbody.put("name",imagefile.getLastPathSegment());
        msgpicbody.put("type","image");
        msgpicbody.put("ffrom",CurrentUserId);
        msgpicbody.put("to",reciveruserid);
        msgpicbody.put("date", savecurrentdate);
        msgpicbody.put("time",savecurrenttime);
        msgpicbody.put("messageId",msgPushId);
        Map msgBodyDetail = new HashMap();
        msgBodyDetail.put(msgSenderref + "/" +msgPushId,msgpicbody);
        msgBodyDetail.put(msgReciverref + "/" +msgPushId,msgpicbody);

        rootRef.updateChildren(msgBodyDetail).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    close_the_Activity();
                }else{
                    close_the_Activity();
                    Toast.makeText(imageViewer.this,"error",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void close_the_Activity() {
        Intent personalChatIntent = new Intent(imageViewer.this,personalChat.class);
        personalChatIntent.putExtra("userIds",reciveruserid);
        personalChatIntent.putExtra("fullname",fullName);
        startActivity(personalChatIntent);
    }
    private void imageViewmethod() {
        File imagepath = new File(intentimagepath);
        if (imagepath.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imagepath.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
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
    private void intialization() {
        send = (ImageButton) findViewById(R.id.send);
        retry = (TextView) findViewById(R.id.retry);
        imageView = (ImageView) findViewById(R.id.imageviewer);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        savecurrenttime = currenttime.format(calendar.getTime());
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        savecurrentdate = currentDate.format(calendar.getTime());
    }
}
