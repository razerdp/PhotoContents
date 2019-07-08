package razerdp.github.com.demo;

import android.app.Application;
import android.content.Context;


/**
 * Created by 大灯泡 on 2016/11/28.
 */

public class PhotoContentsDemoApp extends Application {
    private static Context CONTEXT;

    @Override
    public void onCreate() {
        super.onCreate();
        CONTEXT = PhotoContentsDemoApp.this.getApplicationContext();
    }

    public static Context getAppContext(){
        return CONTEXT;
    }

}
