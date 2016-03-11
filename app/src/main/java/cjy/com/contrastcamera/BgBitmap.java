package cjy.com.contrastcamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by chen on 16/3/10.
 */
public class BgBitmap {
    private Uri uri;
    private Activity act;
    private Bitmap bm = null;
    private Bitmap greyBm = null;//private Uri bgUri = null;
    private Bitmap compressBm = null;
    private int opac = 100;
    private boolean isGrey = false;
    private boolean threadRunning = false;

    public BgBitmap(Activity act, Uri uri) {
        //this.bm = bm;
        //this.cb = cb;
        this.uri = uri;
        this.act = act;
    }

    public void loadBitmap(final loadBitmapListener listener) {
        if (threadRunning) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                threadRunning = true;
                try {
                    bm = MediaStore.Images.Media.getBitmap(act.getContentResolver(), uri);
                    threadRunning = false;
                    listener.loadBitmapDone();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                threadRunning = false;
            }
        }).start();


    }

    public int getOpac() {
        return opac;
    }

    public void setOpac(int opac) {
        this.opac = opac;
    }

    public boolean isGrey() {
        return isGrey;
    }

    public void setGrey(boolean grey) {
        isGrey = grey;
    }

    public void getShowBitmap(final getShowBitmapListener listener) {
        if (threadRunning) {
            return;
        }
        if (isGrey) {
            getGreyBm(new getGreyBmListener() {
                @Override
                public void getGreyBmDone(final Bitmap bitmap) {

                    //final Bitmap mBitmap=bitmap;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            threadRunning = true;
                            Bitmap mBitmap = Util.adjustOpacity(bitmap, opac);
                            mBitmap = Util.adjustOpacity(mBitmap, opac);
                            threadRunning = false;
                            listener.getShowBitmapDone(mBitmap);
                        }
                    }).start();


                }
            });


        } else {
            getCompressBm(new getCompressBmListener() {
                @Override
                public void getCompressBmDone(final Bitmap bitmap) {
                    //final Bitmap bitmap1 = bitmap;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            threadRunning = true;
                            Bitmap mBitmap = Util.adjustOpacity(bitmap, opac);
                            mBitmap = Util.adjustOpacity(mBitmap, opac);
                            threadRunning = false;
                            listener.getShowBitmapDone(mBitmap);
                        }
                    }).start();
                }
            });


        }


    }

    public Bitmap getBm() {
        return bm;
    }

    public void getCompressBm(final getCompressBmListener linstener) {


        if (compressBm == null) {
            if (threadRunning) {
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    threadRunning = true;
                    try {
                        compressBm = Util.compressImage(bm);//
                        threadRunning = false;
                        linstener.getCompressBmDone(compressBm);
                    } catch (Exception e) {

                    }
                    threadRunning = false;
                }
            }).start();
        } else {
            linstener.getCompressBmDone(compressBm);
        }
/*
        if (compressBm == null) {
            compressBm = Util.compressImage(bm);
        }
        return compressBm;*/
    }

    public void getGreyBm(final getGreyBmListener listener) {


        if (greyBm == null) {
            if (threadRunning) {
                return;
            }
            getCompressBm(new getCompressBmListener() {
                @Override
                public void getCompressBmDone(Bitmap bitmap) {

                    final Bitmap mBitmap = bitmap;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            threadRunning = true;
                            try {
                                greyBm = Util.gray2Binary(mBitmap);//
                                threadRunning = false;
                                listener.getGreyBmDone(greyBm);
                            } catch (Exception e) {

                            }

                            threadRunning = false;
                        }
                    }).start();


                }
            });

        } else {
            listener.getGreyBmDone(greyBm);

        }


        //return greyBm;
    }

    public void genPhoto(final byte[] data, final File file, final int rotation, final boolean isMerger, final genPhotoListener listener) throws IOException {
        if (threadRunning) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                threadRunning = true;
                Bitmap bitmap = Util.Bytes2Bimap(data);
                bitmap = Util.rotate(bitmap, rotation);
                if (isMerger && bm != null) {
                    bitmap = Util.toConformBitmap(bm, bitmap);
                }
                try {
                    FileOutputStream fos = new FileOutputStream(file);

                    fos.write(Util.Bitmap2Bytes(bitmap));
                    fos.close();
                    listener.genPhotoDone(file);
                } catch (IOException e) {
                    Logger.e(e.getMessage());
                }
                threadRunning = false;
            }
        }).start();


    }


    public interface loadBitmapListener {
        void loadBitmapDone();
    }

    public interface getCompressBmListener {
        void getCompressBmDone(Bitmap bitmap);
    }

    public interface getGreyBmListener {
        void getGreyBmDone(Bitmap bitmap);
    }

    public interface getShowBitmapListener {
        void getShowBitmapDone(Bitmap bitmap);

    }

    public interface genPhotoListener {
        void genPhotoDone(File file);
    }

}
