package cjy.com.contrastcamera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity {

    protected int CURRENT_CAM = Util.USE_BACKGROUND_CAM;
    FrameLayout preview = null;
    ImageView mImageView;
    PhotoViewAttacher mAttacher;
    private Camera mCamera;
    private CameraPreview mPreview;
    //private BgView bgView;
    private FloatingActionButton captureButton;
    private FloatingActionButton frontbackButton;
    private int RESULT_LOAD_IMAGE = 1989;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = Util.getOutputMediaFile(Util.MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Logger.wtf("Error creating media file, check storage permissions: ");
                return;
            }


            try {


                Display display = getWindowManager().getDefaultDisplay();
                int rotation = 0;
                switch (display.getRotation()) {
                    case Surface.ROTATION_0: // This is display orientation
                        rotation = 90;
                        break;
                    case Surface.ROTATION_90:
                        rotation = 0;
                        break;
                    case Surface.ROTATION_180:
                        rotation = 270;
                        break;
                    case Surface.ROTATION_270:
                        rotation = 180;
                        break;
                }

                Bitmap bitmap = Util.Bytes2Bimap(data);
                bitmap = Util.rotate(bitmap, rotation);


                FileOutputStream fos = new FileOutputStream(pictureFile);


                fos.write(Util.Bitmap2Bytes(bitmap));
                fos.close();

                Snackbar.make(preview, "done!" + pictureFile.getAbsolutePath(), Snackbar.LENGTH_SHORT)
                        .setAction("Ok", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        })
                        .show();
                resumeCamera();

            } catch (FileNotFoundException e) {
                Logger.wtf("File not found: " + e.getMessage());
            } catch (IOException e) {
                Logger.wtf("Error accessing file: " + e.getMessage());
            }
        }
    };

    public Camera getCameraInstance(int screenWidth, int screenHeight) {
        Camera c = null;
        try {
            int camId = -1;
            if (CURRENT_CAM == Util.USE_BACKGROUND_CAM) {
                camId = findBackCamera();
            } else {
                if (CURRENT_CAM == Util.USE_FRONT_CAM) {
                    camId = findFrontCamera();
                }
            }

            c = Camera.open(camId); // attempt to get a Camera instance
            c.setDisplayOrientation(90);
            Camera.Parameters params = c.getParameters();
            //params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            params.setExposureCompensation(0);
            params.setPictureFormat(ImageFormat.JPEG);
            params.setJpegQuality(10);
            //params.setPreviewSize(screenWidth,screenHeight);
            //params.setPictureSize(screenWidth,screenHeight);
            //params.setRotation(90);
            c.setParameters(params);

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

        Logger.init(Util.TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preview = (FrameLayout) findViewById(R.id.camera_preview);

        // Create an instance of Camera
        mCamera = getCameraInstance(preview.getWidth(), preview.getHeight());
        //Camera.Parameters parameters = mCamera.getParameters();


        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);


        // TextView tv=(TextView) findViewById(R.id.relative_view).findViewById(R.id.textView);
        //tv.setText("fffffsdfsdfds");

        //preview.addView(mPreview);
        mImageView = new ImageView(this);

        preview.addView(mPreview, 0);
        preview.addView(mImageView, 1);
        // Set the Drawable displayed
        //Drawable bitmap = getResources().getDrawable(R.drawable.wallpaper);


        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        mAttacher = new PhotoViewAttacher(mImageView);

        captureButton = (FloatingActionButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );

        frontbackButton = (FloatingActionButton) findViewById(R.id.button_frontback);
        frontbackButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        if (CURRENT_CAM == Util.USE_BACKGROUND_CAM) {
                            CURRENT_CAM = Util.USE_FRONT_CAM;
                        } else {
                            if (CURRENT_CAM == Util.USE_FRONT_CAM) {
                                CURRENT_CAM = Util.USE_BACKGROUND_CAM;
                            }
                        }
                        //releaseCamera();
                        //preview.removeView(mPreview,0);
                        releaseCamera();
                        mCamera = getCameraInstance(preview.getWidth(), preview.getHeight());
                        //Camera.Parameters parameters = mCamera.getParameters();
                        // Create our Preview view and set it as the content of our activity.
                        mPreview = new CameraPreview(MainActivity.this, mCamera);
                        preview.removeViewAt(0);
                        preview.addView(mPreview, 0);
                    }
                }
        );
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
    protected void onResume() {
        super.onResume();
        resumeCamera();
    }

    private void resumeCamera() {

        if (mCamera == null) {
            mCamera = getCameraInstance(preview.getWidth(), preview.getHeight());
        } else {
            mCamera.startPreview();
        }

       /* if(mCamera!=null){
            mCamera.startPreview();
        }*/
        // mCamera = getCameraInstance(preview.getWidth(), preview.getHeight());

    }

    @Override
    protected void onPause() {
        super.onPause();
        //releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
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

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {


            if (data != null) {

                Uri dataUri = data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), dataUri);
                    bitmap = Util.compress(bitmap);
                    bitmap = Util.adjustOpacity(bitmap, 100);
                    mImageView.setImageBitmap(bitmap);
                    //mImageView.getBackground().setAlpha(100);
                    mAttacher.update();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private int findFrontCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                return camIdx;
            }
        }
        return -1;
    }

    private int findBackCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                return camIdx;
            }
        }
        return -1;
    }
}
