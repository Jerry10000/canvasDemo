package com.hanvon.canvasdemo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.hanvon.canvasdemo.R;
import com.hanvon.canvasdemo.engine.HwPenEngine;
import com.hanvon.canvasdemo.view.MyView;

import java.io.IOException;
import java.util.LinkedList;

public class FirstActivityForView extends AppCompatActivity {
    private static final String TAG = "FirstActivity";

    private int mTotalWidth, mTotalHeight;
    private HwPenEngine hwPen;
    private int penBtnClickNum = 0;
    private int eraserBtnClickNum = 0;
    private long engine = -1;
//    private Template template;
    private int penType, penWidth, penColor, penAlpha;
    private boolean penIsBeautify, penIsTransparent;
//    LinkedList<Stroke> list_strokes;
    private MyView mSurfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: setContentView之前");

        setContentView(R.layout.activity_main_for_view);
//        setContentView(new SurfaceViewL(this));
        mSurfaceView = (MyView) findViewById(R.id.strokeView1);

        if(ContextCompat.checkSelfPermission(FirstActivityForView.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(FirstActivityForView.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (hwPen == null){
            hwPen = mSurfaceView.getPenEngine();
        }
        switch (item.getItemId()){
            case R.id.item_pen:
                penBtnClickNum++;
                if(penBtnClickNum%5 == 1) {
                    hwPen.setPenInfo(0, HwPenEngine.PEN_TYPE_MARKER, 0x80000000, 45, 0);
                    mSurfaceView.penType = HwPenEngine.PEN_TYPE_MARKER;
                }else if(penBtnClickNum%5 == 2){
                    hwPen.setPenInfo(0, HwPenEngine.PEN_TYPE_FOUNTAIN1, 0x80000000, 45, 0);
                    mSurfaceView.penType = HwPenEngine.PEN_TYPE_FOUNTAIN1;
                }
                else if(penBtnClickNum%5 == 3){
                    hwPen.setPenInfo(0, HwPenEngine.PEN_TYPE_PAINTBRUSH, 0x80000000, 45, 0);
                    mSurfaceView.penType = HwPenEngine.PEN_TYPE_PAINTBRUSH;
                }else if(penBtnClickNum%5 == 4){
                    hwPen.setPenInfo(0, HwPenEngine.PEN_TYPE_INK, 0x80000000, 45, 0);
                    mSurfaceView.penType = HwPenEngine.PEN_TYPE_INK;
                }
                else {
                    hwPen.setPenInfo(0, HwPenEngine.PEN_TYPE_PENCIL, 0x80000000, 45, 0);
                    mSurfaceView.penType = HwPenEngine.PEN_TYPE_PENCIL;
                }

                break;
            case R.id.item_eraser:
                eraserBtnClickNum++;
                if (eraserBtnClickNum % 2 == 0){
                    hwPen.setEraserType(HwPenEngine.PEN_TYPE_ERASER_FOR_STROKE, 35);
                    mSurfaceView.penType = HwPenEngine.PEN_TYPE_ERASER_FOR_STROKE;
                }
                else if (eraserBtnClickNum % 2 == 1){
                    hwPen.setEraserType(HwPenEngine.PEN_TYPE_ERASER_FOR_POINT, 35);
                    mSurfaceView.penType = HwPenEngine.PEN_TYPE_ERASER_FOR_POINT;
                }

                break;
            case R.id.item_pre:
                if (hwPen.getUndoSteps() > 0){
                    long time0 = System.currentTimeMillis();
                    hwPen.undo();
//                    mSurfaceView.update(new Rect(0, 0, mSurfaceView.getmWidth(), mSurfaceView.getHeight()));
                    Toast.makeText(FirstActivityForView.this, (System.currentTimeMillis() - time0) + "ms", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.item_next:
                if (hwPen.getRedoSteps() > 0){
                    hwPen.redo();
                    mSurfaceView.invalidate(0, 0, mSurfaceView.getmWidth(), mSurfaceView.getHeight());
                }
                break;
            case R.id.save:

                long begin0 = System.currentTimeMillis();
                try {
                    hwPen.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                long end0 = System.currentTimeMillis();
                Toast.makeText(FirstActivityForView.this, "save:" + (end0 - begin0) + " 豪秒", Toast.LENGTH_SHORT).show();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        final String log = getLog();
//                    }
//                }).start();
//                Toast.makeText(FirstActivity.this, "点击了保存", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting:
                Toast.makeText(FirstActivityForView.this, "点击了设置", Toast.LENGTH_SHORT).show();
                break;
            case R.id.clear:
                mSurfaceView.clear();
                break;
            case R.id.load:
                begin0 = System.currentTimeMillis();
                try {
                    hwPen.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mSurfaceView.invalidate(0, 0, mSurfaceView.getmWidth(), mSurfaceView.getHeight());
                end0 = System.currentTimeMillis();
                Toast.makeText(FirstActivityForView.this, "load:" + (end0 - begin0) + " 豪秒", Toast.LENGTH_SHORT).show();
                break;
            case R.id.changePage:
                Intent intent = new Intent();
                intent.setClass(FirstActivityForView.this, ThirdActivity.class);
                startActivity(intent);
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hwPen != null){
            hwPen.destroy();
            hwPen = null;
        }

    }
}
