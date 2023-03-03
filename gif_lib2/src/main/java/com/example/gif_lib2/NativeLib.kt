package com.example.gif_lib2

class NativeLib {

    /**
     * A native method that is implemented by the 'gif_lib2' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'gif_lib2' library on application startup.
        init {
            System.loadLibrary("gif_lib2")
        }
    }
}