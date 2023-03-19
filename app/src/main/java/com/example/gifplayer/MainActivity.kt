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
import org.xml.sax.ext.DefaultHandler2
import java.io.File
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    val imageView: ImageView by lazy { findViewById(R.id.main_iv) }
    val imageView2: ImageView by lazy { findViewById(R.id.main_iv2) }
    val textView: TextView by lazy { findViewById(R.id.main_tv) }

    val bigView by lazy { findViewById<NeBigView>(R.id.main_bv) }

    private lateinit var gifFile: File
    lateinit var bitmap: Bitmap
    lateinit var gifHandler: GifHandler

    private lateinit var gifFile2: File
    lateinit var bitmap2: Bitmap
    lateinit var gifHandler2: GifHandler

    private val callback: Callback = Callback {
        if (it.what == 1) {
            imageView.setImageBitmap(bitmap)
            val nextFrame = gifHandler.updateFrame(bitmap)
            handler.sendEmptyMessageDelayed(1, nextFrame.toLong())
        } else if (it.what == 2) {
            imageView2.setImageBitmap(bitmap2)
            val nextFrame = gifHandler2.updateFrame(bitmap2)
            handler.sendEmptyMessageDelayed(2, nextFrame.toLong())
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

        loadBigBitmap()

    }

    private fun loadBigBitmap() {

        val str1 = "changtu.jpeg"
        val str2 = "sanmartino.jpg"
        assets.open(str1).use {
            bigView.setImage(it)
        }

    }

    private fun loadGif() {
        FileUtil.getGifFile(this) {
            gifFile = it
            handler.post {
                gifHandler = GifHandler(gifFile.absolutePath)

//                Glide.with(this).load(gifFile).into(imageView)
                textView.setText("gif tv: " + gifHandler.stringFromJNI())
                ndkLoadGif()
            }
        }

        FileUtil.getGifFile(this, "xk.gif", "xk2.gif") {
            gifFile2 = it
            handler.post {
                gifHandler2 = GifHandler(gifFile2.absolutePath)

//                Glide.with(this).load(gifFile).into(imageView)
//                textView.setText("gif tv: " + gifHandler.stringFromJNI())
                ndkLoadGif2()
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

    private fun ndkLoadGif2() {

        val width = gifHandler2.width
        val height = gifHandler2.height

        bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        var nextFrame = gifHandler2.updateFrame(bitmap2)
        Log.i("vamGif", "next: $111")
        handler.sendEmptyMessageDelayed(2, nextFrame.toLong())

    }

}