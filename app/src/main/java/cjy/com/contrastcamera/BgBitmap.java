package cjy.com.contrastcamera;

import android.graphics.Bitmap;
import android.net.Uri;

import com.orhanobut.logger.Logger;

/**
 * Created by chen on 16/3/10.
 */
public class BgBitmap {
    private Bitmap bm = null;
    private Bitmap greyBm = null;
    private Uri bgUri = null;


    private Bitmap compressBm = null;
    private int opac = 100;
    private boolean isGrey = false;

    public BgBitmap(Bitmap bm) {
        this.bm = bm;
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

    public Bitmap getShowBitmap() {
        Bitmap mBitmap;
        if (isGrey)
            mBitmap = getGreyBm();
        else
            mBitmap = getCompressBm();
        if (opac != 255) {
            mBitmap = Util.adjustOpacity(mBitmap, opac);
        }

        return mBitmap;

    }

    public Bitmap getBm() {
        return this.bm;
    }

    public Bitmap getCompressBm() {
        if (compressBm == null) {
            compressBm = Util.compressImage(bm);
        }
        return compressBm;
    }

    public Bitmap getGreyBm() {
        if (this.greyBm == null) {
            Logger.e("greybm==null");
            greyBm = Util.gray2Binary(getCompressBm());
        }
        return this.greyBm;
    }


}
