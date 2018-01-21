package com.hanvon.canvasdemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.hanvon.canvasdemo.engine.HwPenEngine;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by pc on 2017/11/23.
 */

public class MyView extends View {
    private static final String TAG = "SurfaceViewL";

    private static final int MSG_UPDATE = 1;

    private boolean isDrawing;

    private Canvas mCanvas;
    private Paint mPaint;
    private Bitmap mBitmap;
    private int mWidth, mHeight;
    private int[] mPixels;
    private int[] updateRect = new int[4];
    private Rect bitRect = new Rect(65535,65535,0,0);
    private LinkedList<Rect> rects = new LinkedList<Rect>();
    public static HwPenEngine mPenEngine;
    public int penType = HwPenEngine.PEN_TYPE_INK;

    private Context context;
    ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
    private Handler mainHandler;


    public MyView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mainHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MSG_UPDATE:
                        invalidate(new Rect(0, 0, getmWidth(), getHeight()));
                        break;
                }

            }
        };

        mWidth = getWidth();
        mHeight = getHeight();
        mPixels = new int[mWidth * mHeight];
        //创建画布
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mPixels = new int[mWidth * mHeight];
        mBitmap.getPixels(mPixels, 0, mWidth, 0, 0, mWidth, mHeight);

        //初始化画笔引擎
        if(mPenEngine == null){
            mPenEngine = new HwPenEngine();
        }
        mPenEngine.init(mWidth, mHeight, mPixels);
        mPenEngine.setSavePath("/mnt/sdcard/wwl/", ".st");
        mPenEngine.setHandler(mainHandler);
        mPenEngine.setPenInfo(0, HwPenEngine.PEN_TYPE_INK, 0x800000ff, 45, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float pressure = 1.0f;
        if (e.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER) {
            pressure = 1.0f;
        } else if (e.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS) {
            pressure = e.getPressure();
        } else if (e.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER) {
            pressure = e.getPressure();
        }
        final Rect[] rect = new Rect[1];

        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                mPenEngine.beginStroke();
                break;
            case MotionEvent.ACTION_MOVE:
                mPenEngine.strokePoint(e.getX(), e.getY(), pressure, updateRect);

                    singleThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (penType == HwPenEngine.PEN_TYPE_ERASER_FOR_STROKE){
//                                update(new Rect(0, 0, getmWidth(), getHeight()));
                            }else {
                                rect[0] = new Rect(updateRect[0], updateRect[1], updateRect[2], updateRect[3]);

                                bitRect.union(rect[0]);
                                rects.add(rect[0]);
                                invalidate(new Rect(bitRect));
                            }
                        }
                    });


                break;
            case MotionEvent.ACTION_UP:
                mPenEngine.endStroke(updateRect);
                singleThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        rect[0] = new Rect(updateRect[0], updateRect[1], updateRect[2], updateRect[3]);
                        bitRect.union(rect[0]);
                        rects.add(rect[0]);
                        invalidate(bitRect);

                        //重置最大矩形框
                        bitRect.set(65535, 65535, 0, 0);
                    }
                });
                break;
        }
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }


    public void clear(){
        mPenEngine.clear();
        invalidate();
    }

    public HwPenEngine getPenEngine(){
        return mPenEngine;
    }

    /**
     * 测量
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (wSpecMode == MeasureSpec.AT_MOST && hSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(300, 300);
        } else if (wSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(300, hSpecSize);
        } else if (hSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(wSpecSize, 300);
        }
    }

    public int getmWidth() {
        return mWidth;
    }

    public int getmHeight() {
        return mHeight;
    }
}
