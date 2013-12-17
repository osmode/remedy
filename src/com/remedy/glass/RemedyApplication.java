package com.remedy.glass;

import android.app.Application;

public class RemedyApplication extends Application {
    @Override
    public void onCreate() {
        // Simply add the handler, and that's it! No need to add any code
        // to every activity. Everything is contained in MyLifecycleHandler
        // with just a few lines of code. Now *that's* nice.
        registerActivityLifecycleCallbacks(new AppLifecycleHandler());
    }
}