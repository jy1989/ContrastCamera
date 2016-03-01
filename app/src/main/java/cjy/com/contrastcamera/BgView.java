package cjy.com.contrastcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by chen on 16/3/1.
 */
public class BgView extends SurfaceView implements SurfaceHolder.Callback {
    Bitmap bgBitmap = null;
    String TAG = "cjydebug";
    /*public void setBgBitmap(Bitmap bgBitmap){
        this.bgBitmap=bgBitmap;
    }*/

    public BgView(Context context, Bitmap bgBitmap) {
        super(context);
        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);//半透明
        this.bgBitmap = bgBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        //canvas.drawColor(Color.BLACK);1
        if (this.bgBitmap != null) {
            Paint paint = new Paint();
            paint.setAlpha(0x40);
            canvas.drawBitmap(this.bgBitmap, 0, 0, paint);
            Log.e(TAG, "setttttt");

        } else {

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        }


    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas(null);
            synchronized (holder) {
                onDraw(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }
}