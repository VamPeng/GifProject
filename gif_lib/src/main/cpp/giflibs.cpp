#include <jni.h>
#include <string>
#include "malloc.h"
#include "gif_lib.h"
#include <cstring>
#include <android/log.h>
#include "android/bitmap.h"

#define argb(a, r, g, b) ( ((a) & 0xff) << 24 ) | ( ((b) & 0xff) << 16 ) | ( ((g)&0xff) << 8 ) | ( (r) & 0xff )

#define LOG_TAG "vamGif"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef struct GifBean {
    // 播放帧数 第几帧
    int current_frame;
    int total_frame;
    int *dealys;
} GifBean;

void drawFrame(GifFileType *gifFileType, GifBean *gifBean, AndroidBitmapInfo info, void *pixels) {

    SavedImage savedImage = gifFileType->SavedImages[gifBean->current_frame];


    GifImageDesc frameInfo = savedImage.ImageDesc;
    GifByteType gifByteType;
    // rgb数据
    ColorMapObject *colorMapObject = frameInfo.ColorMap;
    if (colorMapObject == NULL) {
        colorMapObject = gifFileType->SColorMap;
    }

    int pointPixel;
    int *px = (int *) pixels;
    // 一行的像素个数 stride * 行数
    px = (int *) ((char *) px + info.stride * frameInfo.Top);
    int *line;
    for (int y = frameInfo.Top; y < frameInfo.Top + frameInfo.Height; y++) {
        line = px;
        for (int x = frameInfo.Left; x < frameInfo.Left + frameInfo.Width; x++) {
            pointPixel = (y - frameInfo.Top) * frameInfo.Width + (x - frameInfo.Left);
            gifByteType = savedImage.RasterBits[pointPixel];
            GifColorType gifColorType = colorMapObject->Colors[gifByteType];
            line[x] =
                    argb(255, gifColorType.Red, gifColorType.Green, gifColorType.Blue);
        }
        px = (int *) ((char *) px + info.stride);
    }

}

extern "C"
JNIEXPORT jint
JNICALL
Java_com_vam_giflib_gif_GifHandler_stringFromJNI(JNIEnv *env, jobject thiz) {
    LOGE("返回string 12345");
    return 1234;
}

extern "C"
JNIEXPORT jlong
JNICALL
Java_com_vam_giflib_gif_GifHandler_loadPath(JNIEnv *env, jobject thiz, jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    int err;
    GifFileType *gifFileType = DGifOpenFileName(path, &err);
    DGifSlurp(gifFileType);

    // 初始化并清内存
    GifBean *gifBean = (GifBean *) malloc(sizeof(GifBean));

    memset(gifBean, 0, sizeof(GifBean));
    gifFileType->UserData = gifBean;

    gifBean->dealys = (int *) malloc(sizeof(int) * gifFileType->ImageCount);
    memset(gifBean->dealys, 0, sizeof(int) * gifFileType->ImageCount);

    // 延迟时间     读取
    gifFileType->UserData = gifBean;
    gifBean->current_frame = 0;
    gifBean->total_frame = gifFileType->ImageCount;
    ExtensionBlock *ext;

    LOGE("gif长度大小 %d ", gifFileType->ImageCount);
    for (int i = 0; i < gifBean->total_frame; ++i) {
        SavedImage savedImage = gifFileType->SavedImages[i];
        for (int j = 0; j < savedImage.ExtensionBlockCount; ++j) {
            if (savedImage.ExtensionBlocks[j].Function == GRAPHICS_EXT_FUNC_CODE) {
                ext = &savedImage.ExtensionBlocks[j];
                break;
            }
        }
        if (ext) {
            int frame_delay = (ext->Bytes[1] | (ext->Bytes[2] << 8)) * 10;
//            LOGE("%d - 时间 %d ", i, frame_delay);
            gifBean->dealys[i] = frame_delay;
        }
    }


    env->ReleaseStringUTFChars(path_, path);
    return (jlong)
            gifFileType;
}

extern "C"
JNIEXPORT jint
JNICALL
Java_com_vam_giflib_gif_GifHandler_getHeight(JNIEnv *env, jobject thiz, jlong ndk_gif) {
    GifFileType *gifFileType = (GifFileType *) ndk_gif;
    return gifFileType->SHeight;
}

extern "C"
JNIEXPORT jint
JNICALL
Java_com_vam_giflib_gif_GifHandler_getWidth(JNIEnv *env, jobject thiz, jlong ndk_gif) {
    GifFileType *gifFileType = (GifFileType *) ndk_gif;
    return gifFileType->SWidth;
}

extern "C"
JNIEXPORT jint
JNICALL
Java_com_vam_giflib_gif_GifHandler_updateFrame(JNIEnv *env, jobject thiz, jlong ndk_gif,
                                               jobject bitmap) {

    GifFileType *gifFileType = (GifFileType *) ndk_gif;

    GifBean *gifBean = (GifBean *) gifFileType->UserData;

    AndroidBitmapInfo info;

    AndroidBitmap_getInfo(env, bitmap, &info);

    void *pixels;
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    drawFrame(gifFileType, gifBean, info, pixels);
    gifBean->current_frame += 1;
    if (gifBean->current_frame >= gifBean->total_frame - 1) {
        gifBean->current_frame = 0;
        LOGE("重新播放 %d ", gifBean->current_frame);
    }

    AndroidBitmap_unlockPixels(env, bitmap);

    return gifBean->dealys[gifBean->current_frame];

}


