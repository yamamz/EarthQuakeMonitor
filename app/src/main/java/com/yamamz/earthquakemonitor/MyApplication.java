package com.yamamz.earthquakemonitor;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by AMRI on 9/11/2017.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}