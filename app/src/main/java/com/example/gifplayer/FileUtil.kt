package com.example.gifplayer

import android.content.Context
import android.os.Message
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

object FileUtil {

    fun getGifFile(context: Context, func: (File)->Unit) {
        getGifFile(context,"demo.gif","xk.gif",func)
//        var ins = context.assets.open("demo.gif")
//        val bytes = ByteArray(1024)
//
//        var file = File(context.cacheDir, "gif")
//        if (!file.exists()) {
//            file.mkdir()
//        }
//        val gifFile = File(file, "xk.gif")
//        if (gifFile.exists()) {
//            gifFile.delete()
//        }
//        gifFile.createNewFile()
//        val ops = FileOutputStream(gifFile)
//        var length = 0
//        Executors.newSingleThreadExecutor().execute {
//            ins.use {
//                ops.use {
//                    while (ins.read(bytes).also { length = it } != -1) {
//                        ops.write(bytes, 0, length)
//                    }
//                }
//                func.invoke(gifFile)
//            }
//        }
    }

    fun getGifFile(context: Context,assetPath:String,dstPath:String, func: (File)->Unit){
        var ins = context.assets.open(assetPath)
        val bytes = ByteArray(1024)

        var file = File(context.cacheDir, "gif")
        if (!file.exists()) {
            file.mkdir()
        }
        val gifFile = File(file, dstPath)
        if (gifFile.exists()) {
            gifFile.delete()
        }
        gifFile.createNewFile()
        val ops = FileOutputStream(gifFile)
        var length = 0
        Executors.newSingleThreadExecutor().execute {
            ins.use {
                ops.use {
                    while (ins.read(bytes).also { length = it } != -1) {
                        ops.write(bytes, 0, length)
                    }
                }
                func.invoke(gifFile)
            }
        }
    }

}