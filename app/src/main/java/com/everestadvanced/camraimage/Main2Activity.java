package com.everestadvanced.camraimage;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Main2Activity extends Activity {

    GridView gv1;
    GridViewAdapter madapter;
    private List<String> listOfImagesPath;

    public Button btnCaptureimg;
    private Uri imageToUploadUri;
    private static final int CAMERA_PHOTO = 111;
    Bitmap reducedSizeBitmap;

    public static final String GridViewDemo_ImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Ganesh/";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public boolean result;
    public int REQUEST_ID_MULTIPLE_PERMISSIONS = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        gv1=(GridView)findViewById(R.id.gridView1);


        //check user permission granted or not granted
        GetPermissionDetails();

        btnCaptureimg=(Button) findViewById(R.id.btncapture);
        btnCaptureimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent chooserIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = new File(Environment.getExternalStorageDirectory(), "POST_IMAGE.jpg");
                chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                imageToUploadUri = Uri.fromFile(f);
                startActivityForResult(chooserIntent, CAMERA_PHOTO);

            }
        });

        listOfImagesPath = null;
        listOfImagesPath = RetriveCapturedImagePath();
        if(listOfImagesPath!=null)
        {
            madapter=new GridViewAdapter(listOfImagesPath,Main2Activity.this);
            gv1.setAdapter(madapter);
        }


    }

    /////////////////////////////////// Capture image  Result/////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_PHOTO && resultCode == Activity.RESULT_OK)
        {
            if(imageToUploadUri != null)
            {
                Uri selectedImage = imageToUploadUri;
                getContentResolver().notifyChange(selectedImage, null);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap yourBitmap=getBitmap(imageToUploadUri.getPath());
                reducedSizeBitmap = Bitmap.createScaledBitmap(yourBitmap,(int)(yourBitmap.getWidth()*0.4), (int)(yourBitmap.getHeight()*0.4), true);

                if(reducedSizeBitmap != null)
                {
                    String imgcurTime = dateFormat.format(new Date());
                    File imageDirectory = new File(GridViewDemo_ImagePath);
                    imageDirectory.mkdirs();
                    String _path = GridViewDemo_ImagePath + imgcurTime+".jpg";

                    try
                    {
                        FileOutputStream out = new FileOutputStream(_path);
                        yourBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.close();
                    }
                    catch (FileNotFoundException e)
                    {
                        e.getMessage();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    listOfImagesPath = null;
                    listOfImagesPath = RetriveCapturedImagePath();

                    if(listOfImagesPath!=null)
                    {
                        madapter=new GridViewAdapter(listOfImagesPath,Main2Activity.this);
                        gv1.setAdapter(madapter);
                    }
                }
                else
                {
                    Toast.makeText(this,"Error while capturing Image",Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                Toast.makeText(this,"Error while capturing Image",Toast.LENGTH_LONG).show();
            }
        }
    }

    private Bitmap getBitmap(String path) {

        Uri uri = Uri.fromFile(new File(path));
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();


            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap b = null;
            in = getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
                Log.d("", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                        (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

            Log.d("", "bitmap size - width: " + b.getWidth() + ", height: " +
                    b.getHeight());
            return b;
        } catch (IOException e) {
            Log.e("", e.getMessage(), e);
            return null;
        }
    }

    private List<String> RetriveCapturedImagePath() {

        List<String> tFileList = new ArrayList<String>();
        File f = new File(GridViewDemo_ImagePath);
        if (f.exists()) {
            File[] files=f.listFiles();
            Arrays.sort(files);

            for(int i=0; i<files.length; i++){
                File file = files[i];
                if(file.isDirectory())
                    continue;
                tFileList.add(file.getPath());
            }
        }
        return tFileList;
    }


    ///////////////////////////////// Run time permissions //////////////////////////////////////

    public void  GetPermissionDetails() {

        //Check manual permission if andorid version >6.0 runtime permission
        result=CheckPermissionsGranted();

        if(result==false)
        {
            //call for runtime permission
            requestPermission();
        }
    }

    //Check all permission are granted ro not granted
    public boolean CheckPermissionsGranted() {

        //this code for multiple permission to check like location and phone state
        int writestorgePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readstoraePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if(writestorgePermission==0 && readstoraePermission==0 && cameraPermission==0)
        {
            //permission is granted
            return true;
        }
        else
        {
            //permission not granted
            return false;
        }
    }

    //Requesting permission
    private void requestPermission() {

        try
        {
            //this code for multiple permission to check like location and phone state
            int writestorgePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readstoraePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            List<String> listPermissionsNeeded = new ArrayList<>();

            if (writestorgePermission != PackageManager.PERMISSION_GRANTED)
            {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (readstoraePermission != PackageManager.PERMISSION_GRANTED)
            {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (cameraPermission != PackageManager.PERMISSION_GRANTED)
            {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }


            if (!listPermissionsNeeded.isEmpty())
            {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }

        }
        catch (Exception er)
        {
            Toast.makeText(getApplicationContext(),"Permissions Not Access Try Again !!!",Toast.LENGTH_LONG).show();
        }
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS)
        {

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //permission is granted
            }
            else
            {
                //permission is dented for user
                Toast.makeText(getApplicationContext(),"Oops Permissions Is Denied !!!",Toast.LENGTH_LONG).show();
            }
        }
    }

}
