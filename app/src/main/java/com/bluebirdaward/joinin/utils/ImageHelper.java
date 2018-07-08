package com.bluebirdaward.joinin.utils;

import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.bluebirdaward.joinin.R;

/**
 * Created by Minh on 4/21/2016.
 */
public class ImageHelper {
    private AppCompatActivity activity;
    private String mCurrentPhotoPath;

    public ImageHelper(AppCompatActivity activity){
        this.activity = activity;
    }

    public Intent getTakePictureIntent() {
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.setAction(Intent.ACTION_PICK);
        String pickTitle = activity.getString(R.string.Image_Pick);
        Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
        try {

            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(createImageFile()));
            chooserIntent.putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    new Intent[]{takePhotoIntent}
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chooserIntent;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_cache_" + new Date().getTime() / 1000;
        //File storageDir = mContext.getFilesDir();

        ContextWrapper cw = new ContextWrapper(activity);

        //File storageDir = cw.getDir("image", Context.MODE_PRIVATE);
        File storageDir;
        storageDir = Environment.getExternalStorageDirectory();
        if (storageDir == null)
            storageDir = new File("/mnt/image");

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public String getPhotoPath(Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = activity.getContentResolver().query(
                selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String tmpPhotoPath = cursor.getString(columnIndex);
        cursor.close();
        return tmpPhotoPath;
    }

    public Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public String getmCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public void setmCurrentPhotoPath(String mCurrentPhotoPath) {
        this.mCurrentPhotoPath = mCurrentPhotoPath;
    }
}
