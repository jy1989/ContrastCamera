package cjy.com.contrastcamera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_MIN_WIDTH = 1500;
    private static final int CAMERA_MAX_WIDTH = 2500;
    private static Bitmap bgBitmap = null;
    protected int CURRENT_CAM = Util.USE_BACKGROUND_CAM;
    AspectRatioLayout preview = null;
    ImageView mImageView;
    PhotoViewAttacher mAttacher;
    private Camera mCamera;
    private CameraPreview mPreview;
    //private BgView bgView;
    private FloatingActionButton captureButton;
    private FloatingActionButton frontbackButton;
    private int RESULT_LOAD_IMAGE = 1989;
    private boolean isMerger = false;
    //private Camera.Parameters parameters = null;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Snackbar.make(preview, "gening!!", Snackbar.LENGTH_LONG).show();

            File pictureFile = Util.getOutputMediaFile(Util.MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Logger.wtf("Error creating media file, check storage permissions: ");
                return;
            }


            try {

                /*
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
                */
                int rotation = 90;
                if (CURRENT_CAM == Util.USE_FRONT_CAM) {
                    rotation = 270;
                }
                //Logger.e(rotation+" "+CURRENT_CAM+" "+Util.USE_FRONT_CAM);
                Bitmap bitmap = Util.Bytes2Bimap(data);
                bitmap = Util.rotate(bitmap, rotation);
                if (isMerger) {
                    bitmap = Util.toConformBitmap(bgBitmap, bitmap);
                }

                FileOutputStream fos = new FileOutputStream(pictureFile);


                fos.write(Util.Bitmap2Bytes(bitmap));
                fos.close();

                Snackbar.make(preview, pictureFile.getAbsolutePath(), Snackbar.LENGTH_SHORT)
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

    private Map getSize(Camera.Parameters parameters) {
        // Cambio la resolución entre 2 y 3 mpx
        Map sizeMap = new HashMap();
        //Camera.Parameters parameters = mCamera.getParameters();
        final List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        //int max = 0, min = 0;
        int width = 0;
        int height = 0;
        for (Camera.Size size : sizes) {

            // 2mpx = 1920x1080 (16:9)
            // 1.9mpx = 1600x1200 (4:3)
            // 3.1mpx = 2048x1536 (4:3)
            if (size.width >= CAMERA_MIN_WIDTH && size.width <= CAMERA_MAX_WIDTH) {
                sizeMap.put("width", size.width);
                sizeMap.put("height", size.height);
                width = size.width;
                height = size.height;
                //width = size.width;
                //height = size.height;
                break;
            }
        }
        final List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        //int max = 0, min = 0;
        for (Camera.Size size : previewSizes) {

            /**
             * El preview debe tener el mismo ratio
             */
            Logger.e("presize: " + size.width + "," + size.height + ", preview: " + width + "," + height + " " + ((float) size.width / size.height) + "," + ((float) width / height));
            // Logger.e(sizeMap.toString());
            if ((float) size.width / size.height == (float) width / height) {
                sizeMap.put("previewWidth", size.width);
                sizeMap.put("previewHeight", size.height);
                //previewWidth = size.width;
                // previewHeight = size.height;
                break;
            }
        }

        // autofocus
        /*
        try {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } catch (Exception e) {
            Logger.log("e", "No tengo autofocus");
            Logger.log("e", Log.getStackTraceString(e));
        }
        // 90 grados en portrait
        //mCamera.setDisplayOrientation(90);

        // jpg 80 de calidad
        parameters.setJpegQuality(70);

        //mCamera.setParameters(parameters);

        setCameraDisplayOrientation();*/

        return sizeMap;
    }

    private android.hardware.Camera.Parameters getPara(Camera.Parameters parameters, int width, int height, int previewWidth, int previewHeight) {
        // Cambio la resolución entre 2 y 3 mpx
        //Logger.e("getPara");
        //Camera.Parameters parameters = mCamera.getParameters();

        // autofocus
        try {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } catch (Exception e) {
            Logger.e("No tengo autofocus");
            Logger.e(Log.getStackTraceString(e));
        }
        // 90 grados en portrait
        //mCamera.setDisplayOrientation(90);

        // jpg 80 de calidad
        parameters.setJpegQuality(70);
        parameters.setPictureSize(width, height);
        parameters.setPreviewSize(previewWidth, previewHeight);

        Logger.e("picture size: " + parameters.getPictureSize().width + "x" + parameters.getPictureSize().height);
        Logger.e("preview size: " + parameters.getPreviewSize().width + "x" + parameters.getPreviewSize().height);
        //mCamera.setParameters(parameters);

        //setCameraDisplayOrientation();
        return parameters;
    }
/*
    public void setCameraDisplayOrientation() {

        mCamera.setDisplayOrientation(0);




        mCamera.setParameters(parameters);

        // se haya hecho o no el cambio de mpx
        // ajusto el contenedor de la cámara al aspecto seleccionado
        float ratio = (float) width / height;
        aspectRatioLayout = (AspectRatioLayout) rootView.findViewById(R.id.camera_frame);
        aspectRatioLayout.setAspectRatio(ratio);

        //startPreview();
    }
*/

    public Camera getCameraInstance() {
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
            Camera.Parameters parameters = c.getParameters();
            Map sizeMap = getSize(parameters);


            int width = (Integer) sizeMap.get("width");
            int height = (Integer) sizeMap.get("height");
            int previewWidth = (Integer) sizeMap.get("previewWidth");
            int previewHeight = (Integer) sizeMap.get("previewHeight");

            parameters = getPara(parameters, width, height, previewWidth, previewHeight);
            c.setDisplayOrientation(90);
            c.setParameters(parameters);

            float ratio = (float) height / width;
            preview.setAspectRatio(ratio);
            /*
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
            */
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Logger.e(e.getMessage());
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

        Logger.e("oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preview = (AspectRatioLayout) findViewById(R.id.camera_preview);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        if (mCamera != null) {
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
                            resumeCamera();
                        }
                    }
            );


        }

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
            mCamera = getCameraInstance();
            mPreview = new CameraPreview(MainActivity.this, mCamera);
            preview.removeViewAt(0);
            preview.addView(mPreview, 0);
        }

       /* if(mCamera!=null){
            mCamera.startPreview();
        }*/
        // mCamera = getCameraInstance(preview.getWidth(), preview.getHeight());

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
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
       /* if (id == R.id.action_settings) {
            return true;
        } else */
        if (id == R.id.load_background) {
            Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(choosePictureIntent, RESULT_LOAD_IMAGE);
            return true;

        } else if (id == R.id.merger_bg) {
            if (item.isChecked()) item.setChecked(false);
            else item.setChecked(true);
            isMerger = item.isChecked();
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
                // Bitmap bitmap = null;
                try {
                    bgBitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), dataUri);
                    bgBitmap = Util.compress(bgBitmap);
                    //bgBitmap = Util.adjustOpacity(bgBitmap, 101);
                    Bitmap mBitmap = bgBitmap;
                    mBitmap = Util.adjustOpacity(mBitmap, 101);
                    //mBitmap=Util.gray2Binary(mBitmap);
                    mImageView.setImageBitmap(mBitmap);
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
