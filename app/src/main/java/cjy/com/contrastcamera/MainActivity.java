package cjy.com.contrastcamera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.orhanobut.logger.Logger;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.senab.photoview.PhotoViewAttacher;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    private static final int CAMERA_MIN_WIDTH = 1500;
    private static final int CAMERA_MAX_WIDTH = 2500;
    private static int RESULT_LOAD_IMAGE = 1989;
    private static int SHOW_PB = 1990;
    private static int HIDE_PB = 1991;
    private static int UPDATE_BM = 1992;


    protected int CURRENT_CAM = Util.USE_BACKGROUND_CAM;
    GenPhoto gp;
    private ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    //private static Bitmap bgBitmap = null;
    private BgBitmap bgBitmap = null;
    @ViewInject(R.id.camera_preview)
    private AspectRatioLayout preview;
    @ViewInject(R.id.toolbar)
    private Toolbar toolbar;
    @ViewInject(R.id.seekBar_bg)
    private SeekBar seekBarBg;
    @ViewInject(R.id.progressbar)
    private fr.castorflex.android.smoothprogressbar.SmoothProgressBar progressBar;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SHOW_PB) {
                if (!progressBar.isShown()) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            } else if (msg.what == HIDE_PB) {
                if (progressBar.isShown()) {
                    progressBar.setVisibility(View.GONE);
                }

            } else if (msg.what == UPDATE_BM) {
                if (msg.obj != null) {
                    mImageView.setImageBitmap((Bitmap) msg.obj);
                    mAttacher.update();
                }

            }
            super.handleMessage(msg);
        }


    };
    private Camera mCamera;
    private CameraPreview mPreview;
    private boolean isMerger = true;
    //private boolean threadRunning = false;
    //private boolean isGrey = false;
    //private int bgOpac = 100;

    //private Camera.Parameters parameters = null;
    //private Uri bgUri = null;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {



            File pictureFile = Util.getOutputMediaFile(Util.MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Logger.wtf("Error creating media file, check storage permissions: ");
                return;
            }



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

            try {

                Bitmap mBitmap = null;
                if (bgBitmap != null) {
                    mBitmap = bgBitmap.getBm();
                }

                gp.genPhoto(mBitmap, data, pictureFile, rotation, isMerger, new GenPhoto.genPhotoListener() {
                    @Override
                    public void genPhotoStart() {
                        Snackbar.make(preview, "gening!!", Snackbar.LENGTH_LONG).show();

                        Message msg = mHandler.obtainMessage();
                        msg.what = SHOW_PB;
                        msg.sendToTarget();
                    }

                    @Override
                    public void genPhotoDone(File file) {
                        Snackbar.make(preview, file.getAbsolutePath(), Snackbar.LENGTH_LONG).show();
                        resumeCamera();

                        Message msg = mHandler.obtainMessage();
                        msg.what = HIDE_PB;
                        msg.sendToTarget();
                    }
                });

            } catch (IOException e) {
                Logger.e(e.getMessage());
            }

            /*
                //Logger.e(rotation+" "+CURRENT_CAM+" "+Util.USE_FRONT_CAM);
                Bitmap bitmap = Util.Bytes2Bimap(data);
                bitmap = Util.rotate(bitmap, rotation);
                if (isMerger) {
                    bitmap = Util.toConformBitmap(bgBitmap.getBm(), bitmap);
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
                resumeCamera();*/


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
            //Logger.e("presize: " + size.width + "," + size.height + ", preview: " + width + "," + height + " " + ((float) size.width / size.height) + "," + ((float) width / height));
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

    private android.hardware.Camera.Parameters getPara(Camera.Parameters parameters, int width, int height, int previewWidth, int previewHeight) {
        // Cambio la resolución entre 2 y 3 mpx
        //Logger.e("getPara");
        //Camera.Parameters parameters = mCamera.getParameters();

        // autofocus
        try {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        } catch (Exception e) {
            //Logger.e("No tengo autofocus");
            Logger.e(Log.getStackTraceString(e));
        }
        // 90 grados en portrait
        //mCamera.setDisplayOrientation(90);

        // jpg 80 de calidad
        parameters.setJpegQuality(80);
        parameters.setPictureSize(width, height);
        parameters.setPreviewSize(previewWidth, previewHeight);


        //Logger.e("picture size: " + parameters.getPictureSize().width + "x" + parameters.getPictureSize().height);
        //Logger.e("preview size: " + parameters.getPreviewSize().width + "x" + parameters.getPreviewSize().height);
        //mCamera.setParameters(parameters);

        //setCameraDisplayOrientation();
        return parameters;
    }

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
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //progressBar.setVisibility(View.GONE);
        //preview = (AspectRatioLayout) findViewById(R.id.camera_preview);
        // Create an instance of Camera
        mCamera = getCameraInstance();
        if (mCamera != null) {
            mPreview = new CameraPreview(this, mCamera);

            mImageView = new ImageView(this);
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            preview.addView(mPreview, 0);
            preview.addView(mImageView, 1);

            mAttacher = new PhotoViewAttacher(mImageView);


        }
        gp = new GenPhoto();

    }

    @Event(value = R.id.checkBox_grey, type = CheckBox.OnCheckedChangeListener.class)
    private void onCheckGreyMode(CompoundButton button, boolean isChecked) {
        if (bgBitmap != null) {
            bgBitmap.setGrey(isChecked);
            updateBgBitmap();
        }


    }

    @Event(value = R.id.checkBox_merge, type = CheckBox.OnCheckedChangeListener.class)
    private void onCheckMerge(CompoundButton button, boolean isChecked) {
        isMerger = isChecked;
    }

    @Event(value = R.id.seekBar_bg, type = android.widget.SeekBar.OnSeekBarChangeListener.class, method = "onProgressChanged")
    private void onBgProgressChanged(SeekBar seekBar, int progress,
                                     boolean fromUser) {
        if (bgBitmap != null) {
            bgBitmap.setOpac(progress);
            updateBgBitmap();
        }

    }

    @Event(R.id.button_pickpic)
    private void clickPickPic(View view) {
        Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(choosePictureIntent, RESULT_LOAD_IMAGE);

    }

    @Event(R.id.button_capture)
    private void clickCapture(View view) {
        mCamera.takePicture(null, null, mPicture);

    }

    @Event(R.id.button_frontback)
    private void clickChangeFrontBack(View view) {

        if (CURRENT_CAM == Util.USE_BACKGROUND_CAM) {
            CURRENT_CAM = Util.USE_FRONT_CAM;
        } else {
            if (CURRENT_CAM == Util.USE_FRONT_CAM) {
                CURRENT_CAM = Util.USE_BACKGROUND_CAM;
            }
        }
        releaseCamera();
        resumeCamera();


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
        if (id == R.id.action_settings) {
            return true;
        } /*else
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
        }*/

        return super.onOptionsItemSelected(item);
    }

    private void loadBgBitmap(Uri bgUri) {


        bgBitmap = new BgBitmap(MainActivity.this, bgUri);
        bgBitmap.loadBitmap(new BgBitmap.loadBitmapListener() {
            @Override
            public void loadBitmapStart() {
                Message msg = mHandler.obtainMessage();
                msg.what = SHOW_PB;
                msg.sendToTarget();
            }

            @Override
            public void loadBitmapDone() {

                updateBgBitmap();


            }
        });


        //Thread t = new Thread(new LoadBitmapRunnable(bgUri));
        // t.start();
    }

    private void updateBgBitmap() {


        bgBitmap.getShowBitmap(new BgBitmap.getShowBitmapListener() {
            @Override
            public void getShowBitmapStart() {
                Message msg = mHandler.obtainMessage();
                msg.what = SHOW_PB;
                msg.sendToTarget();
            }

            @Override
            public void getShowBitmapDone(Bitmap bitmap) {
                // mImageView.setImageBitmap(bitmap);
                //mAttacher.update();

                Message msg = mHandler.obtainMessage();

                msg.what = UPDATE_BM;
                msg.obj = bitmap;
                msg.sendToTarget();

                Message msg1 = mHandler.obtainMessage();
                msg1.what = HIDE_PB;
                msg1.sendToTarget();

            }
        });



        /*
        Thread t = new Thread(new adjustBitmapRunnable());
        t.start();*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {


            if (data != null) {
                loadBgBitmap(data.getData());
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
/*
    public class LoadBitmapRunnable implements Runnable {
        Uri bgUri = null;

        public LoadBitmapRunnable(Uri bgUri) {
            this.bgUri = bgUri;
        }

        @Override
        public void run() {




                //bgBitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), bgUri);
               bgBitmap = new BgBitmap(MainActivity.this, bgUri, new BgBitmapImpl() {
                   @Override
                   public void initDone() {

                   }

                   @Override
                   public void getCompressBm(Bitmap bitmap) {

                   }
               }));

                threadRunning = false;
                updateBgBitmap();



        }

    }
*/
    /*
    public class adjustBitmapRunnable implements Runnable {


        @Override
        public void run() {

            try {
                if (bgBitmap != null) {
                    Bitmap mBitmap = bgBitmap.getShowBitmap();
                    mImageView.setImageBitmap(mBitmap);
                    mAttacher.update();


                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Message msg = mHandler.obtainMessage();
            msg.what = HIDE_PB;
            msg.sendToTarget();
            threadRunning = false;
        }

    }*/
}
