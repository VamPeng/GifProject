package com.vam.giflib;

public class NativeLib {

    // Used to load the 'giflib' library on application startup.
    static {
        System.loadLibrary("giflib");
    }

    /**
     * A native method that is implemented by the 'giflib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}