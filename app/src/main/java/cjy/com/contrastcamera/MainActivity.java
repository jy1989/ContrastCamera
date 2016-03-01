package cjy.com.contrastcamera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity {
    String TAG = "cjydebug";
    FrameLayout preview = null;
    ImageView mImageView;
    PhotoViewAttacher mAttacher;
    private Camera mCamera;
    private CameraPreview mPreview;
    private BgView bgView;
    private int RESULT_LOAD_IMAGE = 1989;

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        // this device has a camera
// no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);

        preview = (FrameLayout) findViewById(R.id.camera_preview);
        // TextView tv=(TextView) findViewById(R.id.relative_view).findViewById(R.id.textView);
        //tv.setText("fffffsdfsdfds");

        //preview.addView(mPreview);
        mImageView = new ImageView(this);

        preview.addView(mPreview);
        preview.addView(mImageView);
        // Set the Drawable displayed
        //Drawable bitmap = getResources().getDrawable(R.drawable.wallpaper);


        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        mAttacher = new PhotoViewAttacher(mImageView);

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.load_background) {
            Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(choosePictureIntent, RESULT_LOAD_IMAGE);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    protected Bitmap compress(Bitmap image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, out);
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        int be = 2;
        newOpts.inSampleSize = be;
        ByteArrayInputStream isBm = new ByteArrayInputStream(out.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    private Bitmap adjustOpacity(Bitmap bitmap, int opacity) {
        Bitmap mutableBitmap = bitmap.isMutable()
                ? bitmap
                : bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        int colour = (opacity & 0xFF) << 24;
        canvas.drawColor(colour, PorterDuff.Mode.DST_IN);
        return mutableBitmap;
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            Toast.makeText(this, "choice", Toast.LENGTH_SHORT).show();
            Log.v(TAG, "choice");
            if (data != null) {

                Uri dataUri = data.getData();
                //Bitmap bitmap = null;
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), dataUri);
                    bitmap = compress(bitmap);
                    bitmap = adjustOpacity(bitmap, 100);
                    mImageView.setImageBitmap(bitmap);
                    //mImageView.getBackground().setAlpha(100);
                    mAttacher.update();
                    /*if(bgView!=null){
                        preview.removeView(bgView);
                    }
                    bgView = new BgView(this,bitmap);
                    preview.addView(bgView);
*/
                    Log.e(TAG, "setbg");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}
