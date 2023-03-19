package com.example.gifplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;

import java.io.IOException;
import java.io.InputStream;

public class NeBigView extends View implements GestureDetector.OnGestureListener, View.OnTouchListener, GestureDetector.OnDoubleTapListener {

    private Rect rect;
    private BitmapFactory.Options options;
    private GestureDetector gestureDetector;
    private Scroller scroller;
    private int imageWidth;
    private int imageHeight;
    private BitmapRegionDecoder bitmapRegionDecoder;
    private int measuredWidth;
    private int measuredHeight;
    private Bitmap bitmap;
    private float scale;
    private Matrix matrix;
    private ScaleGestureDetector scaleGestureDetector;
    private float originalScale;

    public NeBigView(Context context) {
        this(context, null);
    }

    public NeBigView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NeBigView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {

        // 第1步：设置bigview需要的成员变量
        rect = new Rect();
        // 内存复用
        options = new BitmapFactory.Options();
        // 手势识别
        gestureDetector = new GestureDetector(getContext(), this);
        // 滚动类
        scroller = new Scroller(getContext());

        // 缩放手势识别
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGesture());

        setOnTouchListener(this);

    }

    public void setImage(InputStream ins) {

        // 获取图片信息时不能将整张图片加载进内存
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(ins, null, options);
        imageWidth = options.outWidth;
        imageHeight = options.outHeight;

        // 开启复用
        options.inMutable = true;
        // 设置格式
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        options.inJustDecodeBounds = false;

        // 创建一个区域解码器
        try {
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(ins, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        requestLayout();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measuredWidth = getMeasuredWidth();
        measuredHeight = getMeasuredHeight();

        // 确定图片加载区域
//        rect.left = 0;
//        rect.top = 0;
//        rect.right = imageWidth;
//        scale = measuredWidth / (float) imageWidth;
//        rect.bottom = (int) (measuredHeight / scale);

        // 加了缩放手势
        rect.left = 0;
        rect.top = 0;
        rect.right = Math.min(imageWidth, measuredWidth);
        rect.bottom = Math.min(imageHeight, measuredHeight);

        originalScale = measuredWidth / (float) imageWidth;
        scale = originalScale;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null == bitmapRegionDecoder) {
            return;
        }

        // 复用内存
        options.inBitmap = bitmap;

        bitmap = bitmapRegionDecoder.decodeRegion(rect, options);

        if (matrix == null) {
            matrix = new Matrix();
        } else {
            matrix.reset();
        }

        matrix.setScale(measuredWidth / (float) rect.width(), measuredWidth / (float) rect.width());

        canvas.drawBitmap(bitmap, matrix, null);

    }

    // 手按下
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        // 直接将事件传递给手势处理
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);

        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {

        Log.i("Vam", "point down");
        if (!scroller.isFinished()) {
            scroller.forceFinished(true);
        }

        return true;
    }

    // 处理滑动事件

    /**
     * @param e1        开始事件，手指按下去.
     * @param e2        当前事件.
     * @param distanceX .
     * @param distanceY .
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        // 上下移动的时候， rect 需要改变现实区域
        rect.offset((int) distanceX, (int) distanceY);
        // 移动时，处理到达顶部和底部的情况
        if (rect.bottom > imageHeight) {
            rect.bottom = imageHeight;
            rect.top = imageHeight - (int) (measuredHeight / scale);
        }
        if (rect.top < 0) {
            rect.top = 0;
            rect.bottom = (int) (measuredHeight / scale);
        }
        if (rect.right > imageWidth) {
            rect.right = imageWidth;
            rect.left = imageWidth - (int) (measuredWidth / scale);
        }
        if (rect.left < 0) {
            rect.left = 0;
            rect.right = (int) (measuredWidth / scale);
        }

        invalidate();

        return false;
    }
    // 处理惯性问题

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        scroller.fling(rect.left, rect.top, (int) -velocityX, (int) -velocityY, 0, (int) (imageWidth - (measuredWidth / scale)), 0, (int) (imageHeight - (measuredHeight / scale)));

        return false;
    }
    // 处理结果

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.isFinished()) {
            return;
        }

        if (scroller.computeScrollOffset()) {
            rect.top = scroller.getCurrY();
            rect.bottom = rect.top + (int) (measuredHeight / scale);
            invalidate();
        }

    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {

        // 双击事件
        if (scale < originalScale * 1.5) {
            scale = originalScale * 3;
        } else {
            scale = originalScale;
        }
        rect.right = rect.left + (int) (measuredWidth / scale);
        rect.bottom = rect.bottom + (int) (measuredHeight / scale);

        if (rect.bottom > imageHeight) {
            rect.bottom = imageHeight;
            rect.top = imageHeight - (int) (measuredHeight / scale);
        }
        if (rect.top < 0) {
            rect.top = 0;
            rect.bottom = (int) (measuredHeight / scale);
        }
        if (rect.right > imageWidth) {
            rect.right = imageWidth;
            rect.left = imageWidth - (int) (measuredWidth / scale);
        }
        if (rect.left < 0) {
            rect.left = 0;
            rect.right = (int) (measuredWidth / scale);
        }
        invalidate();

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    class ScaleGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float childScale = scale;
            childScale += detector.getScaleFactor() - 1;

            if (childScale <= originalScale) {
                childScale = originalScale;
            } else if (childScale > originalScale * 5) {
                childScale = originalScale * 5;
            }
            rect.right = rect.left + (int) (measuredWidth / childScale);
            rect.bottom = rect.top + (int) (measuredHeight / childScale);
            scale = childScale;
            invalidate();

            return true;
        }
    }

}
