package cz.cvut.fjfi.decin.mandalbrot2022;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private static final int WRITE_EXTERNAL_STORAGE = 0;
    MandelbrotView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.view);
    }


    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() != MotionEvent.ACTION_DOWN)
            return false;
        int x = (int)event.getX();
        int y = (int)event.getY();

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];

        Log.i("View location", String.format("%d, %d", left, top));

        y = y - top;
        x = x - left;

        Transformation t = new Transformation(view.mc, view.dc);

        PointF mpoint = t.toMathematical(new Point(x, y));
        float mx = mpoint.x;
        float my = mpoint.y;

        float halfwidth = view.mc.width() / 4.0f;
        float halfheight = view.mc.height() / 4.0f;

        RectF newmc = new RectF(mx - halfwidth, my - halfheight, mx + halfwidth, my + halfheight );
        view.setMc(newmc);
        return true;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.global:
                view.setScaleFactor(1.0f);
                view.setMc(new RectF(-2, 1, 1, -1));
                return true;
            case R.id.share:
                shareWithPermission();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareWithPermission() {
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
            share();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_STORAGE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                share();
            }
        }
    }

    private void share() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uriToImage = getImageUriByGallery();
        if (uriToImage == null)
            return;
        intent.putExtra(Intent.EXTRA_STREAM, uriToImage);
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));
    }


    private Uri getImageUriByGallery() {
        OutputStream output = null;
        RectF mc = view.getMc();
        String loc = String.format("%x", mc.hashCode());

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "mandelbrot " + loc);
        values.put(MediaStore.Images.Media.DESCRIPTION, "mandelbrot set");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            output = resolver.openOutputStream(uri);
        } catch (FileNotFoundException e) {
            Log.e("media uri", e.getMessage());
            return null;
        }

        if (view.bitmap == null) {
            return null;
        }
        view.bitmap.compress(Bitmap.CompressFormat.PNG, 60, output);

        try {
            output.flush(); //nadbytečné
            output.close();
        } catch (IOException e) {
            Log.e("file", "flush error");
            return null;
        }

        return uri;
    }

    private Uri getImageUri() {
        OutputStream output = null;
        RectF mc = view.getMc();
        String loc = String.format("%x", mc.hashCode());
        String filename = "mandelbrot_" + loc + ".png";
        File root = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        root.mkdirs();
        File file = new File(root, filename);
        try {
            output = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Log.e("file", e.getMessage());
            Toast.makeText(this, "Invalid path to image file", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (view.bitmap == null) {
            return null;
        }
        view.bitmap.compress(Bitmap.CompressFormat.PNG, 60, output);

        try {
            output.flush(); //nadbytečné
            output.close();
        } catch (IOException e) {
            Log.e("file", "flush error");
            return null;
        }
            return FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider", file);
        }
    }








