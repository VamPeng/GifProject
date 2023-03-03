package com.example.gifplayer

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Handler.Callback
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.vam.giflib.gif.GifHandler
import java.io.File

class MainActivity : AppCompatActivity() {

    val imageView: ImageView by lazy { findViewById(R.id.main_iv) }
    val textView: TextView by lazy { findViewById(R.id.main_tv) }
    private lateinit var gifFile: File
    lateinit var bitmap: Bitmap
    lateinit var gifHandler: GifHandler

    private val callback: Callback = Callback {
        imageView.setImageBitmap(bitmap)
        if (it.what == 1) {
            val nextFrame = gifHandler.updateFrame(bitmap)
            handler.sendEmptyMessageDelayed(1, nextFrame.toLong())
        }else{
        }
        true
    }

    private val handler = Handler(
        Looper.getMainLooper(),
        callback
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FileUtil.getGifFile(this) {
            gifFile = it
            handler.post {
                gifHandler = GifHandler(gifFile.absolutePath)

//                Glide.with(this).load(gifFile).into(imageView)
                textView.setText("gif tv: " + gifHandler.stringFromJNI())
                ndkLoadGif()
            }
        }

    }


    private fun ndkLoadGif() {

        val width = gifHandler.width
        val height = gifHandler.height

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        var nextFrame = gifHandler.updateFrame(bitmap)
        Log.i("vamGif", "next: $111")
        handler.sendEmptyMessageDelayed(1, nextFrame.toLong())

    }

}