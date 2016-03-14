package cjy.com.contrastcamera;

import android.graphics.Bitmap;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by chen on 16/3/14.
 */
public class GenPhoto {
    boolean threadRunning = false;

    public GenPhoto() {
    }

    public void genPhoto(final Bitmap bgBitMap, final byte[] data, final File file, final int rotation, final boolean isMerger, final genPhotoListener listener) throws IOException {
        listener.genPhotoStart();
        if (threadRunning) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                threadRunning = true;
                Bitmap bitmap = Util.Bytes2Bimap(data);
                bitmap = Util.rotate(bitmap, rotation);
                if (isMerger && bgBitMap != null) {
                    bitmap = Util.mergerBitmap(bgBitMap, bitmap);
                }
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(Util.Bitmap2Bytes(bitmap));
                    fos.close();
                    listener.genPhotoDone(file, bitmap);
                } catch (IOException e) {
                    Logger.e(e.getMessage());
                }
                threadRunning = false;
            }
        }).start();


    }

    public interface genPhotoListener {
        void genPhotoStart();

        void genPhotoDone(File file, Bitmap bm);
    }

}
