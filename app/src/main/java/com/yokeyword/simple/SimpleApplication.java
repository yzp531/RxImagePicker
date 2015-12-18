package com.yokeyword.simple;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by YoKeyword on 15/12/18.
 */
public class SimpleApplication extends Application {
    private static SimpleApplication instance;
    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        refWatcher = LeakCanary.install(this);
    }


    public synchronized static SimpleApplication getInstance() {
        if (instance == null) {
            instance = new SimpleApplication();
        }
        return instance;
    }

    public RefWatcher getRefWatcher(Context context) {
        SimpleApplication application = (SimpleApplication) context.getApplicationContext();
        return application.refWatcher;
    }
}
