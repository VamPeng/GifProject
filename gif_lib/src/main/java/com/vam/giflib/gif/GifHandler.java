package com.vam.giflib.gif;

import android.graphics.Bitmap;

public class GifHandler {

    private long gifAddr;

    static {
        System.loadLibrary("giflib");
    }

    public GifHandler(String path) {
        this.gifAddr = loadPath(path);
    }

    public native int stringFromJNI();

    private native long loadPath(String path);

    public native int getWidth(long ndkGif);

    public native int getHeight(long ndkGif);

    public native int updateFrame(long ndkGif, Bitmap bitmap);

    public int getWidth() {
        return getWidth(gifAddr);
    }

    public int getHeight() {
        return getHeight(gifAddr);
    }

    public int updateFrame(Bitmap bitmap) {
        return updateFrame(gifAddr, bitmap);
    }

}
