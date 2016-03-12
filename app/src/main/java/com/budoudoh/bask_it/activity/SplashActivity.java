package com.budoudoh.bask_it.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.budoudoh.bask_it.R;

import java.util.Timer;
import java.util.TimerTask;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;

/**
 * Created by basilu on 3/11/16.
 */
@ContentView(R.layout.activity_splash)
public class SplashActivity extends RoboActivity{

    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(context, OrderActivity.class);
                startActivity(intent);
                finish();
            }
        }, 5000);

    }
}
