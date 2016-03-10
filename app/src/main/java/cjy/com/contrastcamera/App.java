package cjy.com.contrastcamera;

import android.app.Application;

import com.orhanobut.logger.Logger;

import org.xutils.x;
/**
 * Created by chen on 16/3/9.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        //x.Ext.setDebug(true); // 是否输出debug日志
        Logger.init(Util.TAG);
        // Logger.e("Application oncreate");

    }
}
