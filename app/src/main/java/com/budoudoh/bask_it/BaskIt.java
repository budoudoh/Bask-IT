package com.budoudoh.bask_it;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by basilu on 3/12/16.
 */
public class BaskIt extends Application {
    private Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
    }
}
