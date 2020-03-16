package com.realmax.cars;

import android.app.Application;
import android.content.Context;

/**
 * @ProjectName: Cars
 * @Package: com.realmax.cars
 * @ClassName: App
 * @CreateDate: 2020/3/16 12:37
 */
public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
