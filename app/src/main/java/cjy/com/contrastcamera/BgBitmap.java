package cjy.com.contrastcamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

/**
 * Created by chen on 16/3/10.
 */
public class BgBitmap {
    private Uri uri;
    private Activity act;
    private Bitmap bm = null;
    private Bitmap greyBm = null;
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
        listener.loadBitmapStart();
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
        listener.getShowBitmapStart();
        if (threadRunning) {
            return;
        }
        if (isGrey) {
            getGreyBm(new getGreyBmListener() {
                @Override
                public void getGreyBmStart() {

                }

                @Override
                public void getGreyBmDone(final Bitmap bitmap) {

                    //final Bitmap mBitmap=bitmap;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            threadRunning = true;
                            Bitmap mBitmap = Util.adjustOpacity(bitmap, opac);
                            threadRunning = false;
                            listener.getShowBitmapDone(mBitmap);
                        }
                    }).start();


                }
            });


        } else {
            getCompressBm(new getCompressBmListener() {
                @Override
                public void getCompressStart() {

                }

                @Override
                public void getCompressBmDone(final Bitmap bitmap) {
                    //final Bitmap bitmap1 = bitmap;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            threadRunning = true;
                            Bitmap mBitmap = Util.adjustOpacity(bitmap, opac);
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

    public void getCompressBm(final getCompressBmListener listener) {

        listener.getCompressStart();
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
                        listener.getCompressBmDone(compressBm);
                    } catch (Exception e) {

                    }
                    threadRunning = false;
                }
            }).start();
        } else {
            listener.getCompressBmDone(compressBm);
        }

    }

    public void getGreyBm(final getGreyBmListener listener) {

        listener.getGreyBmStart();
        if (greyBm == null) {
            if (threadRunning) {
                return;
            }
            getCompressBm(new getCompressBmListener() {
                @Override
                public void getCompressStart() {

                }

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

    }

    public interface loadBitmapListener {
        void loadBitmapStart();

        void loadBitmapDone();
    }

    public interface getCompressBmListener {
        void getCompressStart();

        void getCompressBmDone(Bitmap bitmap);
    }

    public interface getGreyBmListener {
        void getGreyBmStart();

        void getGreyBmDone(Bitmap bitmap);
    }

    public interface getShowBitmapListener {
        void getShowBitmapStart();

        void getShowBitmapDone(Bitmap bitmap);

    }


}
